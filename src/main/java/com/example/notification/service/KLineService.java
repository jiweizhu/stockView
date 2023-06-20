package com.example.notification.service;

import com.example.notification.http.RestRequest;
import com.example.notification.util.EmailUtil;
import com.example.notification.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
public class KLineService {
    private static final Logger logger = LoggerFactory.getLogger(KLineService.class);
    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static String localStockFile = "C:\\code\\tools\\notification\\src\\main\\resources\\sNum.txt";

    private static ArrayList<StockNameVO> stockNameList = new ArrayList<>();

    private static List<StockNameVO> upTenDayList = new ArrayList<>();
    private static List<StockNameVO> downTenDayList = new ArrayList<>();
    private static List<StockNameVO> twentyDayList = new ArrayList<>();
    private static List<StockNameVO> thirtyDayList = new ArrayList<>();
    private static List<StockNameVO> downTriggerOnePointFivePercentList = new ArrayList<>();
    private static Map<String, DayAvgVO> dayAvgMap = new HashMap<>();

    public static void clearCollect() {
        upTenDayList.clear();
        downTenDayList.clear();
    }

    @Value("${notification.monitor.file}")
    private String stockFile;

    //load stock list
    private void readStockFile() {
        BufferedReader reader = null;
        String line;
        try {
            if (null == stockFile || "".equals(stockFile)) {
                stockFile = localStockFile;
            }
            FileReader fileReader = new FileReader(stockFile);
            reader = new BufferedReader(fileReader);
            while ((line = reader.readLine()) != null) {
                StockNameVO stockNameVO = new StockNameVO();
                stockNameVO.setIdentifier(line.toLowerCase());
                stockNameList.add(stockNameVO);
            }
            logger.info("Successfully read stock name file !!");
        } catch (IOException e) {
            logger.error("Fail read stock name file !!", e);
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.error("error occurs. ", e);
            }
        }
    }

    @Autowired
    private RestRequest restRequest;

    public void startToQueryRealTimePrice() throws InterruptedException, JsonProcessingException {
        readStockFile();
        //iterator to query 20day price history and calculate 10day 20day price, and store in db
        for (StockNameVO stockNameVO : stockNameList) {
            //slow down the speech, just intend to avoid website protection
            Thread.sleep(100);
            if (stockNameVO.getIdentifier().contains("sh") || stockNameVO.getIdentifier().contains("sz")) {
                if (upTenDayList.contains(stockNameVO)) {
                    //meaning that the email is sent today
                    return;
                }

                WebQueryParam webQueryParam = new WebQueryParam();
                webQueryParam.setIdentifier(stockNameVO.getIdentifier());
                DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLine(webQueryParam);
                calculateDayAvgPrice(dailyQueryResponse, webQueryParam, stockNameVO);
            }
        }

        dayAvgMap.forEach((k, v) -> {
            logger.info("key: "+k+"Value: "+ v.toString());
        });
    }


    public void calculateDayAvgPrice(DailyQueryResponseVO dailyQueryResponse, WebQueryParam webQueryParam, StockNameVO stockNameVO) throws JsonProcessingException {
        List dailyPriceList = getDayList(dailyQueryResponse, webQueryParam, stockNameVO);
        if (dailyPriceList == null) return;
        double totalPrice = 0;
        double tenDaysAvg = 0;
        DayAvgVO dayAvgVO = new DayAvgVO();
        for (int i = 0; i < dailyPriceList.size(); i++) {
            List<String> day = (List<String>) dailyPriceList.get(i);
            String dayEndPrice = day.get(2);
            totalPrice = totalPrice + Double.valueOf(dayEndPrice);
            if (i == 9) {
                tenDaysAvg = totalPrice / (i + 1);
            }
            if (i == dailyPriceList.size()) {
                dayAvgVO.setLastDayPrice(Double.valueOf(dayEndPrice));
            }
        }

        BigDecimal tempPrice = new BigDecimal(tenDaysAvg);
        dayAvgVO.setTenDayAvgPrice(tempPrice.setScale(3, BigDecimal.ROUND_HALF_UP).doubleValue());
        dayAvgVO.setTwentyDayAvgPrice(0);
        dayAvgVO.setThirtyDayAvgPrice(0);
        dayAvgMap.put(webQueryParam.getIdentifier(), dayAvgVO);
        putCache(dayAvgVO);
    }

    private static List getDayList(DailyQueryResponseVO dailyQueryResponse, WebQueryParam webQueryParam, StockNameVO stockNameVO) throws JsonProcessingException {
        if (Objects.isNull(dailyQueryResponse)) {
            return null;
        }
        Map map = (Map) dailyQueryResponse.getData().get(webQueryParam.getIdentifier().toLowerCase());
        Object dayListObj = map.get("day");
        if (Objects.isNull(dayListObj)) {
            dayListObj = map.get("qfqday");
        }
        List<OneDayPrice> dayList = objectMapper.readValue(objectMapper.writeValueAsString(dayListObj), List.class);
        if (Objects.isNull(dayList)) {
            return null;
        }
        String stockChineseName = ((List) (((Map) map.get("qt")).get(webQueryParam.getIdentifier()))).get(1).toString();
        stockNameVO.setChineseName(stockChineseName);
        return dayList;
    }

    public void filterUpTenDayPriceStock(DailyQueryResponseVO dailyQueryResponse, WebQueryParam webQueryParam, StockNameVO stockNameVO) throws JsonProcessingException {
        List dayList = getDayList(dailyQueryResponse, webQueryParam, stockNameVO);
        if (!checkIfTodayData(dayList)) {
            return;
        }

        double tenDayAvgPrice = dayAvgMap.get(stockNameVO.getIdentifier()).getTenDayAvgPrice();
        double lastDayPrice = Double.valueOf(((List) dayList.get(dayList.size() - 2)).get(2).toString());
        if (lastDayPrice > tenDayAvgPrice) {
            return;
        }
        double realPrice = Double.valueOf(((List) dayList.get(dayList.size() - 1)).get(2).toString());
        if (tenDayAvgPrice < realPrice) {
            upTenDayList.add(stockNameVO);
            sendAlarmEmail(stockNameVO);
        }
    }

    private void sendAlarmEmail(StockNameVO stockNameVO) {
        try {
            logger.info("start to send email!====", stockNameVO);
            EmailUtil.sendMail(stockNameVO);
        } catch (Exception e) {
            logger.error("Fail to send email! ===", e);
        }

    }

    private boolean checkIfTodayData(List dayList) {
        String date = ((List) dayList.get(dayList.size() - 1)).get(0).toString();
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String today = format.format(new Date());
        //if the date yyyy-MM-dd is equal, it means the market is started!
        if (today.equals(date)) {
            return false;
        }
        return true;
    }


    public void realTimeQuery() throws JsonProcessingException {
        for (StockNameVO stockNameVO : stockNameList) {
            if (stockNameVO.getIdentifier().contains("sh") || stockNameVO.getIdentifier().contains("sz")) {
                WebQueryParam webQueryParam = new WebQueryParam();
                stockNameVO.setIdentifier(stockNameVO.getIdentifier());
                webQueryParam.setDaysToQuery(1);

                DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLine(webQueryParam);

                //for test
                //DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLineTest(webQueryParam);

                filterUpTenDayPriceStock(dailyQueryResponse, webQueryParam, stockNameVO);
            }
        }
    }


    @CachePut(value = "dayAvgVO", key = "#tele")
    public DayAvgVO putCache(DayAvgVO dayAvgVO) {
        return dayAvgVO;
    }
}
