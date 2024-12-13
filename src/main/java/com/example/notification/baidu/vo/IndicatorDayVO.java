package com.example.notification.baidu.vo;

import lombok.Data;

@Data
public class IndicatorDayVO {

    private Long timestamp; // 时间戳
    private String day; // 时间
    private Double open; // 开盘价
    private Double close; // 收盘价
    private Long volume; // 成交量
    private Double high; // 最高价
    private Double low; // 最低价
    private Double amount; // 成交额
    private Double range; // 振幅
    private Double ratio; // 比率（‌可能是涨跌幅）‌
    private Double turnoverratio; // 换手率
    private Double preClose; // 前收盘价
    private Double ma5avgprice; // 5日均价
    private Long ma5volume; // 5日成交量
    private Double ma10avgprice; // 10日均价
    private Long ma10volume; // 10日成交量
    private Double ma20avgprice; // 20日均价
    private Long ma20volume; // 20日成交量

    public IndicatorDayVO() {
    }

    public IndicatorDayVO(Long timestamp, String day, Double open, Double close, Long volume, Double high, Double low,
                          Double amount, Double range, Double ratio, Double turnoverratio, Double preClose, Double ma5avgprice,
                          Long ma5volume, Double ma10avgprice, Long ma10volume, Double ma20avgprice, Long ma20volume) {
        this.timestamp = timestamp;
        this.day = day;
        this.open = open;
        this.close = close;
        this.volume = volume;
        this.high = high;
        this.low = low;
        this.amount = amount;
        this.range = range;
        this.ratio = ratio;
        this.turnoverratio = turnoverratio;
        this.preClose = preClose;
        this.ma5avgprice = ma5avgprice;
        this.ma5volume = ma5volume;
        this.ma10avgprice = ma10avgprice;
        this.ma10volume = ma10volume;
        this.ma20avgprice = ma20avgprice;
        this.ma20volume = ma20volume;
    }
}
