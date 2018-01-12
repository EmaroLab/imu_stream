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

import java.util.Timer;
import java.util.TimerTask;

public class SendingActivity extends WearableActivity implements SensorEventListener {

    private vectorMessage accMsg = new vectorMessage(), gyroMsg = new vectorMessage();
    private float[] last_acc = new float[3], last_gyro = new float[3];


    private TextView dataAcc, dataGyro;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer, senGyroscope;
    private Timer myTimer;

    private long lastUpdate = 0;

    private float precision = 1000;
    private int period = 20;

    private boolean sensorUpdate = true;

    private String msg_intertial;

    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sending);

        // Enables Always-on
        setAmbientEnabled();

        Intent intent = getIntent();
        String deviceName = intent.getStringExtra(MainActivity.deviceNamePath);

        TextView TextDevName;
        TextDevName = (TextView) findViewById(R.id.deviceName);
        TextDevName.setText(deviceName);

        accMsg.setTopic("sensors/accelerometer");
        gyroMsg.setTopic("sensors/gyroscope");

        senSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        senSensorManager.registerListener(this, senAccelerometer , SensorManager.SENSOR_DELAY_GAME);

        senGyroscope = senSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        senSensorManager.registerListener(this, senGyroscope , SensorManager.SENSOR_DELAY_GAME);


        dataAcc = (TextView) findViewById(R.id.acc);
        dataGyro = (TextView) findViewById(R.id.gyro);


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

        myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sender();
            }

        }, 0, period);

    }


    private void sender()
    {
        if (sensorUpdate) {
            sensorUpdate = false;
            syncSampleDataItem(accMsg,gyroMsg);
        }
    }

     private void syncSampleDataItem(final vectorMessage msg_acc, final vectorMessage msg_gyro) {
        if (mGoogleApiClient == null)
            return;

        final PutDataMapRequest putRequest = PutDataMapRequest.create("/IMU");
        final DataMap map = putRequest.getDataMap();

        map.putFloatArray(msg_acc.getTopic(), msg_acc.getData());
        map.putFloatArray(msg_gyro.getTopic(), msg_gyro.getData());
        map.putLong("sensors/time", System.currentTimeMillis());
        PutDataRequest request = putRequest.asPutDataRequest();
        request.setUrgent();
        Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                //Log.d("sending", " Sending was successful: " + dataItemResult.getStatus()
                //        .isSuccess());
                sensorUpdate = true;
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_GYROSCOPE) {
            last_gyro[0] = ((int) (sensorEvent.values[0] * precision)) / precision;
            last_gyro[1] = ((int) (sensorEvent.values[1] * precision)) / precision;
            last_gyro[2] = ((int) (sensorEvent.values[2] * precision)) / precision;

            dataGyro.setText("gyro: " + last_gyro[0] + " " + last_gyro[0] + " " + last_gyro[2]);
            gyroMsg.setData(last_gyro);
        }

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            last_acc[0] = ((int) (sensorEvent.values[0] * precision)) / precision;
            last_acc[1] = ((int) (sensorEvent.values[1] * precision)) / precision;
            last_acc[2] = ((int) (sensorEvent.values[2] * precision)) / precision;

            dataAcc.setText("acc: " + last_acc[0] + " " + last_acc[1] + " " + last_acc[2]);
            accMsg.setData(last_acc);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    public void stopStreaming(View view) {
        senSensorManager.unregisterListener(this);
        myTimer.cancel();
        Intent intent = new Intent(this, MainActivity.class);
        finish();
        startActivity(intent);

    }
}
