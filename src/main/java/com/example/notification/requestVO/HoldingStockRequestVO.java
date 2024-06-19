package com.example.notification.requestVO;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Date;

@Data
public class HoldingStockRequestVO {
    private String stockId;
    private String id;

    private BigDecimal costPrice;

    private Integer buyInLot;

    private String belongEtf;

    private Date buyInDay;

}
