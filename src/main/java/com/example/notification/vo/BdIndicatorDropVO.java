package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Entity
@IdClass(BdIndicatorDropKey.class)
@Table(name = "indicator_drop")
public class BdIndicatorDropVO {
    @Id
    @Column
    private String indicatorId;

    @Id
    @Column(name = "day_start")
    private Date dayStart;

    @Column(name = "day_end")
    private Date dayEnd;

    @Column(name = "last_updated_time")
    private Timestamp lastUpdatedTime;

    @Column(name = "drop_percent", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal dropPercent;

    @Column(name = "stock_ids")
    private String stockIds;

    public BdIndicatorDropVO() {
    }

    public BdIndicatorDropVO(String indicatorId, Date dayStart, Date dayEnd, Timestamp lastUpdatedTime, BigDecimal dropPercent, String stockIds) {
        this.indicatorId = indicatorId;
        this.dayStart = dayStart;
        this.dayEnd = dayEnd;
        this.lastUpdatedTime = lastUpdatedTime;
        this.dropPercent = dropPercent;
        this.stockIds = stockIds;
    }
}
