package com.example.notification.http;

import com.example.notification.vo.DailyQueryResponseVO;
import com.example.notification.vo.WebQueryParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class RestRequest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static String dailyQueryUrl = "https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?_var=kline_dayhfq&param=stockNum,day,,,daysToQuery,qfq";
    static String IntraDay_URL = "https://web.ifzq.gtimg.cn/appstock/app/minute/query?code=";

    @Autowired
    private RestTemplate restTemplate;

    public static void main(String[] args) throws JsonProcessingException {
        WebQueryParam webQueryParam = new WebQueryParam();
//        queryKLine(webQueryParam);
    }

    public DailyQueryResponseVO queryKLine(WebQueryParam webQueryParam) {
        String queryUrl = dailyQueryUrl.replaceFirst("stockNum", webQueryParam.getIdentifier())
                .replaceFirst("daysToQuery", String.valueOf(webQueryParam.getDaysToQuery()));
        logger.debug("queryUrl = " + queryUrl);
        String ret = restTemplate.getForObject(queryUrl, String.class);
        ret = ret.replaceFirst("kline_dayhfq=", "");
        logger.debug("ret = " + ret);
        DailyQueryResponseVO dailyQueryResponseVO = null;
        try {
            dailyQueryResponseVO = objectMapper.readValue(ret, DailyQueryResponseVO.class);
        } catch (JsonProcessingException e) {
            //send an alarm email and stop to work
            logger.error("Website query error!! Stop to query real price. ", e);
        }
        logger.debug("dailyQueryResponseVO = " + dailyQueryResponseVO);
        return dailyQueryResponseVO;
    }

    public DailyQueryResponseVO getIntraDayData(WebQueryParam webQueryParam) {
        String queryUrl = IntraDay_URL + webQueryParam.getIdentifier();
        logger.debug("getIntraDayData queryUrl = " + queryUrl);
        String ret = restTemplate.getForObject(queryUrl, String.class);
        logger.debug("ret = " + ret);
        DailyQueryResponseVO dailyQueryResponseVO = null;
        try {
            dailyQueryResponseVO = objectMapper.readValue(ret, DailyQueryResponseVO.class);
        } catch (JsonProcessingException e) {
            //send an alarm email and stop to work
            logger.error("Website query error!! Stop to query real price. ", e);
        }
        logger.debug("dailyQueryResponseVO = " + dailyQueryResponseVO);
        if(dailyQueryResponseVO == null || dailyQueryResponseVO.getCode() != 0){
            return null;
        }
        return dailyQueryResponseVO;
    }

    public DailyQueryResponseVO queryKLineTest(WebQueryParam webQueryParam) {
        DailyQueryResponseVO dailyQueryResponseVO = new DailyQueryResponseVO();
        String ret = "{\"code\":0,\"msg\":\"\",\"data\":{\"sh588060\":{\"day\":[[\"2023-06-06\",\"0.669\",\"0.654\",\"0.670\",\"0.651\",\"1208355.000\"],[\"2023-06-07\",\"0.653\",\"0.652\",\"0.656\",\"0.648\",\"977269.000\"],[\"2023-06-08\",\"0.651\",\"0.644\",\"0.651\",\"0.640\",\"979462.000\"],[\"2023-06-09\",\"0.646\",\"0.659\",\"0.660\",\"0.641\",\"1270131.000\"],[\"2023-06-12\",\"0.658\",\"0.654\",\"0.659\",\"0.652\",\"945286.000\"],[\"2023-06-13\",\"0.654\",\"0.663\",\"0.666\",\"0.651\",\"1031744.000\"],[\"2023-06-14\",\"0.662\",\"0.660\",\"0.663\",\"0.657\",\"654019.000\"],[\"2023-06-15\",\"0.661\",\"0.661\",\"0.662\",\"0.657\",\"1047705.000\"],[\"2023-06-16\",\"0.661\",\"0.671\",\"0.672\",\"0.659\",\"954822.000\"],[\"2023-06-19\",\"0.669\",\"0.657\",\"0.675\",\"0.666\",\"755912.000\"],[\"2023-06-20\",\"0.669\",\"0.671\",\"0.675\",\"0.666\",\"755912.000\"]],\"qt\":{\"sh588060\":[\"1\",\"科创50ETF龙头\",\"588060\",\"0.671\",\"0.671\",\"0.669\",\"755912\",\"424975\",\"330937\",\"0.671\",\"13514\",\"0.670\",\"1910\",\"0.669\",\"1250\",\"0.668\",\"447\",\"0.667\",\"5107\",\"0.672\",\"3447\",\"0.673\",\"21848\",\"0.674\",\"16484\",\"0.675\",\"37626\",\"0.676\",\"16314\",\"\",\"20230619155934\",\"0.000\",\"0.00\",\"0.675\",\"0.666\",\"0.671/755912/50765710\",\"755912\",\"5077\",\"3.95\",\"\",\"\",\"0.675\",\"0.666\",\"1.34\",\"12.84\",\"12.84\",\"0.00\",\"0.805\",\"0.537\",\"0.82\",\"-73491\",\"0.672\",\"\",\"\",\"\",\"\",\"\",\"5076.5710\",\"0.0000\",\"0\",\" \",\"ETF\",\"9.11\",\"2.60\",\"\",\"\",\"\",\"0.761\",\"0.562\",\"0.00\",\"1.98\",\"2.13\",\"1913140000\",\"1913140000\",\"-62.31\",\"6.85\",\"1913140000\",\"-0.21\",\"0.672\",\"-4.82\",\"-0.15\",\"0.6714\",\"CNY\",\"0\",\"___D__F__N\"],\"market\":[\"2023-06-19 20:52:52|HK_close_已收盘|SH_close_已收盘|SZ_close_已收盘|US_close_美国联邦假日休市|SQ_close_已休市|DS_close_已休市|ZS_close_已休市|NEWSH_close_已收盘|NEWSZ_close_已收盘|NEWHK_close_已收盘|NEWUS_close_美国联邦假日休市|REPO_close_已收盘|UK_open_交易中|KCB_close_已收盘|IT_open_交易中|MY_close_已收盘|EU_open_交易中|AH_close_已收盘|DE_open_交易中|JW_close_已收盘|CYB_close_已收盘|USA_close_美国联邦假日休市|USB_close_美国联邦假日休市|ZQ_close_已收盘\"]},\"mx_price\":{\"mx\":[],\"price\":[]},\"prec\":\"0.671\",\"version\":\"16\"}}}";
        try {
            dailyQueryResponseVO = objectMapper.readValue(ret, DailyQueryResponseVO.class);
        } catch (JsonProcessingException e) {
            //send an alarm email and stop to work
            logger.error("Website query error!! Stop to query again", e);
        }
        logger.debug("dailyQueryResponseVO = " + dailyQueryResponseVO);
        return dailyQueryResponseVO;
    }
}
