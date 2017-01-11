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
    final int windowSize = 100;
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
            if(accXBuffer.size()<windowSize){
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

    private float[] max_min_avg(float[] data){
        float[] res = new float[3];
        float max = data[0];
        float min = data[0];
        float avg = data[0];
        for (float f: data){
            if(f>max){
                max = f;
            }
            if(f<min){
                min = f;
            }
            avg += f;
        }
        avg = avg/data.length;
        res[0] = max;
        res[1] = min;
        res[2] = avg;
        return res;
    }

    private float[] std_zcr(float[] data, float avg){
        float[] res = new float[2];
        float std = 0;
        float zcr = 0;
        for(float f: data){
            std += (f-avg)*(f-avg);
            if(f>avg){
                zcr++;
            }
        }
        std = (float) Math.sqrt(std/data.length);
        res[0] = std;
        res[1] = zcr;
        return res;
    }

// Sliding Window Algorithm
    private class SlidingWindow extends AsyncTask<Void, Void, Void>{

    @Override
    protected Void doInBackground(Void... voids) {
        float[] accWindow = new float[windowSize];
        float[] rollWindow = new float[windowSize];
        float[] pitchWindow = new float[windowSize];
        int pos = 0;
        while(true){
            if (isCancelled())
            {
                break;
            }

            if(accXBuffer.peek()!=null&&accYBuffer.peek()!=null&&accZBuffer.peek()!=null){
                float acc_x = accXBuffer.poll();
                float acc_y = accYBuffer.poll();
                float acc_z = accZBuffer.poll();
                float magnitude = calculateMagnitude(acc_x, acc_y, acc_z);
                float roll = calculateRoll(acc_x, acc_z);
                float pitch = calculatePitch(acc_y, acc_z);
                accWindow[pos] = magnitude;
                rollWindow[pos] = roll;
                pitchWindow[pos] = pitch;
                pos++;
                if(pos==windowSize){
                    pos = 0;
                    // extract max-min, mean, standard deviation, zero cross rate in window

                    float[] roll_max_min_avg = max_min_avg(rollWindow);
                    float roll_max = roll_max_min_avg[0];
                    float roll_min = roll_max_min_avg[1];
                    float roll_avg = roll_max_min_avg[2];
                    float[] roll_std_zcr = std_zcr(rollWindow, roll_avg);
                    float roll_std = roll_std_zcr[0];
                    float roll_zcr = roll_std_zcr[1];

                    float[] pitch_max_min_avg = max_min_avg(pitchWindow);
                    float pitch_max = pitch_max_min_avg[0];
                    float pitch_min = pitch_max_min_avg[1];
                    float pitch_avg = pitch_max_min_avg[2];
                    float[] pitch_std_zcr = std_zcr(pitchWindow, pitch_avg);
                    float pitch_std = pitch_std_zcr[0];
                    float pitch_zcr = pitch_std_zcr[1];

                    Log.v("roll,pitch-100", (roll_max-roll_min)+","+roll_avg+","+roll_std+","+roll_zcr+","+(pitch_max-pitch_min)+","+pitch_avg+","+pitch_std+","+pitch_zcr);

                }
            }
        }
        return null;
    }
}
}
