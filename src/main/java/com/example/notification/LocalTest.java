package com.example.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class LocalTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public static void main(String[] args) throws JsonProcessingException {

        int index = 101;
       BigDecimal subtract = BigDecimal.valueOf(1.2);
       BigDecimal beforeDayAvgPrice = BigDecimal.valueOf(1.091);
        BigDecimal divide = subtract.divide(beforeDayAvgPrice, 2, RoundingMode.HALF_UP);
        System.out.println(divide);
    }
}
