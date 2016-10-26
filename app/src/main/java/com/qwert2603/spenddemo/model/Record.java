package com.qwert2603.spenddemo.model;

import com.qwert2603.spenddemo.base.ViewType;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class Record extends RealmObject implements ViewType {

    public static final String ID = "mId";
    public static final String KIND = "mKind";
    public static final String DATE = "mDate";

    @PrimaryKey
    private int mId;

    @Index
    private String mKind;
    private int mValue;
    private Date mDate;

    public Record() {
    }

    public Record(int id, String kind, int value, Date date) {
        mId = id;
        mKind = kind;
        mValue = value;
        mDate = date;
    }

    public Record(ResultSet resultSet) throws SQLException {
        mId = resultSet.getInt("id");
        mKind = resultSet.getString("kind");
        mValue = resultSet.getInt("value");
        mDate = resultSet.getDate("date");
    }

    @Override
    public int getViewType() {
        return ViewType.RECORD;
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    public String getKind() {
        return mKind;
    }

    public void setKind(String kind) {
        mKind = kind;
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = value;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date date) {
        mDate = date;
    }

    @Override
    public String toString() {
        return "Record{" +
                "mId=" + mId +
                ", mKind='" + mKind + '\'' +
                ", mValue=" + mValue +
                ", mDate=" + mDate +
                '}';
    }

    public String toStringLines() {
        return "Record{" +
                "\n mId=" + mId +
                ",\n mKind='" + mKind + '\'' +
                ",\n mValue=" + mValue +
                ",\n mDate=" + mDate +
                "\n}";
    }
}
