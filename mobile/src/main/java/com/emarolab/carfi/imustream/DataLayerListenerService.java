package com.emarolab.carfi.imustream;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.wearable.CapabilityClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.NodeClient;
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
                    float[] acc = map.getFloatArray(device.getName()+"/accelerometer");
                    float[] gyro = map.getFloatArray(device.getName()+"/gyroscope");
                    float[] orient = map.getFloatArray(device.getName()+"/orientation");

                    long[] time_acc = map.getLongArray(device.getName()+"/time_accelerometer");
                    long[] time_gyro = map.getLongArray(device.getName()+"/time_gyroscope");
                    long[] time_orient = map.getLongArray(device.getName()+"/time_orientation");

                    if (gyro != null){
                        intent.putExtra(device.getName()+"/velocity", gyro);
                        intent.putExtra(device.getName()+"/time_velocity", time_gyro);
                    }

                    if (acc != null) {
                        intent.putExtra(device.getName()+"/acceleration", acc);
                        intent.putExtra(device.getName()+"/time_acceleration", time_acc);
                    }

                    if (orient != null) {
                        intent.putExtra(device.getName()+"/orientation", orient);
                        intent.putExtra(device.getName()+"/time_orientation", time_orient);
                    }

                    connected_devices.add(device.getName());
                }
                intent.putStringArrayListExtra("deviceList", (ArrayList<String>) connected_devices);
                sendBroadcast(intent);
            }
        }
    }
}