package com.example.notification.controller;

import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.service.KLineService;
import com.example.notification.util.EmailUtil;
import com.example.notification.vo.StockNameVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Autowired
    private KLineService kLineService;

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


    @RequestMapping(value = {"/stock/{stockId}"})
    @ResponseBody
    public ResponseEntity stockDataById(@PathVariable String stockId) throws InterruptedException, JsonProcessingException {
        Object body = kLineMarketClosedService.stockJsonData(stockId);
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/import"})
    @ResponseBody
    public String importETF() throws InterruptedException, JsonProcessingException {
        kLineMarketClosedService.importStocks();
        return "ok";
    }

    @RequestMapping(value = {"/avg"})
    @ResponseBody
    public String getAvgPrice() throws InterruptedException, JsonProcessingException {
        kLineMarketClosedService.getDaysPriceOnLineAndStoreInDb();
        return "ok";
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

    @RequestMapping(value = {"/report"})
    @ResponseBody
    public ResponseEntity report() throws Exception {
        Object body = kLineMarketClosedService.report();
        return ResponseEntity.ofNullable(body);
    }

}
