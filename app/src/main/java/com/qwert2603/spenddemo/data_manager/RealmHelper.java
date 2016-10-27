package com.qwert2603.spenddemo.data_manager;

import com.qwert2603.spenddemo.model.Change;
import com.qwert2603.spenddemo.model.ChangeKind;
import com.qwert2603.spenddemo.model.Record;
import com.qwert2603.spenddemo.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class RealmHelper {

    private void executeSyncTransaction(Realm.Transaction transaction) {
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

    List<Record> getAllRecords() {
        Realm realm = Realm.getDefaultInstance();
        String[] sortFields = {Record.DATE, Record.ID};
        Sort[] sortOrders = {Sort.ASCENDING, Sort.ASCENDING};
        RealmResults<Record> realmResults = realm.where(Record.class).findAllSorted(sortFields, sortOrders);
        List<Record> records = realm.copyFromRealm(realmResults);
        realm.close();
        return records;
    }

    List<Change> getAllChanges() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Change> realmResults = realm.where(Change.class).findAll();
        List<Change> changeList = realm.copyFromRealm(realmResults);
        realm.close();
        return changeList;
    }

    void replaceRecordsList(List<Record> records) {
        executeSyncTransaction(realm -> {
            realm.delete(Record.class);
            realm.insert(records);
            Set<Integer> recordIds = new HashSet<>();
            for (Record record : records) {
                recordIds.add(record.getId());
            }
            RealmResults<Change> changes = realm.where(Change.class).findAll();
            for (Change change : changes) {
                if (!recordIds.contains(change.getRecordId())) {
                    LogUtils.d("no record for change " + change + " so delete it.");
                    change.deleteFromRealm();
                }
            }
        });
    }

    void changeRecordId(int oldId, int newId) {
        executeSyncTransaction(realm -> {
            Record old = realm.where(Record.class).equalTo(Record.ID, oldId).findFirst();
            Record record = realm.copyFromRealm(old);
            old.deleteFromRealm();
            record.setId(newId);
            realm.copyToRealm(record);
        });
    }

    void deleteChange(int recordId) {
        executeSyncTransaction(realm1 -> realm1.where(Change.class).equalTo(Change.RECORD_ID, recordId).findFirst().deleteFromRealm());
    }

    Record getRecordById(int recordId) {
        Realm realm = Realm.getDefaultInstance();
        Record realmRecord = realm.where(Record.class).equalTo(Record.ID, recordId).findFirst();
        Record record = realm.copyFromRealm(realmRecord);
        realm.close();
        return record;
    }

    List<String> getDistinctKind() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<Record> realmResults = realm.where(Record.class).distinct(Record.KIND);
        List<Record> records = realm.copyFromRealm(realmResults);
        realm.close();
        List<String> kinds = new ArrayList<>();
        for (Record record : records) {
            kinds.add(record.getKind());
        }
        return kinds;
    }

    void insertRecord(Record record) {
        executeSyncTransaction(realm -> {
            realm.copyToRealm(record);

            Change change = new Change();
            change.setKind(ChangeKind.INSERT);
            change.setRecordId(record.getId());

            realm.copyToRealm(change);
        });
    }

    void updateRecord(Record record) {
        executeSyncTransaction(realm -> {
            Record realmRecord = realm.where(Record.class).equalTo(Record.ID, record.getId()).findFirst();
            realmRecord.setKind(record.getKind());
            realmRecord.setValue(record.getValue());
            realmRecord.setDate(record.getDate());

            if (realm.where(Change.class).equalTo(Change.RECORD_ID, record.getId()).findFirst() == null) {
                Change change = new Change();
                change.setKind(ChangeKind.UPDATE);
                change.setRecordId(record.getId());

                realm.copyToRealm(change);
            }
        });
    }

    void removeRecord(int recordId) {
        executeSyncTransaction(realm -> {
            realm.where(Record.class).equalTo(Record.ID, recordId).findFirst().deleteFromRealm();

            Change prevChange = realm.where(Change.class).equalTo(Change.RECORD_ID, recordId).findFirst();
            if (prevChange != null && prevChange.getKind() == ChangeKind.INSERT) {
                prevChange.deleteFromRealm();
            } else {
                Change change = new Change();
                change.setKind(ChangeKind.DELETE);
                change.setRecordId(recordId);
                realm.copyToRealmOrUpdate(change);
            }
        });
    }

}
