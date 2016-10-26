package com.qwert2603.spenddemo.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Change extends RealmObject {

    public static final String RECORD_ID = "mRecordId";

    @ChangeKind
    private int mKind;

    @PrimaryKey
    private int mRecordId;

    @ChangeKind
    public int getKind() {
        return mKind;
    }

    public void setKind(@ChangeKind int kind) {
        mKind = kind;
    }

    public Integer getRecordId() {
        return mRecordId;
    }

    public void setRecordId(Integer recordId) {
        mRecordId = recordId;
    }

    @Override
    public String toString() {
        return "Change{" +
                "mKind=" + mKind +
                ", mRecordId=" + mRecordId +
                '}';
    }
}
