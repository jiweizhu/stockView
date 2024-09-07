package com.example.notification.util;

import com.example.notification.vo.StockDailyVO;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Utils {

    public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private static final String TODAYDATE = Utils.todayDate();
    private static final String MAIN_INDICATOR =
            "sh510760_上证综指ETF,sh510300_沪深300ETF,sh510500_中证500ETF,sh512100_中证1000ETF,sz159949_创业板50ETF,"
          + "sh588080_科创板50ETF,sh510190_上证50ETF,sz159915_创业板ETF,sh512910_中证100ETF,sz399306_深证ETF,sz159901_深证100ETF";

    public static String getOneWeekAgeDay() {
        LocalDate today = LocalDate.now();

        LocalDate oneWeekAgo = today.minusWeeks(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String formattedDate = oneWeekAgo.format(formatter);
        return formattedDate;

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


}
