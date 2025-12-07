package com.example.notification.legulegu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LeguResponse {

    private List<LeguDataItem> data;

    public List<LeguDataItem> getData() {
        return data;
    }

    public void setData(List<LeguDataItem> data) {
        this.data = data;
    }
}