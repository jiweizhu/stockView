package com.example.notification.http;

import com.example.notification.baidu.vo.TTMVo;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class BdRestRequest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    // For baidu stock TTM last 5 years
    //https://gushitong.baidu.com/opendata?openapi=1&query=%E5%B8%82%E7%9B%88%E7%8E%87(TTM)&code=002371&resource_id=51171&market=ab&tag=%E5%B8%82%E7%9B%88%E7%8E%87(TTM)&chart_select=%E8%BF%91%E4%BA%94%E5%B9%B4
    private static String Bd_TTM_URL = "https://gushitong.baidu.com/opendata?openapi=1&dspName=iphone&tn=tangram&client=app&query=市盈率(TTM)&code=$code&word=&resource_id=51171&market=ab&tag=市盈率(TTM)&chart_select=近五年&skip_industry=1&finClientType=pc";

    //https://gushitong.baidu.com/opendata?openapi=1&dspName=iphone&tn=tangram&client=app&query=市净率&code=000957&word=&resource_id=51171&market=ab&tag=市净率&chart_select=近五年&skip_industry=1&finClientType=pc
    private static String Bd_PBR_URL = "https://gushitong.baidu.com/opendata?openapi=1&dspName=iphone&tn=tangram&client=app&query=市净率&code=$code&word=&resource_id=51171&market=ab&tag=市净率&chart_select=近五年&skip_industry=1&finClientType=pc";

    //PCF_OCF_TTM 市现率
    //gushitong.baidu.com/opendata?openapi=1&dspName=iphone&tn=tangram&client=app&query=市现率&code=000651&word=&resource_id=51171&market=ab&tag=市现率&chart_select=近五年&skip_industry=1&finClientType=pc
    private static String Bd_PCF_URL = "https://gushitong.baidu.com/opendata?openapi=1&dspName=iphone&tn=tangram&client=app&query=市现率&code=$code&word=&resource_id=51171&market=ab&tag=市现率&chart_select=近五年&skip_industry=1&finClientType=pc";


    @Autowired
    private RestTemplate restTemplate;

    public static void main(String[] args) throws JsonProcessingException {

//        queryBaiduStockTTM("000001");
    }

    public List<TTMVo> queryBaiduStockTTM(String stockId) {
        logger.info("Enter method queryBaiduStockTTM ===============" + stockId);
        String url = Bd_TTM_URL.replace("$code", stockId.startsWith("s") ? stockId.substring(2) : stockId);
        String jsonResponse = restTemplate.getForObject(url, String.class);
        List<TTMVo> retVOList = new ArrayList<>();

        try {
            // 将整个JSON响应解析为Map
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);

            // 导航到 Result 数组
            List<Map<String, Object>> results = (List<Map<String, Object>>) responseMap.get("Result");

            if (results != null && !results.isEmpty()) {
                // 导航到 Result 数组的第一个元素
                Map<String, Object> firstResult = results.get(0);

                // 导航到 DisplayData
                Map<String, Object> displayData = (Map<String, Object>) firstResult.get("DisplayData");

                if (displayData != null) {
                    // 导航到 resultData
                    Map<String, Object> resultData = (Map<String, Object>) displayData.get("resultData");

                    if (resultData != null) {
                        // 导航到 tplData
                        Map<String, Object> tplData = (Map<String, Object>) resultData.get("tplData");

                        if (tplData != null) {
                            // 导航到 result
                            Map<String, Object> result = (Map<String, Object>) tplData.get("result");

                            if (result != null) {
                                // 导航到 chartInfo 数组
                                List<Map<String, Object>> chartInfoList = (List<Map<String, Object>>) result.get("chartInfo");

                                if (chartInfoList != null && !chartInfoList.isEmpty()) {
                                    // 导航到 chartInfo 数组的第一个元素
                                    Map<String, Object> firstChartInfo = chartInfoList.get(0);

                                    // 获取 body 数据，它是一个 List<List<String>>
                                    List<List<String>> bodyData = (List<List<String>>) firstChartInfo.get("body");

                                    if (bodyData != null) {
                                        // 遍历 bodyData 并转换为 TTMVo 列表
                                        for (List<String> dailyData : bodyData) {
                                            if (dailyData.size() == 2) {
                                                retVOList.add(new TTMVo(dailyData.get(0), dailyData.get(1)));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error buildHolderDiv===========", e);
        }
        return retVOList;
    }

    private static String TTM_URL = "TTM_URL";
    private static String PBR_URL = "PBR_URL";
    private static String PCF_URL = "PCF_URL";

    public List<TTMVo> queryStockValuationFromBd(String stockId, String urlType) {
        logger.info("Enter method queryDataFromBd ===============" + stockId);
        String url = "";
        if (urlType == TTM_URL) {
            url = Bd_TTM_URL;
        } else if (urlType == PBR_URL) {
            url = Bd_PBR_URL;
        } else if (urlType == PCF_URL) {
            url = Bd_PCF_URL;
        }
        url = url.replace("$code", stockId.startsWith("s") ? stockId.substring(2) : stockId);
        String jsonResponse = restTemplate.getForObject(url, String.class);
        List<TTMVo> retVOList = new ArrayList<>();

        try {
            // 将整个JSON响应解析为Map
            Map<String, Object> responseMap = objectMapper.readValue(jsonResponse, Map.class);

            // 导航到 Result 数组
            List<Map<String, Object>> results = (List<Map<String, Object>>) responseMap.get("Result");

            if (results != null && !results.isEmpty()) {
                // 导航到 Result 数组的第一个元素
                Map<String, Object> firstResult = results.get(0);

                // 导航到 DisplayData
                Map<String, Object> displayData = (Map<String, Object>) firstResult.get("DisplayData");

                if (displayData != null) {
                    // 导航到 resultData
                    Map<String, Object> resultData = (Map<String, Object>) displayData.get("resultData");

                    if (resultData != null) {
                        // 导航到 tplData
                        Map<String, Object> tplData = (Map<String, Object>) resultData.get("tplData");

                        if (tplData != null) {
                            // 导航到 result
                            Map<String, Object> result = (Map<String, Object>) tplData.get("result");

                            if (result != null) {
                                // 导航到 chartInfo 数组
                                List<Map<String, Object>> chartInfoList = (List<Map<String, Object>>) result.get("chartInfo");

                                if (chartInfoList != null && !chartInfoList.isEmpty()) {
                                    // 导航到 chartInfo 数组的第一个元素
                                    Map<String, Object> firstChartInfo = chartInfoList.get(0);

                                    // 获取 body 数据，它是一个 List<List<String>>
                                    List<List<String>> bodyData = (List<List<String>>) firstChartInfo.get("body");

                                    if (bodyData != null) {
                                        // 遍历 bodyData 并转换为 TTMVo 列表
                                        for (List<String> dailyData : bodyData) {
                                            if (dailyData.size() == 2) {
                                                retVOList.add(new TTMVo(dailyData.get(0), dailyData.get(1)));
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            logger.error("Error buildHolderDiv===========", e);
        }
        return retVOList;
    }

}
