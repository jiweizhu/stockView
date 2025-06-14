package com.example.notification.businessVo;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Data
public class StockBisVO {
    private String stockId;

    private String stockName;

    private Integer financialType = 100;

    private Double grossProfitGain;

    private Integer capitalType = 300;

    private String stockIds;

    private String belongEtf;

    private Integer upwardDaysFive = 0;

    private BigDecimal gainPercentFive;

    private Integer flipUpwardDaysFive = 0;

    private BigDecimal flipGainPercentFive;

    private Date flipDayFive;

    private Date flipEndDayFive;

    private Integer upwardDaysTen = 0;

    private BigDecimal gainPercentTen;

    private Integer flipUpwardDaysTen = 0;

    private Date flipDayTen;

    private Date flipEndDayTen;

    private BigDecimal flipGainPercentTen;

    private Timestamp lastUpdatedTime;

    private String customerRange;

    private BigDecimal customerRangeGainPre;

    private BigDecimal customerRangeGainPost;

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


    //this is used to sort the stock list, for html show
    private Integer sortColumn = 0;

    public StockBisVO() {
    }

    public StockBisVO(String stockName, String stockId) {
        this.stockName = stockName;
        this.stockId = stockId;
    }

    public StockBisVO(String stockId) {
        this.stockId = stockId;
    }


    @Override
    public String toString() {
        return stockId + "_" + stockName + "</br>";
    }
}
