package com.example.notification.legulegu.dto;

import lombok.Data;

import java.util.List;

@Data
public class IndustrySeriesDto {

    private String industryCode;
    private String industryName; // 你后面可以从行业表 join 或缓存里补

    /**
     * 对应某个行业的所有日线点
     */
    private List<IndustryDailyPointDto> rows;

}
