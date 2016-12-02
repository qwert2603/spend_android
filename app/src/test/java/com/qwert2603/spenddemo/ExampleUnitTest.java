package com.qwert2603.spenddemo;

import com.qwert2603.retrobase.generated.SpendDBImpl;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {

    private static void qq(Object o) {
        System.out.println((System.currentTimeMillis()) + " " + Thread.currentThread() + " " + o);
    }

    @Test
    public void addition_isCorrect() throws Exception {


        BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\alex\\Downloads\\r.txt"));
        String s;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        SpendDBImpl spendDB = new SpendDBImpl();
        boolean bb = false;
        Date date = null;
        while ((s = bufferedReader.readLine()) != null) {
            String[] split = s.split("\t");

            if (!bb) {
                bb = true;
                split[0] = split[0].substring(1);
            }

            if (date == null || split[0].length() > 2) {
                date = new Date(dateFormat.parse(split[0]).getTime());
            }

            spendDB.insertMother(split[1], split[2], Double.valueOf(split[3].replace(",", ".")), date);
        }


//        SpendDBRx spendDBRx = new SpendDBRx(new SpendDBImpl());
//        spendDBRx.deleteRecord(3896)
//                .subscribe(new CompletableObserver() {
//                    @Override
//                    public void onSubscribe(Disposable d) {
//                        qq("onSubscribe " + d);
//                    }
//
//                    @Override
//                    public void onComplete() {
//                        qq("onComplete");
//                    }
//
//                    @Override
//                    public void onError(Throwable e) {
//                        qq("onError " + e);
//                    }
//                });

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


        //Thread.sleep(1000000);
    }
}