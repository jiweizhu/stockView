package com.example.notification.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.sql.Date;

@Data
@Entity
@Table(name = "range_sort_id")
public class RangeSortIDVO {

    @Id
    @Column(name = "rangeId")
    private String rangeId;

    @Column(name = "dayStart")
    private Date dayStart;

    @Column(name = "dayEnd")
    private Date dayEnd;

    @Column(name = "description")
    private String description;


    public RangeSortIDVO() {
    }

}
