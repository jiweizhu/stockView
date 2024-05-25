package com.example.notification.responseVo;

import lombok.Data;

@Data
public class StockData {
    long timestamp;
    double open;
    double high;
    double low;
    double close;

    public StockData(long timestamp, double open, double high, double low, double close) {
        this.timestamp = timestamp;
        this.open = open;
        this.high = high;
        this.low = low;
        this.close = close;
    }

    @Override
    public String toString() {
        return "[" + timestamp + "," + open + ", " + high + "," + low + ", " + close + "]";
    }
}
