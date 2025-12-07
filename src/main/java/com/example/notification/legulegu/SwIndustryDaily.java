package com.example.notification.legulegu;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
@Data
@Entity
@Table(name = "sw_industry_daily",
        indexes = {
                @Index(name = "idx_trade_date", columnList = "trade_date"),
                @Index(name = "idx_industry_code", columnList = "industry_code")
        },
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_code_date", columnNames = {"industry_code", "trade_date"})
        })
public class SwIndustryDaily {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "industry_code", nullable = false, length = 20)
    private String industryCode;

    @Column(name = "trade_date", nullable = false)
    private LocalDate tradeDate;

    @Column(name = "pe", precision = 16, scale = 4)
    private BigDecimal pe;

    @Column(name = "pe_ttm", precision = 16, scale = 4)
    private BigDecimal peTtm;

    @Column(name = "pb", precision = 16, scale = 4)
    private BigDecimal pb;

    @Column(name = "index_close", precision = 16, scale = 4)
    private BigDecimal indexClose;

    // 之前已有字段略...

    @Column(name = "lyr_pe_quantile", precision = 10, scale = 5)
    private BigDecimal lyrPeQuantile;

    @Column(name = "ttm_pe_quantile", precision = 10, scale = 5)
    private BigDecimal ttmPeQuantile;

    @Column(name = "pb_quantile", precision = 10, scale = 5)
    private BigDecimal pbQuantile;

    @Column(name = "dv_ratio", precision = 16, scale = 4)
    private BigDecimal dvRatio;

    @Column(name = "dv_ratio_quantile", precision = 10, scale = 5)
    private BigDecimal dvRatioQuantile;

    @Column(name = "dv_ttm", precision = 16, scale = 4)
    private BigDecimal dvTtm;

    @Column(name = "dv_ttm_quantile", precision = 10, scale = 5)
    private BigDecimal dvTtmQuantile;

    @Column(name = "add_lyr_pe", precision = 16, scale = 4)
    private BigDecimal addLyrPe;

    @Column(name = "add_lyr_pe_quantile", precision = 10, scale = 5)
    private BigDecimal addLyrPeQuantile;

    @Column(name = "add_ttm_pe", precision = 16, scale = 4)
    private BigDecimal addTtmPe;

    @Column(name = "add_ttm_pe_quantile", precision = 10, scale = 5)
    private BigDecimal addTtmPeQuantile;   // 你最关心的这个

    @Column(name = "add_pb", precision = 16, scale = 4)
    private BigDecimal addPb;

    @Column(name = "add_pb_quantile", precision = 10, scale = 5)
    private BigDecimal addPbQuantile;

    @Column(name = "add_dv_ratio", precision = 16, scale = 4)
    private BigDecimal addDvRatio;

    @Column(name = "add_dv_ttm", precision = 16, scale = 4)
    private BigDecimal addDvTtm;

    @Column(name = "turnover_rate", precision = 16, scale = 4)
    private BigDecimal turnoverRate;

    @Column(name = "turnover_rate_f", precision = 16, scale = 4)
    private BigDecimal turnoverRateF;

    @Column(name = "add_turnover_rate", precision = 16, scale = 4)
    private BigDecimal addTurnoverRate;

    @Column(name = "add_turnover_rate_f", precision = 16, scale = 4)
    private BigDecimal addTurnoverRateF;

    @Column(name = "turnover_rate_f_quantile", precision = 10, scale = 5)
    private BigDecimal turnoverRateFQuantile;

    @Column(name = "total_mv", precision = 20, scale = 2)
    private BigDecimal totalMv;

    @Column(name = "close_price", precision = 16, scale = 4)
    private BigDecimal closePrice;

    @Column(name = "add_close", precision = 16, scale = 4)
    private BigDecimal addClose;

    @Column(name = "middle_lyr_pe", precision = 16, scale = 4)
    private BigDecimal middleLyrPe;

    @Column(name = "middle_lyr_pe_quantile", precision = 10, scale = 5)
    private BigDecimal middleLyrPeQuantile;

    @Column(name = "middle_ttm_pe", precision = 16, scale = 4)
    private BigDecimal middleTtmPe;

    @Column(name = "middle_ttm_pe_quantile", precision = 10, scale = 5)
    private BigDecimal middleTtmPeQuantile;

    @Column(name = "middle_pb", precision = 16, scale = 4)
    private BigDecimal middlePb;

    @Column(name = "middle_pb_quantile", precision = 10, scale = 5)
    private BigDecimal middlePbQuantile;

    @Column(name = "below_net_asset_percent", precision = 10, scale = 4)
    private BigDecimal belowNetAssetPercent;

    @Column(name = "below_net_asset_count")
    private Integer belowNetAssetCount;

    @Column(name = "total")
    private Integer total;

// 对应 getter/setter 省略, 用 Lombok @Data/@Getter/@Setter 也行

}
