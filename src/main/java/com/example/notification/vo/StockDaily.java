package com.example.notification.vo;

import lombok.Data;

import java.util.List;
@Data
public class StockDaily {
    private String stockNum;
    private List<String[]> day;

}
