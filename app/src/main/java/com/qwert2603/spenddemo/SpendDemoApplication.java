package com.qwert2603.spenddemo;

import android.app.Application;

import io.realm.Realm;

public class SpendDemoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Realm.init(SpendDemoApplication.this);
    }
}
