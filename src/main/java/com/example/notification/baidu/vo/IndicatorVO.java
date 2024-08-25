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
    private List<Stock> rise_first;
    private Integer riseCount;
    private Integer fallCount;
    private Integer memberCount;
    private double increase;
    private String pcUrl;


    @Data
    public static class Ratio {
        private String value;
        private String status;
    }

    @Data
    public static class Stock {
        private String code;
        private String name;
        private String market;
        private Price price;
        private Ratio ratio;
        private String exchange;
        private String url;
        private String webAppUrl;

        @Data
        public static class Price {
            private String value;
            private String status;

        }
    }

}
