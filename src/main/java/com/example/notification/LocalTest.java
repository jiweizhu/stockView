package com.example.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

public class LocalTest {


    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void main(String[] args) throws JsonProcessingException {
        SimpleDateFormat formatter_yyyy_mm_day = new SimpleDateFormat("YYYY-MM-dd");
        LocalDate localDate = LocalDate.now().minusDays(5);
        String format = formatter_yyyy_mm_day.format((new Date(localDate.toEpochDay())));

        System.out.println("s = " + format);
    }
}
