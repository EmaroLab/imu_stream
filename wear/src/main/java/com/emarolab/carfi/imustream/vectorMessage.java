package com.emarolab.carfi.imustream;

import java.util.Vector;

/**
 * Created by Alessandro on 15/12/2017.
 */

public class vectorMessage {
    private Vector<Float> data;
    private Vector<Long> timestamp;
    private String topic;
    private String topicTime;
    private int minSizePack;

    public vectorMessage(int sizePack) {
        this.minSizePack = sizePack;
        this.topic = "/topic";
        this.topicTime = "/topic";
        this.data = new Vector<>();
        this.timestamp = new Vector<>();
    }

    public boolean push(float[] data, long timestamp) {
        this.data.add(data[0]);
        this.data.add(data[1]);
        this.data.add(data[2]);

        this.timestamp.add(timestamp) ;

        if (this.timestamp.size() >= minSizePack) {
            return true;
        } else {
            return false;
        }
    }

    public void flush() {
        this.data = new Vector<>();
        this.timestamp = new Vector<>();
    }

    public void setTopics(String topic, String topicTime) {
        this.topic = topic;
        this.topicTime = topicTime;
    }

    public String getTopic() {
        return topic;
    }

    public String getTopicTime(){
        return topicTime;
    }

    public Vector<Float> getData() {return this.data;}

    public Vector<Long> getTimestamp() {return this.timestamp;}


}
