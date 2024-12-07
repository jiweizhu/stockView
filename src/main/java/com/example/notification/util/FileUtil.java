package com.example.notification.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ezhxjwx
 * @date 2022-11-28 18:47
 */
public class FileUtil {

    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);

    public static InputStream inputStreamReader(String filePath) {
        InputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            logger.error("can not find inputStreamReader file");
        }
        return in;
    }

    public static BufferedReader bufferReader(String filePath) {
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            logger.error("can not find bufferReader file");
        }
        return in;
    }

    public static Set<String> readTargetFileStocks(String filePath) {
        Set<String> targetStockSet = new HashSet<>();
        BufferedReader reader = null;
        String line;
        try {
            FileReader fileReader = new FileReader(filePath);
            reader = new BufferedReader(fileReader);
            if(filePath.contains("xls")){
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#")) continue;
                    String stock_id;
                    if (line.toLowerCase().startsWith("s")) {
                        //split by
                        String[] commaSplit = line.split("\\t");
                        stock_id = commaSplit[0].toLowerCase();
                        targetStockSet.add(stock_id);
                    }
                }
            }else {
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("#")) continue;
                    String stock_id;
                    if (line.toLowerCase().startsWith("s")) {
                        //split by
                        String[] commaSplit = line.split("_");
                        stock_id = commaSplit[0].toLowerCase();
                        targetStockSet.add(stock_id);
                    }
                }
            }
            logger.info("Successfully read stock name file !!");
        } catch (IOException e) {
            logger.error("Fail read stock name file !!", e);
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.error("error occurs. ", e);
            }
        }
        return targetStockSet;
    }


}
