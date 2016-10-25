package com.qwert2603.spenddemo.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Change extends RealmObject {

    @PrimaryKey
    private int mId;

    @ChangeKind
    private int mKind;

    private int mRecordId;

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

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
                "mId=" + mId +
                ", mKind=" + mKind +
                ", mRecordId=" + mRecordId +
                '}';
    }
}
