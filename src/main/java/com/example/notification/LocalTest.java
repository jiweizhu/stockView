package com.example.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class LocalTest {


    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void main(String[] args) throws JsonProcessingException, InterruptedException {
        // Initialize Wind connection

        String url = "https://www.csindex.com.cn/csindex-home/perf/index-perf?indexCode=931811&startDate=20190829&endDate=20240828";
        LocalDate yyyymmdd = LocalDate.parse("20141008", DateTimeFormatter.ofPattern("yyyyMMdd"));
        System.out.println("yyyymmdd = " + yyyymmdd);
    }
}
