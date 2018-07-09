package com.emarolab.carfi.imustream;


import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;


public class MainActivity extends WearableActivity {
    public static final String deviceNamePath = "com.emarolab.carfi.imustream.deviceName";
    String deviceName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView TextDevName;
        TextDevName = (TextView) findViewById(R.id.deviceName);

        BluetoothAdapter myDevice = BluetoothAdapter.getDefaultAdapter();
        deviceName = myDevice.getName();

        TextDevName.setText(deviceName);
        setAmbientEnabled();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startStreaming(View view) {
        Intent intent = new Intent(this, SendingActivity.class);
        intent.putExtra(deviceNamePath, deviceName);
        finish();
        startActivity(intent);
    }
}
