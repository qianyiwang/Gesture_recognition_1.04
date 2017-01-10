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
    Queue<Float> pitchBuffer, rollBuffer;
    Queue<Float> accXBuffer, accYBuffer, accZBuffer;
    private SlidingWindow slidingWindow;
    @Override
    public void onCreate() {
        super.onCreate();

        mSensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));
        senAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_FASTEST);//adjust the frequency
        mSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_FASTEST);//adjust the frequency
        pitchBuffer = new LinkedList<Float>();
        rollBuffer = new LinkedList<Float>();
        accXBuffer = new LinkedList<Float>();
        accYBuffer = new LinkedList<Float>();
        accZBuffer = new LinkedList<Float>();
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

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            float acc_x = event.values[0];
            float acc_y = event.values[1];
            float acc_z= event.values[2];
            float pitch = calculatePitch(acc_y, acc_z);
            float roll = calculateRoll(acc_x, acc_z);
            if(accXBuffer.size()<40){
                accXBuffer.offer(acc_x);
                accYBuffer.offer(acc_y);
                accZBuffer.offer(acc_z);
            }


//            float omegaMagnitude = (float) Math.sqrt(acc_x * acc_x + acc_y * acc_y + acc_z * acc_z);

//            Log.v("pitch",pitch+"");
//            Log.v("_roll",roll+"");

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
    private float calculatePitch(float y, float z){
        return (float) (180/Math.PI * Math.atan2(y/9.8,z/9.8));
    }
    private float calculateRoll(float x, float z){
        return (float) (180/Math.PI * Math.atan2(x/9.8,z/9.8));
    }
    private float calculateMagnitude(float x, float y, float z){
        return (float) Math.sqrt(x * x + y * y + z * z);
    }



// Sliding Window Algorithm
    private class SlidingWindow extends AsyncTask<Void, Void, Void>{

    @Override
    protected Void doInBackground(Void... voids) {
        float[] accXWindow = new float[40];
        float[] accYWindow = new float[40];
        float[] accZWindow = new float[40];
        int pos = 0;
        while(true){
            if (isCancelled())
            {
                break;
            }

            if(accXBuffer.peek()!=null&&accYBuffer.peek()!=null&&accZBuffer.peek()!=null){
                accXWindow[pos] = accXBuffer.poll();
                accYWindow[pos] = accYBuffer.poll();
                accZWindow[pos] = accZBuffer.poll();
                pos++;
                if(pos==40){
                    pos = 0;

                    for(int i=0; i<40; i++){
                        float roll = calculateRoll(accXWindow[i], accZWindow[i]);
                        float pitch = calculatePitch(accYWindow[i], accZWindow[i]);
                        Log.v("_roll", roll+"");
                        Log.v("pitch", pitch+"");
                    }
                }
            }
        }
        return null;
    }
}
}
