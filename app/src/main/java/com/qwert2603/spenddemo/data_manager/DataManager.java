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

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableTransformer;
import io.reactivex.Single;
import io.reactivex.SingleTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class DataManager {

    @SuppressWarnings("unused")
    private static final Record IF_LIST_EMPTY_RECORD
            = new Record(-1, "error happen, this should never be seen", 0, new java.sql.Date(0));

    private SpendDB mSpendDB = new SpendDBImpl();
    private SpendDBRx mSpendDBRx = new SpendDBRx(mSpendDB);

    private RealmHelper mRealmHelper = new RealmHelper();

    public Single<List<ViewType>> getAllRecords() {
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
                .startWith(Observable.fromCallable(this::pushChangesToServer).ignoreElements().toObservable())
                .toList()
                .doOnSuccess(records -> mRealmHelper.replaceRecordsList(records))
                .onErrorResumeNext(throwable -> {
                    LogUtils.e(throwable);
                    LogUtils.d("loading records from realm");
                    return Single.just(mRealmHelper.getAllRecords());
                })
                .toObservable()
                .flatMap(Observable::fromIterable)
                .map(record -> ((ViewType) record))
                .toList()
                .compose(applySchedulersSingle());
    }

    public Single<Id> insertRecord(Record record) {
        return createObservableForChange((disposable) -> mRealmHelper.insertRecord(record), record.getId());
    }

    public Single<Id> removeRecord(int recordId) {
        return createObservableForChange((disposable) -> mRealmHelper.removeRecord(recordId), recordId);
    }

    public Single<Id> updateRecord(Record record) {
        return createObservableForChange((disposable) -> mRealmHelper.updateRecord(record), record.getId());
    }

    private Single<Id> createObservableForChange(Consumer<? super Disposable> onSubscribe, int recordId) {
        return Observable.fromCallable(this::pushChangesToServer)
                .doOnSubscribe(onSubscribe)
                .flatMap(Observable::fromIterable)
                .last(new Id(recordId))
                .onErrorReturn(throwable -> new Id(recordId))
                .compose(applySchedulersSingle());
    }

    private List<Id> pushChangesToServer() throws Exception {
        List<Id> ids = new ArrayList<>();

        List<Change> changes = mRealmHelper.getAllChanges();
        for (Change change : changes) {
            Integer recordId = change.getRecordId();
            int kind = change.getKind();
            if (kind == ChangeKind.DELETE) {
                mSpendDB.deleteRecord(recordId);
            } else {
                Record record = mRealmHelper.getRecordById(recordId);
                if (kind == ChangeKind.INSERT) {
                    ResultSet resultSet = mSpendDB.insertRecord(record.getKind(), record.getValue(), DateUtils.dateToSql(record.getDate()));
                    if (resultSet.next()) {
                        mRealmHelper.changeRecordId(recordId, new Id(resultSet).getId());
                    }
                    resultSet.close();
                } else if (kind == ChangeKind.UPDATE) {
                    ResultSet resultSet = mSpendDB.updateRecord(record.getKind(), record.getValue(), DateUtils.dateToSql(record.getDate()), record.getId());
                    resultSet.close();
                }
            }
            mRealmHelper.deleteChange(recordId);
            ids.add(new Id(recordId));
        }

        return ids;
    }

    public Single<List<String>> getDistinctKinds() {
        return mSpendDBRx.getDistinctKinds()
                .map(Kind::getKind)
                .toList()
                .onErrorResumeNext(throwable -> Single.just(mRealmHelper.getDistinctKind()))
                .compose(applySchedulersSingle());
    }

    public Single<List<Change>> getAllChanges() {
        return Single
                .just(mRealmHelper.getAllChanges())
                .compose(applySchedulersSingle());
    }

    @SuppressWarnings("all")    // redundant casting
    private final ObservableTransformer mObservableTransformer = observable -> ((Observable) observable)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

    @SuppressWarnings(value = {"unchecked", "unused"})
    private <T> ObservableTransformer<T, T> applySchedulersObservable() {
        return (ObservableTransformer<T, T>) mObservableTransformer;
    }

    @SuppressWarnings("all")    // redundant casting
    private final SingleTransformer mSingleTransformer = single -> ((Single) single)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread());

    @SuppressWarnings("unchecked")
    private <T> SingleTransformer<T, T> applySchedulersSingle() {
        return (SingleTransformer<T, T>) mSingleTransformer;
    }

    @SuppressWarnings("unused")
    private <T, K> ObservableTransformer<T, List<T>> sortedGroupBy(Function<T, K> keySelector) {
        return tObservable -> tObservable
                .toList()
                .toObservable()
                .flatMap(ts -> Observable.create(subscriber -> {
                    K k = null;
                    List<T> list = new ArrayList<>();
                    for (T t : ts) {
                        if (k == null) {
                            k = keySelector.apply(t);
                        } else if (!k.equals(keySelector.apply(t))) {
                            subscriber.onNext(list);
                            k = keySelector.apply(t);
                            list = new ArrayList<>();
                        }
                        list.add(t);
                    }
                    if (!list.isEmpty()) {
                        subscriber.onNext(list);
                    }
                    subscriber.onComplete();
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
    private <E, T, V> ObservableTransformer<Observable<List<E>>, List<T>> addAggregatedValue(Function<E, T> itemsTransformer,
                                                                                             Function<E, V> valueSelector,
                                                                                             V initialValue,
                                                                                             BiFunction<V, V, V> valueAggregator,
                                                                                             E ifListEmptyItem,
                                                                                             BiFunction<V, E, T> aggregatedValueCreator) {
        return observable -> observable.flatMap(listObservable -> {
            Observable<E> items = listObservable.flatMap(Observable::fromIterable);
            Observable<T> transformedItems = items.map(itemsTransformer);
            Single<T> aggregatedValue = items
                    .map(valueSelector)
                    .reduce(initialValue, valueAggregator)
                    .zipWith(items.first(ifListEmptyItem), aggregatedValueCreator);
            return Observable.concat(aggregatedValue.toObservable(), transformedItems).toList().toObservable();
        });
    }

}
