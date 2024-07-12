package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@Entity
@IdClass(WeekPriceKey.class)
@Table(name = "week_price")
public class WeekPriceVO {
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

    @Column(name = "weekHigh", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal weekHigh;

    @Column(name = "weekLow", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal weekLow;

    public WeekPriceVO(String stockId, String day, String openingPrice, String closingPrice, String weekHigh, String weekLow) {
        this.stockId = stockId;
        this.day = Date.valueOf(day);
        this.openingPrice = new BigDecimal(openingPrice);
        this.closingPrice = new BigDecimal(closingPrice);
        this.weekHigh = new BigDecimal(weekHigh);
        this.weekLow = new BigDecimal(weekLow);
    }

    public WeekPriceVO() {
    }

}
