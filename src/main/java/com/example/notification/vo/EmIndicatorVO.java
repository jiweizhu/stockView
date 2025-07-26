package com.example.notification.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@Table(name = "easy_indicator")
//easyMoneyIndicator
public class EmIndicatorVO {
    @Id
    @Column(name = "boardId")
    private String boardId;

    @Column(name = "boardName")
    private String boardName;

    @Column(name = "stock_ids")
    private String stockIds;

    @Column(name = "ttm_percentile")
    private Double ttmPercentile;

    //在距离最高点相对值为多少
    @Column(name = "ttm_max_rel")
    private Double ttmRangePct;

    @Column(name = "last_updated_time")
    private Timestamp lastUpdatedTime;

    public EmIndicatorVO() {
    }

}
