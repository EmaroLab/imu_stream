package com.emarolab.carfi.imustream;

/**
 * Created by Alessandro on 14/11/2018.
 */

import android.content.Intent;
import android.net.Uri;
import com.google.android.gms.common.data.FreezableUtils;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
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
            if("/FLAG".equals(path)) {
                Intent intent = new Intent();
                intent.setAction("com.example.Broadcast");
                intent.putExtra("/received", true);
                sendBroadcast(intent);
            }
        }
    }
}
