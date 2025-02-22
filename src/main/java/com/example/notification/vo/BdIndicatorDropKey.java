package com.example.notification.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "indicator_drop")
public class BdIndicatorDropKey implements Serializable {

    @Id
    @Column(name = "indicator_id")
    private String indicatorId;

    @Id
    @Column(name = "day_start")
    private Date dayStart;

}
