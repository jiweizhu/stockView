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
    static String dailyQueryUrl = "https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?_var=kline_dayhfq&param=stockNum,day,,,daysToQuery,qfq";
    static String weeklyQueryUrl = "https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?_var=kline_dayhfq&param=stockNum,week,,,daysToQuery,qfq";
    static String IntraDay_URL = "https://web.ifzq.gtimg.cn/appstock/app/minute/query?code=";


    //https://finance.pae.baidu.com/vapi/v1/getquotation?pointType=string&group=quotation_index_kline&code=399300&market_type=ab&ktype=week
    private static String BdIndictor_StockId_URL = "https://finance.pae.baidu.com/vapi/v1/getquotation?pointType=string&group=quotation_index_kline&code=$code&market_type=ab&ktype=$type";

    // For baidu indicators start
    //https://gushitong.baidu.com/opendata?resource_id=5352&group=block_stocks&finance_type=block&market=ab&marketType=ab&pc_web=1&finClientType=pc&code=340700&query=340700&pn=0&rn=500
    private static String BdIndictor_OwnedStockIds_URL = "https://gushitong.baidu.com/opendata?resource_id=5352&group=block_stocks&finance_type=block&market=ab&marketType=ab&pc_web=1&finClientType=pc&code=$code&query=$code&pn=0&rn=500";

    // https://gushitong.baidu.com/opendata?query=561560&resource_id=5803&finClientType=pc
    private static String Bd_ETFINFO_URL = "https://gushitong.baidu.com/opendata?query=$code&resource_id=5803&finClientType=pc";

    //for sh000300
    //https://finance.pae.baidu.com/vapi/v1/getquotation?pointType=string&group=quotation_index_kline&code=000300&market_type=ab&ktype=week&start_time=2024-07-30
    private static final String BaiduIndustry_Quotation_Url = "https://finance.pae.baidu.com/vapi/v1/getquotation?pointType=string&group=quotation_index_kline&code=000300&market_type=ab&ktype=week&start_time=$startTime";

    //https://finance.pae.baidu.com/vapi/v1/getquotation?pointType=string&group=quotation_block_kline&code=110200&market_type=ab&ktype=week&start_time=2024-07-30
    private static final String BaiduIndustry_KLine_Url = "https://finance.pae.baidu.com/vapi/v1/getquotation?pointType=string&group=quotation_block_kline&code=$code&market_type=ab&ktype=$ktype&start_time=$startTime";

    //  "https://finance.pae.baidu.com/vapi/v1/getquotation?pointType=string&group=quotation_block_kline&code=110200&market_type=ab&ktype=week&start_time=2024-07-30";
    private static final String Baidu_IndustryIndicators_Url = "https://finance.pae.baidu.com/vapi/v2/blocks?pn=0&rn=150&market=ab&typeCode=HY&finClientType=pc";
    // For baidu indicators end

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
        String url = BaiduIndustry_KLine_Url.replace("$code", code).replace("$ktype", kType).replace("$startTime", startDay);
        if(code.equals("sh000300")){
            url=  BaiduIndustry_Quotation_Url.replace("$startTime", startDay);
        }
        List blockList = new ArrayList();
        try {
            //========test data start ============
//            String str = "{\"ResultCode\":0,\"ResultNum\":0,\"QueryID\":\"15878606086767837875\",\"Result\":{\"newMarketData\":{\"headers\":[\"时间戳\",\"时间\",\"开盘\",\"收盘\",\"成交量\",\"最高\",\"最低\",\"成交额\",\"涨跌额\",\"涨跌幅\",\"换手率\",\"昨收\",\"ma5均价\",\"ma5成交量\",\"ma10均价\",\"ma10成交量\",\"ma20均价\",\"ma20成交量\"],\"keys\":[\"timestamp\",\"time\",\"open\",\"close\",\"volume\",\"high\",\"low\",\"amount\",\"range\",\"ratio\",\"turnoverratio\",\"preClose\",\"ma5avgprice\",\"ma5volume\",\"ma10avgprice\",\"ma10volume\",\"ma20avgprice\",\"ma20volume\"],\"marketData\":\"1722528000,2024-08-02,386.06,404.72,377905688,413.49,382.64,1153733000.00,+19.61,+5.09,8.66,385.11,--,--,--,--,--,--;1723132800,2024-08-09,402.03,408.97,556910074,423.45,396.40,1759742000.00,+4.25,+1.05,12.76,404.72,--,--,--,--,--,--;1723737600,2024-08-16,408.64,396.71,384204638,408.64,395.92,1178719000.00,-12.26,-3.00,8.81,408.97,--,--,--,--,--,--;1724342400,2024-08-23,395.68,394.27,504846472,405.82,388.37,1512805000.00,-2.44,-0.62,11.57,396.71,--,--,--,--,--,--;1724947200,2024-08-30,393.58,409.08,548006805,416.67,383.86,1632110000.00,+14.81,+3.76,12.57,394.27,402.75,474374735,--,--,--,--;1725552000,2024-09-06,407.81,394.25,507392485,409.82,393.62,1437271000.00,-14.83,-3.63,11.63,409.08,400.66,500272095,--,--,--,--;1726156800,2024-09-13,392.14,396.67,537687022,408.72,388.56,1387736000.00,+2.42,+0.61,12.33,394.25,398.20,496427484,--,--,--,--;1726761600,2024-09-20,396.15,405.74,337427922,409.34,388.11,1030068000.00,+9.07,+2.29,7.74,396.67,400.00,487072141,--,--,--,--;1727366400,2024-09-27,403.03,454.03,860103865,456.87,400.11,2605300000.00,+48.29,+11.90,19.70,405.74,411.95,558123620,--,--,--,--;1727625600,2024-09-30,467.54,496.20,375108730,499.46,457.09,1352175000.00,+42.17,+9.29,8.60,454.03,429.38,523544005,416.06,498959370,--,--;1728576000,2024-10-11,549.36,456.00,1062141793,549.36,450.36,4123613000.00,-40.20,-8.10,24.34,496.20,441.73,634493866,421.19,567382981,--,--;1729180800,2024-10-18,458.35,459.77,613548953,469.16,448.96,2211738000.00,+3.77,+0.83,14.06,456.00,454.35,649666253,426.27,573046868,--,--;1729785600,2024-10-25,465.52,490.89,821725498,493.99,465.25,3113329000.00,+31.12,+6.77,18.82,459.77,471.38,746525768,435.69,616798954,--,--;1730390400,2024-11-01,491.22,518.19,1261964550,534.68,486.99,4533437000.00,+27.30,+5.56,28.91,490.89,484.21,826897905,448.08,692510762,--,--;1730995200,2024-11-08,516.33,598.14,2603343630,620.03,502.55,10255950000.00,+79.95,+15.43,59.66,518.19,504.60,1272544885,466.99,898044445,--,--;1731600000,2024-11-15,584.46,538.99,1705291980,590.71,538.99,7043106000.00,-59.15,-9.89,39.07,598.14,521.20,1401174922,481.46,1017834394,--,--;1732204800,2024-11-22,542.73,528.30,1038577579,557.18,524.20,4054383000.00,-10.69,-1.98,23.79,538.99,534.90,1486180647,494.62,1067923450,--,--;1732809600,2024-11-29,534.47,582.02,1514934529,584.29,534.46,6162136000.00,+53.72,+10.17,34.73,528.30,553.13,1624822454,512.25,1185674111,--,--;1733414400,2024-12-06,585.02,612.02,1912799209,622.63,579.70,7875750000.00,+30.00,+5.15,43.83,582.02,571.89,1754989385,528.05,1290943645,--,--;1733673600,2024-12-09,608.81,618.57,433270600,625.07,605.48,1912399000.00,+6.55,+1.07,9.93,612.02,575.98,1320974779,540.29,1296759832,478.18,897859601\"}}}";
//            Map ret = objectMapper.readValue(str, Map.class);
            //========test data end ============

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
            logger.info("Fail to queryBaiduIndustriesKline ============ Please have a check:{}", url, e);
        }
        logger.info("Exit queryBaiduIndustriesKline ============ code=={}, kType={}, startDay={}, return blockList size = {},", code, kType, startDay, blockList.size());
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
        vo.setDay(lineVo[1]);
        vo.setOpen(Double.parseDouble(lineVo[2]));
        vo.setClose(Double.parseDouble(lineVo[3]));
        vo.setVolume(Long.parseLong(lineVo[4]));
        vo.setHigh(Double.parseDouble(lineVo[5]));
        vo.setLow(Double.parseDouble(lineVo[6]));
        vo.setAmount(Double.parseDouble(lineVo[7]));
        vo.setRange(Double.parseDouble(lineVo[8]));
        vo.setRatio(lineVo[9].equals("--") ? null : Double.parseDouble(lineVo[9]));
        vo.setTurnoverratio(lineVo[10].equals("--") ? null : Double.parseDouble(lineVo[10]));
        vo.setPreClose(lineVo[11].equals("--") ? null : Double.parseDouble(lineVo[11]));
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
        logger.info("queryUrl = " + queryUrl);
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

    public JsonNode queryBaiduIndustryOwnedStocks(String indicatorId) {
        logger.info("Enter queryBaiduIndustryStocks === indicatorId= {}", indicatorId);
        try {
            String queryUrl = BdIndictor_OwnedStockIds_URL.replace("$code", indicatorId);
            String ret = restTemplate.getForObject(queryUrl, String.class);
            JsonNode rootNode = objectMapper.readTree(ret);
            JsonNode listNode = rootNode.at("/Result/0/DisplayData/resultData/tplData/result/list");
            if (listNode == null) {
                logger.info("=====Error==to get stockIds from Net====indicatorId =={}", indicatorId);
                return null;
            }
            return listNode;
        } catch (Exception e) {
            logger.info("=====Error======Exception======", e);
        }
        return null;
    }

    public JsonNode queryEtfInfoFromBd(String etfId) {
        logger.info("Enter queryEtfInfoFromBd === ETF id = {}", etfId);
        try {
            String queryUrl = Bd_ETFINFO_URL.replace("$code", etfId.substring(2));
            String ret = restTemplate.getForObject(queryUrl, String.class);
            JsonNode rootNode = objectMapper.readTree(ret);

            JsonNode bodyNode = rootNode.at("/Result/0/DisplayData/resultData/tplData/result/content/tabs/0/content/heavyStock/body");
            if (bodyNode.isArray()) {
               return bodyNode;
            }
            logger.info("=====Warning ======queryEtfInfoFromBd==not found ETF info from Baidu==etfId={}", etfId);
            return null;
        } catch (Exception e) {
            logger.info("=====Error======Exception======", e);
        }
        return null;
    }
}
