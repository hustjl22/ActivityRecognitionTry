package com.example.jennahuston.activityrecognitiontry;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class BoundedService extends Service implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private final static int DELAY=100;
    private Handler myHandler = new Handler();
    private double rms;

    private LinkedList<Double> listAccX;
    private LinkedList<Double> listAccY;
    private LinkedList<Double> listAccZ;

    private ArrayList<Activity> activities;

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this, accelerometer);
    }

    public BoundedService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL, DELAY);

        listAccX = new LinkedList<Double>();
        listAccY = new LinkedList<Double>();
        listAccZ = new LinkedList<Double>();

        activities = new ArrayList<>();
        activities.add(new Activity());

        final Handler handler = new Handler();
        Timer timer = new Timer();
        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @SuppressWarnings("unchecked")
                    public void run() {
                        try {
                            determineActivity();
                        }
                        catch (Exception e) {
                            System.out.println("Error getting activity");
                            System.out.println(e.getStackTrace());
                        }
                    }
                });
            }
        };
        timer.schedule(doAsynchronousTask, 120000, 120000);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor mySensor = event.sensor;

        switch (mySensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                myHandler.post(new AcclWork(event));
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private MyBinder mybinder_ = new MyBinder();

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.

        return mybinder_;

    }

    public synchronized ArrayList<Activity> getActivities() {

        return activities;
    }

    public synchronized void determineActivity() {
        Activity currentActivity = activities.get(activities.size() - 1);
        currentActivity.setEnd(new Date());

        double averageAX = 0;
        double averageAY = 0;
        double averageAZ = 0;

        for(int i = 0; i < listAccX.size(); i++) {
            averageAX += listAccX.get(i);
            averageAY += listAccY.get(i);
            averageAZ += listAccZ.get(i);
        }

        averageAX /= listAccX.size();
        averageAY /= listAccX.size();
        averageAZ /= listAccX.size();

        double angle = Math.atan2(averageAY, averageAZ)/(Math.PI/180);


        if(angle < 10) {
            currentActivity.setType(Activity.Type.SLEEPING);
            Toast.makeText(this, "Sleeping " + angle, Toast.LENGTH_LONG).show();
        }
        else {
            if(averageAX > -.8 || averageAZ < .8) {
                currentActivity.setType(Activity.Type.SITTING);
                Toast.makeText(this, "sitting " + angle + " " + averageAX, Toast.LENGTH_LONG);
            }
            else {
                currentActivity.setType(Activity.Type.RUNNING);
                Toast.makeText(this, "running " + angle + " " + averageAX, Toast.LENGTH_LONG);
            }
        }

        listAccX = new LinkedList<>();
        listAccY = new LinkedList<>();
        listAccZ = new LinkedList<>();


        activities.add(new Activity());
    }

    private class AcclWork implements Runnable {
        private SensorEvent event_;

        public AcclWork(SensorEvent event) {
            event_ = event;
        }

        //Add code to get and process data for activity recognition
        @Override
        public void run() {
            double acclx = event_.values[0];
            double accly = event_.values[1];
            double acclz = event_.values[2];

            listAccX.add((Double) acclx);
            listAccY.add((Double) accly);
            listAccZ.add((Double) acclz);

        }
    }



    public class MyBinder extends Binder {

        BoundedService getService() {
            return BoundedService.this;
        }
    }

    public String msg(){
        return "Hello World";
    }


}
