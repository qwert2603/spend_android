package com.qwert2603.spenddemo;

import com.qwert2603.retrobase.generated.SpendDBImpl;
import com.qwert2603.retrobase.rx.generated.SpendDBRx;
import com.qwert2603.spenddemo.model.Record;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Date;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.functions.BiFunction;
import io.reactivex.schedulers.Schedulers;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static void qq(Object o) {
        System.out.println((System.currentTimeMillis() / 1000) + " " + Thread.currentThread() + " " + o);
    }

    @Test
    public void addition_isCorrect() throws Exception {

        SpendDBRx spendDBRx = new SpendDBRx(new SpendDBImpl());
        BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\alex\\Downloads\\spend 2016-10-15.csv"));
        String s;
        while ((s=bufferedReader.readLine())!=null){
            String[] split = s.split(",");
            spendDBRx.insertRecord(split[1], Integer.parseInt(split[2]), Date.valueOf(split[3])).subscribe(id -> System.out.println(""+id.getId()));
        }

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


//        SpendDBImpl spendDB = new SpendDBImpl();
//        for (int i = 0; i < 14; i++) {
//            spendDB.insertRecord("food", i * 3 + 1, new Date(System.currentTimeMillis()));
//        }

//        Observable
//                .fromEmitter(objectEmitter -> {
//                    try {
//                        ResultSet resultSet = spendDB.getAllRecords();
//                        while (resultSet.next()) {
//                            objectEmitter.onNext(new com.qwert2603.spenddemo.model.Record(resultSet));
//                        }
//                        objectEmitter.onCompleted();
//                    } catch (Exception e) {
//                        objectEmitter.onError(e);
//                    }
//                }, Emitter.BackpressureMode.BUFFER)
//                .zipWith(Observable.interval(1, TimeUnit.NANOSECONDS), (dataBaseRecord, aLong) -> dataBaseRecord)
//                .subscribeOn(Schedulers.io())
//                .observeOn(Schedulers.newThread())
//                .subscribe(System.out::println);


        Thread.sleep(1000000);
    }
}