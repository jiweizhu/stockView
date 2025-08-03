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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

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

    public void getFromBdAndUpdatePEByIndicator() throws InterruptedException {
        List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
        if (Utils.isWinSystem()) {
            stockIdsAndIndicatorId = new ArrayList<>();
            BdIndicatorVO e = new BdIndicatorVO("730200");
            e.setStockIds("sz000063");
            e.setStockIds("sh600498");
            stockIdsAndIndicatorId.add(e);
        }
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            extractedPEFromBD(bdIndicatorVO);
        }
    }

    @Async
    private void extractedPEFromBD(BdIndicatorVO bdIndicatorVO) throws InterruptedException {
        logger.info("getFromBdAndUpdatePE =======bdIndicatorVO===name={}", bdIndicatorVO.getStockName());
        for (String stockId : bdIndicatorVO.getStockIds().split(",")) {
            Thread.sleep(100);
            List<StockDailyVO> dbVoList = stockDailyDao.findDayNullPEByStockId(stockId);
            if (dbVoList.isEmpty()) {
                continue;
            }
            Map<String, TTMVo> ttmVoMap = new HashMap<>();
            List<TTMVo> ttmVoList = bdRestRequest.queryStockValuationFromBd(stockId, TTM_URL);
            ttmVoList.forEach(dailyVO -> {
                ttmVoMap.put(dailyVO.getDate(), dailyVO);
            });
            AtomicInteger count = new AtomicInteger();
            dbVoList.forEach(dailyVO -> {
                String nullDay = dailyVO.getDay().toString();
                TTMVo ttmVo = ttmVoMap.get(nullDay);
                if (ttmVo != null && dailyVO.getTtm() == null) {
                    dailyVO.setTtm(Double.valueOf(ttmVo.getValue()));
                    count.getAndIncrement();
                    stockDailyDao.save(dailyVO);
                }
            });
            logger.info("getFromBdAndUpdatePE ======stockId={}, ==inserted=count==={}", stockId, count);
        }
    }

    public void getFromBdAndUpdateIndicatorPCF() {
        List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
        if (Utils.isWinSystem()) {
            stockIdsAndIndicatorId = new ArrayList<>();
            BdIndicatorVO e = new BdIndicatorVO("730200");
            e.setStockIds("sz000063");
            e.setStockIds("sh600498");
            stockIdsAndIndicatorId.add(e);
        }
        String lastOpeningDay = Utils.getLastOpeningDay();
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            for (String stockId : bdIndicatorVO.getStockIds().split(",")) {
                extractedPCF(stockId, lastOpeningDay);
            }
        }
    }

    @Async
    private void extractedPCF(String stockId, String lastOpeningDay) {
        logger.info("getFromBdAndUpdatePCF ===============" + stockId);
        //skip updated pcf

        StockDailyVO lastVo = stockDailyDao.findLastOneDayPriceByStockId(stockId);
        if (lastVo == null || lastVo.getPcf() != null) {
            logger.info("getFromBdAndUpdatePCF ===skip update======stockId={}=name={}", stockId, holdingService.getStockIdOrNameByMap(stockId));
            return;
        }
        List<TTMVo> ttmVoList = bdRestRequest.queryStockValuationFromBd(stockId, PCF_URL);
        int count = 0;
        for (TTMVo pcfVo : ttmVoList) {
            String date = pcfVo.getDate();
            StockDailyVO stockDailyVO = stockDailyDao.findDayPriceByStockIdAndDay(stockId, Utils.stringToDate(date));
            if (stockDailyVO != null && stockDailyVO.getPcf() == null) {
                stockDailyVO.setPcf(Double.valueOf(pcfVo.getValue()));
                count++;
                stockDailyDao.save(stockDailyVO);
            }
        }
        logger.info("getFromBdAndUpdatePCF ======stockId={}, ==inserted=count==={}", stockId, count);
    }

    public void getFromBdAndUpdateIndicatorPBR() {
        logger.info("Enter method getFromBdAndUpdatePBR ====");
        List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
        if (Utils.isWinSystem()) {
            stockIdsAndIndicatorId = new ArrayList<>();
            BdIndicatorVO e = new BdIndicatorVO("730200");
            e.setStockIds("sz000063");
            e.setStockIds("sh600498");
            stockIdsAndIndicatorId.add(e);
        }
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            for (String stockId : bdIndicatorVO.getStockIds().split(",")) {
                extractedPBR(stockId);
            }
        }
    }

    @Async
    private void extractedPBR(String stockId) {
        List<TTMVo> ttmVoList = bdRestRequest.queryStockValuationFromBd(stockId, PBR_URL);
        int count = 0;
        for (TTMVo ttmVo : ttmVoList) {
            String date = ttmVo.getDate();
            StockDailyVO stockDailyVO = stockDailyDao.findDayPriceByStockIdAndDay(stockId, Utils.stringToDate(date));
            if (stockDailyVO != null && stockDailyVO.getPbr() == null) {
                stockDailyVO.setPbr(Double.valueOf(ttmVo.getValue()));
                count++;
                stockDailyDao.save(stockDailyVO);
            }
        }
        logger.info("getFromBdAndUpdateIndicatorPBR ======stockId={}, ==inserted=count==={}", stockId, count);
    }

    //to fix some ttm is null
    public void fixNullTtm() {
        logger.info("Enter method fixNullTtm ====");
        //backward to update null ttm
        //1.get all stockIds from table daily_price
        //2.get all daily_price by stockid
        //3.iterator to update ttm, if ttm is null, use previous ttm
        // 获取所有股票ID
        List<String> stockIds = stockDao.findStockIdsHasName();
        if (Utils.isWinSystem()) {
            stockIds = new ArrayList<>();
            stockIds.add("sz000063");
            stockIds.add("sh600498");
        }

        for (String stockId : stockIds) {
            if (!stockId.startsWith("s")) {
                continue;
            }
            // 查询该股票的所有 daily_price 数据，按日期升序排列
            extractedFixNull(stockId);

        }
    }

    @Async
    private void extractedFixNull(String stockId) {
        //as i get time range is five year, baidu may lost some data, so i use five year data to fix it
        //if updated, skip to update it
        StockDailyVO nullRow = stockDailyDao.findByStockIdWithNullTTMPBRPCF(stockId);
        if (nullRow == null) {
            logger.info("fixNullTtm ===========stockId={}, stockName={}=====already fixed===", stockId, holdingService.getStockIdOrNameByMap(stockId));
            //no need update again
            return;
        }
        List<StockDailyVO> dailyList = stockDailyDao.findByStockIdOrderByDayAsc(stockId, easymoneyRangeCount);
        //start to fix
        Double lastValidTtm = null;
        Double lastValidPbr = null;
        Double lastValidPcf = null;
        int count = 0;
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
            count++;
            dailyList.set(i, dailyVO);
        }
        logger.info("fixNullTtm ===========stockId={}, stockName={}, count={}", stockId, holdingService.getStockIdOrNameByMap(stockId), count);
    }


    //update stock wave/range of ttm, pbr, pcf
    public void updateStockPercentile() {
        List<String> stockIds = stockDao.findStockIdsHasName();
        if (Utils.isWinSystem()) {
            stockIds = new ArrayList<>();
            stockIds.add("sz000063");
            stockIds.add("sh600498");
        }
        stockIds.stream().filter(vo -> {
            String codeId = vo.toLowerCase();
            return codeId.startsWith("s");
        }).forEach(id -> {
            updatePct(id);
        });
    }

    @Async
    private void updatePct(String id) {
        calculatePCFPercentile(id);
        calculatePEPercentile(id);
        calculatePBRPercentile(id);
    }

    @Autowired
    private HoldingService holdingService;

    @Value("${notification.easymoney.band.range.count}")
    private Integer easymoneyRangeCount;

    @Async
    private void calculatePBRPercentile(String id) {
        logger.info("======Enter calculatePBRPercentile =======id={}", id);
        //every friday to update
        //first to update daily_price price, then update daily_price ttm/pcf, then calculatePercentile
        StockDailyVO lastDayVo = stockDailyDao.findLastOneDayPriceByStockId(id);
        //if updated, skip to  update
        String lastOpeningDay = Utils.getLastOpeningDay();
        if (lastDayVo.getDay().toString().equals(lastOpeningDay) && lastDayVo.getTtm() != null && lastDayVo.getPbr() != null && lastDayVo.getPcf() != null) {
            //skip to update
            return;
        }
        //start to  calculate
        List<StockDailyVO> dailyVOS = stockDailyDao.multiKFindByStockIdOrderByDay(id, easymoneyRangeCount);
        if (dailyVOS.isEmpty()) {
            String stockIdOrNameByMap = holdingService.getStockIdOrNameByMap(id);
            logger.info("======Enter calculatePBRPercentile =======id={}, name = {}, no data", id, stockIdOrNameByMap);
            return;
        }
        //get all ttm,pbr,pcf from daily data
        List<BigDecimal> pbrList = new ArrayList<>();
        for (int i = 0; i < dailyVOS.size(); i++) {
            StockDailyVO vo = dailyVOS.get(i);
            if (vo.getTtm() == null || vo.getPbr() == null || vo.getPcf() == null) {
                String name = holdingService.getStockIdOrNameByMap(id);
                logger.info("======Enter calculatePBRPercentile =======id={}, name = {}, day ={} has no data", id, name, vo.getDay() == null ? "" : vo.getDay().toString());
                return;
            }
            //add all of list ttm
            pbrList.add(new BigDecimal(vo.getPbr()));
        }
        BigDecimal pbrWavePercentile = null;
        BigDecimal pbrRangePercentile = null;
        if (!pbrList.isEmpty()) {
            pbrWavePercentile = FormulaUtils.calculateWavePercentile(pbrList, pbrList.get(0));
            pbrRangePercentile = FormulaUtils.calculateRangePercentile(pbrList, pbrList.get(0));
        }

        StockNameVO stockVo = stockDao.findById(id).get();
        stockVo.setPbr(pbrList.get(0) == null ? null : pbrList.get(0).doubleValue());
        stockVo.setPbrWavePct(pbrWavePercentile.doubleValue());
        stockVo.setPbrRangePct(pbrRangePercentile.doubleValue());
        logger.info("======Enter calculatePBRPercentile =====insert data ==id={}, name = {}", id, holdingService.getStockIdOrNameByMap(id));
        stockVo.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
        stockDao.save(stockVo);
    }

    private void calculatePCFPercentile(String id) {
        logger.info("======Enter calculatePCFPercentile =======id={}", id);
        //every friday to update
        //first to update daily_price price, then update daily_price ttm/pcf, then calculatePercentile
        StockDailyVO lastDayVo = stockDailyDao.findLastOneDayPriceByStockId(id);
        //if updated, skip to  update
        String lastOpeningDay = Utils.getLastOpeningDay();
        if (lastDayVo.getDay().toString().equals(lastOpeningDay) && lastDayVo.getTtm() != null && lastDayVo.getPbr() != null && lastDayVo.getPcf() != null) {
            //skip to update
            return;
        }
        //start to  calculate
        List<StockDailyVO> dailyVOS = stockDailyDao.multiKFindByStockIdOrderByDay(id, easymoneyRangeCount);
        if (dailyVOS.isEmpty()) {
            String stockIdOrNameByMap = holdingService.getStockIdOrNameByMap(id);
            logger.info("======Enter calculatePCFPercentile =======id={}, name = {}, no data", id, stockIdOrNameByMap);
            return;
        }
        //get all ttm,pbr,pcf from daily data
        List<BigDecimal> pcfList = new ArrayList<>();
        for (int i = 0; i < dailyVOS.size(); i++) {
            StockDailyVO vo = dailyVOS.get(i);
            if (vo.getTtm() == null || vo.getPbr() == null || vo.getPcf() == null) {
                String name = holdingService.getStockIdOrNameByMap(id);
                logger.info("======Enter calculatePCFPercentile =======id={}, name = {}, day ={} has no data", id, name, vo.getDay() == null ? "" : vo.getDay().toString());
                return;
            }
            //add all of list ttm
            pcfList.add(new BigDecimal(vo.getPcf()));
        }

        BigDecimal pcfWavePercentile = null;
        BigDecimal pcfRangePercentile = null;
        if (!pcfList.isEmpty()) {
            pcfWavePercentile = FormulaUtils.calculateWavePercentile(pcfList, pcfList.get(0));
            pcfRangePercentile = FormulaUtils.calculateRangePercentile(pcfList, pcfList.get(0));
        }
        StockNameVO stockVo = stockDao.findById(id).get();
        stockVo.setPcf(pcfList.get(0) == null ? null : pcfList.get(0).doubleValue());
        stockVo.setPcfWavePct(pcfWavePercentile.doubleValue());
        stockVo.setPcfRangePct(pcfRangePercentile.doubleValue());
        logger.info("======Enter calculatePCFPercentile =====insert data ==id={}, name = {}", id, holdingService.getStockIdOrNameByMap(id));
        stockVo.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
        stockDao.save(stockVo);
    }


    private void calculatePEPercentile(String id) {
        logger.info("======Enter calculatePEPercentile =======id={}", id);
        //every friday to update
        //first to update daily_price price, then update daily_price ttm/pcf, then calculatePercentile
        StockDailyVO lastDayVo = stockDailyDao.findLastOneDayPriceByStockId(id);
        //if updated, skip to  update
        String lastOpeningDay = Utils.getLastOpeningDay();
        if (lastDayVo.getDay().toString().equals(lastOpeningDay) && lastDayVo.getTtm() != null) {
            //skip to update
            return;
        }
        //start to  calculate
        List<StockDailyVO> dailyVOS = stockDailyDao.multiKFindByStockIdOrderByDay(id, easymoneyRangeCount);
        if (dailyVOS.isEmpty()) {
            String stockIdOrNameByMap = holdingService.getStockIdOrNameByMap(id);
            logger.info("======Enter calculatePercentile =======id={}, name = {}, no data", id, stockIdOrNameByMap);
            return;
        }
        //get all ttm,pbr,pcf from daily data
        List<BigDecimal> ttmList = new ArrayList<>();
        for (int i = 0; i < dailyVOS.size(); i++) {
            StockDailyVO vo = dailyVOS.get(i);
            if (vo.getTtm() == null || vo.getPbr() == null || vo.getPcf() == null) {
                String name = holdingService.getStockIdOrNameByMap(id);
                logger.info("======Enter calculatePercentile =======id={}, name = {}, day ={} has no data", id, name, vo.getDay() == null ? "" : vo.getDay().toString());
                return;
            }
            //add all of list ttm
            ttmList.add(new BigDecimal(vo.getTtm()));
        }
        BigDecimal ttmWavePercentile = null;
        BigDecimal ttmRangePercentile = null;
        if (!ttmList.isEmpty()) {
            ttmWavePercentile = FormulaUtils.calculateWavePercentile(ttmList, ttmList.get(0));
            ttmRangePercentile = FormulaUtils.calculateRangePercentile(ttmList, ttmList.get(0));
        }
        StockNameVO stockVo = stockDao.findById(id).get();
        stockVo.setPe(ttmList.get(0) == null ? null : ttmList.get(0).doubleValue());
        stockVo.setTtmWavePct(ttmWavePercentile.doubleValue());
        stockVo.setTtmRangePct(ttmRangePercentile.doubleValue());
        logger.info("======Enter calculatePEPercentile =====insert data ==id={}, name = {}", id, holdingService.getStockIdOrNameByMap(id));
        stockVo.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
        stockDao.save(stockVo);
    }

}
