package com.example.notification.constant;

import com.example.notification.util.FileUtil;
import com.example.notification.vo.StockNameVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Constants {
    private static final Logger logger = LoggerFactory.getLogger(Constants.class);
    // should set bigger than 9, then it can get 10 days avg value
    public static Integer MARKETDAYCLOSEDJOB_QUERY_PRICE_DAY= 15;

    public static String TOTAL= "total";
    public static String ROWS= "rows";


    public static List<StockNameVO> getMainBoard300(String path){
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
        }catch (IOException exception){
            logger.error("Error : ",exception);
        }
        return retList;
    }
}
