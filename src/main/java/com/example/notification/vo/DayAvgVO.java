package com.example.notification.vo;

import lombok.Data;

@Data
public class DayAvgVO {
    private StockNameVO stockNameVO;
    private Double lastDayPrice;
    private Double fiveDayPrice;
    private Double tenDayAvgPrice;
    private Double twentyDayAvgPrice;
    private Double thirtyDayAvgPrice;

    public DayAvgVO() {
        this.stockNameVO = new StockNameVO();
    }

    @Override
    public String toString() {
        return "{" + "stockId=" + stockNameVO.getStockId() + ", stockName='" + stockNameVO.getStockName() + ", fiveDayPrice=" + fiveDayPrice + ", tenDayAvgPrice=" + tenDayAvgPrice + ", twentyDayAvgPrice=" + twentyDayAvgPrice + ", thirtyDayAvgPrice=" + thirtyDayAvgPrice + '}';
    }
}
