package com.example.notification.baidu.respVo;

import lombok.Data;

import java.sql.Date;

@Data
public class RangeSortRespVO {
    private String rangeId;

    private Date dayStart;

    private Date dayEnd;

    private String description;
}
