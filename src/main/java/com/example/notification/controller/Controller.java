package com.example.notification.controller;

import com.example.notification.service.KLineService;
import com.example.notification.util.EmailUtil;
import com.example.notification.vo.StockNameVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    private KLineService kLineService;

    @RequestMapping(value= {"/real","/index"})
    @ResponseBody
    public String realTimeQuery() throws Exception {
         kLineService.realTimeQuery();
        return "ok";
    }

    @RequestMapping(value= {"/avg"})
    @ResponseBody
    public String getAvgPrice() throws InterruptedException, JsonProcessingException {
        kLineService.getAvgPrice();
        return "ok";
    }

    @RequestMapping(value= {"/mail"})
    @ResponseBody
    public String sendEmailTest() throws Exception {
        StockNameVO stockNameVO = new StockNameVO();
        stockNameVO.setStockId("testId");
        stockNameVO.setStockName("testStockName");
        EmailUtil.sendMailSingle(stockNameVO);
        return "ok";
    }

}
