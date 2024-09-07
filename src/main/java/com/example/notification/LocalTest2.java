package com.example.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class LocalTest2 {


    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void main(String[] args) throws JsonProcessingException, InterruptedException {
        // Initialize Wind connection

        String url = "https://finance.pae.baidu.com/vapi/v2/blocks?pn=0&rn=20&market=ab&typeCode=HY&finClientType=pc";

        RestTemplate restTemplate = new RestTemplate();

        // 创建HttpHeaders对象并设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.add("Accept", "*/*");
        headers.add("Accept-Encoding", "gzip, deflate, br, zstd");
        headers.add("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,de;q=0.6");
        headers.add("Access-Control-Request-Headers", "acs-token");
        headers.add("Access-Control-Request-Method", "GET");
        headers.add("Connection", "keep-alive");
        headers.add("Host", "finance.pae.baidu.com");
        headers.add("Origin", "https://gushitong.baidu.com");
        headers.add("Referer", "https://gushitong.baidu.com/");
        headers.add("Sec-Fetch-Dest", "empty");
        headers.add("Sec-Fetch-Mode", "cors");
        headers.add("Sec-Fetch-Site", "same-site");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/128.0.0.0 Safari/537.36");

        // 创建HttpEntity对象，‌将请求头设置进去
        HttpEntity<String> entity = new HttpEntity<>(headers);

        // 发送GET请求并获取响应
        for (int i = 0; i < 10000; i++) {
            Thread.sleep(50);
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            System.out.println(i + "=======Response Status Code: " + response.getStatusCode()+"========"+response.getBody().length());
        }
    }
}
