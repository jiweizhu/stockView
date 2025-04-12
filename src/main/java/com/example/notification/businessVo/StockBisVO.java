package com.example.notification.businessVo;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

@Data
public class StockBisVO {
    private String stockId;

    private String stockName;

    private String marketValue;

    private Integer financialType = 100;

    private Double grossProfitGain;

    private Integer capitalType = 300;

    private String stockIds;

    private String belongEtf;

    private Integer upwardDaysFive = 0;

    private BigDecimal gainPercentFive;

    private Integer flipUpwardDaysFive = 0;

    private BigDecimal flipGainPercentFive;

    private Date flipDayFive;

    private Date flipEndDayFive;

    private Integer upwardDaysTen = 0;

    private BigDecimal gainPercentTen;

    private Integer flipUpwardDaysTen = 0;

    private Date flipDayTen;

    private Date flipEndDayTen;

    private BigDecimal flipGainPercentTen;

    private Timestamp lastUpdatedTime;

    private String customerRange;

    private BigDecimal customerRangeGainPre;

    private BigDecimal customerRangeGainPost;

    //this is used to sort the stock list, for html show
    private Integer sortColumn = 0;

    public StockBisVO() {
    }

    public StockBisVO(String stockName, String stockId) {
        this.stockName = stockName;
        this.stockId = stockId;
    }

    public StockBisVO(String stockId) {
        this.stockId = stockId;
    }


    @Override
    public String toString() {
        return stockId + "_" + stockName + "</br>";
    }
}
