package com.example.jennahuston.activityrecognitiontry;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getName();

    private BoundedService myService;
    private BoundedService.MyBinder binder_;
    private Boolean connected = false;

    private ArrayList<Activity> activityHistoryList;
    private ListView activityHistoryListView;
    private TextView curActivityText;
    private ActivityAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Bind the service
        Intent myIntent2 = new Intent(this, BoundedService.class);
        bindService(myIntent2, mConnection, BIND_AUTO_CREATE);

        Timer timer = new Timer();
        GetActivitiesTimerTask doAsynchronousTask = new GetActivitiesTimerTask();
        timer.schedule(doAsynchronousTask, 125000, 120000);

        curActivityText = (TextView) findViewById(R.id.curActivityText);

        activityHistoryListView = (ListView) findViewById(R.id.activityHistoryView);
        curActivityText = (TextView) findViewById(R.id.curActivityText);

        activityHistoryList = new ArrayList<>();
        adapter = new ActivityAdapter(this, activityHistoryList);

        activityHistoryListView.setAdapter(adapter);
    }

    public void setActivityList(){
        if (connected) {
            activityHistoryList.clear();
            activityHistoryList.addAll(myService.getActivities());
            Log.d(TAG, "Got activity list (length = " + activityHistoryList.size() + ")");

            if (activityHistoryList.size() > 0) {
                Activity curActivity = activityHistoryList.get(activityHistoryList.size() - 1);
                Log.d(TAG, "Setting current activity to " + curActivity.getTypeString());
                curActivityText.setText(curActivity.getTypeString());

                Collections.sort(activityHistoryList);
                adapter.notifyDataSetChanged();
                writeHistoryToFile();
            } else {
                Log.w(TAG, "No activities returned");
            }
        } else {
            Log.w(TAG, "Service is not connected");
        }
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
            connected = false;
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

    public void writeHistoryToFile() {
        String fileName = new SimpleDateFormat("yyyyMMdd").format(new Date()) + ".txt";

        File path = this.getExternalFilesDir(null);
        File file = new File(path, fileName);

        Log.d(TAG, "Attempting to write to " + file.getAbsolutePath());

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter writer = new PrintWriter(f);

            for (Activity a : activityHistoryList) {
                writer.write(a.toString() + "\n");
            }
            writer.close();
            f.close();

            Log.d(TAG, "Successfully wrote history to " + file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private class GetActivitiesTimerTask extends TimerTask {

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        setActivityList();
                    }
                    catch (Exception e) {
                        Log.e(TAG, "Error getting activities: " + e.getMessage());
                        e.printStackTrace();
                    }
                }
            });
        }
    }
}
