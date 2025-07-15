package com.example.notification.http;

import com.example.notification.easymoney.netVo.EmBandNetVO;
import com.example.notification.repository.EmIndicatorDao;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.List;

@Component
public class RestRequestFromDongCai {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    //https://datacenter-web.eastmoney.com/api/data/v1/get?reportName=RPT_VALUEINDUSTRY_DET&columns=ALL&pageNumber=1&pageSize=5000&sortColumns=TRADE_DATE&source=WEB&client=WEB&filter=(BOARD_CODE%3D%22016029%22)


    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EmIndicatorDao emIndicatorDao;

    public static void main(String[] args) throws JsonProcessingException {
//        queryValueIndustryDet("");

//        String baseUrlTemplate = "https://xueqiu.com/statuses/original/timeline.json?user_id=6594360415&page=1"; // filter 参数值用占位符
//
//        // 执行 GET 请求
//        RestTemplate restRequest = new RestTemplate();
//        ResponseEntity<String> response = restRequest.getForEntity(baseUrlTemplate, String.class);
//        System.out.println("response = " + response);

    }


    public List<EmBandNetVO> queryValueIndustryDet(String boardCode) {
        String filterValue = "(BOARD_CODE=" + boardCode + ")";
        String baseUrlTemplate = "https://datacenter-web.eastmoney.com/api/data/v1/get?" +
                "reportName=RPT_VALUEINDUSTRY_DET&columns=ALL&pageNumber=1&pageSize=5000&sortColumns=TRADE_DATE&source=WEB&client=WEB" +
                "&filter={filterPlaceholder}";

        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(baseUrlTemplate);

        URI uri = builder.build().expand(filterValue).toUri();

        String fullUrl = uri.toString();

        try {
            String retMap = restTemplate.getForObject(fullUrl, String.class);
            JsonNode jsonNode = objectMapper.readTree(retMap);
            String ret = jsonNode.get("result").get("data").toString();
            List<EmBandNetVO> industries = objectMapper.readValue(ret, new TypeReference<>() {
            });
            if (!industries.isEmpty()) {
                logger.info("Exit queryValueIndustryDet ============ size = {},", industries.size());
                return industries;
            }
        } catch (Exception e) {
            logger.info("Failed queryValueIndustryDet ============ Please have a check", e);
        }
        return Collections.emptyList();
    }

}
