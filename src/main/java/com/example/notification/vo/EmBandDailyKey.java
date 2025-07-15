package com.example.notification.vo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;
import java.sql.Date;

@Data
@Entity
@Table(name = "easy_band_daily")
public class EmBandDailyKey implements Serializable {

    @Id
    private String boardCode;

    @Id
    private Date tradeDate;

}
