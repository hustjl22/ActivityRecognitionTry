package com.example.jennahuston.activityrecognitiontry;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

public class MainActivity extends AppCompatActivity {

    BoundedService myService;
    BoundedService.MyBinder binder_;
    Boolean connected = false;

    ArrayList<Activity> activityHistoryList;
    ListView activityHistoryListView;
    TextView curActivityText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Bind the service
        Intent myIntent2 = new Intent(this, BoundedService.class);
        bindService(myIntent2, mConnection, BIND_AUTO_CREATE);

        activityHistoryListView = (ListView) findViewById(R.id.activityHistoryView);
        curActivityText = (TextView) findViewById(R.id.curActivityText);

        activityHistoryList = new ArrayList<>();
        ActivityAdapter adapter = new ActivityAdapter(this, activityHistoryList);

        activityHistoryListView.setAdapter(adapter);

        // TODO Remove fake activities
        Activity a1 = new Activity();
        Activity a2 = new Activity(Activity.Type.RUNNING);
        Activity a3 = new Activity(Activity.Type.SITTING);
        a1.start();
        a1.finish();
        adapter.add(a1);
        adapter.add(a2);
        adapter.add(a3);
        Collections.sort(activityHistoryList);
        adapter.notifyDataSetChanged();
        // TODO Remove fake activities
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            binder_ = (BoundedService.MyBinder) service;
            myService = binder_.getService();
            connected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
