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

    static String localTestStockFile = "C:\\code\\tools\\notification\\src\\main\\resources\\sNum_test.txt";

    private static ArrayList<StockNameVO> stockNameList = new ArrayList<>();

    private static Map<String, StockNameVO> exceedTenDayMap = new HashMap<>();
    private static Map<String, StockNameVO> downTenDayMap = new HashMap<>();
    private static Map<String, StockNameVO> collectTenDayMap = new HashMap<>();
    private static List<StockNameVO> downTriggerOnePointFivePercentList = new ArrayList<>();
    private static Map<String, DayAvgVO> dayAvgMap = new HashMap<>();

    public static void clearCollect() {
        exceedTenDayMap.clear();
        downTenDayMap.clear();
    }

    @Value("${notification.monitor.file}")
    private String stockFile;

    //load stock list
    private void readStockFile() {
        stockNameList.clear();
        BufferedReader reader = null;
        String line;
        try {
            boolean winSystem = isWinSystem();
            if (winSystem) {
                stockFile = localTestStockFile;
            }
            FileReader fileReader = new FileReader(stockFile);
            reader = new BufferedReader(fileReader);
            while ((line = reader.readLine()) != null) {
                StockNameVO stockNameVO = new StockNameVO();
                stockNameVO.setStockId(line.toLowerCase());
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

    private boolean isWinSystem() {
        String os = System.getenv("OS");
        if (null != os && os.toLowerCase().contains("windows")) {
            return true;
        }
        return false;
    }

    @Autowired
    private RestRequest restRequest;

    public void getAvgPrice() throws InterruptedException, JsonProcessingException {
        readStockFile();
        //iterator to query 20day price history and calculate 10day 20day price, and store in db
        for (StockNameVO stockNameVO : stockNameList) {
            //slow down the speech, just intend to avoid website protection
            Thread.sleep(100);
            if (stockNameVO.getStockId().contains("sh") || stockNameVO.getStockId().contains("sz")) {
                WebQueryParam webQueryParam = new WebQueryParam();
                webQueryParam.setIdentifier(stockNameVO.getStockId());
                DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLine(webQueryParam);
                calculateDayAvgPrice(dailyQueryResponse, webQueryParam, stockNameVO);
            }
        }

        dayAvgMap.forEach((k, v) -> {
            logger.info(k + "," + v.getStockName() + "=" + v.getTenDayAvgPrice() + ";");
        });
    }


    public void calculateDayAvgPrice(DailyQueryResponseVO dailyQueryResponse, WebQueryParam webQueryParam, StockNameVO stockNameVO) throws JsonProcessingException {
        List<OneDayPrice> dailyPriceList = getDayList(dailyQueryResponse, webQueryParam, stockNameVO);
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

        //keep three decimals
        BigDecimal tempPrice = new BigDecimal(tenDaysAvg);
        BigDecimal bigDecimal = tempPrice.setScale(3, BigDecimal.ROUND_HALF_UP);
        String lowerCase = stockNameVO.getStockName().toLowerCase();
        if (!lowerCase.contains("etf") && !lowerCase.contains("lof")) {
            bigDecimal = tempPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        dayAvgVO.setTenDayAvgPrice(bigDecimal.doubleValue());
        dayAvgVO.setStockName(stockNameVO.getStockName());
        dayAvgMap.put(webQueryParam.getIdentifier(), dayAvgVO);
        putCache(dayAvgVO);
    }

    private static List<OneDayPrice> getDayList(DailyQueryResponseVO dailyQueryResponse, WebQueryParam webQueryParam, StockNameVO stockNameVO) throws JsonProcessingException {
        if (Objects.isNull(dailyQueryResponse)) {
            return null;
        }
        Map<String, Object> dataMap = (Map) dailyQueryResponse.getData().get(webQueryParam.getIdentifier().toLowerCase());
        Object dayListObj = dataMap.get("day");
        if (Objects.isNull(dayListObj)) {
            dayListObj = dataMap.get("qfqday");
        }
        List<OneDayPrice> dayList = objectMapper.readValue(objectMapper.writeValueAsString(dayListObj), List.class);
        if (Objects.isNull(dayList)) {
            return null;
        }
        List identifierList = (List) ((Map) dataMap.get("qt")).get(webQueryParam.getIdentifier());
        if (identifierList.size() == 0) {
            logger.error("==============ERROR ===== Input Stock Name must be wrong: " + webQueryParam.getIdentifier());
            logger.error("==============ERROR ====try this url ==https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?_var=kline_dayhfq&param=" + webQueryParam.getIdentifier() + ",day,,,10,qfq");
            return null;
        }
        Object stockChineseNameObject = identifierList.get(1);
        String stockChineseName = stockChineseNameObject.toString();
        stockNameVO.setStockName(stockChineseName);
        return dayList;
    }

    public void filterUpTenDayPriceStock(DailyQueryResponseVO dailyQueryResponse, WebQueryParam webQueryParam, StockNameVO stockNameVO) throws JsonProcessingException {
        List<OneDayPrice> dayList = getDayList(dailyQueryResponse, webQueryParam, stockNameVO);
        if (dayList == null || !checkIfTodayData(dayList)) {
            return;
        }

        double tenDayAvgPrice = dayAvgMap.get(stockNameVO.getStockId()).getTenDayAvgPrice();
        double lastDayPrice = Double.valueOf(((List) dayList.get(dayList.size() - 2)).get(2).toString());
        if (lastDayPrice > tenDayAvgPrice) {
            return;
        }
        double realPrice = Double.valueOf(((List) dayList.get(dayList.size() - 1)).get(2).toString());
        if (tenDayAvgPrice < realPrice) {
            exceedTenDayMap.put(stockNameVO.getStockId(), stockNameVO);
        }
    }

    private void filterDownTenDayPriceStock(DailyQueryResponseVO dailyQueryResponse, WebQueryParam webQueryParam, StockNameVO stockNameVO) throws JsonProcessingException {
        List<OneDayPrice> dayList = getDayList(dailyQueryResponse, webQueryParam, stockNameVO);
        if (dayList == null || !checkIfTodayData(dayList)) {
            return;
        }

        double tenDayAvgPrice = dayAvgMap.get(stockNameVO.getStockId()).getTenDayAvgPrice();
        double lastDayPrice = Double.valueOf(((List) dayList.get(dayList.size() - 2)).get(2).toString());
        if (lastDayPrice <= tenDayAvgPrice) {
            return;
        }
        double realPrice = Double.valueOf(((List) dayList.get(dayList.size() - 1)).get(2).toString());
        if (tenDayAvgPrice > realPrice) {
            downTenDayMap.put(stockNameVO.getStockId(), stockNameVO);
        }
    }

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

    private boolean checkIfTodayData(List<OneDayPrice> dayList) {
        String date = ((List) dayList.get(dayList.size() - 1)).get(0).toString();
        String today = format.format(new Date());
        //if the date yyyy-MM-dd is equal, it means the market is started!
        if (today == date) {
            return false;
        }
        return true;
    }


    public void realTimeQuery() throws Exception {
        int exceedTenDayMapSize = exceedTenDayMap.size();
        int downTenDayMapSize = downTenDayMap.size();
        for (StockNameVO stockNameVO : stockNameList) {
            if (exceedTenDayMap.containsKey(stockNameVO.getStockId())){
                //email already sent today!
                return;
            }
            if (stockNameVO.getStockId().contains("sh") || stockNameVO.getStockId().contains("sz")) {
                WebQueryParam webQueryParam = new WebQueryParam();
                stockNameVO.setStockId(stockNameVO.getStockId());
                webQueryParam.setIdentifier(stockNameVO.getStockId());
                webQueryParam.setDaysToQuery(2);

                DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLine(webQueryParam);

                //for test
                //DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLineTest(webQueryParam);

                filterUpTenDayPriceStock(dailyQueryResponse, webQueryParam, stockNameVO);
                filterDownTenDayPriceStock(dailyQueryResponse, webQueryParam, stockNameVO);
            }
        }
        if(exceedTenDayMapSize < exceedTenDayMap.size() || downTenDayMapSize < downTenDayMap.size()){
            //means that after this loop, new Stock needs to be alarmed.
            EmailUtil.sendMail(exceedTenDayMap, downTenDayMap);
        }
    }


    @CachePut(value = "dayAvgVO", key = "#tele")
    public DayAvgVO putCache(DayAvgVO dayAvgVO) {
        return dayAvgVO;
    }
}
