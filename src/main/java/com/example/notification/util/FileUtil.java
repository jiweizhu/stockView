package com.example.notification.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

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

    public static Reader bufferReader(String filePath) {
        Reader in = null;
        try {
            in = new BufferedReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            logger.error("can not find bufferReader file");
        }
        return in;
    }

}
