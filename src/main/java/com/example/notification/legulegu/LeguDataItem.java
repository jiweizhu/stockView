package com.example.notification.legulegu;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
class LeguDataItem {

    private String industryCode;
    private Long date;          // 毫秒时间戳
    private BigDecimal lyrPe;   // 静态 PE
    private BigDecimal ttmPe;   // TTM PE
    private BigDecimal pb;
    private BigDecimal indexClose;


    private BigDecimal lyrPeQuantile;
    private BigDecimal ttmPeQuantile;
    private BigDecimal pbQuantile;
    private BigDecimal dvRatio;
    private BigDecimal dvRatioQuantile;
    private BigDecimal dvTtm;
    private BigDecimal dvTtmQuantile;
    private BigDecimal addLyrPe;
    private BigDecimal addLyrPeQuantile;
    private BigDecimal addTtmPe;
    private BigDecimal addTtmPeQuantile;
    private BigDecimal addPb;
    private BigDecimal addPbQuantile;
    private BigDecimal addDvRatio;
    private BigDecimal addDvTtm;
    private BigDecimal turnoverRate;
    private BigDecimal turnoverRateF;
    private BigDecimal addTurnoverRate;
    private BigDecimal addTurnoverRateF;
    private BigDecimal turnoverRateFQuantile;
    private BigDecimal totalMv;
    private BigDecimal close;
    private BigDecimal addClose;
    private BigDecimal middleLyrPe;
    private BigDecimal middleLyrPeQuantile;
    private BigDecimal middleTtmPe;
    private BigDecimal middleTtmPeQuantile;
    private BigDecimal middlePb;
    private BigDecimal middlePbQuantile;
    private BigDecimal belowNetAssetPercent;
    private Integer belowNetAssetCount;
    private Integer total;

}