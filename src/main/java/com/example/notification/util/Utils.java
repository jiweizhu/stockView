package com.example.notification.util;

import com.example.notification.vo.StockDailyVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class Utils {
    private static final Logger logger = LoggerFactory.getLogger(Utils.class);
    public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private static final String TODAYDATE = Utils.todayDate();
    private static final String MAIN_INDICATOR = "sh510760_上证综指ETF,sh510300_沪深300ETF,sh510500_中证500ETF,sh512100_中证1000ETF,sz159949_创业板50ETF," + "sh588080_科创板50ETF,sh510190_上证50ETF,sz159915_创业板ETF,sh512910_中证100ETF,sz399306_深证ETF,sz159901_深证100ETF";

    public static String getOneWeekAgeDay() {
        LocalDate today = LocalDate.now();

        LocalDate oneWeekAgo = today.minusWeeks(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String formattedDate = oneWeekAgo.format(formatter);
        return formattedDate;

    }

    public static String getServerIp() {
        InetAddress localHost = null;
        try {
            localHost = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            logger.error("============Fail to get ipAddr=============", e);
        }
        String ipAddress = localHost.getHostAddress();
        if (!Utils.isWinSystem()) {
            return "124.71.19.6";
        }
        return "localhost";
    }

    private static SimpleDateFormat FORMATTER_YYYYMMDD = new SimpleDateFormat("YYYY/MM/dd");

    public static synchronized String getFormat(java.sql.Date day) {
        return FORMATTER_YYYYMMDD.format(day);
    }

    private static SimpleDateFormat FORMATTER_YYYY_MM_DD = new SimpleDateFormat("YYYY-MM-dd");

    public static synchronized String getFormat_YYYY_MM_DD(java.sql.Date day) {
        return FORMATTER_YYYY_MM_DD.format(day);
    }

    public static boolean isWinSystem() {
        String os = System.getenv("OS");
        if (null != os && os.toLowerCase().contains("windows")) {
            return true;
        }
        return false;
    }

    public static boolean checkIfTodayData(StockDailyVO stockDailyVO) {
        String date = Utils.format.format(stockDailyVO.getDay());
        //if the date yyyy-MM-dd is equal, it means the market is started!
        if (TODAYDATE.equals(date)) {
            return true;
        }
        return false;
    }

    public static BigDecimal calculateDayGainPercentage(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || numerator == null || denominator.equals(new BigDecimal(0))) {
            return new BigDecimal(0);
        }
        BigDecimal subtract = numerator.subtract(denominator).multiply(BigDecimal.valueOf(100));
        BigDecimal gainPercentage = subtract.divide(denominator, 2, BigDecimal.ROUND_HALF_UP);
        return gainPercentage;
    }

    public static String todayDate() {
        String today = format.format(new Date());
        return today;
    }

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String getYesterDay() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);

        String formattedDate = yesterday.format(formatter);
        return formattedDate;
    }

    public static String getFormattedDaysBefore(int daysBofore) {
        LocalDate today = LocalDate.now();
        LocalDate ret = today.minusDays(daysBofore);
        String formattedDate = ret.format(formatter);
        return formattedDate;
    }

    public static String getTodayDate() {
        LocalDate today = LocalDate.now();
        String formattedDate = today.format(formatter);
        return formattedDate;
    }


    public static String getHourMinuteTime() {
        Date date = new Date();
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return formatter.format(localDateTime);
    }


    public static BigDecimal calculateDayAvg(String stockName, int index, List<ArrayList<String>> dailyPriceList, Integer dayCount) {
        if (dayCount > dailyPriceList.size()) {
            return BigDecimal.valueOf(0);
        }
        BigDecimal totalPrice = new BigDecimal(0);
        for (int y = 0; y < dayCount; y++) {
            List<String> day = dailyPriceList.get(index - y);
            BigDecimal dayEndPrice = new BigDecimal(day.get(2));
            totalPrice = totalPrice.add(dayEndPrice);
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


    public static List<Integer> analysisFlipDay(List<StockDailyVO> etfPriceList, String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (etfPriceList.size() == 0) return null;
        Integer flipBeginIndex = 0;
        Integer flipEndIndex = 0;
        Integer loopCount = 0;
        Boolean countFlip = Boolean.FALSE;
        Boolean todayKLineUpward = Boolean.TRUE;

        StockDailyVO today = etfPriceList.get(etfPriceList.size() - 1);
        Method getDayGainMethod = today.getClass().getMethod(methodName);
        BigDecimal gainValue = (BigDecimal) getDayGainMethod.invoke(today);
        if (gainValue.compareTo(BigDecimal.ZERO) < 0) {
            //today is downward!
            todayKLineUpward = Boolean.FALSE;
        }
        //set today upwardDay count = 0!
        for (int index = etfPriceList.size() - 1; index > 0; index--) {
            StockDailyVO indexDay = etfPriceList.get(index);
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
        List<Integer> flipDayList = Arrays.asList(flipBeginIndex, flipEndIndex);
        return flipDayList;
    }


}
