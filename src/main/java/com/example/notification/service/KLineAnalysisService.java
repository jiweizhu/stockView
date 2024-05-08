package com.example.notification.service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class KLineAnalysisService {
    private static final Logger logger = LoggerFactory.getLogger(KLineAnalysisService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    //analysis if 5 dayLine begins to raise?
    public void calculateAvg() {
    }

}
