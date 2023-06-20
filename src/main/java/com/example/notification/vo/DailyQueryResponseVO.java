package com.example.notification.vo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.Map;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DailyQueryResponseVO {

    private int code;

    private String msg;

    private Map<String, Object> data;

}
