package com.example.notification.vo;

import lombok.Data;

@Data
public class WebQueryParam {

    private Integer daysToQuery = 50;
    private String identifier = "sh600519"; //maotai

    public WebQueryParam(Integer daysToQuery, String identifier) {
        this.daysToQuery = daysToQuery;
        this.identifier = identifier;
    }

    public WebQueryParam() {
    }
}
