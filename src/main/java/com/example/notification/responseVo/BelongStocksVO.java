package com.example.notification.responseVo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class BelongStocksVO {
    private String name;
    private List<BigDecimal> data;
}
