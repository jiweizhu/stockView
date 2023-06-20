package com.example.notification.vo;

import lombok.Data;

@Data
public class OneDayPrice {

    String Date;
    String startPrice;
    String endPrice;
    String highestPrice;
    String lowestPrice;
    String totalHands;
}
