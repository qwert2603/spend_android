package com.qwert2603.spenddemo.data_manager;

import java.util.concurrent.ConcurrentLinkedQueue;

import io.realm.Realm;

public class RealmHelper {

    private ConcurrentLinkedQueue<Realm.Transaction> mTransactions = new ConcurrentLinkedQueue<>();

    public void addTransaction(Realm.Transaction transaction) {
        mTransactions.offer(transaction);
    }

    public void executeSyncAllTransactions() {
        Realm realm = Realm.getDefaultInstance();
        Realm.Transaction transaction;
        while ((transaction = mTransactions.peek()) != null) {
            realm.executeTransaction(transaction);
            mTransactions.remove();
        }
        realm.close();
        mTransactions.clear();
    }

    public boolean hasTransactions() {
        return mTransactions.isEmpty();
    }

}
