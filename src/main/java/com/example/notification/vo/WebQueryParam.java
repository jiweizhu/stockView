package com.example.notification.vo;

import lombok.Data;

@Data
public class WebQueryParam {

    private Integer daysToQuery = 50;
    private String identifier = "sh600519"; //maotai
    private Boolean toQueryDailyPrice = true; //defult to query dayprice, otherwise is weekly price.

    public WebQueryParam(Integer daysToQuery, String identifier) {
        this.daysToQuery = daysToQuery;
        this.identifier = identifier;
    }

    public WebQueryParam() {
    }
}
