package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.sql.Timestamp;

@Data
@Entity
@IdClass(BdFinancialSumKey.class)
@Table(name = "bd_indicator_financial_summary")
public class BdFinancialSumVO {

    @Id
    @Column(name = "indicator_id")
    private String indicatorId;

    @Id
    @Column(name = "report_day")
    private String reportDay;

    @Column(name = "profit_sum")
    private String profitSum;

    @Column(name = "gross_sum")
    private String grossSum;

    @Column(name = "profit_gain_asc_num")
    private Integer profitGainAscNum;

    @Column(name = "profit_gain_desc_num")
    private Integer profitGainDescNum;

    @Column(name = "gross_gain_asc_num")
    private Integer grossGainAscNum;

    @Column(name = "gross_gain_desc_num")
    private Integer grossGainDescNum;

    @Column(name = "profit_gain_asc_ids")
    private String profitGainAscIds;

    @Column(name = "profit_gain_desc_ids")
    private String profitGainDescIds;

    @Column(name = "gross_gain_asc_ids")
    private String grossGainAscIds;

    @Column(name = "gross_gain_desc_ids")
    private String grossGainDescIds;

    //gain range 0-10%, has 10 stocks, gain range 10-20%, has 5 stocks
    //gain<-50%, -40%,... gain> 40%, 50%
    @Column(name = "gross_gain_count_distribution")
    private String grossGainCountDistribution;


    @Column(name = "profit_gain_count_distribution")
    private String profitGainCountDistribution;

    @Column(name = "last_updated_time")
    private Timestamp lastUpdatedTime;





    public static BdFinancialSumVO getInitVO(){
        BdFinancialSumVO vo = new BdFinancialSumVO();
        vo.setGrossGainAscNum(0);
        vo.setGrossGainDescNum(0);
        vo.setProfitGainAscNum(0);
        vo.setProfitGainDescNum(0);
        vo.setGrossGainAscIds("");
        vo.setGrossGainDescIds("");
        vo.setProfitGainAscIds("");
        vo.setProfitGainDescIds("");
        return vo;
    }

}
