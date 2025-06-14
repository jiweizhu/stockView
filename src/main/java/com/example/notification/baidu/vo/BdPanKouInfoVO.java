package com.example.notification.baidu.vo;

import lombok.Data;

@Data
public class BdPanKouInfoVO {
    //滚动市盈率（TTM）为：当前市值除以最近4个季度净利润之和
    private String peratio;
    //市盈(静)
    private String lyr;
    //总股本
    private String totalShareCapital;
    //流通股本
    private String currencyValue;
    //市销率
    private String priceSaleRatio;
    //市净率
    private String bvRatio;

    private String stockName;
    
    //总市值
    private String capitalization;
}
