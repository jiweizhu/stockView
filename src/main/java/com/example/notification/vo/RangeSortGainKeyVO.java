package com.example.notification.vo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "range_sort_gain")
public class RangeSortGainKeyVO implements Serializable {

    @Id
    private String rangeId;

    @Id
    private String stockId;

}
