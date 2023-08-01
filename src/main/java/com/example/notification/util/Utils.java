package com.example.notification.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Utils {

    private static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");

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


}
