package com.example.notification.util;

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

//   every day in 8:00
    @Scheduled(cron = "0 0 8 * * ?")
    public void eightHourQuery() {
        try {
            logger.info("Start eightHourQuery=====");
            // also need to clear the upTenDayList, meaning that the notification email is sent today
            KLineService.clearCollect();
            kLineService.getAvgPrice();

        } catch (Exception e) {
            logger.error("==== Timer run error! ===== Detail is: ", e);
        }
    }


    //  real time query, every 15min
    @Scheduled(cron = "30 0/15 9-15 * * ?")
    public void realTimeQuery() {
        try {
            logger.info("===== Start realTimeQuery ====");
            kLineService.realTimeQuery();
        } catch (Exception e) {
            logger.error("==== Timer run error! ===== Detail is: ", e);
        }
    }



}
