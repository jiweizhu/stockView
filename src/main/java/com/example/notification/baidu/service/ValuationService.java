package com.example.notification.baidu.service;

import com.example.notification.baidu.vo.TTMVo;
import com.example.notification.http.BdRestRequest;
import com.example.notification.repository.*;
import com.example.notification.service.HoldingService;
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
    public void getFromBdAndUpdateIndicatorTTM() throws InterruptedException {
        List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
        if (Utils.isWinSystem()) {
            stockIdsAndIndicatorId = new ArrayList<>();
            BdIndicatorVO e = new BdIndicatorVO("730200");
            e.setStockIds("sz000063");
            stockIdsAndIndicatorId.add(e);
        }
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            logger.info("getFromBdAndUpdateTTM =======bdIndicatorVO===name={}", bdIndicatorVO.getStockName());
            for (String stockId : bdIndicatorVO.getStockIds().split(",")) {
                logger.info("getFromBdAndUpdateTTM ===============" + stockId);
                Thread.sleep(100);
                List<TTMVo> ttmVoList = bdRestRequest.queryDataFromBd(stockId, TTM_URL);
                extractedTTM(stockId, ttmVoList);
            }
        }
        //todo
        //calculate rangePct
        //calculate wavePct
    }

    @Async
    private void extractedTTM(String stockId, List<TTMVo> ttmVoList) {
        for (TTMVo ttmVo : ttmVoList) {
            String date = ttmVo.getDate();
            StockDailyVO stockDailyVO = stockDailyDao.findDayPriceByStockIdAndDay(stockId, Utils.stringToDate(date));
            if (stockDailyVO != null && stockDailyVO.getTtm() == null) {
                stockDailyVO.setTtm(Double.valueOf(ttmVo.getValue()));
                stockDailyDao.save(stockDailyVO);
            }
        }
    }

    @Async
    public void getFromBdAndUpdateIndicatorPCF() {
        List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
        if (Utils.isWinSystem()) {
            stockIdsAndIndicatorId = new ArrayList<>();
            BdIndicatorVO e = new BdIndicatorVO("730200");
            e.setStockIds("sz000063");
            stockIdsAndIndicatorId.add(e);
        }
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            for (String stockId : bdIndicatorVO.getStockIds().split(",")) {
                logger.info("getFromBdAndUpdatePCF ===============" + stockId);
                List<TTMVo> ttmVoList = bdRestRequest.queryDataFromBd(stockId, PCF_URL);
                extractedPCF(stockId, ttmVoList);
            }
        }
    }

    @Async
    private void extractedPCF(String stockId, List<TTMVo> ttmVoList) {
        for (TTMVo pcfVo : ttmVoList) {
            String date = pcfVo.getDate();
            StockDailyVO stockDailyVO = stockDailyDao.findDayPriceByStockIdAndDay(stockId, Utils.stringToDate(date));
            if (stockDailyVO != null && stockDailyVO.getPcf() == null) {
                stockDailyVO.setPcf(Double.valueOf(pcfVo.getValue()));
                stockDailyDao.save(stockDailyVO);
            }
        }
    }

    @Async
    public void getFromBdAndUpdateIndicatorPBR() {
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
                extractedPBR(stockId, ttmVoList);
            }
        }
    }

    @Async
    private void extractedPBR(String stockId, List<TTMVo> ttmVoList) {
        for (TTMVo ttmVo : ttmVoList) {
            String date = ttmVo.getDate();
            StockDailyVO stockDailyVO = stockDailyDao.findDayPriceByStockIdAndDay(stockId, Utils.stringToDate(date));
            if (stockDailyVO != null && stockDailyVO.getPbr() == null) {
                stockDailyVO.setPbr(Double.valueOf(ttmVo.getValue()));
                stockDailyDao.save(stockDailyVO);
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
            if (!stockId.startsWith("s")) {
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
        List<String> stockIds = stockDao.findStockIdsHasName();
        if (Utils.isWinSystem()) {
            stockIds = new ArrayList<>();
            stockIds.add("sz000063");
        }
        stockIds.stream().filter(vo -> {
            String codeId = vo.toLowerCase();
            return codeId.startsWith("s");
        }).forEach(id -> {
            calculatePercentile(id);
        });
    }

    @Autowired
    private HoldingService holdingService;

    @Async
    private void calculatePercentile(String id) {
        logger.info("======Enter calculatePercentile =======id={}", id);
        List<StockDailyVO> dailyVOS = stockDailyDao.multiKFindByStockIdWithTTMOrderByDay(id);
        if (dailyVOS.isEmpty()) {
            String stockIdOrNameByMap = holdingService.getStockIdOrNameByMap(id);
            logger.info("======Enter calculatePercentile =======id={}, name = {}, no data", id, stockIdOrNameByMap);
            return;
        }
        //get all ttm,pbr,pcf from daily data
        List<BigDecimal> ttmList = new ArrayList<>();
        List<BigDecimal> pbrList = new ArrayList<>();
        List<BigDecimal> pcfList = new ArrayList<>();
        for (int i = 0; i < dailyVOS.size(); i++) {
            StockDailyVO vo = dailyVOS.get(i);
            if (vo.getTtm() == null || vo.getPbr() == null || vo.getPcf() == null) {
                String name = holdingService.getStockIdOrNameByMap(id);
                logger.info("======Enter calculatePercentile =======id={}, name = {}, day ={} has no data", id, name, vo.getDay());
                return;
            }
            //add all of list ttm
            ttmList.add(new BigDecimal(vo.getTtm()));
            pbrList.add(new BigDecimal(vo.getPbr()));
            pcfList.add(new BigDecimal(vo.getPcf()));
        }
        BigDecimal ttmWavePercentile = null;
        BigDecimal ttmRangePercentile = null;
        if (!ttmList.isEmpty()) {
            ttmWavePercentile = FormulaUtils.calculateWavePercentile(ttmList, ttmList.get(0));
            ttmRangePercentile = FormulaUtils.calculateRangePercentile(ttmList, ttmList.get(0));
        }
        BigDecimal pbrWavePercentile = null;
        BigDecimal pbrRangePercentile = null;
        if (!pcfList.isEmpty()) {
            pbrWavePercentile = FormulaUtils.calculateWavePercentile(pbrList, pbrList.get(0));
            pbrRangePercentile = FormulaUtils.calculateRangePercentile(pbrList, pbrList.get(0));
        }

        BigDecimal pcfWavePercentile = null;
        BigDecimal pcfRangePercentile = null;
        if (!pcfList.isEmpty()) {
            pcfWavePercentile = FormulaUtils.calculateWavePercentile(pcfList, pcfList.get(0));
            pcfRangePercentile = FormulaUtils.calculateRangePercentile(pcfList, pcfList.get(0));
        }
        StockNameVO stockNameVO = stockDao.findById(id).get();
        stockNameVO.setTtm(ttmList.get(0) == null ? null : ttmList.get(0).doubleValue());
        stockNameVO.setPbr(pbrList.get(0) == null ? null : pbrList.get(0).doubleValue());
        stockNameVO.setPcf(pcfList.get(0) == null ? null : pcfList.get(0).doubleValue());
        stockNameVO.setPbrWavePct(pbrWavePercentile.doubleValue());
        stockNameVO.setPbrRangePct(pbrRangePercentile.doubleValue());
        stockNameVO.setPcfWavePct(pcfWavePercentile.doubleValue());
        stockNameVO.setPcfRangePct(pcfRangePercentile.doubleValue());
        stockNameVO.setTtmWavePct(ttmWavePercentile.doubleValue());
        stockNameVO.setTtmRangePct(ttmRangePercentile.doubleValue());
        logger.info("======Enter calculatePercentile =====insert data ==id={}, name = {}", id, holdingService.getStockIdOrNameByMap(id));
        stockDao.save(stockNameVO);
    }

}
