package com.example.notification.service;

import com.example.notification.http.RestRequest;
import com.example.notification.util.EmailUtil;
import com.example.notification.util.Utils;
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
import java.math.RoundingMode;
import java.util.*;

@Service
public class KLineService {
    private static final Logger logger = LoggerFactory.getLogger(KLineService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static String importStockFile = "C:\\code\\tools\\notification\\src\\main\\resources\\import.txt";

    public static Boolean ifMarketOpen = Boolean.FALSE;

    private static ArrayList<StockNameVO> stockFileList = new ArrayList<>();

    private static Map<String, StockNameVO> exceedFiveDayMap = new HashMap<>();
    private static Map<String, StockNameVO> exceedTenDayMap = new HashMap<>();
    private static Map<String, StockNameVO> exceedTwentyDayMap = new HashMap<>();
    private static Map<String, StockNameVO> downFiveDayMap = new HashMap<>();
    private static Map<String, StockNameVO> downTenDayMap = new HashMap<>();
    private static Map<String, StockNameVO> downTwentyDayMap = new HashMap<>();
    private static Map<String, DayAvgVO> dayAvgMap = new HashMap<>();
    private static Map<String, List<ArrayList<String>>> daysPriceMap = new HashMap<>();

    @Value("${notification.monitor.file}")
    private String stockFile;

    @Autowired
    private RestRequest restRequest;

    //load stock list
    private void readStockFile() {
        stockFileList.clear();
        BufferedReader reader = null;
        String line;
        try {
            boolean winSystem = Utils.isWinSystem();
            if (winSystem) {
                stockFile = importStockFile;
            }
            FileReader fileReader = new FileReader(stockFile);
            reader = new BufferedReader(fileReader);
            while ((line = reader.readLine()) != null) {
                StockNameVO stockNameVO = new StockNameVO();
                stockNameVO.setStockId(line.toLowerCase());
                stockFileList.add(stockNameVO);
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

    public Boolean checkIfMarketOpenToday() throws JsonProcessingException {
        readStockFile();
        WebQueryParam webQueryParam = new WebQueryParam();
        webQueryParam.setDaysToQuery(1);
        DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLine(webQueryParam);
        List<ArrayList<String>> dailyPriceList = getRealPriceList(dailyQueryResponse, webQueryParam.getIdentifier());
        String date = dailyPriceList.get(dailyPriceList.size() - 1).get(0);
        if (Utils.todayDate().equals(date)) {
            ifMarketOpen = Boolean.TRUE;
        }
        return ifMarketOpen;
    }

    //every one day to run
    public void getAvgPrice() throws InterruptedException, JsonProcessingException {
        readStockFile();
        //iterator to query 20day price history and calculate 10day 20day price, and store in db
        for (StockNameVO stockNameVO : stockFileList) {
            //slow down the speech, just intend to avoid website protection
            Thread.sleep(100);
            if (stockNameVO.getStockId().contains("sh") || stockNameVO.getStockId().contains("sz")) {
                WebQueryParam webQueryParam = new WebQueryParam();
                webQueryParam.setIdentifier(stockNameVO.getStockId());
                StockNameVO nameVO = new StockNameVO();
                nameVO.setStockId(stockNameVO.getStockId());
                DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLine(webQueryParam);
                calculateDayAvgPrice(dailyQueryResponse, nameVO);
            }
        }

        dayAvgMap.forEach((k, v) -> {
            logger.info("===={}", v);
        });
    }


    public void calculateDayAvgPrice(DailyQueryResponseVO dailyQueryResponse, StockNameVO stockNameVO) throws JsonProcessingException {
        List<ArrayList<String>> dailyPriceList = getLastThirtyDaysList(dailyQueryResponse, stockNameVO);
        if (dailyPriceList == null) return;
        int size = dailyPriceList.size();
        if (dailyPriceList.get(size - 1).get(0).equals(Utils.todayDate())) {
            //if the last one is today, remove today
            dailyPriceList.remove(size - 1);
        }

        calculateDayAvg(stockNameVO, dailyPriceList, 5);
        calculateDayAvg(stockNameVO, dailyPriceList, 10);
        calculateDayAvg(stockNameVO, dailyPriceList, 20);
        calculateDayAvg(stockNameVO, dailyPriceList, 30);
    }

    private void calculateDayAvg(StockNameVO stockNameVO, List<ArrayList<String>> dailyPriceList, Integer dayCount) {
        if (dayCount > dailyPriceList.size()) {
            return;
        }
        BigDecimal totalPrice = new BigDecimal(0);
        DayAvgVO dayAvgVO = dayAvgMap.get(stockNameVO.getStockId());
        if (dayAvgVO == null) {
            dayAvgVO = new DayAvgVO();
            dayAvgVO.getStockNameVO().setStockId(stockNameVO.getStockId());
            dayAvgMap.put(stockNameVO.getStockId(), dayAvgVO);
        }
        int size = dailyPriceList.size();
        for (int i = size - 1; i > (size - dayCount - 1); i--) {
            List<String> day = dailyPriceList.get(i);
            BigDecimal dayEndPrice = new BigDecimal(day.get(2));
            totalPrice = totalPrice.add(dayEndPrice);
        }
        //keep three decimals
        BigDecimal bigDecimal = totalPrice.divide(BigDecimal.valueOf(dayCount), 3, RoundingMode.HALF_UP);
        String stockName = stockNameVO.getStockName();
        String lowerCase = stockName.toLowerCase();
        if (!lowerCase.contains("etf") && !lowerCase.contains("lof")) {
            bigDecimal = bigDecimal.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        if (dayCount == 5) {
            dayAvgVO.setFiveDayPrice(bigDecimal);
        }
        if (dayCount == 10) {
            dayAvgVO.setTenDayAvgPrice(bigDecimal);
        }
        if (dayCount == 20) {
            dayAvgVO.setTwentyDayAvgPrice(bigDecimal);
        }
        if (dayCount == 30) {
            dayAvgVO.setThirtyDayAvgPrice(bigDecimal);
        }
        dayAvgVO.getStockNameVO().setStockName(stockNameVO.getStockName());
        putCache(dayAvgVO);
    }

    private static List<ArrayList<String>> getLastThirtyDaysList(DailyQueryResponseVO dailyQueryResponse, StockNameVO stockNameVO) throws JsonProcessingException {
//        List<ArrayList<String>> daysPrice = daysPriceMap.get(stockNameVO.getStockId());
//        if (daysPrice != null) {
//            return daysPrice;
//        }
        Map<String, Object> dataMap = (Map) dailyQueryResponse.getData().get(stockNameVO.getStockId().toLowerCase());
        Object dayListObj = dataMap.get("day");
        if (Objects.isNull(dayListObj)) {
            dayListObj = dataMap.get("qfqday");
        }
        List<ArrayList<String>> dayList = objectMapper.readValue(objectMapper.writeValueAsString(dayListObj), List.class);
        if (dayList.size() == 0) {
            logger.error("==============ERROR ===== Input Stock Name must be wrong: " + stockNameVO.getStockId());
            logger.error("==============ERROR ====try this url ==https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?_var=kline_dayhfq&param=" + stockNameVO.getStockId() + ",day,,,10,qfq");
            return null;
        }
        extractStockName(stockNameVO, dataMap);
        daysPriceMap.put(stockNameVO.getStockId(), dayList);
        return dayList;
    }

    private static void extractStockName(StockNameVO stockNameVO, Map<String, Object> dataMap) {
        List identifierList = (List) ((Map) dataMap.get("qt")).get(stockNameVO.getStockId());
        Object stockChineseNameObject = identifierList.get(1);
        String stockChineseName = stockChineseNameObject.toString();
        stockNameVO.setStockName(stockChineseName);
    }


    public void realTimeQuery() throws Exception {
        logger.info("======start realTimeQuery=======");
        MailMaps.resetSendEmailMaps();
        for (String stockId : dayAvgMap.keySet()) {
            WebQueryParam webQueryParam = new WebQueryParam();
            webQueryParam.setIdentifier(stockId);
            //at least 2 days to query, because need to check if avg price is between real price and yesterday's price
            webQueryParam.setDaysToQuery(2);

            DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLine(webQueryParam);

            List<ArrayList<String>> twoDayList = getRealPriceList(dailyQueryResponse, stockId);
            if (twoDayList == null || !checkIfTodayData(twoDayList)) {
                continue;
            }

            DayAvgVO dayAvgVO = dayAvgMap.get(stockId);
            StockNameVO stockNameVO = dayAvgVO.getStockNameVO();
            filterExceedPriceStock(twoDayList, exceedFiveDayMap, dayAvgVO.getFiveDayPrice(), stockNameVO, "up5day");
            filterExceedPriceStock(twoDayList, exceedTenDayMap, dayAvgVO.getTenDayAvgPrice(), stockNameVO, "up10day");
            filterExceedPriceStock(twoDayList, exceedTwentyDayMap, dayAvgVO.getTwentyDayAvgPrice(), stockNameVO, "up20day");
            filterDownPriceStock(twoDayList, downFiveDayMap, dayAvgVO.getFiveDayPrice(), stockNameVO, "down5day");
            filterDownPriceStock(twoDayList, downTenDayMap, dayAvgVO.getTenDayAvgPrice(), stockNameVO, "down10day");
            filterDownPriceStock(twoDayList, downTwentyDayMap, dayAvgVO.getTwentyDayAvgPrice(), stockNameVO, "down20day");
        }
        logger.info("============exceedFiveDayMap======={}", exceedFiveDayMap);
        logger.info("============exceedTenDayMap======={}", exceedTenDayMap);
        logger.info("============downFiveDayMap======={}", downFiveDayMap);
        logger.info("============downTenDayMap======={}", downTenDayMap);
        EmailUtil.sendMail();
    }


    public void filterExceedPriceStock(List<ArrayList<String>> twoDayList, Map<String, StockNameVO> dayMap, BigDecimal avgPrice, StockNameVO stockNameVO, String mailListIndex) {
        BigDecimal lastDayPrice = new BigDecimal(((List) twoDayList.get(twoDayList.size() - 2)).get(2).toString());
        BigDecimal realPrice = new BigDecimal(((List) twoDayList.get(twoDayList.size() - 1)).get(2).toString());
        if (lastDayPrice.compareTo(avgPrice) <= 0 && avgPrice.compareTo(realPrice) <= 0) {
            if (dayMap.get(stockNameVO.getStockId()) == null) {
                stockNameVO.setStockName(stockNameVO.getStockName() + "_" + Utils.getHourMinuteTime());
                dayMap.put(stockNameVO.getStockId(), stockNameVO);
                MailMaps.getNamingMap().put(mailListIndex, dayMap);
                MailMaps.needToSendEmail();
            }
        }
    }

    private void filterDownPriceStock(List<ArrayList<String>> twoDayList, Map<String, StockNameVO> dayMap, BigDecimal avgPrice, StockNameVO stockNameVO, String mailListIndex) {
        BigDecimal lastDayPrice = new BigDecimal(((List) twoDayList.get(twoDayList.size() - 2)).get(2).toString());
        BigDecimal realPrice = new BigDecimal(((List) twoDayList.get(twoDayList.size() - 1)).get(2).toString());
        if (realPrice.compareTo(avgPrice) <= 0 && avgPrice.compareTo(lastDayPrice) <= 0) {
            if (dayMap.get(stockNameVO.getStockId()) == null) {
                stockNameVO.setStockName(stockNameVO.getStockName() + "_" + Utils.getHourMinuteTime());
                dayMap.put(stockNameVO.getStockId(), stockNameVO);
                MailMaps.getNamingMap().put(mailListIndex, dayMap);
                MailMaps.needToSendEmail();
            }
        }
    }

    private List<ArrayList<String>> getRealPriceList(DailyQueryResponseVO dailyQueryResponse, String stockId) throws JsonProcessingException {
        if (dailyQueryResponse == null) {
            logger.info("==============getRealPriceList ===== dailyQueryResponse is null.====={}", stockId);
            return null;
        }
        Map<String, Object> dataMap = (Map) dailyQueryResponse.getData().get(stockId.toLowerCase());
        Object dayListObj = dataMap.get("day");
        if (Objects.isNull(dayListObj)) {
            dayListObj = dataMap.get("qfqday");
        }

        List<ArrayList<String>> dayList = objectMapper.readValue(objectMapper.writeValueAsString(dayListObj), List.class);
        if (dayList.size() == 0) {
            logger.error("==============ERROR ===== Input Stock Name must be wrong: " + stockId);
            logger.error("==============ERROR ====try this url ==https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?_var=kline_dayhfq&param=" + stockId + ",day,,,10,qfq");
            return null;
        }
        return dayList;
    }

    private boolean checkIfTodayData(List<ArrayList<String>> dayList) {
        String date = ((List) dayList.get(dayList.size() - 1)).get(0).toString();
        String todayDate = Utils.todayDate();
        //if the date yyyy-MM-dd is equal, it means the market is started!
        if (todayDate.equals(date)) {
            return true;
        }
        return false;
    }


    @CachePut(value = "dayAvgVO", key = "#tele")
    public DayAvgVO putCache(DayAvgVO dayAvgVO) {
        return dayAvgVO;
    }

}
