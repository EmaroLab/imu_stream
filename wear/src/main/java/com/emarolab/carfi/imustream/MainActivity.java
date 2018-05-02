package com.emarolab.carfi.imustream;


import android.content.Intent;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.view.View;


public class MainActivity extends WearableActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        setAmbientEnabled();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public void startStreaming(View view) {
        Intent intent = new Intent(this, SendingActivity.class);
        finish();
        startActivity(intent);
    }
}
