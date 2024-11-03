package com.example.notification.cnIndex.service;

import com.example.notification.http.RestRequest;
import com.example.notification.repository.CNDailyDao;
import com.example.notification.repository.CNIndicatorDao;
import com.example.notification.util.Utils;
import com.example.notification.vo.CNDailyVO;
import com.example.notification.vo.CNIndicatorVO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static com.example.notification.constant.Constants.getRangeSize;

@Service
public class CNService {
    private static final Logger logger = LoggerFactory.getLogger(CNService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private CNIndicatorDao cnIndicatorDao;

    @Autowired
    private RestRequest restRequest;

    @Autowired
    private ThreadPoolTaskExecutor executorService;

    @Autowired
    private CNDailyDao cnDailyDao;

    public Object saveIndicatorsEveryDay(String stockId) {
        ArrayList<String[]> result = new ArrayList<>();
        logger.info("enter CNService saveIndicatorsEveryDay =============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }
        return result;
    }


    public void calculateAvgPrice(List<String> idList) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        for (String indexId : idList) {
            CNIndicatorVO cnIndicatorVO = cnIndicatorDao.findById(indexId).orElse(new CNIndicatorVO());
            String stockName = cnIndicatorVO.getIndexNameCn();
            if (stockName == null) {
                continue;
            }
            Pageable pageable = PageRequest.of(0, 60, Sort.by(Sort.Direction.DESC, "tradeDate"));
            List<CNDailyVO> etfPriceList = cnDailyDao.findByIndexCode(indexId, pageable).stream().sorted(Comparator.comparing(CNDailyVO::getTradeDate)).collect(Collectors.toList());
            if (etfPriceList.isEmpty()) {
                logger.error("=====failed=======can not find daily data of ={}={}====", indexId, stockName);
                break;
            }
            List<Integer> flipDayFive = analysisFlipDay(etfPriceList, "getDayGainOfFive");
            List<Integer> flipDayTen = analysisFlipDay(etfPriceList, "getDayGainOfTen");
            setUpwardDaysAndGain(etfPriceList, flipDayFive.get(0), flipDayFive.get(1), cnIndicatorVO, "Five");
            setUpwardDaysAndGain(etfPriceList, flipDayTen.get(0), flipDayTen.get(1), cnIndicatorVO, "Ten");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            cnIndicatorVO.setLastUpdatedTime(timestamp);
            cnIndicatorDao.save(cnIndicatorVO);
        }
    }

    public static List<Integer> analysisFlipDay(List<CNDailyVO> etfPriceList, String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (etfPriceList.isEmpty()) return null;
        int flipBeginIndex = 0;
        int flipEndIndex = 0;
        int loopCount = 0;
        Boolean countFlip = Boolean.FALSE;
        Boolean todayKLineUpward = Boolean.TRUE;

        CNDailyVO today = etfPriceList.get(etfPriceList.size() - 1);
        Method getDayGainMethod = today.getClass().getMethod(methodName);
        BigDecimal gainValue = (BigDecimal) getDayGainMethod.invoke(today);
        if (gainValue.compareTo(BigDecimal.ZERO) < 0) {
            //today is downward!
            todayKLineUpward = Boolean.FALSE;
        }
        //set today upwardDay count = 0!
        for (int index = etfPriceList.size() - 1; index > 0; index--) {
            CNDailyVO indexDay = etfPriceList.get(index);
            loopCount++;
            BigDecimal dayGainPercent = (BigDecimal) getDayGainMethod.invoke(indexDay);
            if (dayGainPercent == null) {
                flipBeginIndex = loopCount;
                break;
            }
            boolean indexDayPositive = dayGainPercent.compareTo(BigDecimal.ZERO) >= 0;
//            if (countFlip == Boolean.TRUE) {
//                indexDayPositive = dayGainPercent.compareTo(BigDecimal.ZERO) > 0;
//            }
            if (!todayKLineUpward.equals(indexDayPositive)) {
                //start to find adjustment days, turn the opposite
                if (countFlip.equals(Boolean.FALSE)) {
                    flipBeginIndex = loopCount - 1;
                }
                if (countFlip.equals(Boolean.TRUE)) {
                    flipEndIndex = loopCount - 1;
                    break;
                }
                countFlip = Boolean.TRUE;
                if (todayKLineUpward.equals(Boolean.TRUE)) {
                    todayKLineUpward = Boolean.FALSE;
                } else {
                    todayKLineUpward = Boolean.TRUE;
                }
            }
        }
        return Arrays.asList(flipBeginIndex, flipEndIndex);
    }


    private void setUpwardDaysAndGain(List<CNDailyVO> etfPriceList, Integer flipBeginIndex, Integer flipEndIndex, CNIndicatorVO cnIndicatorVO, String dayIdentify) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        logger.debug("Enter setUpwardDaysAndGain = " + etfPriceList.size());
        int listSize = etfPriceList.size();
        CNDailyVO today = etfPriceList.get(etfPriceList.size() - 1);

        CNDailyVO flipDay = etfPriceList.get(listSize - flipBeginIndex - 1);
        BigDecimal gainPercent = getGainPercent(today, flipDay, dayIdentify);

        CNDailyVO flipEndDay = etfPriceList.get(listSize - flipEndIndex - 1);
        BigDecimal gainPercentFlip = getGainPercent(flipDay, flipEndDay, dayIdentify);

        String methodName = "getDayGainOf" + dayIdentify;
        Method getDayGainMethod = today.getClass().getMethod(methodName);
        BigDecimal gainValue = (BigDecimal) getDayGainMethod.invoke(today);
        boolean todayUpward = gainValue.compareTo(BigDecimal.ZERO) >= 0;

        int flipUpwardDays = flipBeginIndex - flipEndIndex;
        if (!todayUpward) {
            flipBeginIndex = -flipBeginIndex;
            flipUpwardDays = -flipUpwardDays;
        }

        Class<? extends CNIndicatorVO> aClass = cnIndicatorVO.getClass();
        aClass.getMethod("setUpwardDays" + dayIdentify, Integer.class).invoke(cnIndicatorVO, flipBeginIndex);
        aClass.getMethod("setGainPercent" + dayIdentify, BigDecimal.class).invoke(cnIndicatorVO, gainPercent);
        aClass.getMethod("setFlipUpwardDays" + dayIdentify, Integer.class).invoke(cnIndicatorVO, flipUpwardDays);
        aClass.getMethod("setFlipGainPercent" + dayIdentify, BigDecimal.class).invoke(cnIndicatorVO, gainPercentFlip);
        aClass.getMethod("setFlipDay" + dayIdentify, Date.class).invoke(cnIndicatorVO, Date.valueOf(LocalDate.parse(flipDay.getTradeDate(), DateTimeFormatter.ofPattern("yyyyMMdd"))));
        aClass.getMethod("setFlipEndDay" + dayIdentify, Date.class).invoke(cnIndicatorVO, Date.valueOf(LocalDate.parse(flipDay.getTradeDate(), DateTimeFormatter.ofPattern("yyyyMMdd"))));
    }

