package com.example.notification.util;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Utils {

    public static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    private static SimpleDateFormat timeFormat = new SimpleDateFormat("HH:MM");

    public static boolean isWinSystem() {
        String os = System.getenv("OS");
        if (null != os && os.toLowerCase().contains("windows")) {
            return true;
        }
        return false;
    }

    public static String todayDate(){
        String today = format.format(new Date());
        return today;
    }

    public static String getHourMinuteTime(){
        Date date = new Date();
        Instant instant = date.toInstant();
        ZoneId zone = ZoneId.systemDefault();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        LocalDateTime localDateTime = LocalDateTime.ofInstant(instant, zone);
        return formatter.format(localDateTime);
    }


}
