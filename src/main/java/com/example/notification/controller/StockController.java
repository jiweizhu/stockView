package com.example.notification.controller;

import com.example.notification.responseVo.RespVO;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.vo.StockNameVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class StockController {
    private static final Logger logger = LoggerFactory.getLogger(StockController.class);

    private static ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Value("${notification.init.history.price.day}")
    private Integer initHistoryPriceDay;

    @RequestMapping(value = {"/stock/addNew"})
    @ResponseBody
    public RespVO addNewStock(@ModelAttribute StockNameVO requestVO) throws JsonProcessingException {
        logger.info("Enter StockController addNewStock=========");
        String stockId = requestVO.getStockId();
        List<String> list = new ArrayList<>();
        list.add(stockId);
        String body = kLineMarketClosedService.addNewInTable(list);
        RespVO ret = new RespVO();
        if (body.contains("imported")) {
            kLineMarketClosedService.getHistoryPriceOnLineAndStoreInDb(initHistoryPriceDay);
            ret.setCode(200);
        } else {
            ret.setCode(401);
        }
        return ret;
    }

}
