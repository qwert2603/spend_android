package com.qwert2603.spenddemo.utils;

import android.util.Log;

public class LogUtils {
    private static final String TAG = "AASSDD";

    public static void d(String msg) {
        Log.d(TAG, msg);
    }

    public static void e(Throwable throwable) {
        Log.e(TAG, "error!", throwable);
    }
}
