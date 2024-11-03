package com.example.notification.http;

import com.example.notification.baidu.vo.IndicatorDayVO;
import com.example.notification.baidu.vo.IndicatorVO;
import com.example.notification.vo.CNDailyVO;
import com.example.notification.vo.QueryFromTencentResponseVO;
import com.example.notification.vo.WebQueryParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class RestRequest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    //https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?_var=kline_dayhfq&param=sh000001,day,,,10,qfq
    static String dailyQueryUrl = "https://web.jifzq.gtimg.cn/appstock/app/fqkline/get?_var=kline_dayhfq&param=stockNum,day,,,daysToQuery,qfq";
    static String weeklyQueryUrl = "https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?_var=kline_dayhfq&param=stockNum,week,,,daysToQuery,qfq";
    static String IntraDay_URL = "https://web.ifzq.gtimg.cn/appstock/app/minute/query?code=";

    //https://finance.pae.baidu.com/vapi/v1/getquotation?pointType=string&group=quotation_block_kline&code=110200&market_type=ab&ktype=week&start_time=2024-07-30
    private static final String BaiduIndustry_KLine_Url = "https://finance.pae.baidu.com/vapi/v1/getquotation?pointType=string&group=quotation_block_kline&code=$code&market_type=ab&ktype=$ktype&start_time=$startTime";

    //  "https://finance.pae.baidu.com/vapi/v1/getquotation?pointType=string&group=quotation_block_kline&code=110200&market_type=ab&ktype=week&start_time=2024-07-30";
    private static final String Baidu_IndustryIndicators_Url = "https://finance.pae.baidu.com/vapi/v2/blocks?pn=0&rn=150&market=ab&typeCode=HY&finClientType=pc";


    //https://www.csindex.com.cn/csindex-home/perf/index-perf?indexCode=930693&startDate=20241008
    private static final String CNIndustry_KLine_Url = "https://www.csindex.com.cn/csindex-home/perf/index-perf?indexCode=$code&startDate=$startTime";


    @Autowired
    private RestTemplate restTemplate;

    public static void main(String[] args) throws JsonProcessingException {

        String str = "[{\"market\":\"ab\",\"code\":\"110200\",\"name\":\"渔业\",\"price\":400.05,\"last_price\":392.36,\"ratio\":{\"value\":\"+1.96%\",\"status\":\"up\"},\"rise_first\":[{\"code\":\"002069\",\"name\":\"獐子岛\",\"market\":\"ab\",\"price\":{\"value\":\"2.73\",\"status\":\"up\"},\"ratio\":{\"value\":\"+9.64%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-002069\",\"webAppUrl\":\"\"},{\"code\":\"300094\",\"name\":\"国联水产\",\"market\":\"ab\",\"price\":{\"value\":\"2.76\",\"status\":\"up\"},\"ratio\":{\"value\":\"+3.76%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-300094\",\"webAppUrl\":\"\"},{\"code\":\"600257\",\"name\":\"大湖股份\",\"market\":\"ab\",\"price\":{\"value\":\"4.2\",\"status\":\"up\"},\"ratio\":{\"value\":\"+1.20%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-600257\",\"webAppUrl\":\"\"},{\"code\":\"600467\",\"name\":\"好当家\",\"market\":\"ab\",\"price\":{\"value\":\"1.43\",\"status\":\"down\"},\"ratio\":{\"value\":\"-0.69%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-600467\",\"webAppUrl\":\"\"},{\"code\":\"600097\",\"name\":\"开创国际\",\"market\":\"ab\",\"price\":{\"value\":\"7.56\",\"status\":\"down\"},\"ratio\":{\"value\":\"-1.05%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-600097\",\"webAppUrl\":\"\"},{\"code\":\"000798\",\"name\":\"中水渔业\",\"market\":\"ab\",\"price\":{\"value\":\"6.31\",\"status\":\"down\"},\"ratio\":{\"value\":\"-1.10%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-000798\",\"webAppUrl\":\"\"}],\"fall_first\":[{\"code\":\"000798\",\"name\":\"中水渔业\",\"market\":\"ab\",\"price\":{\"value\":\"6.31\",\"status\":\"down\"},\"ratio\":{\"value\":\"-1.10%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-000798\",\"webAppUrl\":\"\"},{\"code\":\"600097\",\"name\":\"开创国际\",\"market\":\"ab\",\"price\":{\"value\":\"7.56\",\"status\":\"down\"},\"ratio\":{\"value\":\"-1.05%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-600097\",\"webAppUrl\":\"\"},{\"code\":\"600467\",\"name\":\"好当家\",\"market\":\"ab\",\"price\":{\"value\":\"1.43\",\"status\":\"down\"},\"ratio\":{\"value\":\"-0.69%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-600467\",\"webAppUrl\":\"\"},{\"code\":\"600257\",\"name\":\"大湖股份\",\"market\":\"ab\",\"price\":{\"value\":\"4.2\",\"status\":\"up\"},\"ratio\":{\"value\":\"+1.20%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-600257\",\"webAppUrl\":\"\"},{\"code\":\"300094\",\"name\":\"国联水产\",\"market\":\"ab\",\"price\":{\"value\":\"2.76\",\"status\":\"up\"},\"ratio\":{\"value\":\"+3.76%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-300094\",\"webAppUrl\":\"\"},{\"code\":\"002069\",\"name\":\"獐子岛\",\"market\":\"ab\",\"price\":{\"value\":\"2.73\",\"status\":\"up\"},\"ratio\":{\"value\":\"+9.64%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-002069\",\"webAppUrl\":\"\"}],\"riseCount\":3,\"fallCount\":3,\"memberCount\":6,\"minuteData\":{\"priceinfo\":null},\"increase\":7.69,\"pcUrl\":\"https://gushitong.baidu.com/block/ab-110200\"},{\"market\":\"ab\",\"code\":\"480200\",\"name\":\"国有大型银行Ⅱ\",\"price\":1797.06,\"last_price\":1773.22,\"ratio\":{\"value\":\"+1.34%\",\"status\":\"up\"},\"rise_first\":[{\"code\":\"601988\",\"name\":\"中国银行\",\"market\":\"ab\",\"price\":{\"value\":\"5.07\",\"status\":\"up\"},\"ratio\":{\"value\":\"+2.42%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601988\",\"webAppUrl\":\"\"},{\"code\":\"601398\",\"name\":\"工商银行\",\"market\":\"ab\",\"price\":{\"value\":\"6.42\",\"status\":\"up\"},\"ratio\":{\"value\":\"+1.90%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601398\",\"webAppUrl\":\"\"},{\"code\":\"601939\",\"name\":\"建设银行\",\"market\":\"ab\",\"price\":{\"value\":\"8.22\",\"status\":\"up\"},\"ratio\":{\"value\":\"+1.36%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601939\",\"webAppUrl\":\"\"},{\"code\":\"601658\",\"name\":\"邮储银行\",\"market\":\"ab\",\"price\":{\"value\":\"5.13\",\"status\":\"up\"},\"ratio\":{\"value\":\"+1.18%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601658\",\"webAppUrl\":\"\"},{\"code\":\"601288\",\"name\":\"农业银行\",\"market\":\"ab\",\"price\":{\"value\":\"4.91\",\"status\":\"up\"},\"ratio\":{\"value\":\"+0.82%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601288\",\"webAppUrl\":\"\"},{\"code\":\"601328\",\"name\":\"交通银行\",\"market\":\"ab\",\"price\":{\"value\":\"7.99\",\"status\":\"up\"},\"ratio\":{\"value\":\"+0.38%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601328\",\"webAppUrl\":\"\"}],\"fall_first\":[{\"code\":\"601328\",\"name\":\"交通银行\",\"market\":\"ab\",\"price\":{\"value\":\"7.99\",\"status\":\"up\"},\"ratio\":{\"value\":\"+0.38%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601328\",\"webAppUrl\":\"\"},{\"code\":\"601288\",\"name\":\"农业银行\",\"market\":\"ab\",\"price\":{\"value\":\"4.91\",\"status\":\"up\"},\"ratio\":{\"value\":\"+0.82%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601288\",\"webAppUrl\":\"\"},{\"code\":\"601658\",\"name\":\"邮储银行\",\"market\":\"ab\",\"price\":{\"value\":\"5.13\",\"status\":\"up\"},\"ratio\":{\"value\":\"+1.18%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601658\",\"webAppUrl\":\"\"},{\"code\":\"601939\",\"name\":\"建设银行\",\"market\":\"ab\",\"price\":{\"value\":\"8.22\",\"status\":\"up\"},\"ratio\":{\"value\":\"+1.36%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601939\",\"webAppUrl\":\"\"},{\"code\":\"601398\",\"name\":\"工商银行\",\"market\":\"ab\",\"price\":{\"value\":\"6.42\",\"status\":\"up\"},\"ratio\":{\"value\":\"+1.90%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601398\",\"webAppUrl\":\"\"},{\"code\":\"601988\",\"name\":\"中国银行\",\"market\":\"ab\",\"price\":{\"value\":\"5.07\",\"status\":\"up\"},\"ratio\":{\"value\":\"+2.42%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-601988\",\"webAppUrl\":\"\"}],\"riseCount\":6,\"fallCount\":0,\"memberCount\":6,\"minuteData\":{\"priceinfo\":null},\"increase\":23.84,\"pcUrl\":\"https://gushitong.baidu.com/block/ab-480200\"},{\"market\":\"ab\",\"code\":\"350200\",\"name\":\"服装家纺\",\"price\":1435.07,\"last_price\":1417.96,\"ratio\":{\"value\":\"+1.21%\",\"status\":\"up\"},\"rise_first\":[{\"code\":\"300005\",\"name\":\"探路者\",\"market\":\"ab\",\"price\":{\"value\":\"6.06\",\"status\":\"up\"},\"ratio\":{\"value\":\"+20.00%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-300005\",\"webAppUrl\":\"\"},{\"code\":\"300840\",\"name\":\"酷特智能\",\"market\":\"ab\",\"price\":{\"value\":\"9.08\",\"status\":\"up\"},\"ratio\":{\"value\":\"+14.65%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-300840\",\"webAppUrl\":\"\"},{\"code\":\"002762\",\"name\":\"金发拉比\",\"market\":\"ab\",\"price\":{\"value\":\"5.23\",\"status\":\"up\"},\"ratio\":{\"value\":\"+10.11%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-002762\",\"webAppUrl\":\"\"},{\"code\":\"002780\",\"name\":\"三夫户外\",\"market\":\"ab\",\"price\":{\"value\":\"9.22\",\"status\":\"up\"},\"ratio\":{\"value\":\"+10.02%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-002780\",\"webAppUrl\":\"\"},{\"code\":\"603196\",\"name\":\"日播时尚\",\"market\":\"ab\",\"price\":{\"value\":\"8.02\",\"status\":\"up\"},\"ratio\":{\"value\":\"+10.01%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-603196\",\"webAppUrl\":\"\"},{\"code\":\"002193\",\"name\":\"如意集团\",\"market\":\"ab\",\"price\":{\"value\":\"3.76\",\"status\":\"up\"},\"ratio\":{\"value\":\"+9.94%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-002193\",\"webAppUrl\":\"\"},{\"code\":\"603665\",\"name\":\"康隆达\",\"market\":\"ab\",\"price\":{\"value\":\"13.47\",\"status\":\"up\"},\"ratio\":{\"value\":\"+4.18%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-603665\",\"webAppUrl\":\"\"},{\"code\":\"603908\",\"name\":\"牧高笛\",\"market\":\"ab\",\"price\":{\"value\":\"20.13\",\"status\":\"up\"},\"ratio\":{\"value\":\"+3.87%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-603908\",\"webAppUrl\":\"\"},{\"code\":\"600630\",\"name\":\"龙头股份\",\"market\":\"ab\",\"price\":{\"value\":\"7.53\",\"status\":\"up\"},\"ratio\":{\"value\":\"+3.29%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-600630\",\"webAppUrl\":\"\"},{\"code\":\"300591\",\"name\":\"万里马\",\"market\":\"ab\",\"price\":{\"value\":\"3.05\",\"status\":\"up\"},\"ratio\":{\"value\":\"+3.04%\",\"status\":\"up\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-300591\",\"webAppUrl\":\"\"}],\"fall_first\":[{\"code\":\"603307\",\"name\":\"扬州金泉\",\"market\":\"ab\",\"price\":{\"value\":\"29.97\",\"status\":\"down\"},\"ratio\":{\"value\":\"-10.00%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-603307\",\"webAppUrl\":\"\"},{\"code\":\"603839\",\"name\":\"安正时尚\",\"market\":\"ab\",\"price\":{\"value\":\"4.21\",\"status\":\"down\"},\"ratio\":{\"value\":\"-9.66%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-603839\",\"webAppUrl\":\"\"},{\"code\":\"603608\",\"name\":\"*ST天创\",\"market\":\"ab\",\"price\":{\"value\":\"2.81\",\"status\":\"down\"},\"ratio\":{\"value\":\"-3.44%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-603608\",\"webAppUrl\":\"\"},{\"code\":\"603958\",\"name\":\"哈森股份\",\"market\":\"ab\",\"price\":{\"value\":\"8.36\",\"status\":\"down\"},\"ratio\":{\"value\":\"-2.68%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-603958\",\"webAppUrl\":\"\"},{\"code\":\"003016\",\"name\":\"欣贺股份\",\"market\":\"ab\",\"price\":{\"value\":\"5.82\",\"status\":\"down\"},\"ratio\":{\"value\":\"-2.02%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-003016\",\"webAppUrl\":\"\"},{\"code\":\"838262\",\"name\":\"太湖雪\",\"market\":\"ab\",\"price\":{\"value\":\"9.64\",\"status\":\"down\"},\"ratio\":{\"value\":\"-1.83%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"NEEQ\",\"url\":\"https://gushitong.baidu.com/stock/ab-838262\",\"webAppUrl\":\"\"},{\"code\":\"300901\",\"name\":\"中胤时尚\",\"market\":\"ab\",\"price\":{\"value\":\"8.88\",\"status\":\"down\"},\"ratio\":{\"value\":\"-1.33%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-300901\",\"webAppUrl\":\"\"},{\"code\":\"002154\",\"name\":\"报 喜 鸟\",\"market\":\"ab\",\"price\":{\"value\":\"3.4\",\"status\":\"down\"},\"ratio\":{\"value\":\"-1.16%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-002154\",\"webAppUrl\":\"\"},{\"code\":\"002569\",\"name\":\"ST步森\",\"market\":\"ab\",\"price\":{\"value\":\"6.21\",\"status\":\"down\"},\"ratio\":{\"value\":\"-0.96%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SZ\",\"url\":\"https://gushitong.baidu.com/stock/ab-002569\",\"webAppUrl\":\"\"},{\"code\":\"600137\",\"name\":\"浪莎股份\",\"market\":\"ab\",\"price\":{\"value\":\"11.56\",\"status\":\"down\"},\"ratio\":{\"value\":\"-0.77%\",\"status\":\"down\"},\"outMarketInfo\":{},\"exchange\":\"SS\",\"url\":\"https://gushitong.baidu.com/stock/ab-600137\",\"webAppUrl\":\"\"}],\"riseCount\":31,\"fallCount\":23,\"memberCount\":61,\"minuteData\":{\"priceinfo\":null},\"increase\":17.11,\"pcUrl\":\"https://gushitong.baidu.com/block/ab-350200\"}]";
        List<IndicatorVO> blockList = objectMapper.readValue(str, objectMapper.getTypeFactory().constructCollectionType(List.class, IndicatorVO.class));
        System.out.printf(String.valueOf(blockList.size()));
    }

    public List<IndicatorVO> queryBaiduIndustriesRealInfo() {
        Map ret = restTemplate.getForObject(Baidu_IndustryIndicators_Url, Map.class);
        Map result = (Map) ret.get("Result");
        List blockList = Collections.EMPTY_LIST;
        if (result == null || result.get("blocks") == null) {
            logger.error("Fail to queryBaiduIndustriesRealInfo.============ Please have a check: https://finance.pae.baidu.com/vapi/v2/blocks?pn=0&rn=150&market=ab&typeCode=HY&finClientType=pc ");
            return blockList;
        }
        Object blocks = result.get("blocks");
        try {
            blockList = objectMapper.readValue(objectMapper.writeValueAsString(blocks), objectMapper.getTypeFactory().constructCollectionType(List.class, IndicatorVO.class));
        } catch (JsonProcessingException e) {
            logger.error("Website query error!! Stop to query real price. ", e);
        }
        return blockList;
    }

    public List<IndicatorDayVO> queryBaiduIndustriesKline(String code, String kType, String startDay) {
        logger.info("Enter queryBaiduIndustriesKline ============ code==${}, kType=${}, startDay=${},", code, kType, startDay);
        String code1 = code.replace("sh", "").replace("sz", "");
        String url = BaiduIndustry_KLine_Url.replace("$code", code1).replace("$ktype", kType).replace("$startTime", startDay);
        List blockList = new ArrayList();
        try {
            Map ret = restTemplate.getForObject(url, Map.class);
            Map result = (Map) ret.get("Result");
            Map newMarketData = (Map) result.get("newMarketData");
            String blocks = newMarketData.get("marketData").toString();
            String[] dayPriceArray = blocks.split(";");
            if (dayPriceArray.length > 1) {
                for (int i = 0; i < dayPriceArray.length; i++) {
                    String[] lineVo = dayPriceArray[i].split(",");
                    IndicatorDayVO vo = getIndicatorDayVO(lineVo);
                    blockList.add(vo);
                }
            }
        } catch (Exception e) {
            logger.error("Fail to queryBaiduIndustriesKline ============ Please have a check:" + url);
            return blockList;
        }
        return blockList;
    }


    public List<CNDailyVO> queryCNIndustriesKline(String code, String startDay) {
        logger.info("Enter queryCNIndustriesKline ============ code=={}, startDay={}", code, startDay);
        String url = CNIndustry_KLine_Url.replace("$code", code).replace("$startTime", startDay);
        List<CNDailyVO> blockList = new ArrayList();

        try {
            String ret = restTemplate.getForObject(url, String.class);
            JsonNode rootNode = objectMapper.readTree(ret);
            if (!rootNode.path("code").toString().contains("200") || !rootNode.path("success").toString().contains("true")) {
                logger.error("=======faiil to queryCNIndustriesKline=======" + url);
                return blockList;
            }
            JsonNode dataNode = rootNode.path("data");

            for (JsonNode node : dataNode) {
                CNDailyVO indicator = objectMapper.treeToValue(node, CNDailyVO.class);
                blockList.add(indicator);
            }
        } catch (Exception e) {
            logger.error("Fail to queryCNIndustriesKline ============ Please have a check:" + url);
        }

        return blockList;
    }

    private static IndicatorDayVO getIndicatorDayVO(String[] lineVo) {
        IndicatorDayVO vo = new IndicatorDayVO();
        vo.setTimestamp(Long.parseLong(lineVo[0]));
        vo.setTime(lineVo[1]);
        vo.setOpen(Double.parseDouble(lineVo[2]));
        vo.setClose(Double.parseDouble(lineVo[3]));
        vo.setVolume(Long.parseLong(lineVo[4]));
        vo.setHigh(Double.parseDouble(lineVo[5]));
        vo.setLow(Double.parseDouble(lineVo[6]));
        vo.setAmount(Double.parseDouble(lineVo[7]));
        vo.setRange(Double.parseDouble(lineVo[8]));
        vo.setRatio(Double.parseDouble(lineVo[9]));
        vo.setTurnoverratio(Double.parseDouble(lineVo[10]));
        vo.setPreClose(Double.parseDouble(lineVo[11]));
        vo.setMa5avgprice(lineVo[12].equals("--") ? null : Double.parseDouble(lineVo[12]));
        vo.setMa5volume(lineVo[13].equals("--") ? null : Long.parseLong(lineVo[13]));
        vo.setMa10avgprice(lineVo[14].equals("--") ? null : Double.parseDouble(lineVo[14]));
        vo.setMa10volume(lineVo[15].equals("--") ? null : Long.parseLong(lineVo[15]));
        vo.setMa20avgprice(lineVo[16].equals("--") ? null : Double.parseDouble(lineVo[16]));
        vo.setMa20volume(lineVo[17].equals("--") ? null : Long.parseLong(lineVo[17]));
        return vo;
    }

    public QueryFromTencentResponseVO queryKLine(WebQueryParam webQueryParam) {
        String queryUrl = dailyQueryUrl;
        if (!webQueryParam.getToQueryDailyPrice()) {
            queryUrl = weeklyQueryUrl;
        }
        queryUrl = queryUrl.replaceFirst("stockNum", webQueryParam.getIdentifier()).replaceFirst("daysToQuery", String.valueOf(webQueryParam.getDaysToQuery()));
        logger.debug("queryUrl = " + queryUrl);
        String ret = restTemplate.getForObject(queryUrl, String.class);
        ret = ret.replaceFirst("kline_dayhfq=", "");
        logger.debug("ret = " + ret);
        QueryFromTencentResponseVO queryFromTencentResponseVO = null;
        try {
            queryFromTencentResponseVO = objectMapper.readValue(ret, QueryFromTencentResponseVO.class);
        } catch (JsonProcessingException e) {
            //send an alarm email and stop to work
            logger.error("Website query error!! Stop to query real price. ", e);
        }
        logger.debug("dailyQueryResponseVO = " + queryFromTencentResponseVO);
        return queryFromTencentResponseVO;
    }

    public QueryFromTencentResponseVO getIntraDayData(WebQueryParam webQueryParam) {
        String queryUrl = IntraDay_URL + webQueryParam.getIdentifier();
        logger.debug("getIntraDayData queryUrl = " + queryUrl);
        String ret = restTemplate.getForObject(queryUrl, String.class);
        logger.debug("ret = " + ret);
        QueryFromTencentResponseVO queryFromTencentResponseVO = null;
        try {
            queryFromTencentResponseVO = objectMapper.readValue(ret, QueryFromTencentResponseVO.class);
        } catch (JsonProcessingException e) {
            //send an alarm email and stop to work
            logger.error("Website query error!! Stop to query real price. ", e);
        }
        logger.debug("dailyQueryResponseVO = " + queryFromTencentResponseVO);
        if (queryFromTencentResponseVO == null || queryFromTencentResponseVO.getCode() != 0) {
            return null;
        }
        return queryFromTencentResponseVO;
    }

    public QueryFromTencentResponseVO queryKLineTest(WebQueryParam webQueryParam) {
        QueryFromTencentResponseVO queryFromTencentResponseVO = new QueryFromTencentResponseVO();
        String ret = "{\"code\":0,\"msg\":\"\",\"data\":{\"sh588060\":{\"day\":[[\"2023-06-06\",\"0.669\",\"0.654\",\"0.670\",\"0.651\",\"1208355.000\"],[\"2023-06-07\",\"0.653\",\"0.652\",\"0.656\",\"0.648\",\"977269.000\"],[\"2023-06-08\",\"0.651\",\"0.644\",\"0.651\",\"0.640\",\"979462.000\"],[\"2023-06-09\",\"0.646\",\"0.659\",\"0.660\",\"0.641\",\"1270131.000\"],[\"2023-06-12\",\"0.658\",\"0.654\",\"0.659\",\"0.652\",\"945286.000\"],[\"2023-06-13\",\"0.654\",\"0.663\",\"0.666\",\"0.651\",\"1031744.000\"],[\"2023-06-14\",\"0.662\",\"0.660\",\"0.663\",\"0.657\",\"654019.000\"],[\"2023-06-15\",\"0.661\",\"0.661\",\"0.662\",\"0.657\",\"1047705.000\"],[\"2023-06-16\",\"0.661\",\"0.671\",\"0.672\",\"0.659\",\"954822.000\"],[\"2023-06-19\",\"0.669\",\"0.657\",\"0.675\",\"0.666\",\"755912.000\"],[\"2023-06-20\",\"0.669\",\"0.671\",\"0.675\",\"0.666\",\"755912.000\"]],\"qt\":{\"sh588060\":[\"1\",\"科创50ETF龙头\",\"588060\",\"0.671\",\"0.671\",\"0.669\",\"755912\",\"424975\",\"330937\",\"0.671\",\"13514\",\"0.670\",\"1910\",\"0.669\",\"1250\",\"0.668\",\"447\",\"0.667\",\"5107\",\"0.672\",\"3447\",\"0.673\",\"21848\",\"0.674\",\"16484\",\"0.675\",\"37626\",\"0.676\",\"16314\",\"\",\"20230619155934\",\"0.000\",\"0.00\",\"0.675\",\"0.666\",\"0.671/755912/50765710\",\"755912\",\"5077\",\"3.95\",\"\",\"\",\"0.675\",\"0.666\",\"1.34\",\"12.84\",\"12.84\",\"0.00\",\"0.805\",\"0.537\",\"0.82\",\"-73491\",\"0.672\",\"\",\"\",\"\",\"\",\"\",\"5076.5710\",\"0.0000\",\"0\",\" \",\"ETF\",\"9.11\",\"2.60\",\"\",\"\",\"\",\"0.761\",\"0.562\",\"0.00\",\"1.98\",\"2.13\",\"1913140000\",\"1913140000\",\"-62.31\",\"6.85\",\"1913140000\",\"-0.21\",\"0.672\",\"-4.82\",\"-0.15\",\"0.6714\",\"CNY\",\"0\",\"___D__F__N\"],\"market\":[\"2023-06-19 20:52:52|HK_close_已收盘|SH_close_已收盘|SZ_close_已收盘|US_close_美国联邦假日休市|SQ_close_已休市|DS_close_已休市|ZS_close_已休市|NEWSH_close_已收盘|NEWSZ_close_已收盘|NEWHK_close_已收盘|NEWUS_close_美国联邦假日休市|REPO_close_已收盘|UK_open_交易中|KCB_close_已收盘|IT_open_交易中|MY_close_已收盘|EU_open_交易中|AH_close_已收盘|DE_open_交易中|JW_close_已收盘|CYB_close_已收盘|USA_close_美国联邦假日休市|USB_close_美国联邦假日休市|ZQ_close_已收盘\"]},\"mx_price\":{\"mx\":[],\"price\":[]},\"prec\":\"0.671\",\"version\":\"16\"}}}";
        try {
            queryFromTencentResponseVO = objectMapper.readValue(ret, QueryFromTencentResponseVO.class);
        } catch (JsonProcessingException e) {
            //send an alarm email and stop to work
            logger.error("Website query error!! Stop to query again", e);
        }
        logger.debug("dailyQueryResponseVO = " + queryFromTencentResponseVO);
        return queryFromTencentResponseVO;
    }
}
