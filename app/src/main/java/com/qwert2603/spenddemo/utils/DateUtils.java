package com.qwert2603.spenddemo.utils;

import android.support.annotation.Nullable;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {

    private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    public static String formatDate(Date date) {
        return DATE_FORMAT.format(date);
    }

    @Nullable
    public static Date createDate(String s) {
        try {
            return DATE_FORMAT.parse(s);
        } catch (ParseException e) {
            LogUtils.e(e);
            return null;
        }
    }

    public static java.sql.Date dateToSql(java.util.Date date) {
        return new java.sql.Date(date.getTime());
    }
}
