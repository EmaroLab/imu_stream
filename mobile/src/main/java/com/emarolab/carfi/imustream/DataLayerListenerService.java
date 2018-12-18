package com.emarolab.carfi.imustream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by Alessandro on 13/12/2017.
 */



public class DataLayerListenerService extends WearableListenerService {

    @Override
    public void onDataChanged(DataEventBuffer dataEvents) {
        super.onDataChanged(dataEvents);
        GoogleApiClient mGoogleApiClient;

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

        final List<DataEvent> events = FreezableUtils.freezeIterable(dataEvents);
        for(DataEvent event : events) {
            final Uri uri = event.getDataItem().getUri();
            final String path = uri!=null ? uri.getPath() : null;
            if("/IMU".equals(path)) {
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();

                BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
                Set<BluetoothDevice> pairedDevices = myDevice.getBondedDevices();

                Intent intent = new Intent();
                intent.setAction("com.example.Broadcast");
                List<String> connected_devices = new ArrayList<String>();


                for (BluetoothDevice device : pairedDevices) {
                    /* Reply */
                    final PutDataMapRequest putRequest = PutDataMapRequest.create("/FLAG");
                    final DataMap sending_map = putRequest.getDataMap();
                    long time = 0;

                    float[] acc = map.getFloatArray(device.getName()+"/accelerometer");
                    float[] gyro = map.getFloatArray(device.getName()+"/gyroscope");
                    long[] time_acc = map.getLongArray(device.getName()+"/time_accelerometer");
                    long[] time_gyro = map.getLongArray(device.getName()+"/time_gyroscope");

                    if (gyro != null){
                        intent.putExtra(device.getName()+"/velocity", gyro);
                        intent.putExtra(device.getName()+"/time_velocity", time_gyro);
                        time = time_gyro[time_gyro.length-1];
                    }

                    if (acc != null) {
                        intent.putExtra(device.getName()+"/acceleration", acc);
                        intent.putExtra(device.getName()+"/time_acceleration", time_acc);
                        time = time_acc[time_acc.length-1];
                    }

                    if (acc!=null || gyro!=null){
                        sending_map.putLong(device.getName()+"/received", time);
                        PutDataRequest request = putRequest.asPutDataRequest();
                        request.setUrgent();
                        Wearable.DataApi.putDataItem(mGoogleApiClient, request);
                    }
                    connected_devices.add(device.getName());
                }
                intent.putStringArrayListExtra("deviceList", (ArrayList<String>) connected_devices);
                sendBroadcast(intent);
            }
        }
    }
}