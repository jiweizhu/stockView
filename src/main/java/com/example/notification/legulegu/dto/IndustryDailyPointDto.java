package com.example.notification.legulegu.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class IndustryDailyPointDto {

    /**
     * 前端更好处理，用时间戳（毫秒）跟你 JSON 模板一致
     */
    private long date;

    private BigDecimal pe;
    private BigDecimal peTtm;
    private BigDecimal pb;

    private BigDecimal addLyrPeQuantile;
    private BigDecimal addTtmPeQuantile;
    private BigDecimal addPbQuantile;

    private BigDecimal indexClose;

    // getter / setter
}
