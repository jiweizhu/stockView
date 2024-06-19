package com.example.notification.vo;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

@Data
@Entity
@IdClass(IntradayPriceKey.class)
@Table(name = "intraday_price")
public class IntradayPriceVO {

    @Id
    @Column(name = "stockId")
    private String stockId;

    @Id
    @Column(name = "day")
    private Date day;

    @Id
    @Column(name = "minute")
    private String minute;

    @Column(name = "stockName")
    private String stockName;

    @Column(name = "price")
    private BigDecimal price;


}
