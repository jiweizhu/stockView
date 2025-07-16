package com.example.notification.baidu.vo;

import lombok.Data;

@Data
public class TTMVo {
    private String date;
    private String value;

    public TTMVo(String date, String value) {
        this.date = date;
        this.value = value;
    }
}
