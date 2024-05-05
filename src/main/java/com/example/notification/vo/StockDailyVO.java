package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
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

    @Column(name = "openingPrice")
    private BigDecimal openingPrice;

    @Column(name = "closingPrice")
    private BigDecimal closingPrice;

    @Column(name = "intradayHigh")
    private BigDecimal intradayHigh;

    @Column(name = "intradayLow")
    private BigDecimal intradayLow;

    @Column(name = "fiveDayAvg")
    private BigDecimal fiveDayAvg = BigDecimal.valueOf(0);

    @Column(name = "tenDayAvg")
    private BigDecimal tenDayAvg = BigDecimal.valueOf(0);

    public StockDailyVO(String stockId, String day, String openingPrice, String closingPrice, String intradayHigh, String intradayLow) {
        this.stockId = stockId;
        this.day = Date.valueOf(day);
        this.openingPrice = new BigDecimal(openingPrice);
        this.closingPrice = new BigDecimal(closingPrice);
        this.intradayHigh = new BigDecimal(intradayHigh);
        this.intradayLow = new BigDecimal(intradayLow);
    }

    public StockDailyVO() {
    }

}
