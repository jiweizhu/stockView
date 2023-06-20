package com.example.notification.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;


/**
 * 发送https请求 工具类
 * 注意:是Https请求,若是Http,请将 HttpsURLConnection ---> HttpURLConnection
 */
public class HttpsClient {


    private static final Logger log = LoggerFactory.getLogger(HttpsClient .class);

    public String doGet(String strHttpsUrl) {
        String result = null;
//        log.info("[doGet] GET请求地址: {} ", strHttpsUrl);
        try {
            BufferedReader      reader = null;
            HttpsURLConnection  httpConnnect = null;
            try {
                //创建连接
                URL httpUrl = new URL(strHttpsUrl);
                httpConnnect = (HttpsURLConnection) httpUrl.openConnection();

                httpConnnect.setRequestProperty("accept","*/*");
                httpConnnect.setRequestProperty("connection","keep-alive");
                httpConnnect.setRequestProperty("user-agent",
                        "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/47.0.2526.106 Safari/537.36");
                //注意：这行代码设置请求方式根本不起作用，我已经测试过了，感觉他只是给后台head中的信息

                httpConnnect.setRequestMethod("GET");                   //设置请求方式
                httpConnnect.connect();                                         //开始连接
//                MimeMessage mimeMessage;
                //读取响应
                reader = new BufferedReader(new InputStreamReader(httpConnnect.getInputStream(), "UTF-8"));
                String lines;
                StringBuffer stringBuffer = new StringBuffer("");
                while ((lines = reader.readLine()) != null) {
                    lines = new String(lines.getBytes());
                    stringBuffer.append(lines);
                }
                result = stringBuffer.toString();
                log.info("[doGet] 请求返回结果：{}" , result);

                reader.close();
                httpConnnect.disconnect();

            } catch (Exception e) {

                log.error("[doGet] 发送get 请求失败: {}", e.getMessage());
                e.printStackTrace();
            } finally {
                if (reader != null) {
                    reader.close();
                }
                if (httpConnnect != null) {
                    httpConnnect.disconnect();
                }
            }
        } catch (Exception e) {
            log.error("[doGet] 发送get 请求失败: {}", e.getMessage());
        }
        return result;
    }



}