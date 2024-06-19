package com.example.notification.responseVo;

import lombok.Data;

@Data
public class EtfsRespVO {
    private String etfId;
    private String etfName;
    private String stockIds;

    public EtfsRespVO(String etfId, String etfName, String stockIds) {
        this.etfId = etfId;
        this.etfName = etfName;
        this.stockIds = stockIds;
    }

    public EtfsRespVO(String etfId) {
    }

}
