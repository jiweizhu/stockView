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
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.example.notification.constant.Constants.PBR_URL;
import static com.example.notification.constant.Constants.TTM_URL;

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

    @Async
    public void getFromBdAndUpdateTTM() {
        List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
        if (Utils.isWinSystem()) {
            stockIdsAndIndicatorId = new ArrayList<>();
            BdIndicatorVO e = new BdIndicatorVO("730200");
            e.setStockIds("sz000063");
            stockIdsAndIndicatorId.add(e);
        }
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            for (String stockId : bdIndicatorVO.getStockIds().split(",")) {
                logger.info("getFromBdAndUpdateTTM ===============" + stockId);
                List<TTMVo> ttmVoList = bdRestRequest.queryDataFromBd(stockId, TTM_URL);
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

    @Async
    public void getFromBdAndUpdatePBR() {
        logger.info("Enter method getFromBdAndUpdatePBR ====");
        List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
        if (Utils.isWinSystem()) {
            stockIdsAndIndicatorId = new ArrayList<>();
            BdIndicatorVO e = new BdIndicatorVO("730200");
            e.setStockIds("sz000063");
            stockIdsAndIndicatorId.add(e);
        }
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            for (String stockId : bdIndicatorVO.getStockIds().split(",")) {
                logger.info("getFromBdAndUpdatePBR ===============" + stockId);
                List<TTMVo> ttmVoList = bdRestRequest.queryDataFromBd(stockId, PBR_URL);
                for (TTMVo ttmVo : ttmVoList) {
                    String date = ttmVo.getDate();
                    StockDailyVO stockDailyVO = stockDailyDao.findDayPriceByStockIdAndDay(stockId, Utils.stringToDate(date));
                    if (stockDailyVO != null) {
                        stockDailyVO.setPbr(Double.valueOf(ttmVo.getValue()));
                        stockDailyDao.save(stockDailyVO);
                    }
                }
            }
        }
    }

    //to fix some ttm is null
    @Async
    public void fixNullTtm() {
        logger.info("Enter method fixNullTtm ====");
        //backward to update null ttm
        //1.get all stockIds from table daily_price
        //2.get all daily_price by stockid
        //3.iterator to update ttm, if ttm is null, use previous ttm
        // 获取所有股票ID
        List<String> stockIds = stockDao.findStockIds();
        if (Utils.isWinSystem()) {
            stockIds = new ArrayList<>();
            stockIds.add("sz000063");
        }

        for (String stockId : stockIds) {
            // 查询该股票的所有 daily_price 数据，按日期升序排列
            List<StockDailyVO> dailyList = stockDailyDao.findByStockIdOrderByDayAsc(stockId);
            logger.info("fixNullTtm ===============" + stockId);
            Double lastValidTtm = null;
            Double lastValidPbr = null;
            for (int i = 0; i < dailyList.size(); i++) {
                StockDailyVO dailyVO = dailyList.get(i);
                Double currentTtm = dailyVO.getTtm();
                Double currentPbr = dailyVO.getPbr();
                if(i == 0)continue;
                StockDailyVO preOneVo = dailyList.get(i - 1);
                if (currentTtm == null) {
                    lastValidTtm = preOneVo.getTtm();
                    dailyVO.setTtm(lastValidTtm);
                }
                if (currentPbr == null) {
                    lastValidPbr = preOneVo.getPbr();
                    dailyVO.setPbr(lastValidPbr);
                }
                stockDailyDao.save(dailyVO); // 保存更新
                dailyList.set(i, dailyVO);
            }

        }

    }

}
