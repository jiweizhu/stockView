package com.example.notification;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class LocalTest {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    public static void main(String[] args) throws JsonProcessingException {

        String str = "[[\"2023-07-25\",\"0.647\",\"0.656\",\"0.656\",\"0.645\",\"10550268.000\"],[\"2023-07-26\",\"0.655\",\"0.656\",\"0.658\",\"0.650\",\"8214724.000\"],[\"2023-07-27\",\"0.662\",\"0.662\",\"0.669\",\"0.657\",\"8334431.000\"],[\"2023-07-28\",\"0.660\",\"0.690\",\"0.693\",\"0.659\",\"10847854.000\"],[\"2023-07-31\",\"0.706\",\"0.703\",\"0.722\",\"0.699\",\"12816318.000\"]]";


        List<ArrayList<String>> list = objectMapper.readValue(str, List.class);
        System.out.println("============size====="+list.size());

        double totalPrice = 0;
        for (int i = 0; i < list.size(); i++) {
            totalPrice = totalPrice +  Double.valueOf(list.get(i).get(2));
        }
        System.out.println("============totalPrice====="+totalPrice);

    }
}
