package com.example.notification.responseVo;

import lombok.Data;

@Data
public class StockRespVO {
    private String stockId;

    private String stockName;

    public StockRespVO(String stockId, String stockName) {
        this.stockId = stockId;
        this.stockName = stockName;
    }

    public StockRespVO() {
    }
}
