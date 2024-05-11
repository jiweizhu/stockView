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

    @Column(name = "upwardDaysFive")
    private Integer upwardDaysFive = 0;

    @Column(name = "gainPercentFive")
    private BigDecimal gainPercentFive;

    @Column(name = "flipUpwardDaysFive")
    private Integer flipUpwardDaysFive = 0;

    @Column(name = "flipGainPercentFive")
    private BigDecimal flipGainPercentFive;

    @Override
    public String toString() {
        return stockId + "_" + stockName;
    }
}
