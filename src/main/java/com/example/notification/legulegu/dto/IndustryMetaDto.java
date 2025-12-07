package com.example.notification.legulegu.dto;

import lombok.Data;

@Data
public class IndustryMetaDto {
    private String industryCode;
    private String industryName;
    private Integer stockCount;
    private String parentName;

    // getter / setter
}
