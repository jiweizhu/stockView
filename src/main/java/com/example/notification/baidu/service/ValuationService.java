package com.example.notification.baidu.service;

import com.example.notification.baidu.vo.TTMVo;
import com.example.notification.http.BdRestRequest;
import com.example.notification.repository.*;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.util.FormulaUtils;
import com.example.notification.util.Utils;
import com.example.notification.vo.BdIndicatorVO;
import com.example.notification.vo.StockDailyVO;
import com.example.notification.vo.StockNameVO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static com.example.notification.constant.Constants.*;

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
        updateByLineTTM(stockIdsAndIndicatorId);
        //todo
        //calculate rangePct
        //calculate wavePct
    }

    @Async
    private void updateByLineTTM(List<BdIndicatorVO> stockIdsAndIndicatorId) {
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            for (String stockId : bdIndicatorVO.getStockIds().split(",")) {
                logger.info("getFromBdAndUpdateTTM ===============" + stockId);
                List<TTMVo> ttmVoList = bdRestRequest.queryDataFromBd(stockId, TTM_URL);
                for (TTMVo ttmVo : ttmVoList) {
                    String date = ttmVo.getDate();
                    StockDailyVO stockDailyVO = stockDailyDao.findDayPriceByStockIdAndDay(stockId, Utils.stringToDate(date));
                    if (stockDailyVO != null && stockDailyVO.getTtm() == null) {
                        stockDailyVO.setTtm(Double.valueOf(ttmVo.getValue()));
                        stockDailyDao.save(stockDailyVO);
                    }
                }
            }
        }
    }

    @Async
    public void getFromBdAndUpdatePCF() {
        List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
        if (Utils.isWinSystem()) {
            stockIdsAndIndicatorId = new ArrayList<>();
            BdIndicatorVO e = new BdIndicatorVO("730200");
            e.setStockIds("sz000063");
            stockIdsAndIndicatorId.add(e);
        }
        updateLinePCF(stockIdsAndIndicatorId);
    }

    @Async
    private void updateLinePCF(List<BdIndicatorVO> stockIdsAndIndicatorId) {
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            for (String stockId : bdIndicatorVO.getStockIds().split(",")) {
                logger.info("getFromBdAndUpdatePCF ===============" + stockId);
                List<TTMVo> ttmVoList = bdRestRequest.queryDataFromBd(stockId, PCF_URL);
                for (TTMVo pcfVo : ttmVoList) {
                    String date = pcfVo.getDate();
                    StockDailyVO stockDailyVO = stockDailyDao.findDayPriceByStockIdAndDay(stockId, Utils.stringToDate(date));
                    if (stockDailyVO != null && stockDailyVO.getPcf() == null) {
                        stockDailyVO.setPcf(Double.valueOf(pcfVo.getValue()));
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
        updateLinePBR(stockIdsAndIndicatorId);
    }

    @Async
    private void updateLinePBR(List<BdIndicatorVO> stockIdsAndIndicatorId) {
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            for (String stockId : bdIndicatorVO.getStockIds().split(",")) {
                logger.info("getFromBdAndUpdatePBR ===============" + stockId);
                List<TTMVo> ttmVoList = bdRestRequest.queryDataFromBd(stockId, PBR_URL);
                for (TTMVo ttmVo : ttmVoList) {
                    String date = ttmVo.getDate();
                    StockDailyVO stockDailyVO = stockDailyDao.findDayPriceByStockIdAndDay(stockId, Utils.stringToDate(date));
                    if (stockDailyVO != null && stockDailyVO.getPbr() == null) {
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
            if(!stockId.startsWith("sh")|| !stockId.startsWith("sz")){
                continue;
            }
            // 查询该股票的所有 daily_price 数据，按日期升序排列
            List<StockDailyVO> dailyList = stockDailyDao.findByStockIdOrderByDayAsc(stockId);
            logger.info("fixNullTtm ===============" + stockId);
            Double lastValidTtm = null;
            Double lastValidPbr = null;
            Double lastValidPcf = null;
            for (int i = 0; i < dailyList.size(); i++) {
                StockDailyVO dailyVO = dailyList.get(i);
                Double currentTtm = dailyVO.getTtm();
                Double currentPbr = dailyVO.getPbr();
                Double currentPcf = dailyVO.getPcf();
                if (i == 0) continue;
                StockDailyVO preOneVo = dailyList.get(i - 1);
                if (currentTtm == null) {
                    lastValidTtm = preOneVo.getTtm();
                    dailyVO.setTtm(lastValidTtm);
                }
                if (currentPbr == null) {
                    lastValidPbr = preOneVo.getPbr();
                    dailyVO.setPbr(lastValidPbr);
                }
                if (currentPcf == null) {
                    lastValidPcf = preOneVo.getPcf();
                    dailyVO.setPcf(lastValidPcf);
                }
                stockDailyDao.save(dailyVO); // 保存更新
                dailyList.set(i, dailyVO);
            }

        }
    }


    //update stock wave/range of ttm, pbr, pcf
    public void updateStockEvaluationData() {
        List<String> stockIds = stockDao.findStockIds();
        if (Utils.isWinSystem()) {
            stockIds = new ArrayList<>();
            stockIds.add("sz000063");
        }
        stockIds.stream().filter(vo -> {
                    String codeId = vo.toLowerCase();
                    return codeId.startsWith("sh") || codeId.startsWith("sz");
                })
                .forEach(id -> {
                    updateStocksLine(id);
                });
    }

    @Async
    private void updateStocksLine(String id) {
        logger.info("======Enter updateStockEvaluationData =======id={}", id);
        List<StockDailyVO> dailyVOS = stockDailyDao.multiKFindByStockIdOrderByDay(id);
        //get all ttm,pbr,pcf from daily data
        List<BigDecimal> ttmList = new ArrayList<>();
        List<BigDecimal> pbrList = new ArrayList<>();
        List<BigDecimal> pcfList = new ArrayList<>();
        dailyVOS.forEach(vo -> {
            //add all of list ttm
            ttmList.add(new BigDecimal(vo.getTtm()));
            pbrList.add(new BigDecimal(vo.getPbr()));
            pcfList.add(new BigDecimal(vo.getPcf()));
        });
        BigDecimal ttmWavePercentile = FormulaUtils.calculateWavePercentile(ttmList, ttmList.get(0));
        BigDecimal ttmRangePercentile = FormulaUtils.calculateRangePercentile(ttmList, ttmList.get(0));
        BigDecimal pbrWavePercentile = FormulaUtils.calculateWavePercentile(ttmList, ttmList.get(0));
        BigDecimal pbrRangePercentile = FormulaUtils.calculateRangePercentile(ttmList, ttmList.get(0));
        BigDecimal pcfWavePercentile = FormulaUtils.calculateWavePercentile(ttmList, ttmList.get(0));
        BigDecimal pcfRangePercentile = FormulaUtils.calculateRangePercentile(ttmList, ttmList.get(0));
        StockNameVO stockNameVO = stockDao.findById(id).get();
        stockNameVO.setTtm(ttmList.get(0).doubleValue());
        stockNameVO.setPbr(pbrList.get(0).doubleValue());
        stockNameVO.setPcf(pcfList.get(0).doubleValue());
        stockNameVO.setPbrWavePct(pbrWavePercentile.doubleValue());
        stockNameVO.setPbrRangePct(pbrRangePercentile.doubleValue());
        stockNameVO.setPcfWavePct(pcfWavePercentile.doubleValue());
        stockNameVO.setPcfRangePct(pcfRangePercentile.doubleValue());
        stockNameVO.setTtmWavePct(ttmWavePercentile.doubleValue());
        stockNameVO.setTtmRangePct(ttmRangePercentile.doubleValue());
    }

}
