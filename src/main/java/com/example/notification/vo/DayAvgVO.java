package com.example.notification.vo;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DayAvgVO {
    private StockNameVO stockNameVO;
    private BigDecimal lastDayPrice;
    private BigDecimal fiveDayPrice;
    private BigDecimal tenDayAvgPrice;
    private BigDecimal twentyDayAvgPrice;
    private BigDecimal thirtyDayAvgPrice;

    public DayAvgVO() {
        this.stockNameVO = new StockNameVO();
    }

    @Override
    public String toString() {
        return "{" + "stockId=" + stockNameVO.getStockId() + ", stockName='" + stockNameVO.getStockName() + ", fiveDayPrice=" + fiveDayPrice + ", tenDayAvgPrice=" + tenDayAvgPrice + ", twentyDayAvgPrice=" + twentyDayAvgPrice + ", thirtyDayAvgPrice=" + thirtyDayAvgPrice + '}';
    }
}
