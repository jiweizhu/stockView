package com.example.notification.vo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "week_price")
public class WeekPriceKey implements Serializable {

    @Id
    private String stockId;

    @Id
    private Date day;

}
