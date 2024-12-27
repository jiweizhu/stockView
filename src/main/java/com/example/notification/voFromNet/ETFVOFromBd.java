package com.example.notification.voFromNet;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class ETFVOFromBd {

    @JsonProperty("titleHeader")
    private String titleHeader;

    @JsonProperty("body")
    private List<BodyItem> body;
}

class BodyItem {
    @JsonProperty("name")
    private String name;

    @JsonProperty("code")
    private String code;

    @JsonProperty("market")
    private String market;

    @JsonProperty("exchange")
    private String exchange;

    @JsonProperty("price")
    private String price;

    @JsonProperty("priceRatio")
    private String priceRatio;

    @JsonProperty("positionProportion")
    private String positionProportion;

    @JsonProperty("proportionRatio")
    private String proportionRatio;
}