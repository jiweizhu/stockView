package com.example.notification.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "stock")
public class StockNameVO {
    @Id
    @Column(name = "stockId")
    private String stockId;

    @Column(name = "stockName")
    private String stockName;

    @Column(name = "fiveDayUpwardDays")
    private Integer fiveDayUpwardDays = 0;

    @Column(name = "tenDayUpwardDays")
    private Integer tenDayUpwardDays = 0;

    @Column(name = "fiveGainPercent")
    private BigDecimal fiveGainPercent;

    @Column(name = "tenGainPercent")
    private BigDecimal tenGainPercent;

    @Override
    public String toString() {
        return stockId + "_" + stockName;
    }
}
