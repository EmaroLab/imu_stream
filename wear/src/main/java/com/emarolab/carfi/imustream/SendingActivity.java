package com.emarolab.carfi.imustream;

import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import java.util.Vector;

public class SendingActivity extends WearableActivity implements SensorEventListener {

    private float[] last_acc = new float[3], last_gyro = new float[3];
    private long last_acc_time = 0;
    private long last_gyro_time = 0;

    private vectorMessage accMsg, gyroMsg;

    private TextView dataAcc, dataGyro;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer, senGyroscope;

    private String deviceName;

    private float precision = 1000;

    private boolean sensorUpdate = true;
    private boolean accFlag = false;
    private boolean gyroFlag = false;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);

        // Enables Always-on
        setAmbientEnabled();

        Intent intent = getIntent();
        deviceName = intent.getStringExtra(MainActivity.deviceNamePath);


        //Initialize accelerometer and gyroscope data containers
        accMsg = new vectorMessage(1);
        gyroMsg = new vectorMessage(1);

        TextView TextDevName;
        TextDevName = findViewById(R.id.deviceName);
        TextDevName.setText(deviceName);

        accMsg.setTopics((deviceName + "/accelerometer"),(deviceName + "/time_accelerometer"));
        gyroMsg.setTopics((deviceName + "/gyroscope"),(deviceName + "/time_gyroscope"));

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_GAME);

        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_GAME);


        dataAcc = findViewById(R.id.acc);
        dataGyro = findViewById(R.id.gyro);


        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(Bundle connectionHint) {
                    }
                    @Override
                    public void onConnectionSuspended(int cause) {
                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult result) {
                    }
                })
                .addApi(Wearable.API)
                .build();
        mGoogleApiClient.connect();
    }

    private long[] convertLong(Vector<Long> data) {
        long[] output = new long[data.size()];
        for (int i = 0; i < data.size(); i++ ){
            output[i] = data.get(i);
        }
        return output;
    }

    private float[] convertFloat(Vector<Float> data) {
        float[] output = new float[data.size()];
        for (int i = 0; i < data.size(); i++ ){
            output[i] = data.get(i);
        }
        return output;
    }

    private void sender()
    {
        if (sensorUpdate & accFlag || sensorUpdate & gyroFlag) {
            sensorUpdate = false;
            syncSampleDataItem();
        }
    }

     private void syncSampleDataItem() {
        if (mGoogleApiClient == null)
            return;
        final PutDataMapRequest putRequest = PutDataMapRequest.create("/IMU");
        final DataMap map = putRequest.getDataMap();

        if(accFlag) {
            long[] temp = convertLong(accMsg.getTimestamp());
            if (temp.length > 0 ) {
                map.putFloatArray(accMsg.getTopic(), convertFloat(accMsg.getData()));
                map.putLongArray(accMsg.getTopicTime(), convertLong(accMsg.getTimestamp()));
                accMsg.flush();
            }
        }

        if(gyroFlag) {
            long[] temp = convertLong(gyroMsg.getTimestamp());
            if (temp.length > 0) {
                map.putFloatArray(gyroMsg.getTopic(), convertFloat(gyroMsg.getData()));
                map.putLongArray(gyroMsg.getTopicTime(), convertLong(gyroMsg.getTimestamp()));
                gyroMsg.flush();
            }
        }

        if(gyroFlag || accFlag) {
            PutDataRequest request = putRequest.asPutDataRequest();
            request.setUrgent();
            Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
                @Override
                public void onResult(DataApi.DataItemResult dataItemResult) {
                    sensorUpdate = true;
                }
            });
        }
    }

    /** Function triggered when new sensors data should be processed
     */
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;
        /** If the new data is from the gyroscope display it and push it in the gyroscope data
         *  container (gyroMsg)
         */
        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            last_gyro[0] = ((int) (sensorEvent.values[0] * precision)) / precision;
            last_gyro[1] = ((int) (sensorEvent.values[1] * precision)) / precision;
            last_gyro[2] = ((int) (sensorEvent.values[2] * precision)) / precision;

            last_gyro_time = System.currentTimeMillis() + (sensorEvent.timestamp - System.nanoTime()) / 1000000L;

            dataGyro.setText("gyro: " + last_gyro[0] + " " + last_gyro[1] + " " + last_gyro[2]);
            gyroFlag = gyroMsg.push(last_gyro, last_gyro_time);
        }

        /** If the new data is from the accelerometer display it and push it in the accelerometer
         *  data container (gyroAcc)
         */
        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            last_acc[0] = ((int) (sensorEvent.values[0] * precision)) / precision;
            last_acc[1] = ((int) (sensorEvent.values[1] * precision)) / precision;
            last_acc[2] = ((int) (sensorEvent.values[2] * precision)) / precision;

            last_acc_time = System.currentTimeMillis() + (sensorEvent.timestamp - System.nanoTime()) / 1000000L;

            dataAcc.setText("acc: " + last_acc[0] + " " + last_acc[1] + " " + last_acc[2]);
            accFlag = accMsg.push(last_acc, last_acc_time);
        }

        sender();
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void stopStreaming(View view) {
        senSensorManager.unregisterListener(this);
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);
    }
}
