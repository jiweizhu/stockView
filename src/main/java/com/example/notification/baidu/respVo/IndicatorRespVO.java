package com.example.notification.baidu.respVo;

import lombok.Data;

@Data
public class IndicatorRespVO {
    private String name;
    private double dayGain;
    private int raiseCount;
    private int zeroCount;
    private int fallCount;
    private String stocks;
}
