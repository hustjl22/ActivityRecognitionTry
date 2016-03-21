package com.example.jennahuston.activityrecognitiontry;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = MainActivity.class.getName();

    private BoundedService myService;
    private BoundedService.MyBinder binder_;
    private Boolean connected = false;

    private ArrayList<Activity> activityHistoryList;
    private ListView activityHistoryListView;
    private TextView curActivityText;
    private Button saveActivityHistoryButton;
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

        saveActivityHistoryButton = (Button) findViewById(R.id.saveActivityHistoryButton);
        saveActivityHistoryButton.setOnClickListener(this);

        curActivityText = (TextView) findViewById(R.id.curActivityText);

        activityHistoryListView = (ListView) findViewById(R.id.activityHistoryView);
        curActivityText = (TextView) findViewById(R.id.curActivityText);

        activityHistoryList = new ArrayList<>();
        adapter = new ActivityAdapter(this, activityHistoryList);

        activityHistoryListView.setAdapter(adapter);

        // TODO Remove fake activities
        Date d1 = new GregorianCalendar(2016, 1, 1, 0, 0, 0).getTime();
        Date d2 = new GregorianCalendar(2016, 1, 1, 0, 2, 0).getTime();
        Date d3 = new GregorianCalendar(2016, 1, 1, 0, 4, 0).getTime();
        Date d4 = new GregorianCalendar(2016, 1, 1, 0, 6, 0).getTime();
        Date d5 = new GregorianCalendar(2016, 1, 1, 0, 8, 0).getTime();
        Date d6 = new GregorianCalendar(2016, 1, 1, 0, 10, 0).getTime();
        Activity a1 = new Activity(Activity.Type.SLEEPING, d1, d2);
        Activity a2 = new Activity(Activity.Type.SITTING, d2, d3);
        Activity a3 = new Activity(Activity.Type.RUNNING, d3, d4);
        Activity a4 = new Activity(Activity.Type.SITTING, d4, d5);
        Activity a5 = new Activity(Activity.Type.SITTING, d5, d6);
        activityHistoryList.add(a1);
        activityHistoryList.add(a2);
        activityHistoryList.add(a3);
        activityHistoryList.add(a4);
        activityHistoryList.add(a5);
        Collections.sort(activityHistoryList);
        adapter.addAll(activityHistoryList);
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

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.saveActivityHistoryButton:
                writeHistoryToFile();
        }
    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void writeHistoryToFile() {
        if (!isExternalStorageWritable()) {
            String msg = "External storage is not writable!";
            Log.e(TAG, msg);
            Toast.makeText(this, msg, Toast.LENGTH_LONG);
        }

        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS + "/activityRecognition");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File file = new File(dir, new SimpleDateFormat("yyyyMMddhhmmss").format(new Date()) + ".txt");

        Log.d(TAG, "Attempting to write to " + file.getAbsolutePath());

        try {
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter writer = new PrintWriter(f);

            for (Activity a : activityHistoryList) {
                writer.write(a.toString());
            }

            Toast.makeText(this, "History saved in " + dir.getAbsolutePath(), Toast.LENGTH_LONG);
            Log.d(TAG, "Successfully wrote histry to " + file.getAbsolutePath());
        } catch (FileNotFoundException e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG);
        }
    }
}
