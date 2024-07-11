package com.example.notification.responseVo;

import lombok.Data;

@Data
public class IntraDayRespVO {
    private String time;
    private String price;

    public IntraDayRespVO(String time, String price) {
        this.time = time;
        this.price = price;
    }

    public IntraDayRespVO() {
    }

}
