package com.example.notification.controller;

import com.example.notification.easymoney.EasyMoneyService;
import com.example.notification.http.RestRequestFromDongCai;
import com.example.notification.repository.EmIndicatorDao;
import com.example.notification.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class EasyMoneyController {

    private static final Logger logger = LoggerFactory.getLogger(EasyMoneyController.class);


    @Autowired
    private EasyMoneyService easymoneyService;
    @Autowired
    private RestRequestFromDongCai restRequestFromDongCai;

    @Autowired
    private EmIndicatorDao emIndicatorDao;

    @RequestMapping(value = {"/queryValueIndustryDet"})
    @ResponseBody
    public String queryValueIndustryDet() {
        logger.info("Enter EasyMoneyController queryValueIndustryDet ====");
        restRequestFromDongCai.queryValueIndustryDet("016071");
        return "result";
    }

    @RequestMapping(value = {"/updateBandDailyDet"})
    @ResponseBody
    public String updateBandDailyDet() {
        logger.info("Enter EasyMoneyController updateBandDailyDet ====");
        easymoneyService.updateBandDailyDet();
        return "OK";
    }

    @RequestMapping(value = {"/updateBandPercentile"})
    @ResponseBody
    public String updateBandPercentile() {
        logger.info("Enter EasyMoneyController updateBandPercentile ====");
        List<String> ids = emIndicatorDao.findIds();
        if (Utils.isWinSystem()) {
            ids = new ArrayList<>();
            ids.add("016029");
            ids.add("016028");
            ids.add("016020");
        }
        ids.forEach(id -> {
            easymoneyService.updateBandPercentile(id);
        });
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
