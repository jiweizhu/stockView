package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@IdClass(CNDailyKey.class)
@Table(name = "cn_daily_price")
public class CNDailyVO {

    @Id
    @Column(name = "indexCode")
    private String indexCode;

    @Id
    @Column(name = "tradeDate")
    private String tradeDate;

    @Column(name = "indexNameCnAll")
    private String indexNameCnAll;

    @Column(name = "indexNameCn")
    private String indexNameCn;

    @Column(name = "indexNameEnAll")
    private String indexNameEnAll;

    @Column(name = "indexNameEn")
    private String indexNameEn;

    @Column(name = "open_val", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal open;

    @Column(name = "high_val", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal high;
    @Column(name = "low_val", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal low;
    @Column(name = "close_val", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal close;
    @Column(name = "change_val", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal change;
    @Column(name = "change_pct", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal changePct;
    @Column(name = "trading_vol", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal tradingVol;
    @Column(name = "trading_value", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal tradingValue;
    @Column(name = "cons_number", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal consNumber;
    @Column(name = "peg", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal peg;

    @Column(name = "dayAvgFive", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayAvgFive;

    @Column(name = "dayAvgTen", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayAvgTen;

    @Column(name = "day_gain_of_five", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayGainOfFive;

    @Column(name = "day_gain_of_ten", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal dayGainOfTen;


    public CNDailyVO() {
    }

}
