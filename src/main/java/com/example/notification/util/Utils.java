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

    public static String getOneWeekAgeDay() {
        LocalDate today = LocalDate.now();

        LocalDate oneWeekAgo = today.minusWeeks(1);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        String formattedDate = oneWeekAgo.format(formatter);
        return formattedDate;

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
        if (denominator == null) {
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

    public static String getYesterDay() {
        LocalDate today = LocalDate.now();
        LocalDate yesterday = today.minusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String formattedDate = yesterday.format(formatter);
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
