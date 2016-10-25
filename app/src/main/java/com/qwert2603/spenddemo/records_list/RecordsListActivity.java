package com.qwert2603.spenddemo.records_list;

import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import com.qwert2603.spenddemo.R;

import org.postgresql.Driver;

public class RecordsListActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            Class.forName(Driver.class.getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        if (savedInstanceState == null) {
            FragmentManager fragmentManager = getFragmentManager();
            fragmentManager.beginTransaction()
                    .add(R.id.fragment_container, new RecordsListFragment())
                    .commitAllowingStateLoss();
        }

    }
}
