package com.example.fareed.lazeezoserver.Model;

import java.util.Map;

/**
 * Created by fareed on 15/04/2018.
 */

public class DataMessage {
    public String to;
    public Map<String,String> data;

    public DataMessage(){}

    public DataMessage(String to, Map<String, String> data) {
        this.to = to;
        this.data = data;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public Map<String, String> getData() {
        return data;
    }

    public void setData(Map<String, String> data) {
        this.data = data;
    }
}
