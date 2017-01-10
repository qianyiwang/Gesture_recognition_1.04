package com.example.qianyiwang.gesture_recognition_104;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * Created by wangqianyi on 2016-11-21.
 */
public class MotionService extends Service implements SensorEventListener {

    //Sensor variable
    Sensor senAccelerometer, senGyroscope;
    SensorManager mSensorManager;
    Queue<Float> gyrXBuffer, gyrYBuffer, gyrZBuffer;
    private SlidingWindow slidingWindow;
    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
//        senAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
//        mSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);//adjust the frequency
        mSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_FASTEST);//adjust the frequency

        gyrXBuffer = new LinkedList<Float>();
        gyrYBuffer = new LinkedList<Float>();
        gyrZBuffer = new LinkedList<Float>();
    }

    @Override
    public void onDestroy() {
        mSensorManager.unregisterListener(this);
        slidingWindow.cancel(true);
        Toast.makeText(this,"stop motion service",0).show();
        super.onDestroy();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if(event.sensor.getType() == Sensor.TYPE_GYROSCOPE){
            float gyr_x = event.values[0];
            float gyr_y = event.values[1];
            float gyr_z= event.values[2];
            if(gyrXBuffer.size()<40){
                gyrXBuffer.offer(gyr_x);
                gyrYBuffer.offer(gyr_y);
                gyrZBuffer.offer(gyr_z);
            }

        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Toast.makeText(this,"start motion service",0).show();
        slidingWindow = new SlidingWindow();
        slidingWindow.execute();
        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // features methods
    private float calculateMagnitude(float x, float y, float z){
        return (float) Math.sqrt(x * x + y * y + z * z);
    }


// Sliding Window Algorithm
    private class SlidingWindow extends AsyncTask<Void, Void, Void>{

    @Override
    protected Void doInBackground(Void... voids) {
        float[] gyrXWindow = new float[40];
        float[] gyrYWindow = new float[40];
        float[] gyrZWindow = new float[40];
        int pos = 0;
        while(true){
            if (isCancelled())
            {
                break;
            }

            if(gyrXBuffer.peek()!=null&&gyrYBuffer.peek()!=null&&gyrZBuffer.peek()!=null){
                gyrXWindow[pos] = gyrXBuffer.poll();
                gyrYWindow[pos] = gyrYBuffer.poll();
                gyrZWindow[pos] = gyrZBuffer.poll();
                pos++;
                if(pos==40){
                    pos = 0;
                    for(int i=0; i<40; i++){
                        float magnitude = calculateMagnitude(gyrXWindow[i], gyrYWindow[i], gyrZWindow[i]);
                        Log.v("magni", magnitude+"");
                    }
                }
            }
        }
        return null;
    }
}
}
