package com.example.notification.baidu.vo;

import lombok.Data;

import java.util.List;

@Data
public class IndicatorVO {
    private String market;
    private String code;
    private String name;
    private String price;
    private String last_price;
    private Ratio ratio;
    private List<BdStockVO> rise_first;
    private Integer riseCount;
    private Integer fallCount;
    private Integer memberCount;
    private double increase;
    private String pcUrl;
    private String stock_ids;

}
