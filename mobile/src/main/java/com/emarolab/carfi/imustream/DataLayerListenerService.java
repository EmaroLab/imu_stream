package com.emarolab.carfi.imustream;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.DataMapItem;
import com.google.android.gms.wearable.WearableListenerService;

import java.util.List;

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

                float[] acc = map.getFloatArray("sensors/accelerometer");
                float[] gyro = map.getFloatArray("sensors/gyroscope");
                long time = map.getLong("sensors/time");

                Intent intent = new Intent();
                intent.setAction("com.example.Broadcast");
                intent.putExtra("topic", "sensors/imu");
                intent.putExtra("acceleration", acc);
                intent.putExtra("velocity", gyro);
                sendBroadcast(intent);
            } else if("/VELVIB".equals(path)) {
                final DataMap map = DataMapItem.fromDataItem(event.getDataItem()).getDataMap();
                String velState = map.getString("velState");
                long time = map.getLong("time");
                Log.e("Velocità",String.valueOf(velState));
                Intent in = new Intent();
                in.putExtra("topic", "vibration/vel");
                in.putExtra("velState",velState);
                in.putExtra("time",time);
                in.setAction("Vel");
                sendBroadcast(in);
            }
        }
    }
}