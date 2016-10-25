package com.qwert2603.spenddemo.model;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Kind {
    private String mKind;

    public Kind(ResultSet resultSet) throws SQLException {
        mKind = resultSet.getString("kind");
    }

    public String getKind() {
        return mKind;
    }

    public void setKind(String kind) {
        mKind = kind;
    }
}
