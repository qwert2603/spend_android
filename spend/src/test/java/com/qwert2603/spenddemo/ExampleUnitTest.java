package com.qwert2603.spenddemo;

import com.qwert2603.spenddemo.model.remote_db.RemoteDBFacade;
import com.qwert2603.spenddemo.model.remote_db.RemoteDBImpl;

import org.junit.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class ExampleUnitTest {

    @Test
    public void addition_isCorrect() throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new FileReader("/home/alex/android_projects/SpendDemo/app/src/test/r.txt"));
        String s;
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
        RemoteDBFacade remoteDBFacade = new RemoteDBFacade(new RemoteDBImpl(
                "jdbc:postgresql://192.168.1.26:5432/spend",
                "postgres",
                "1234"
        ), "");
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

//            remoteDBFacade.insertMother(split[1], split[2], Double.valueOf(split[3].replace(",", ".")), date);
        }
    }
}