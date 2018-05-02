package com.emarolab.carfi.imustream;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class MqttSettingActivity extends AppCompatActivity {
    public static final String ip_message = "com.example.androidweartest.ip";
    public static final String port_message = "com.example.androidweartest.port";
    public static final String user_message = "com.example.androidweartest.user";
    public static final String password_message = "com.example.androidweartest.password";

    public static final String port_default = "1883";
    public static final String user_default = "no_user";
    public static final String password_default = "no_password";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mqtt_setting);
    }

    public void openPcComunication(View view) {
        Intent intent = new Intent(this, PcComunicationActivity.class);

        EditText editText = (EditText) findViewById(R.id.IP);
        String mqtt_ip = editText.getText().toString();
        if (mqtt_ip.matches("")){
            Toast.makeText(this, "You did not enter an IP address", Toast.LENGTH_SHORT).show();
            return;
        }

        editText = (EditText) findViewById(R.id.port);
        String mqtt_port = editText.getText().toString();
        if (mqtt_port.matches("")){
            mqtt_port = port_default;
        }

        editText = (EditText) findViewById(R.id.userID);
        String mqtt_user = editText.getText().toString();
        if (mqtt_user.matches("")){
            mqtt_user = user_default;
        }

        editText = (EditText) findViewById(R.id.userPSW);
        String mqtt_password = editText.getText().toString();
        if (mqtt_password.matches("")){
            mqtt_password = password_default;
        }

        intent.putExtra(ip_message, mqtt_ip);
        intent.putExtra(port_message, mqtt_port);
        intent.putExtra(user_message, mqtt_user);
        intent.putExtra(password_message, mqtt_password);

        startActivity(intent);
    }
}
