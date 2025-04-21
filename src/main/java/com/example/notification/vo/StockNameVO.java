package com.example.notification.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "stock")
public class StockNameVO {
    @Id
    @Column(name = "stockId")
    private String stockId;

    @Column(name = "stockName")
    private String stockName;


    //滚动市盈率（TTM）为：当前市值除以最近4个季度净利润之和
    @Column(name = "peratio")
    private String peratio;
    //市盈(静)
    @Column(name = "lyr")
    private String lyr;
    //总股本
    @Column(name = "totalShareCapital")
    private String totalShareCapital;
    //流通股本
    @Column(name = "currencyValue")
    private String currencyValue;
    //市销率
    @Column(name = "priceSaleRatio")
    private String priceSaleRatio;
    //市净率
    @Column(name = "bvRatio")
    private String bvRatio;


    // sort type number
    // 400 profit>0 & profitGain > 0
    // 300 profit>0 & profitGain < 0
    // 200 profit< 0 & profitGain > 0
    // 100 profit< 0 & profitGain < 0
    @Column(name = "financial_type")
    private Integer financialType = 100;

    @Column(name = "gross_profit_gain")
    private Double grossProfitGain;

    @Column(name = "capital_type")
    private Integer capitalType = 300;

    @Column(name = "stock_ids")
    private String stockIds;

    @Column(name = "belong_etf")
    private String belongEtf;

    @Column(name = "upwardDaysFive")
    private Integer upwardDaysFive = 0;

    @Column(name = "gainPercentFive", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal gainPercentFive;

    @Column(name = "flipUpwardDaysFive")
    private Integer flipUpwardDaysFive = 0;

    @Column(name = "flipGainPercentFive", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal flipGainPercentFive;

    @Column(name = "flipDayFive")
    private Date flipDayFive;

    @Column(name = "flipEndDayFive")
    private Date flipEndDayFive;

    @Column(name = "upwardDaysTen")
    private Integer upwardDaysTen = 0;

    @Column(name = "gainPercentTen")
    private BigDecimal gainPercentTen;

    @Column(name = "flipUpwardDaysTen")
    private Integer flipUpwardDaysTen = 0;

    @Column(name = "flipDayTen")
    private Date flipDayTen;

    @Column(name = "flipEndDayTen")
    private Date flipEndDayTen;

    @Column(name = "flipGainPercentTen", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal flipGainPercentTen;

    @Column(name = "last_updated_time")
    private Timestamp lastUpdatedTime;

    @Column(name = "customer_range")
    private String customerRange;

    @Column(name = "customer_range_gain_pre", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal customerRangeGainPre;

    @Column(name = "customer_range_gain_post", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal customerRangeGainPost;


    public StockNameVO() {
    }

    public StockNameVO(String stockName, String stockId) {
        this.stockName = stockName;
        this.stockId = stockId;
    }

    public StockNameVO(String stockId) {
        this.stockId = stockId;
    }


    @Override
    public String toString() {
        return stockId + "_" + stockName + "</br>";
    }
}
