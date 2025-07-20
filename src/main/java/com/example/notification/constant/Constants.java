package com.example.notification.constant;

import com.example.notification.util.FileUtil;
import com.example.notification.vo.StockNameVO;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Constants {
    private static final Logger logger = LoggerFactory.getLogger(Constants.class);
    // should set bigger than 9, then it can get 10 days avg value
    public static Integer MARKETDAYCLOSEDJOB_QUERY_PRICE_DAY = 50;

    public static String TOTAL = "total";
    public static String ROWS = "rows";
    public static String CONSTANT_BD = "bd";
    public static String CONSTANT_RANGE_SORT = "range_sort";
    public static String CONSTANT_ETF = "etf";

    // 400 profit>0 & profitGain > 0
    // 300 profit>0 & profitGain < 0
    // 200 profit< 0 & profitGain > 0
    // 100 profit< 0 & profitGain < 0
    public static int PROFIT_TYPE_100 = 100;
    public static int PROFIT_TYPE_200 = 200;
    public static int PROFIT_TYPE_300 = 300;
    public static int PROFIT_TYPE_400 = 400;


    public static String TTM_URL = "TTM_URL";
    public static String PBR_URL = "PBR_URL";


    //green   #00FF00
    public static String GREEN_Color = "#00FF00";

    public static int YangQi = 100;
    public static String ChineseRed_Color = "#FF8888";
    //朱红色(pink) #FF8888 央企100

    public static int GuoQi = 200;
    public static String YELLOW_Color = "#FF9900";
    //yellow #FF9900 国企200

    public static int MinQi = 300;
    public static String GREY_Color = "#C0C0C0";
    //grey   #C0C0C0 民企300


    //sort type number keywords
    public static String YangQiColor = "#FF8888";

    // favorite stock
    public static int FAVORITE_STOCK = 100;

    public static Integer rangeSize = 820;
    public static Integer rangeWkSize = 580;

    public static Integer getRangeWkSize() {
        return rangeWkSize;
    }

    //season
    public static String SEASON_DAY_0331 = "-03-31";
    public static String SEASON_DAY_0630 = "-06-30";
    public static String SEASON_DAY_0930 = "-09-30";
    public static String SEASON_DAY_1231 = "-12-31";

    public static Integer SEASON_ONE_DAY = 331;
    public static Integer SEASON_TWO_DAY = 630;
    public static Integer SEASON_THREE_DAY = 930;
    public static Integer SEASON_FOUR_DAY = 1231;

    static Map<Integer, String> integers = new HashMap<>();
    public static List<Integer> integerLists = new ArrayList<>();

    static {
        integers.put(SEASON_ONE_DAY, SEASON_DAY_0331);
        integers.put(SEASON_TWO_DAY, SEASON_DAY_0630);
        integers.put(SEASON_THREE_DAY, SEASON_DAY_0930);
        integers.put(SEASON_FOUR_DAY, SEASON_DAY_1231);

        integerLists.add(SEASON_ONE_DAY);
        integerLists.add(SEASON_TWO_DAY);
        integerLists.add(SEASON_THREE_DAY);
        integerLists.add(SEASON_FOUR_DAY);
    }

    public static Map<Integer, String> getSeasonDayMap() {
        return integers;
    }

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
