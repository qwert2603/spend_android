package com.qwert2603.spenddemo.data_manager;

import android.support.v4.util.Pair;

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

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
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
                .startWith(pushChangesToServer().ignoreElements().map(id -> ((Record) ((Object) id))))
                .toList()
                .doOnNext(records -> mRealmHelper.replaceRecordsList(records))
                .onErrorResumeNext(throwable -> {
                    LogUtils.e(throwable);
                    LogUtils.d("loading records from realm");
                    return Observable.just(mRealmHelper.getAllRecords());
                })
                .flatMap(Observable::from)
                .map(record -> ((ViewType) record))
                .toList()
                .compose(applySchedulers());
    }

    public Observable<Id> insertRecord(Record record) {
        return createObservableForChange(() -> mRealmHelper.insertRecord(record), record.getId());
    }

    public Observable<Id> removeRecord(int recordId) {
        return createObservableForChange(() -> mRealmHelper.removeRecord(recordId), recordId);
    }

    public Observable<Id> updateRecord(Record record) {
        return createObservableForChange(() -> mRealmHelper.updateRecord(record), record.getId());
    }

    private Observable<Id> createObservableForChange(Action0 onSubscribe, int recordId) {
        return pushChangesToServer()
                .last()
                .doOnSubscribe(onSubscribe)
                .onErrorReturn(throwable -> new Id(recordId))
                .compose(applySchedulers());
    }

    private Observable<Id> pushChangesToServer() {
        return Observable.fromCallable(() -> mRealmHelper.getAllChanges())
                .flatMap(Observable::from)
                .flatMap(change -> {
                    int kind = change.getKind();
                    if (kind == ChangeKind.DELETE) {
                        return mSpendDBRx.deleteRecord(change.getRecordId());
                    } else {
                        Record record = mRealmHelper.getRecordById(change.getRecordId());
                        if (kind == ChangeKind.INSERT) {
                            return mSpendDBRx.insertRecord(record.getKind(), record.getValue(), DateUtils.dateToSql(record.getDate()))
                                    .doOnNext(id -> mRealmHelper.changeRecordId(change.getRecordId(), id.getId()));
                        } else if (kind == ChangeKind.UPDATE) {
                            return mSpendDBRx.updateRecord(record.getKind(), record.getValue(), DateUtils.dateToSql(record.getDate()), record.getId());
                        } else {
                            return Observable.error(new RuntimeException("Unknown change kind!"));
                        }
                    }
                }, (change, o) -> {
                    // pair <id_before, id_after>.
                    // they are different only for "INSERT".
                    Integer recordId = change.getRecordId();
                    if (change.getKind() == ChangeKind.INSERT) {
                        return new Pair<>(recordId, ((Id) o).getId());
                    }
                    return new Pair<>(recordId, recordId);
                }, 1)
                .doOnNext(pair -> mRealmHelper.deleteChange(pair.first))
                .doOnNext(pair -> LogUtils.d("change for record #" + pair.first + "; #" + pair.second + " was pushed to server"))
                .map(pair -> pair.second)
                .map(Id::new);
    }

    public Observable<List<String>> getDistinctKinds() {
        return mSpendDBRx.getDistinctKinds()
                .map(Kind::getKind)
                .toList()
                .onErrorResumeNext(throwable -> Observable.just(mRealmHelper.getDistinctKind()))
                .compose(applySchedulers());
    }

    public Observable<List<Change>> getAllChanges() {
        return Observable
                .just(mRealmHelper.getAllChanges())
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
