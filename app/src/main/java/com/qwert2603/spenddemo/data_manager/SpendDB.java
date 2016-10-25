package com.qwert2603.spenddemo.data_manager;

import com.qwert2603.retrobase.DBInterface;
import com.qwert2603.retrobase.DBQuery;
import com.qwert2603.retrobase.rx.DBInterfaceRx;
import com.qwert2603.retrobase.rx.DBMakeRx;

import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

@DBInterface(url = SpendDB.URL, login = SpendDB.USER_NAME, password = SpendDB.PASSWORD)
@DBInterfaceRx
public interface SpendDB {

    String USER_NAME = "postgres";
    String PASSWORD = "1234";
    String URL = "jdbc:postgresql://192.168.1.26:5432/spend";

    @DBMakeRx(modelClassName = "com.qwert2603.spenddemo.model.Record")
    @DBQuery("SELECT * from spend_test")
    ResultSet getAllRecords();

    @DBMakeRx(modelClassName = "com.qwert2603.spenddemo.model.Record")
    @DBQuery("SELECT * FROM spend_test ORDER BY date, id")
    ResultSet getAllRecordsOrdered() throws SQLException;

    @DBMakeRx
    @DBQuery("DELETE FROM spend_test WHERE id = ?")
    void deleteRecord(int id) throws SQLException;

    @DBMakeRx(modelClassName = "com.qwert2603.spenddemo.model.Id")
    @DBQuery("INSERT INTO spend_test (kind, value, date) VALUES (?, ?, ?) returning id")
    ResultSet insertRecord(String kind, int value, Date date) throws SQLException;

    @DBMakeRx(modelClassName = "com.qwert2603.spenddemo.model.Id")
    @DBQuery("UPDATE spend_test SET kind=?, value=?, date=? WHERE id=? returning id")
    ResultSet updateRecord(String kind, int value, Date date, int id) throws SQLException;

    @DBMakeRx(modelClassName = "com.qwert2603.spenddemo.model.Kind")
    @DBQuery("SELECT kind FROM spend_test GROUP BY kind ORDER BY count(*) DESC")
    ResultSet getDistinctKinds() throws SQLException;
}
