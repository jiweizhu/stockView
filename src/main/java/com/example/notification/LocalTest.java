package com.example.notification;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class LocalTest {

        public static void main(String[] args) {
            // 设置目标URL
            String url = "https://gushitong.baidu.com/stock/ab-600487";

            try {
                // 使用Jsoup连接并获取HTML页面
                Document doc = Jsoup.connect(url).get();

                // 提取股票信息
                String stockCode = "600487"; // 股票代码
                String stockName = doc.select("h1.stock-name").text();
                double stockPrice = Double.parseDouble(doc.select(".stock-price").text().replace("¥", "").trim());
                double peRatio = Double.parseDouble(doc.select(".pe-ratio").text().replace("PE:", "").trim());
                double dividendYield = Double.parseDouble(doc.select(".dividend-yield").text().replace("%", "").trim());

                // 存储数据到数据库
//                saveStockData(stockCode, stockName, stockPrice, peRatio, dividendYield);

            } catch (IOException e) {
                System.err.println("获取页面失败: " + e.getMessage());
            }
        }
    }

