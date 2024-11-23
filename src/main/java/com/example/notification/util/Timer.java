package com.example.notification.util;

import com.example.notification.baidu.service.BaiduInfoService;
import com.example.notification.service.ETFViewService;
import com.example.notification.service.IntraDayService;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.service.KLineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Timer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private KLineService kLineService;

    @Autowired
    private ETFViewService etfViewService;

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Autowired
    private BaiduInfoService baiduInfoService;

    @Autowired
    private IntraDayService intraDayService;

//    @Scheduled(cron = "0 31 9 ? * MON-FRI")
//    public void clearIntradayPriceBeforeOpeningMarket() {
//        logger.info("========= cron exec clearIntradayPriceBeforeOpeningMarket =======");
//        intraDayService.clearTodayIntraPrice();
//    }

//    @Scheduled(cron = "5 */1 9,10,11,13,14 ? * MON-FRI")
//    public Object getPriceByminute() throws ParseException {
//        logger.info("========= cron exec getPriceByminute =======");
//        Object list = intraDayService.getPriceByminute();
//        return list;
//    }
//
//    @Scheduled(cron = "5 * 15 ? * *")
//    public Object getPriceByminute1() throws ParseException {
//        logger.info("========= cron exec getPriceByminute =======");
//        Object list = intraDayService.getPriceByminute();
//        return list;
//    }

    //   every day in 8:00
//    @Scheduled(cron = "0 0 8 * * ?")
//    public void eightHourQuery() {
//        try {
//            logger.info("Start eightHourQuery=====");
//            // also need to clear the upTenDayList, meaning that the notification email is sent today
//            kLineService.getAvgPrice();
//
//        } catch (Exception e) {
//            logger.error("==== Timer run error! ===== Detail is: ", e);
//        }
//    }

//    @Scheduled(cron = "0 10 15 * * *")
//    public void generateReportWhenMarketClose() {
//        try {
//            logger.info("Start cron job baiduInfoService.calculateIndicatorsAvg=====");
//            baiduInfoService.calculateIndicatorsAvg();
//            logger.info("End cron job baiduInfoService.calculateIndicatorsAvg=====");
//
//            logger.info("Start cron job generateReportEveryMarketDay=====");
//            Object body = kLineMarketClosedService.delete_HistoryData();
//            kLineMarketClosedService.getWeekHistoryPriceAndStoreInDb(2);
//            etfViewService.generateReportEveryDay();
//
//            //2 delete intraday_price data before one week
//            String oneWeekAgeDay = Utils.getOneWeekAgeDay();
//            intraDayService.removeOneWeekAgoData(oneWeekAgeDay);
//
//        } catch (Exception e) {
//            logger.error("==== Timer run error! ===== Detail is: ", e);
//        }
//    }


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
