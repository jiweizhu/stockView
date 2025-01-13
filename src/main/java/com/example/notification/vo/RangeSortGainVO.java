package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@IdClass(RangeSortGainKeyVO.class)
@Table(name = "range_sort_gain")
public class RangeSortGainVO {

    @Id
    @Column(name = "rangeId")
    private String rangeId;

    @Id
    @Column(name = "stockId")
    private String stockId;

    @Column(name = "rangeGain")
    private Double rangeGain;


    public RangeSortGainVO() {
    }

}
