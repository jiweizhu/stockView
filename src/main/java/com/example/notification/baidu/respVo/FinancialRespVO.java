package com.example.notification.baidu.respVo;

import lombok.Data;

@Data
public class FinancialRespVO {
    private String stockId;
    private String reportDay;
    private String grossIncome;
    private Double grossIncomeGain;
    private String grossProfit;
    private Double grossProfitGain;

    public FinancialRespVO(String stockId, String reportDay, String grossIncome, Double grossIncomeGain, String grossProfit, Double grossProfitGain) {
        this.stockId = stockId;
        this.reportDay = reportDay;
        this.grossIncome = grossIncome;
        this.grossIncomeGain = grossIncomeGain;
        this.grossProfit = grossProfit;
        this.grossProfitGain = grossProfitGain;
    }

    public FinancialRespVO() {
    }
}
