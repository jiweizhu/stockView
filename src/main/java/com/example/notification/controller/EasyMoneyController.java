package com.example.notification.controller;

import com.example.notification.easymoney.EasyMoneyService;
import com.example.notification.http.RestRequestFromDongCai;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class EasyMoneyController {

    private static final Logger logger = LoggerFactory.getLogger(EasyMoneyController.class);


    @Autowired
    private EasyMoneyService easymoneyService;
    @Autowired
    private RestRequestFromDongCai restRequestFromDongCai;

    @RequestMapping(value = {"/queryValueIndustryDet"})
    @ResponseBody
    public String queryValueIndustryDet() {
        logger.info("Enter EasyMoneyController queryValueIndustryDet ====");
        restRequestFromDongCai.queryValueIndustryDet("016071");
        return "result";
    }

    @RequestMapping(value = {"/updateBandDailyDet"})
    @ResponseBody
    public String updateBandDailyDet()  {
        logger.info("Enter EasyMoneyController updateBandDailyDet ====");
        easymoneyService.updateBandDailyDet();
        return "OK";
    }


    @RequestMapping(value = {"/em/bandView"})
    @ResponseBody
    public ResponseEntity eMoneyNetView() {
        logger.info("Enter EasyMoneyController eMoneyNetView ====");
        Object body = easymoneyService.eMoneyNetView();
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/em/band/{stockId}"})
    @ResponseBody
    public ResponseEntity stockDataById(@PathVariable String stockId) {
        logger.info("Enter EasyMoneyController stockDataById ====");
        Object body = easymoneyService.getStockJsonDataDay(stockId);
        return ResponseEntity.ofNullable(body);
    }
}
