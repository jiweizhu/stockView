package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Data
@Entity
@Table(name = "cn_indicator")
public class CNIndicatorVO {
    @Id
    @Column(name = "indexCode")
    private String indexCode;

    @Column(name = "indexNameCn")
    private String indexNameCn;

    @Column(name = "stock_ids")
    private String stockIds;

    @Column(name = "upwardDaysFive")
    private Integer upwardDaysFive = 0;

    @Column(name = "gainPercentFive", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal gainPercentFive;

    @Column(name = "flipUpwardDaysFive")
    private Integer flipUpwardDaysFive = 0;

    @Column(name = "flipGainPercentFive", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal flipGainPercentFive;

    @Column(name = "flipDayFive")
    private Date flipDayFive;

    @Column(name = "flipEndDayFive")
    private Date flipEndDayFive;

    @Column(name = "upwardDaysTen")
    private Integer upwardDaysTen = 0;

    @Column(name = "gainPercentTen")
    private BigDecimal gainPercentTen;

    @Column(name = "flipUpwardDaysTen")
    private Integer flipUpwardDaysTen = 0;

    @Column(name = "flipDayTen")
    private Date flipDayTen;

    @Column(name = "flipEndDayTen")
    private Date flipEndDayTen;

    @Column(name = "flipGainPercentTen", columnDefinition = "DECIMAL(10,3)")
    private BigDecimal flipGainPercentTen;

    @Column(name = "last_updated_time")
    private Timestamp lastUpdatedTime;

    public CNIndicatorVO() {
    }

    public CNIndicatorVO(String indexCode, String indexNameCn) {
        this.indexCode = indexCode;
        this.indexNameCn = indexNameCn;
    }

    public CNIndicatorVO(String indexCode) {
        this.indexCode = indexCode;
    }

}
