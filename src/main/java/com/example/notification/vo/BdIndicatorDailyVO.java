package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@Entity
@IdClass(StockDailyKey.class)
@Table(name = "bd_daily_price")
public class BdIndicatorDailyVO {
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

    @Column(name = "dayAvgTwenty", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayAvgTwenty;

    @Column(name = "day_gain_of_five", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayGainOfFive;

    @Column(name = "day_gain_of_ten", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayGainOfTen;

    @Column(name = "day_gain_of_twenty", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayGainOfTwenty;


    public BdIndicatorDailyVO() {
    }

}
