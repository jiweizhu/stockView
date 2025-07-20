package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;

@Data
@Entity
@IdClass(StockDailyKey.class)
@Table(name = "daily_price")
public class StockDailyVO {
//
//    @Id
//    @Column(name = "id")
//    private long id;

    @Id
    @Column(name = "stockId")
    private String stockId;

    @Id
    @Column(name = "day")
    private Date day;

    @Column(name = "openingPrice", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal openingPrice;

    @Column(name = "closingPrice", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal closingPrice;

    @Column(name = "intradayHigh", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal intradayHigh;

    @Column(name = "intradayLow", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal intradayLow;

    @Column(name = "dayAvgFive", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayAvgFive;

    @Column(name = "dayAvgTen", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayAvgTen;

    @Column(name = "day_gain_of_five", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayGainOfFive;

    @Column(name = "day_gain_of_ten", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayGainOfTen;
    @Column(name = "ttm", columnDefinition = "DECIMAL(10,2)")
    private Double ttm;

    @Column(name = "pbr", columnDefinition = "DECIMAL(10,2)")
    private Double pbr;

    public StockDailyVO(String stockId, String day, String openingPrice, String closingPrice, String intradayHigh, String intradayLow) {
        this.stockId = stockId;
        this.day = Date.valueOf(day);
        this.openingPrice = new BigDecimal(openingPrice).setScale(3, RoundingMode.DOWN);
        this.closingPrice = new BigDecimal(closingPrice).setScale(3, RoundingMode.DOWN);
        this.intradayHigh = new BigDecimal(intradayHigh).setScale(3, RoundingMode.DOWN);
        this.intradayLow = new BigDecimal(intradayLow).setScale(3, RoundingMode.DOWN);
    }

    public StockDailyVO() {
    }

}
