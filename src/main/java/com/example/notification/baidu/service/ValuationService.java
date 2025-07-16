package com.example.notification.baidu.service;

import com.example.notification.baidu.vo.TTMVo;
import com.example.notification.http.BdRestRequest;
import com.example.notification.repository.*;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.util.Utils;
import com.example.notification.vo.BdIndicatorVO;
import com.example.notification.vo.StockDailyVO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ValuationService {
    private static final Logger logger = LoggerFactory.getLogger(ValuationService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    @Autowired
    private BdRestRequest bdRestRequest;

    @Autowired
    private ThreadPoolTaskExecutor executorService;

    @Autowired
    private BdIndicatorDao bdIndicatorDao;

    @Autowired
    private BdFinancialSumDao bdFinancialSumDao;

    @Autowired
    private BdFinacialDao bdFinacialDao;

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Autowired
    private StockDailyDao stockDailyDao;

    @Autowired
    private StockDao stockDao;

    public void getFromBdAndUpdateTTM() {
        List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
        if(Utils.isWinSystem()){
            stockIdsAndIndicatorId = new ArrayList<>();
            BdIndicatorVO e = new BdIndicatorVO("730200");
            e.setStockIds("sz000063");
            stockIdsAndIndicatorId.add(e);
        }
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            for (String stockId : bdIndicatorVO.getStockIds().split(",")) {
                List<TTMVo> ttmVoList = bdRestRequest.queryBaiduStockTTM(stockId);
                for (TTMVo ttmVo : ttmVoList) {
                    String date = ttmVo.getDate();
                    StockDailyVO stockDailyVO = stockDailyDao.findDayPriceByStockIdAndDay(stockId, Utils.stringToDate(date));
                    if (stockDailyVO != null) {
                        stockDailyVO.setTtm(Double.valueOf(ttmVo.getValue()));
                        stockDailyDao.save(stockDailyVO);
                    }
                }
            }
        }
    }

}
