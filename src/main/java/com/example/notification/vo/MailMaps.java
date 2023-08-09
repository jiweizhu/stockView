package com.example.notification.vo;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class MailMaps {


    public static Map<String, Map<String, StockNameVO>> namingMap = new HashMap<>();

    public static Map<String, Map<String, StockNameVO>> getNamingMap() {
        return namingMap;
    }

    public static void setNamingMap(Map<String, Map<String, StockNameVO>> namingMap) {
        MailMaps.namingMap = namingMap;
    }

    static {
        namingMap.put("up5day", null);
        namingMap.put("up10day", null);
        namingMap.put("up20day", null);
        namingMap.put("down5day", null);
        namingMap.put("down10day", null);
        namingMap.put("down20day", null);
    }

    private static Boolean ifNeedToSend = Boolean.FALSE;

    public static Boolean getIfNeedToSend() {
        return ifNeedToSend;
    }

    public static void needToSendEmail() {
        ifNeedToSend = Boolean.TRUE;
    }


    public static void resetSendEmailMaps() {
        ifNeedToSend = Boolean.FALSE;
        namingMap.forEach((k, v) -> {
            namingMap.put(k, null);
        });
    }
}
