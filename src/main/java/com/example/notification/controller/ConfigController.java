package com.example.notification.controller;

import com.example.notification.repository.BdIndicatorDao;
import com.example.notification.repository.StockDao;
import com.example.notification.service.ETFViewService;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.service.KLineService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ConfigController {
    private static final Logger logger = LoggerFactory.getLogger(ConfigController.class);


    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Autowired
    private KLineService kLineService;
    @Autowired
    private ETFViewService etfViewService;

    @Autowired
    private BdIndicatorDao bdIndicatorDao;

    @Autowired
    private StockDao stockDao;

    private static boolean gainSort = true;

    public static boolean isGainSort() {
        return gainSort;
    }

    public static void setGainSort(boolean gainSort) {
        ConfigController.gainSort = gainSort;
    }

    @RequestMapping(value = {"/gainSort"})
    @ResponseBody
    public String gainSort() throws Exception {
        if (!gainSort) {
            setGainSort(true);
        } else {
            setGainSort(false);
        }
        return "gainSort=" + gainSort;
    }


}
