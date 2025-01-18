package com.example.notification.baidu.respVo;

import lombok.Data;

@Data
public class FinancialRespVO {
    private String stockId;
    private String reportDay;
    private String grossIncome;
    private double grossIncomeGain;
    private String grossProfit;
    private double grossProfitGain;

    public FinancialRespVO(String stockId, String reportDay, String grossIncome, double grossIncomeGain, String grossProfit, double grossProfitGain) {
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
