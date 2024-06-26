package com.example.notification.responseVo;

import lombok.Data;

@Data
public class RespVO {
    private int code;
    private String message;

    public RespVO(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public RespVO(int code) {
    }

    public RespVO() {

    }
}
