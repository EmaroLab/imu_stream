package com.emarolab.carfi.imustream;

/**
 * Created by Alessandro on 15/12/2017.
 */

public class vectorMessage {
    private float[] data;
    private String topic;

    public vectorMessage() {
        this.data = new float[3];
        this.topic = "/topic";
    }

    public void setData(float[] data) {
        this.data = data;
    }

    public void setTopic(String topic) {
        this.topic = topic;
    }

    public float[] getData() {
        return data;
    }

    public String getTopic() {
        return topic;
    }
}
