package com.example.notification.vo;

import lombok.Data;

@Data
public class DayAvgVO {
    private double lastDayPrice;
    private double tenDayAvgPrice;
    private double twentyDayAvgPrice;
    private double thirtyDayAvgPrice;

}
