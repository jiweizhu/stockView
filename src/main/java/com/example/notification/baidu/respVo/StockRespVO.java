package com.example.notification.baidu.respVo;

import lombok.Data;

@Data
public class StockRespVO {

    private String stockName;

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
    //总市值
    private String capitalization;

    //动态市盈率近N年全部的百分位
    private Double ttm;

    //动态市盈率近N年全部的百分位
    private Double ttmWavePct;

    //近N年相对最高最低的百分位
    private Double ttmRangePct;

    //市净率
    private Double pbr;

    private Double pbrWavePct;

    private Double pbrRangePct;

    //市现率OCF_TTM
    private Double pcf;

    private Double pcfWavePct;

    private Double pcfRangePct;
}
