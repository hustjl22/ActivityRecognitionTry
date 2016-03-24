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
import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

public class BoundedService extends Service implements SensorEventListener {

    private static final String TAG = BoundedService.class.getName();

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private final static int DELAY=100;
    private Handler myHandler = new Handler();

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

        listAccX = new LinkedList<>();
        listAccY = new LinkedList<>();
        listAccZ = new LinkedList<>();

        activities = new ArrayList<>();

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
                            Log.e(TAG, "Error getting activity: " + e.getMessage());
                            e.printStackTrace();
                        }
                    }
                });
            }
        };

        // Every two minutes determine the current activity
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
        Log.d(TAG, "Determining the current activity");
        // End current activity and start a new one

        Activity currentActivity;

        // If activities is not empty, there is already a current activity
        // We need to end that one before starting a new activity
        if (activities.size() > 0) {
            currentActivity = activities.get(activities.size() - 1);
            currentActivity.finish();
        }
        currentActivity = new Activity();

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
        }
        else if (averageAX > -.8 || averageAZ < .8) {
            currentActivity.setType(Activity.Type.SITTING);
        } else {
            currentActivity.setType(Activity.Type.RUNNING);
        }

        // Reset history of accelerations
        listAccX = new LinkedList<>();
        listAccY = new LinkedList<>();
        listAccZ = new LinkedList<>();

        Log.d(TAG, "Adding current activity of type " + currentActivity.getTypeString());
        activities.add(currentActivity);
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

            listAccX.add(acclx);
            listAccY.add(accly);
            listAccZ.add(acclz);
        }
    }



    public class MyBinder extends Binder {

        BoundedService getService() {
            return BoundedService.this;
        }
    }

}
