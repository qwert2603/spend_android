package com.qwert2603.spenddemo;

import org.junit.Test;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() throws Exception {



//        Observable
//                .create(new SyncOnSubscribe<ResultSet, Record>() {
//                    int i = 0;
//
//                    @Override
//                    protected ResultSet generateState() {
//                        return new SpendDBImpl().getAllRecords();
//                    }
//
//                    @Override
//                    protected ResultSet next(ResultSet state, Observer<? super Record> observer) {
//                        try {
//
//
//
//                            if (!state.next()) {
//                                observer.onCompleted();
//                            }
//                            System.out.println("_"+new Record(state));
//                            observer.onNext(new Record(state));
//                        } catch (SQLException e) {
//                            observer.onError(e);
//                        }
//                        return state;
//                    }
//                })
//                .zipWith(Observable.interval(1, TimeUnit.SECONDS), (dataBaseRecord, aLong) -> dataBaseRecord)
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.newThread())
//                .subscribe(System.out::println);


//        Observable
//                .fromEmitter(objectEmitter -> {
//                    try {
//                        ResultSet resultSet = new SpendDBImpl().getAllRecords();
//                        while (resultSet.next()) {
//                            objectEmitter.onNext(new com.qwert2603.spenddemo.model.Record(resultSet));
//                        }
//                        objectEmitter.onCompleted();
//                    } catch (Exception e) {
//                        objectEmitter.onError(e);
//                    }
//                }, Emitter.BackpressureMode.BUFFER)
//                .zipWith(Observable.interval(1, TimeUnit.SECONDS), (dataBaseRecord, aLong) -> dataBaseRecord)
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.newThread())
//                .subscribe(System.out::println);


        Thread.sleep(100000);
    }
}