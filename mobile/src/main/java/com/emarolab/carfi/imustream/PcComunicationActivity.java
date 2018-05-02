package com.emarolab.carfi.imustream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.emarolab.carfi.helpers.MqttHelper;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.DataApi;
import com.google.android.gms.wearable.DataMap;
import com.google.android.gms.wearable.PutDataMapRequest;
import com.google.android.gms.wearable.PutDataRequest;
import com.google.android.gms.wearable.Wearable;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Timer;

public class PcComunicationActivity extends AppCompatActivity {
    public TextView AccX, AccY, AccZ, VelX, VelY, VelZ, ipOut, portOut, textConnection, dataReceived;
    private Button p1_button;
    private boolean pause_flag = false;
    private BroadcastReceiver receiver;

    private Timer myTimer;
    private MqttHelper mqttHelper;
    private boolean buttonUpdate = true;
    String topic = null;
    private GoogleApiClient mGoogleApiClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pc_comunication);

        p1_button = (Button)findViewById(R.id.buttonPause);

        Intent intent = getIntent();
        String mqtt_ip = intent.getStringExtra(MqttSettingActivity.ip_message);
        String mqtt_port = intent.getStringExtra(MqttSettingActivity.port_message);
        String mqtt_user = intent.getStringExtra(MqttSettingActivity.user_message);
        String mqtt_password = intent.getStringExtra(MqttSettingActivity.password_message);

        ipOut = (TextView) findViewById(R.id.ipSending);
        portOut = (TextView) findViewById(R.id.portSending);
        ipOut.setText(mqtt_ip);
        portOut.setText(mqtt_port);
        textConnection = (TextView) findViewById(R.id.connectionS);
        dataReceived = (TextView) findViewById(R.id.dataReceived);
        AccX = (TextView) findViewById(R.id.accX);
        AccY = (TextView) findViewById(R.id.accY);
        AccZ = (TextView) findViewById(R.id.accZ);

        VelX = (TextView) findViewById(R.id.velX);
        VelY = (TextView) findViewById(R.id.velY);
        VelZ = (TextView) findViewById(R.id.velZ);

        startMqtt(mqtt_ip,mqtt_port,mqtt_user,mqtt_password);
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
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.Broadcast");
        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                String string = null;
                if (bundle != null) {
                    topic = bundle.getString("topic");
                    if(topic.equals("sensors/imu")) {
                        float[] acc = bundle.getFloatArray("acceleration");
                        float[] vel = bundle.getFloatArray("velocity");
                        string = "acc;" + acc[0] + ";" + acc[1] + ";" + acc[2] + ";gyro;" + vel[0] + ";" + vel[0] + ";" + vel[2];
                        imuVisualization(acc, vel);
                    }else if(topic.equals("vibration/vel")) {
                        string = "Velocità" + bundle.getString("velState");
                    }
                    mqttHelper.onDataReceived(string, topic);
                    connectionCheck();
                }
            }
        };

        registerReceiver(receiver, filter);
    }

    private void connectionCheck(){
        boolean connection = mqttHelper.checkConnection();
        if(connection){
            textConnection.setText("connected");
            textConnection.setTextColor(Color.GREEN);
        }else{
            textConnection.setText("disconnected");
            textConnection.setTextColor(Color.RED);
        }
    }
    private void sender(String id)
    {
        if (buttonUpdate) {
            buttonUpdate = false;
            if(id.equals("1")) {
                syncSampleDataItem(id);
            }else if(id.equals("2")){
                syncSampleDataItem(id);
            }else if(id.equals("3")){
                syncSampleDataItem(id);
            }else{
                syncSampleDataItem(id);
            }
        }
    }
    private void imuVisualization(float[] acc, float[] vel){
        AccX.setText(String.valueOf(acc[0]));
        AccY.setText(String.valueOf(acc[1]));
        AccZ.setText(String.valueOf(acc[2]));

        VelX.setText(String.valueOf(vel[0]));
        VelY.setText(String.valueOf(vel[1]));
        VelZ.setText(String.valueOf(vel[2]));
    }

    @Override
    protected void onDestroy() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (receiver != null) {
            unregisterReceiver(receiver);
            receiver = null;
        }
        mqttHelper.closeClient();
        super.onBackPressed();
    }

    public void pause(View view){
        mqttHelper.setPublishPermission(pause_flag);
        pause_flag = !pause_flag;

        if(pause_flag){
            p1_button.setText("Resume");
        }else{
            p1_button.setText("Pause");
        }
    }
    private void syncSampleDataItem(String id) {
        if (mGoogleApiClient == null)
            return;

        final PutDataMapRequest putRequest = PutDataMapRequest.create("/VIB");
        final DataMap map = putRequest.getDataMap();

        map.putString("id",id);
        PutDataRequest request = putRequest.asPutDataRequest();
        request.setUrgent();

        Wearable.DataApi.putDataItem(mGoogleApiClient, request).setResultCallback(new ResultCallback<DataApi.DataItemResult>() {
            @Override
            public void onResult(DataApi.DataItemResult dataItemResult) {
                //Log.d("sending", " Sending was successful: " + dataItemResult.getStatus()
                //        .isSuccess());
                buttonUpdate = true;
            }
        });
    }
    private void startMqtt(String ip, String port, String user, String password){
        mqttHelper = new MqttHelper(getApplicationContext(),ip,port,user,password);
        mqttHelper.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {

            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                if(topic.equals("vibration/vel")){
                    if(mqttMessage.toString().equals("1") || mqttMessage.toString().equals("2") || mqttMessage.toString().equals("3")) {
                        dataReceived.setText(mqttMessage.toString());
                    }else{
                        dataReceived.setText("VIBRATION OFF");
                    }
                    sender(mqttMessage.toString());
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
