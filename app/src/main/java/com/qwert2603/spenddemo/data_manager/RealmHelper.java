package com.qwert2603.spenddemo.data_manager;

import com.qwert2603.spenddemo.model.Record;

import io.realm.Realm;

public class RealmHelper {

    public void executeSyncTransaction(Realm.Transaction transaction) {
        Realm realm = Realm.getDefaultInstance();
        while (realm.isInTransaction()) {
            Thread.yield();
        }
        realm.executeTransaction(transaction);
        realm.close();
    }

    private static final int LOCAL_RECORDS_START_ID = Integer.MAX_VALUE / 2;

    public static int getNextRecordId(Realm realm) {
        Number number = realm.where(Record.class).greaterThanOrEqualTo(Record.ID, LOCAL_RECORDS_START_ID).max(Record.ID);
        return number != null ? number.intValue() + 1 : LOCAL_RECORDS_START_ID;
    }

}
