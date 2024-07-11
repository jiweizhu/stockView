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
@Table(name = "holding_stock")
public class HoldingStockVO {
    @Id
    @Column(name = "stockId")
    private String stockId;

    @Column(name = "stockName")
    private String stockName;

    @Column(name = "costPrice", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal costPrice;

    @Column(name = "nowPrice", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal nowPrice;

    @Column(name = "buy_in_lot")
    private Integer buyInLot;

    @Column(name = "gain_percent", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal gainPercent;

    @Column(name = "one_day_gain", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal oneDayGain;

    @Column(name = "last_close_price", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal lastClosePrice;

    @Column(name = "buy_day")
    private Date buyInDay;

    @Column(name = "last_updated_time")
    private Timestamp lastUpdatedTime;

}
