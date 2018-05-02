package com.emarolab.carfi.helpers;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MqttPublisher implements Runnable {

    private MqttAndroidClient mqttAndroidClient;
    private String msg = "casa";
    private boolean published = false;
    private String topic;
    public void setMsg(final String msg, final String topic) {
        this.msg = msg;
        published = true;
        this.topic = topic;
    }

    private Context context;

    public MqttPublisher(MqttAndroidClient mqttAndroidClient, Context context) {
        this.mqttAndroidClient = mqttAndroidClient;
    }
    public MqttPublisher(MqttAndroidClient mqttAndroidClient, Context context, String msg) {
        this.mqttAndroidClient = mqttAndroidClient;
        this.msg = msg;
    }

    @Override
    public synchronized void run() {

        while (true) {
            if(published) {
                MqttMessage mqttMessage = new MqttMessage();
                String m = msg;
                mqttMessage.setPayload(m.getBytes());

                try {
                    if(topic.equals("sensors/imu")) {
                        mqttAndroidClient.publish("sensors/imu", mqttMessage);
                    } else if(topic.equals("vibration/vel")){
                        mqttAndroidClient.publish("vibration/vel", mqttMessage);
                    }
                } catch (MqttException e) {
                    e.printStackTrace();
                }

                published = false;
            }
        }
    }

}