    private BigDecimal getGainPercent(CNDailyVO newPrice, CNDailyVO oldPrice, String dayIdentify) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<? extends CNDailyVO> aClass = newPrice.getClass();
        BigDecimal newPriceValue = ((BigDecimal) aClass.getMethod("getDayAvg" + dayIdentify).invoke(newPrice));
        BigDecimal oldPriceValue = ((BigDecimal) aClass.getMethod("getDayAvg" + dayIdentify).invoke(oldPrice));
        if (newPriceValue == null || oldPriceValue == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtract = newPriceValue.subtract(oldPriceValue).multiply(BigDecimal.valueOf(100));
        BigDecimal gainPercentage = subtract.divide(oldPrice.getDayAvgFive(), 2, BigDecimal.ROUND_HALF_UP);
        return gainPercentage;
    }

    public Object queryIndicator(String stockId) {
        ArrayList<String[]> result = new ArrayList<>();
        logger.info("enter CNService saveIndicatorsEveryDay =============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }
        return result;
    }

    @Value("${notification.cn.indicators.startDay}")
    private String startDay;

    public void initDailyPrice(List<String> idList) {
        logger.info("enter CNService initDailyPrice =============");
        List<Callable<Void>> tasks = new ArrayList<>();

        for (String id : idList) {
            tasks.add(() -> {
                List<CNDailyVO> kLineList = restRequest.queryCNIndustriesKline(id, startDay);

                // save new in db
                Set<String> dayLinesInDbSet = new HashSet<>(cnDailyDao.findAllTradeDayById(id));

                //===============
                BigDecimal beforeDay_FiveDayAvgPrice = null;
                BigDecimal beforeDay_TenDayAvgPrice = null;
                int size = kLineList.size();
                logger.info("==CN initDailyPrice=====got {} days, == lastest day is {}", size, kLineList.get(size - 1));
                for (int i = 0; i < size; i++) {
                    CNDailyVO dailyVO = kLineList.get(i);

                    String stockName = dailyVO.getIndexNameCn();
                    if (stockName == null || i < 9) {
                        //i < 9 just ignore to store avg price = null
                        continue;
                    }

                    //calculate 5 day avg, need at least 5 day price list
                    BigDecimal fiveDayAvgPrice = calculateDayAvg(stockName, i, kLineList, 5);
                    dailyVO.setDayAvgFive(fiveDayAvgPrice);
                    BigDecimal dayGainFive = Utils.calculateDayGainPercentage(fiveDayAvgPrice, beforeDay_FiveDayAvgPrice);
                    dailyVO.setDayGainOfFive(dayGainFive);
                    beforeDay_FiveDayAvgPrice = fiveDayAvgPrice;

                    //get 10 day avg
                    BigDecimal tenDayAvgPrice = calculateDayAvg(stockName, i, kLineList, 10);
                    dailyVO.setDayAvgTen(tenDayAvgPrice);
                    BigDecimal dayGainTen = Utils.calculateDayGainPercentage(tenDayAvgPrice, beforeDay_TenDayAvgPrice);
                    dailyVO.setDayGainOfTen(dayGainTen);
                    beforeDay_TenDayAvgPrice = tenDayAvgPrice;

                    if (dayLinesInDbSet.contains(dailyVO.getTradeDate())) {
                        //history day already stored
                        continue;
                    }
                    cnDailyDao.save(dailyVO);
                }
                return null;
            });
        }

        try {
            List<Future<Void>> futures = executorService.getThreadPoolExecutor().invokeAll(tasks);
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    logger.error("Error executing task", e.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Task execution interrupted", e);
        }

    }

    public static BigDecimal calculateDayAvg(String stockName, int index, List<CNDailyVO> dailyPriceList, Integer dayCount) {
        if (dayCount > dailyPriceList.size()) {
            return BigDecimal.valueOf(0);
        }
        BigDecimal totalPrice = new BigDecimal(0);
        for (int y = 0; y < dayCount; y++) {
            BigDecimal close = dailyPriceList.get(index - y).getClose();
            totalPrice = totalPrice.add(close);
        }
        //keep three decimals
        BigDecimal avgPrice = totalPrice.divide(BigDecimal.valueOf(dayCount), 3, RoundingMode.HALF_UP);
        String lowerCase = stockName.toLowerCase();
        if (!lowerCase.contains("etf") && !lowerCase.contains("lof")) {
            //if it is not etf or lof, it is stock price
            avgPrice = avgPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return avgPrice;
    }

    public Object queryIndicatorDailyData(String indexId) {
        ArrayList<String[]> result = new ArrayList<>();
        logger.info("enter cn queryIndicatorDailyData indexId =============" + indexId);
        if (indexId.contains("_")) {
            indexId = indexId.split("_")[0];
        }
        Integer rangeSize = getRangeSize();
        List<CNDailyVO> voList = cnDailyDao.findByIndexStockIdOrderByDay(indexId, rangeSize).stream().sorted(Comparator.comparing(CNDailyVO::getTradeDate)).toList();
        if (!voList.isEmpty()) {
            if (voList.get(voList.size() - 1).getOpen() == null) {
                for (CNDailyVO vo : voList) {
                    String[] strings = new String[7];
                    strings[0] = vo.getTradeDate();
                    String closeVal = vo.getClose().toString();
                    strings[1] = closeVal;
                    strings[2] = closeVal;
                    strings[3] = closeVal;
                    strings[4] = closeVal;
                    result.add(strings);
                }
            } else {
                for (CNDailyVO vo : voList) {
                    String[] strings = new String[7];
                    strings[0] = vo.getTradeDate();
                    strings[1] = vo.getOpen().toString();
                    strings[2] = vo.getClose().toString();
                    strings[3] = vo.getHigh().toString();
                    strings[4] = vo.getLow().toString();
                    result.add(strings);
                }
            }
            return result;
        }
        return result;
    }
}
