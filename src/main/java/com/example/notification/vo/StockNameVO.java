package com.example.notification.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

@Data
@Entity
@Table(name = "stock")
public class StockNameVO {
    @Id
    @Column(name = "stockId")
    private String stockId;

    @Column(name = "stockName")
    private String stockName;

    @Column(name = "stock_ids")
    private String stockIds;

    @Column(name = "belong_etf")
    private String belongEtf;

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

    @Column(name = "customer_range")
    private String customerRange;

    @Column(name = "customer_range_gain_pre", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal customerRangeGainPre;

    @Column(name = "customer_range_gain_post", columnDefinition = "DECIMAL(10,2)")
    private BigDecimal customerRangeGainPost;

    static Map<String, String[]> sortArrayMap = new HashMap<>();

    public StockNameVO() {
    }

    public StockNameVO(String stockId) {
        this.stockId = stockId;
    }

    public static Map<String, String[]> getSortArrayMap() {
        sortArrayMap.put("five", new String[]{"upwardDaysFive", "gainPercentFive"});
        sortArrayMap.put("fiveFlip", new String[]{"flipUpwardDaysFive", "flipGainPercentFive"});
        sortArrayMap.put("ten", new String[]{"upwardDaysTen", "gainPercentTen"});
        sortArrayMap.put("tenFlip", new String[]{"flipUpwardDaysTen", "flipGainPercentTen"});
        return sortArrayMap;
    }

    @Override
    public String toString() {
        return stockId + "_" + stockName + "</br>";
    }
}
