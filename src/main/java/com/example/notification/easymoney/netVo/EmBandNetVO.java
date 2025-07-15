package com.example.notification.easymoney.netVo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class EmBandNetVO {

    @JsonProperty("BOARD_CODE")
    private String boardCode;

    @JsonProperty("ORIGINALCODE")
    private String originalCode;

    @JsonProperty("TRADE_DATE")
    private String tradeDate; // Or use java.time.LocalDateTime if you need date object

    @JsonProperty("PE_TTM")
    private Double peTtm;

    @JsonProperty("PE_LAR")
    private Double peLar;

    @JsonProperty("PB_MRQ")
    private Double pbMrq;

    @JsonProperty("PCF_OCF_TTM")
    private Double pcfOcfTtm;

    @JsonProperty("PS_TTM")
    private Double psTtm;

    @JsonProperty("PEG_CAR")
    private Double pegCar;

    @JsonProperty("TOTAL_MARKET_CAP")
    private Double totalMarketCap;

    @JsonProperty("MARKET_CAP_VAG")
    private Double marketCapVag;

    @JsonProperty("NOTLIMITED_MARKETCAP_A")
    private Double notLimitedMarketcapA;

    @JsonProperty("NOMARKETCAP_A_VAG")
    private Double noMarketcapAVag;

    @JsonProperty("TOTAL_SHARES")
    private Long totalShares;

    @JsonProperty("TOTAL_SHARES_VAG")
    private Double totalSharesVag;

    @JsonProperty("FREE_SHARES_VAG")
    private Double freeSharesVag;

    @JsonProperty("NUM")
    private Integer num;

    @JsonProperty("LOSS_COUNT")
    private Integer lossCount;
}
