package com.qwert2603.spenddemo;

import com.qwert2603.retrobase.generated.SpendDBImpl;
import com.qwert2603.retrobase.rx.generated.SpendDBRx;

import org.junit.Test;

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


        SpendDBRx spendDBRx = new SpendDBRx(new SpendDBImpl());
        spendDBRx.getAllM()
                .test()
                .assertNoErrors()
                .assertComplete();

//        Thread.sleep(100000);

//        BufferedReader bufferedReader = new BufferedReader(new FileReader("C:\\Users\\alex\\Downloads\\Новый текстовый документ.txt"));
//        String s;
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
//        SpendDBImpl spendDB = new SpendDBImpl();
//        boolean bb = false;
//        Date date = null;
//        while ((s = bufferedReader.readLine()) != null) {
//            String[] split = s.split("\t");
//
//
//            if (!bb) {
//                bb = true;
//                split[0] = split[0].substring(1);
//            }
//
//            if (date == null || split[0].length() > 2) {
//                date = new Date(dateFormat.parse(split[0]).getTime());
//            }
//
//            //calendar.set(Calendar.MONTH, Calendar.AUGUST);
//            //calendar.set(Calendar.DAY_OF_MONTH, (split[0].charAt(i) - '0') * 10 + (split[1].charAt(i + 1) - '0'));
//            spendDB.insertMother(split[1], split[2], Double.valueOf(split[3].replace(",", ".")),
//                    date);
//            //    new Date(calendar.getTimeInMillis()));
//        }


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