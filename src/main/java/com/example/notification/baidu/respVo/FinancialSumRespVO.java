package com.example.notification.baidu.respVo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FinancialSumRespVO {
    private String indicatorId;
    private List<ReportDayNums> list = new ArrayList<>();

    public FinancialSumRespVO() {
    }


}

@Data
class ReportDayNums {
    private String reportDay;
    private Integer profit_gain_asc_num = 0;
    private Integer profit_gain_desc_num = 0;
    private Integer gross_gain_asc_num = 0;
    private Integer gross_gain_desc_num = 0;
}
