package com.qwert2603.spenddemo;

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


//        BufferedReader bufferedReader = new BufferedReader(new FileReader("/home/alex/android_projects/SpendDemo/app/src/test/r.txt"));
//        String s;
//        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
//        SpendDBImpl spendDB = new SpendDBImpl();
//        boolean bb = false;
//        Date date = null;
//        while ((s = bufferedReader.readLine()) != null) {
//            String[] split = s.split("\t");
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
//            spendDB.insertMother(split[1], split[2], Double.valueOf(split[3].replace(",", ".")), date);
//        }
    }
}