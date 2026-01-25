package com.example.notification.util;

import com.example.notification.baidu.service.BaiduInfoService;
import com.example.notification.baidu.service.ValuationService;
import com.example.notification.easymoney.EasyMoneyService;
import com.example.notification.repository.BdIndicatorDao;
import com.example.notification.service.ETFViewService;
import com.example.notification.service.IntraDayService;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.service.KLineService;
import com.example.notification.vo.BdIndicatorVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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

    @Autowired
    private EasyMoneyService easymoneyService;

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


    //   every updateEveryWeek
    @Scheduled(cron = "0 0 9 * * 6")
    public void updateEveryWeek() {
        try {
            logger.info("====cron==start updateEveryWeek=====");
            kLineMarketClosedService.deleteWkHistoryData(2);
            baiduInfoService.updateBdIndicatorWkFromNetAndStoreWeek(false);

            //for stock from Tencent
            kLineMarketClosedService.getWeekHistoryPriceAndStoreInDb(10);

            baiduInfoService.calculateBdIndicatorDropRange();

            //api is not allowed to query data from bd.
//            baiduInfoService.updateStockBasicDataFromBd();

            baiduInfoService.queryBaiduIncomeDataFromNetForAllStocks();

            baiduInfoService.updateFinancialReportSum();

        } catch (Exception e) {
            logger.error("==== Timer run error! ===== Detail is: ", e);
        }
        logger.info("====cron==end updateEveryWeek=====");
    }


    @Autowired
    private ValuationService valuationService;

    //   every updateEvery Friday
    @Scheduled(cron = "0 0 16 * * 5")
    public void updateEveryFriday() {
        try {
            logger.info("====cron==start updateEveryFriday=====");
            List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
            if (Utils.isWinSystem()) {
                stockIdsAndIndicatorId = new ArrayList<>();
                BdIndicatorVO e = new BdIndicatorVO("730200");
                e.setStockIds("sz000063");
                e.setStockIds("sh600498");
                stockIdsAndIndicatorId.add(e);
            }

            easymoneyService.updateBandDailyDet();

            valuationService.getFromBdAndUpdateIndicatorPE(stockIdsAndIndicatorId);
            valuationService.getFromBdAndUpdateIndicatorPBR(stockIdsAndIndicatorId);
            valuationService.getFromBdAndUpdateIndicatorPCF(stockIdsAndIndicatorId);
        } catch (Exception e) {
            logger.error("==== Timer run error! ===== Detail is: ", e);
        }
        logger.info("====cron==end updateEveryFriday=====");
    }


    @Autowired
    private BdIndicatorDao bdIndicatorDao;

    //for temp
    @Scheduled(cron = "0 59 3 * * ?")
    public void fixNull() {
        try {
            logger.info("====cron==start fixNull=====");
            valuationService.fixNullTtm();
            valuationService.updateStockPercentile();
//todo
            //think not to fix null, and update percentile by first data!
            //because some stock only has last 400 ttm rows, some has 500!

        } catch (Exception e) {
            logger.error("==== Timer run error! ===== Detail is: ", e);
        }
        logger.info("====cron==end fixNull=====");
    }


    @Scheduled(cron = "0 10 15 * * ?")
    public void generateReportWhenMarketClose() {
        try {
            logger.info("Start cron job generateReportEveryMarketDay=====");
            baiduInfoService.updateZ1ToToday();

            logger.info("End cron job deleteDayHistoryData=====");
            Object body = kLineMarketClosedService.deleteDayHistoryData();
            etfViewService.generateReportEveryDay();


        } catch (Exception e) {
            logger.error("==== Timer run error! ===== Detail is: ", e);
        }
    }

    @Scheduled(cron = "0 20 15 * * ?")
    public void generateBaiduInfo() {
        try {
            logger.info("Start cron job baiduInfoService.getFromNetAndStore=====");
            baiduInfoService.getFromNetAndStoreDay(100);
            logger.info("End cron job baiduInfoService.getFromNetAndStore=====");

            logger.info("Start cron job baiduInfoService.calculateIndicatorsAvg=====");
            baiduInfoService.calculateIndicatorsAvg();
            logger.info("End cron job baiduInfoService.calculateIndicatorsAvg=====");
        } catch (Exception e) {
            logger.error("==== Timer run error! ===== Detail is: ", e);
        }
    }


}
