package com.example.notification.util;

import com.example.notification.service.ETFViewService;
import com.example.notification.service.KLineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class Timer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KLineService kLineService;

    @Autowired
    private ETFViewService etfViewService;

//   every day in 8:00
    @Scheduled(cron = "0 0 8 * * ?")
    public void eightHourQuery() {
        try {
            logger.info("Start eightHourQuery=====");
            // also need to clear the upTenDayList, meaning that the notification email is sent today
            kLineService.getAvgPrice();

        } catch (Exception e) {
            logger.error("==== Timer run error! ===== Detail is: ", e);
        }
    }

    @Scheduled(cron = "0 10 15 * * ?")
    public void generateReportEveryMarketDay() {
        try {
            logger.info("Start generateReportEveryMarketDay=====");
            etfViewService.generateReportEveryDay();
        } catch (Exception e) {
            logger.error("==== Timer run error! ===== Detail is: ", e);
        }
    }



    //  real time query, every 15min
//    @Scheduled(cron = "30 0/15 9-15 * * ?")
    public void realTimeQuery() {
        try {
            logger.info("===== Start realTimeQuery ====");
            kLineService.realTimeQuery();
        } catch (Exception e) {
            logger.error("==== Timer run error! ===== Detail is: ", e);
        }
    }



}
