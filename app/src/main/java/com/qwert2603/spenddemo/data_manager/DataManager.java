package com.qwert2603.spenddemo.data_manager;

import com.qwert2603.retrobase.generated.SpendDBImpl;
import com.qwert2603.retrobase.rx.generated.SpendDBRx;
import com.qwert2603.spenddemo.base.ViewType;
import com.qwert2603.spenddemo.model.Change;
import com.qwert2603.spenddemo.model.ChangeKind;
import com.qwert2603.spenddemo.model.Id;
import com.qwert2603.spenddemo.model.Kind;
import com.qwert2603.spenddemo.model.Record;
import com.qwert2603.spenddemo.utils.DateUtils;
import com.qwert2603.spenddemo.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class DataManager {

    @SuppressWarnings("unused")
    private static final Record IF_LIST_EMPTY_RECORD
            = new Record(-1, "error happen, this should never be seen", 0, new java.sql.Date(0));

    private SpendDB mSpendDB = new SpendDBImpl();
    private SpendDBRx mSpendDBRx = new SpendDBRx(mSpendDB);

    private RealmHelper mRealmHelper = new RealmHelper();

    public Observable<List<ViewType>> getAllRecords() {
//        return getAllRecordsFromDataBase()
//                .compose(sortedGroupBy(Record::getDate))
//                // каждый Observable будет обрабатываться целиком в #addAggregatedValue(),
//                // поэтому в каждом Observable должен быть только один List<Record>.
//                .map(Observable::just)
//                .compose(addAggregatedValue(
//                        dataBaseRecord -> (ViewType) dataBaseRecord,
//                        Record::getValue,
//                        0,
//                        (v1, v2) -> v1 + v2,
//                        IF_LIST_EMPTY_RECORD,
//                        (sum, dataBaseRecord) -> new SumInDate(dataBaseRecord.getDate(), sum)
//                ))
//                .concatMap(Observable::from)
//                .toList()
//                .compose(applySchedulers());

        return mSpendDBRx.getAllRecordsOrdered()
                .startWith(pushChangesOnServer())
                .toList()
                .doOnNext(records -> mRealmHelper.executeSyncTransaction(realm -> {
                    LogUtils.d("saving records to realm");
                    realm.delete(Record.class);
                    realm.insert(records);
                }))
                .onErrorResumeNext(throwable -> {
                    LogUtils.e(throwable);
                    LogUtils.d("loading records from realm");
                    Realm realm = Realm.getDefaultInstance();
                    String[] sortFields = {Record.DATE, Record.ID};
                    Sort[] sortOrders = {Sort.ASCENDING, Sort.ASCENDING};
                    RealmResults<Record> realmResults = realm.where(Record.class).findAllSorted(sortFields, sortOrders);
                    List<Record> records = realm.copyFromRealm(realmResults);
                    realm.close();
                    return Observable.just(records);
                })
                .flatMap(Observable::from)
                .map(record -> ((ViewType) record))
                .toList()
                .compose(applySchedulers());
    }

    public Observable<Id> insertRecord(Record record) {
        return mSpendDBRx.insertRecord(record.getKind(), record.getValue(), DateUtils.dateToSql(record.getDate()))
                .startWith(pushChangesOnServer())
                .doOnSubscribe(() -> mRealmHelper.executeSyncTransaction(realm -> realm.copyToRealm(record)))
                .doOnNext(id -> mRealmHelper.executeSyncTransaction(realm -> {
                    realm.where(Record.class).equalTo(Record.ID, record.getId()).findFirst().deleteFromRealm();
                    record.setId(id.getId());
                    realm.copyToRealm(record);
                }))
                .onErrorResumeNext(throwable -> {
                    LogUtils.e(throwable);

                    mRealmHelper.executeSyncTransaction(realm -> {
                        Change change = new Change();
                        change.setKind(ChangeKind.INSERT);
                        change.setRecordId(record.getId());

                        realm.copyToRealm(change);
                    });

                    return Observable.just(new Id(record.getId()));
                })
                .compose(applySchedulers());
    }

    public Observable<Object> removeRecord(int recordId) {
        return mSpendDBRx.deleteRecord(recordId)
                .startWith(pushChangesOnServer())
                .doOnSubscribe(() -> mRealmHelper.executeSyncTransaction(realm -> realm.where(Record.class).equalTo(Record.ID, recordId).findFirst().deleteFromRealm()))
                .onErrorResumeNext(throwable -> {
                    LogUtils.e(throwable);
                    mRealmHelper.executeSyncTransaction(realm -> {
                        Change prevChange = realm.where(Change.class).equalTo(Change.RECORD_ID, recordId).findFirst();
                        if (prevChange != null && prevChange.getKind() == ChangeKind.INSERT) {
                            prevChange.deleteFromRealm();
                        } else {
                            Change change = new Change();
                            change.setKind(ChangeKind.DELETE);
                            change.setRecordId(recordId);
                            realm.copyToRealmOrUpdate(change);
                        }
                    });
                    return Observable.just(new Object());
                })
                .compose(applySchedulers());
    }

    public Observable<Id> updateRecord(Record record) {
        return mSpendDBRx.updateRecord(record.getKind(), record.getValue(), DateUtils.dateToSql(record.getDate()), record.getId())
                .startWith(pushChangesOnServer())
                .doOnSubscribe(() -> mRealmHelper.executeSyncTransaction(realm -> {
                    Record realmRecord = realm.where(Record.class).equalTo(Record.ID, record.getId()).findFirst();
                    realmRecord.setKind(record.getKind());
                    realmRecord.setValue(record.getValue());
                    realmRecord.setDate(record.getDate());
                    LogUtils.d("updated realm record " + realmRecord);
                }))
                // because if record was sent to server in "pushChangesOnServer()",
                // its id was changed and nothing will be updated after "mSpendDBRx.updateRecord(...)"
                // but we need element in observable.
                .defaultIfEmpty(new Id(record.getId()))
                .onErrorResumeNext(throwable -> {
                    LogUtils.e(throwable);

                    mRealmHelper.executeSyncTransaction(realm -> {
                        if (realm.where(Change.class).equalTo(Change.RECORD_ID, record.getId()).findFirst() == null) {
                            Change change = new Change();
                            change.setKind(ChangeKind.UPDATE);
                            change.setRecordId(record.getId());

                            realm.copyToRealm(change);
                        }
                    });

                    return Observable.just(new Id(record.getId()));
                })
                .compose(applySchedulers());
    }

    private <T> Observable<T> pushChangesOnServer() {
        return Observable
                .defer(() -> {
                    Realm realm = Realm.getDefaultInstance();
                    RealmResults<Change> realmResults = realm.where(Change.class).findAll();
                    List<Change> changes = realm.copyFromRealm(realmResults);
                    realm.close();
                    return Observable.just(changes);
                })
                .flatMap(Observable::from)
                .flatMap(change -> {
                    int kind = change.getKind();
                    Integer recordId = change.getRecordId();
                    if (kind == ChangeKind.DELETE) {
                        return mSpendDBRx.deleteRecord(recordId);
                    }
                    Realm realm = Realm.getDefaultInstance();
                    Record record = realm.where(Record.class).equalTo(Record.ID, recordId).findFirst();
                    record = realm.copyFromRealm(record);
                    realm.close();
                    if (kind == ChangeKind.INSERT) {
                        Record finalRecord = record;
                        return mSpendDBRx.insertRecord(record.getKind(), record.getValue(), DateUtils.dateToSql(record.getDate()))
                                .doOnNext(id -> {
                                    finalRecord.setId(id.getId());
                                    mRealmHelper.executeSyncTransaction(realm1 -> {
                                        realm1.where(Record.class).equalTo(Record.ID, change.getRecordId()).findFirst().deleteFromRealm();
                                        realm1.copyToRealm(finalRecord);
                                    });
                                })
                                .doOnCompleted(() -> LogUtils.d("record inserted to server " + finalRecord));
                    } else if (kind == ChangeKind.UPDATE) {
                        return mSpendDBRx.updateRecord(record.getKind(), record.getValue(),
                                DateUtils.dateToSql(record.getDate()), record.getId());
                    } else {
                        return Observable.error(new Exception("Unknown change kind!"));
                    }
                }, (change, o) -> change, 1)
                .doOnNext(change -> mRealmHelper.executeSyncTransaction(realm ->
                        realm.where(Change.class).equalTo(Change.RECORD_ID, change.getRecordId()).findFirst().deleteFromRealm()))
                .ignoreElements()
                .map(change -> (T) change);
    }

    public Observable<List<String>> getDistinctKinds() {
        return mSpendDBRx.getDistinctKinds()
                .map(Kind::getKind)
                .toList()
                .onErrorResumeNext(throwable -> {
                    LogUtils.e(throwable);
                    Realm realm = Realm.getDefaultInstance();
                    RealmResults<Record> realmResults = realm.where(Record.class).distinct(Record.KIND);
                    List<Record> records = realm.copyFromRealm(realmResults);
                    realm.close();
                    return Observable.just(records)
                            .flatMap(Observable::from)
                            .map(Record::getKind)
                            .toList();
                })
                .compose(applySchedulers());
    }

    public Observable<List<Change>> getAllChanges() {
        return Observable
                .fromCallable(() -> {
                    Realm realm = Realm.getDefaultInstance();
                    RealmResults<Change> realmResults = realm.where(Change.class).findAll();
                    List<Change> changeList = realm.copyFromRealm(realmResults);
                    realm.close();
                    return changeList;
                })
                .compose(applySchedulers());
    }

    @SuppressWarnings("all")    // redundant casting
    private final Observable.Transformer mTransformer = observable -> ((Observable) observable)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

    @SuppressWarnings("unchecked")
    private <T> Observable.Transformer<T, T> applySchedulers() {
        return (Observable.Transformer<T, T>) mTransformer;
    }

    @SuppressWarnings("unused")
    private <T, K> Observable.Transformer<T, List<T>> sortedGroupBy(Func1<T, K> keySelector) {
        return tObservable -> tObservable
                .toList()
                .flatMap(ts -> Observable.create(subscriber -> {
                    K k = null;
                    List<T> list = new ArrayList<>();
                    for (T t : ts) {
                        if (k == null) {
                            k = keySelector.call(t);
                        } else if (!k.equals(keySelector.call(t))) {
                            subscriber.onNext(list);
                            k = keySelector.call(t);
                            list = new ArrayList<>();
                        }
                        list.add(t);
                    }
                    if (!list.isEmpty()) {
                        subscriber.onNext(list);
                    }
                    subscriber.onCompleted();
                }));
    }

    /**
     * Добавить в начало списка элемент, содаржащий агрегированное значение всех элементов списка.
     * Элемент, содержащий агрегированное значение, и все элементы списка должны иметь общий тип,
     * поэтому элементы из входного списка можно преобразовать с помощью itemsTransformer.
     *
     * @param itemsTransformer       функция для преобразования входящих элементов в выходящие.
     * @param valueSelector          функция для получения значения для агрегации из элемента входного списка.
     * @param initialValue           начальное значение для агрегации.
     * @param valueAggregator        агрегатор для значений.
     * @param ifListEmptyItem        элемент по умолчанию, если входящий список пуст.
     * @param aggregatedValueCreator функция для создания элемента, содержащего агрегированное значение.
     *                               Ей передается агрегированное значение и первый элемент входящего списка.
     * @param <E>                    тип элементов списка входного Observable.
     * @param <T>                    тип элементов списка возвращаемого Observable.
     * @param <V>                    тип значения, которое надо агрегировать.
     * @return Observable для списка элементов типа T.
     */
    @SuppressWarnings("unused")
    private <E, T, V> Observable.Transformer<Observable<List<E>>, List<T>> addAggregatedValue(Func1<E, T> itemsTransformer,
                                                                                              Func1<E, V> valueSelector,
                                                                                              V initialValue,
                                                                                              Func2<V, V, V> valueAggregator,
                                                                                              E ifListEmptyItem,
                                                                                              Func2<V, E, T> aggregatedValueCreator) {
        return observable -> observable.flatMap(listObservable -> {
            Observable<E> items = listObservable.flatMap(Observable::from);
            Observable<T> transformedItems = items.map(itemsTransformer);
            Observable<T> aggregatedValue = items
                    .map(valueSelector)
                    .reduce(initialValue, valueAggregator)
                    .zipWith(items.firstOrDefault(ifListEmptyItem), aggregatedValueCreator);
            return Observable.concat(aggregatedValue, transformedItems).toList();
        });
    }

}
