package com.example.notification.constant;

import com.example.notification.util.FileUtil;
import com.example.notification.vo.StockNameVO;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Constants {
    private static final Logger logger = LoggerFactory.getLogger(Constants.class);
    // should set bigger than 9, then it can get 10 days avg value
    public static Integer MARKETDAYCLOSEDJOB_QUERY_PRICE_DAY = 15;

    public static String TOTAL = "total";
    public static String ROWS = "rows";
    public static String CONSTANT_BD = "bd";
    public static String CONSTANT_RANGE_SORT = "range_sort";
    public static String CONSTANT_ETF = "etf";

    //green   #00FF00

    public static int YangQi = 100;
    public static String YangQi_Color = "#FF8888";
    //朱红色(pink) #FF8888 央企100

    public static int GuoQi = 200;
    public static String GuoQi_Color = "#FF9900";
    //yellow #FF9900 国企200

    public static int MinQi = 300;
    public static String MinQi_Color = "#C0C0C0";
    //grey   #C0C0C0 民企300

    // favorite stock
    public static int FAVORITE_STOCK = 100;

    public static Integer rangeSize = 700;

    public static Integer getRangeSize() {
        return rangeSize;
    }

    @Getter
    private static String rangeSortDay;

    public static void setRangeSortDay(String rangeSortDay) {
        Constants.rangeSortDay = rangeSortDay;
    }


    public static List<StockNameVO> getMainBoard300(String path) {
        String line;
        List<StockNameVO> retList = new ArrayList<>();
        try {
            BufferedReader reader = FileUtil.bufferReader(path);
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                if (line.toLowerCase().startsWith("s") && !line.startsWith("#")) {
                    String[] split = line.split("_");
                    retList.add(new StockNameVO(split[0]));
                }
            }
        } catch (IOException exception) {
            logger.error("Error : ", exception);
        }
        return retList;
    }

    public static List<String> getImportFileList(String path) {
        String line;
        List<String> retList = new ArrayList<>();
        try {
            BufferedReader reader = FileUtil.bufferReader(path);
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                retList.add(line);
            }
        } catch (IOException exception) {
            logger.error("Error : ", exception);
        }
        return retList;
    }


}
