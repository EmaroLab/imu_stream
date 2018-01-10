package com.emarolab.carfi.helpers;

import android.content.Context;
import android.util.Log;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;


/**
 * Created by Alessandro on 06/12/2017.
 */

public class MqttHelper {
    public MqttAndroidClient mqttAndroidClient;
    public Context cont;
    public MqttPublisher Publisher;

    private boolean publishPermission = false;

    private String serverUri;
    final String clientId = "IMUAndroidClient";
    final String subscriptionTopic = "sensors/+";

    private String username;
    private String password;

    public MqttHelper(Context context, String ip, String port, String user, String pwd) {
        serverUri = "tcp://"+ip+":"+port;
        username = user;
        password = pwd;

        cont = context;
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {
                Log.w("Mqtt", "connectionLost");
            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });

        connect();

    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private static final ExecutorService pool = Executors.newCachedThreadPool();

    private void connect() {
        final MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(1);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    //mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);

                    Publisher = new MqttPublisher(mqttAndroidClient, cont);
                    pool.execute(Publisher);
                    setPublishPermission(true);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });


        } catch (MqttException ex) {
            ex.printStackTrace();
        }
    }


    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exceptionst subscribing");
            ex.printStackTrace();
        }
    }


    public void onDataReceived(String msg) {
        if(publishPermission) {
            Publisher.setMsg(msg);
        }
    }

    public boolean checkConnection(){
        return mqttAndroidClient.isConnected();
    }

    public void setPublishPermission(boolean flag){
        publishPermission = flag;
    }

    public void closeClient(){
        if(mqttAndroidClient.isConnected()) {
            mqttAndroidClient.close();
            try {
                mqttAndroidClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}