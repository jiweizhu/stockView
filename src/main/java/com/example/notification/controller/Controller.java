package com.example.notification.controller;

import com.example.notification.service.KLineService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {

    @Autowired
    private KLineService kLineService;

    @RequestMapping(value= {"/","/index"})
    @ResponseBody
    public String getIndex() throws JsonProcessingException {
         kLineService.realTimeQuery();
        return "ok";
    }

    @RequestMapping(value= {"/aa"})
    @ResponseBody
    public String getIndex2() throws InterruptedException, JsonProcessingException {
        kLineService.startToQueryRealTimePrice();
        return "ok";
    }

}
