package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@IdClass(BdFinancialKey.class)
@Table(name = "bd_financial")
public class BdFinancialVO {

    @Id
    @Column(name = "stockId")
    private String stockId;

    @Id
    @Column(name = "report_day")
    private String reportDay;

    @Column(name = "content")
    private String content;

    @Column(name = "gross_income")
    private String grossIncome;

    @Column(name = "gross_income_gain")
    private Double grossIncomeGain;

    @Column(name = "gross_profit")
    private String grossProfit;

    @Column(name = "gross_profit_gain")
    private Double grossProfitGain;

    @Column(name = "last_updated_time")
    private Timestamp lastUpdatedTime;

    @Column(name = "holder_num")
    private String holderNum;

    @Column(name = "top_holders")
    private String topHolders;


}
