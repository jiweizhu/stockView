package com.example.notification.vo;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.Data;

import java.sql.Date;

@Data
@Entity
@IdClass(EmBandDailyKey.class)
@Table(name = "easy_band_daily")
public class EmBandDailyVO {

    @Id
    @Column(name = "BOARD_CODE")
    private String boardCode;

    @Id
    @Column(name = "TRADE_DATE")
    private Date tradeDate; // Or use java.time.LocalDateTime if you need date object

    @Column(name = "ORIGINALCODE")
    private String originalCode;

    @Column(name = "PE_TTM")
    private Double peTtm;

    @Column(name = "PE_LAR")
    private Double peLar;

    @Column(name = "PB_MRQ")
    private Double pbMrq;

    @Column(name = "PCF_OCF_TTM")
    private Double pcfOcfTtm;

    @Column(name = "PS_TTM")
    private Double psTtm;

    @Column(name = "PEG_CAR")
    private Double pegCar;

    @Column(name = "TOTAL_MARKET_CAP")
    private Double totalMarketCap;

    @Column(name = "MARKET_CAP_VAG")
    private Double marketCapVag;

    @Column(name = "NOTLIMITED_MARKETCAP_A")
    private Double notLimitedMarketcapA;

    @Column(name = "NOMARKETCAP_A_VAG")
    private Double noMarketcapAVag;

    @Column(name = "TOTAL_SHARES")
    private Long totalShares;

    @Column(name = "TOTAL_SHARES_VAG")
    private Double totalSharesVag;

    @Column(name = "FREE_SHARES_VAG")
    private Double freeSharesVag;

    @Column(name = "NUM")
    private Integer num;

    @Column(name = "LOSS_COUNT")
    private Integer lossCount;
}
