package com.example.notification.vo;

import lombok.Data;

@Data
public class DayAvgVO {
    private String stockId;
    private String stockName;
    private Double lastDayPrice;
    private Double tenDayAvgPrice;
    private Double twentyDayAvgPrice;
    private Double thirtyDayAvgPrice;

}
