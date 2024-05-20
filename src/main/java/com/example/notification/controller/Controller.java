package com.example.notification.controller;

import com.example.notification.service.ETFViewService;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.service.KLineService;
import com.example.notification.util.EmailUtil;
import com.example.notification.vo.StockNameVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.InvocationTargetException;

@RestController
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    @Value("${notification.init.history.price.day}")
    private Integer initHistoryPriceDay;

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Autowired
    private KLineService kLineService;
    @Autowired
    private ETFViewService etfViewService;

    @RequestMapping(value = {"/real", "/index"})
    @ResponseBody
    public String realTimeQuery() throws Exception {
        kLineService.realTimeQuery();
        return "ok";
    }

    @RequestMapping(value = {"/stock/list"})
    @ResponseBody
    public ResponseEntity listAllETFs() throws JsonProcessingException {
        Object body = kLineMarketClosedService.listEtfs();
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/etfsCurveView"})
    @ResponseBody
    public ResponseEntity etfsCurveView() throws JsonProcessingException {
        Object body = kLineMarketClosedService.etfsCurveView();
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/stock/{stockId}"})
    @ResponseBody
    public ResponseEntity stockDataById(@PathVariable String stockId) throws InterruptedException, JsonProcessingException {
        Object body = kLineMarketClosedService.stockJsonData(stockId);
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/etfs/{num}"})
    @ResponseBody
    public ResponseEntity findAllEtfSort(@PathVariable String num) throws InterruptedException, JsonProcessingException {
        logger.info("Enter method findAllEtfSort===="+num);
        Object body = etfViewService.findAllEtfSortView(num);
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/etfs/table1"})
    @ResponseBody
    public ResponseEntity findAllEtfsForTable_1() throws JsonProcessingException {
        logger.info("Enter method findAllEtfsForTable_1====");
        Object body = etfViewService.findAllEtfsForTable(1);
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/etfs/table2"})
    @ResponseBody
    public ResponseEntity findAllEtfsForTable_2() throws JsonProcessingException {
        logger.info("Enter method findAllEtfsForTable_2====");
        Object body = etfViewService.findAllEtfsForTable(2);
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/etfs/five/noflip"})
    @ResponseBody
    public ResponseEntity fiveDayAdjusted() throws InterruptedException, JsonProcessingException {
        Object body = etfViewService.fiveDayAdjustedView();
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/etfs/ten"})
    @ResponseBody
    public ResponseEntity tenDayAdjusted() throws InterruptedException, JsonProcessingException {
        Object body = etfViewService.tenDayAdjustedView();
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/import"})
    @ResponseBody
    public String importETF() throws InterruptedException, JsonProcessingException {
        logger.info("Enter method importETF====");
        String result = kLineMarketClosedService.importStocks();
        return result;
    }

    @RequestMapping(value = {"/generateReportEveryDay"})
    @ResponseBody
    public String generateReportEveryDay() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JsonProcessingException, InterruptedException {
        logger.info("Enter method generateReportEveryDay====");
        etfViewService.generateReportEveryDay();
        return "ok";
    }

    @RequestMapping(value = {"/storeHistoryData"})
    @ResponseBody
    public String storeHistoryData() throws InterruptedException, JsonProcessingException {
        String historyPriceOnLineAndStoreInDb = kLineMarketClosedService.getHistoryPriceOnLineAndStoreInDb(initHistoryPriceDay);
        return historyPriceOnLineAndStoreInDb;
    }

    @RequestMapping(value = {"/mail"})
    @ResponseBody
    public String sendEmailTest() throws Exception {
        StockNameVO stockNameVO = new StockNameVO();
        stockNameVO.setStockId("testId");
        stockNameVO.setStockName("testStockName");
        EmailUtil.sendMailSingle(stockNameVO);
        return "ok";
    }

    @RequestMapping(value = {"/handleEtfsAvg"})
    @ResponseBody
    public ResponseEntity handleEtfsAvg() throws Exception {
        Object body = kLineMarketClosedService.handleStocksFlipDaysAndGainReport();
        return ResponseEntity.ofNullable(body);
    }

}
