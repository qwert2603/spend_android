package com.qwert2603.spenddemo.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Id {
    private int mId;

    public Id(int id) {
        mId = id;
    }

    public Id(ResultSet resultSet) throws SQLException {
        mId = resultSet.getInt(1);
    }

    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }
}
