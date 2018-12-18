package com.emarolab.carfi.imustream;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import com.emarolab.carfi.helpers.MqttHelper;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.ArrayList;
import java.util.List;

public class PcComunicationActivity extends AppCompatActivity {

    public TextView ipOut, portOut, textConnection;
    public List<String> devicesName = new ArrayList<String>();

    public TableLayout imu_container;
    public List<TextView> table_contents = new ArrayList<TextView>();

    private Button p1_button;
    private boolean pause_flag = false;

    private BroadcastReceiver receiver;
    private MqttHelper mqttHelper;

    private String lastGyro = "";
    private String lastAcc = "";

    private int lastPkgSizeAcc, lastPkgSizeGyro;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pc_comunication);

        p1_button = (Button)findViewById(R.id.buttonPause);
        imu_container = findViewById(R.id.IMUcontainer);

        Intent intent = getIntent();
        String mqtt_ip = intent.getStringExtra(MqttSettingActivity.ip_message);
        String mqtt_port = intent.getStringExtra(MqttSettingActivity.port_message);
        String mqtt_user = intent.getStringExtra(MqttSettingActivity.user_message);
        String mqtt_password = intent.getStringExtra(MqttSettingActivity.password_message);

        ipOut = (TextView) findViewById(R.id.ipSending);
        portOut = (TextView) findViewById(R.id.portSending);
        ipOut.setText(mqtt_ip);
        portOut.setText(mqtt_port);
        textConnection = (TextView) findViewById(R.id.connectionStatus);

        startMqtt(mqtt_ip,mqtt_port,mqtt_user,mqtt_password);

        IntentFilter filter = new IntentFilter();
        filter.addAction("com.example.Broadcast");

        receiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Bundle bundle = intent.getExtras();
                if (bundle != null) {
                    ArrayList<String> deviceList = intent.getStringArrayListExtra("deviceList");
                    if (deviceList != null) {
                        for (String device : deviceList) {
                            float[] acc = bundle.getFloatArray(device + "/acceleration");
                            float[] vel = bundle.getFloatArray(device + "/velocity");
                            long[] accTimestamp = bundle.getLongArray(device + "/time_acceleration");
                            long[] velTimestamp = bundle.getLongArray(device + "/time_velocity");

                            imuVisualization(device, acc, vel);

                            int pkgSizeAcc;
                            int pkgSizeGyro;

                            String gyroMsg = "";
                            String accMsg = "";

                            if (vel != null) {
                                gyroMsg = gyroMsg + "g;";
                                for (float velo : vel) {
                                    gyroMsg = gyroMsg + velo + ";";
                                }
                                for (long timestamp : velTimestamp) {
                                    gyroMsg = gyroMsg + timestamp + ";";
                                }
                                pkgSizeGyro = velTimestamp.length;
                                lastPkgSizeGyro = pkgSizeGyro;
                                lastGyro = gyroMsg;
                            }else {
                                pkgSizeGyro = lastPkgSizeGyro;
                                gyroMsg = lastGyro;
                            }


                            if (acc != null) {
                                accMsg = accMsg + "a;";
                                for (float accel : acc) {
                                    accMsg = accMsg + accel + ";";
                                }
                                for (long timestamp : accTimestamp) {
                                    accMsg = accMsg + timestamp + ";";
                                }
                                pkgSizeAcc = accTimestamp.length;
                                lastPkgSizeAcc = pkgSizeAcc;
                                lastAcc = accMsg;
                            } else {
                                pkgSizeAcc = lastPkgSizeAcc;
                                accMsg = lastAcc;
                            }

                            if (acc != null || vel != null) {
                                String imuMsg = pkgSizeAcc + ";" + accMsg + pkgSizeGyro + ";" + gyroMsg;
                                mqttHelper.onDataReceived(imuMsg, device);
                            }
                        }
                    }
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

    private void addDevice(String device, float[] acc, float[] vel){
        devicesName.add(device);

        TextView deviceName = new TextView(this);
        deviceName.setText(device);
        deviceName.setPadding(0,0,50,0);
        TextView AccString = new TextView(this);
        AccString.setText("Acc: ");
        TextView deviceAcc = new TextView(this);
        if (acc != null) {
            deviceAcc.setText("" + acc[0] + " " + acc[1] + " " + acc[2]);
        }
        deviceAcc.setGravity(Gravity.RIGHT);
        table_contents.add(deviceAcc);
        TextView VelString = new TextView(this);
        VelString.setText("Vel: ");
        TextView deviceGyro = new TextView(this);
        if (vel != null) {
            deviceGyro.setText("" + vel[0] + " " + vel[1] + " " + vel[2]);
        }
        deviceGyro.setGravity(Gravity.RIGHT);
        table_contents.add(deviceGyro);

        TableRow tr1 = new TableRow(this);
        tr1.addView(deviceName);
        tr1.addView(AccString);
        tr1.addView(deviceAcc);

        TextView EmptyTextView = new TextView(this);

        TableRow tr2 = new TableRow(this);
        tr2.addView(EmptyTextView);
        tr2.addView(VelString);
        tr2.addView(deviceGyro);

        tr2.setPadding(0, 0,0, 80);
        imu_container.addView(tr1);
        imu_container.addView(tr2);
    }
    private void imuVisualization(String device, float[] acc, float[] vel){
        int id = devicesName.indexOf(device);

        if (id == -1){
            addDevice(device, acc, vel);
        }else{
            TextView deviceAcc = table_contents.get(2*id);
            TextView deviceGyro = table_contents.get(2*id+1);
            if (acc != null) {
                deviceAcc.setText("" + acc[0] + " " + acc[1] + " " + acc[2]);
            }

            if (vel != null) {
                deviceGyro.setText("" + vel[0] + " " + vel[1] + " " + vel[2]);
            }
        }
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
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
