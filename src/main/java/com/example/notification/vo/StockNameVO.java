package com.example.notification.vo;

import lombok.Data;

@Data
public class StockNameVO {
    String stockId;
    String stockName;

    @Override
    public String toString() {
        return stockId + "," + stockName;
    }
}
