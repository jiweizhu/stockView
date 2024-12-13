package com.example.notification.baidu.vo;

import lombok.Data;

@Data
public class BdStockVO {
    private String code;
    private String name;
    private String market;
    private Ratio ratio;
    private String exchange;
    private String url;
    private String webAppUrl;
}
