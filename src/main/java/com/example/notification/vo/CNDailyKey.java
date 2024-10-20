package com.example.notification.vo;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.io.Serializable;

@Data
@Entity
@Table(name = "cn_daily_price")
public class CNDailyKey implements Serializable {

    @Id
    private String indexCode;

    @Id
    private String tradeDate;

}
