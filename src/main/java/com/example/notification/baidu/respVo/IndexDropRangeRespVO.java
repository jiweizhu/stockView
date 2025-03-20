package com.example.notification.baidu.respVo;

import lombok.Data;

import java.sql.Date;

@Data
public class IndexDropRangeRespVO {
    private String indicatorId;

    private String indicatorName;

    private Date dayStart;

    private Date dayEnd;

    private double dropPercent;
}
