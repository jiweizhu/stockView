package com.example.notification.responseVo;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

@Data
public class HoldingStockViewRespVO {
    private String stockId;

    private String stockName;

    private BigDecimal costPrice;

    private String nowPrice;

    private Integer buyInLot;

    private BigDecimal gainPercent;

    private BigDecimal oneDayGain;

    private String lastClosePrice;

    private String belongEtf;

    private Date buyInDay;

    private String lastUpdatedTime;

}
