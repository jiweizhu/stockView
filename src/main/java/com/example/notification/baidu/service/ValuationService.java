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

    public void getFromBdAndUpdateIndicatorPE(List<BdIndicatorVO> stockIdsAndIndicatorId) throws InterruptedException {
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            extractedPEFromBD(bdIndicatorVO);
        }
    }

    @Async
    private void extractedPEFromBD(BdIndicatorVO bdIndicatorVO) {
        logger.info("getFromBdAndUpdatePE =======bdIndicatorVO===name={}", bdIndicatorVO);
        String[] stockList = bdIndicatorVO.getStockIds().split(",");
        for (String stockId : stockList) {
            List<TTMVo> ttmVoList = bdRestRequest.queryStockValuationFromBd(stockId, TTM_URL);
            if (ttmVoList.isEmpty()) {
                continue;
            }
            List<StockDailyVO> dbVoList = stockDailyDao.findByStockIdOrderByDayAsc(stockId, easymoneyRangeCount);
            if (dbVoList.isEmpty()) {
                continue;
            }
            Map<String, StockDailyVO> dbVoMap = new HashMap<>();
            dbVoList.forEach(dbVo -> {
                dbVoMap.put(dbVo.getDay().toString(), dbVo);
            });
            AtomicInteger count = new AtomicInteger();
            ttmVoList.forEach(ttmVo -> {
                StockDailyVO dbVo = dbVoMap.get(ttmVo.getDate().toString());
                if (dbVo != null) {
                    dbVo.setTtm(Double.valueOf(ttmVo.getValue()));
                    count.getAndIncrement();
                    stockDailyDao.save(dbVo);
                }
            });
            logger.info("getFromBdAndUpdatePE ======stockId={}, ==inserted=count==={}", stockId, count);
        }
    }

    public void getFromBdAndUpdateIndicatorPCF(List<BdIndicatorVO> stockIdsAndIndicatorId) {
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            extractedPCF(bdIndicatorVO);
        }
    }

    @Async
    private void extractedPCF(BdIndicatorVO bdIndicatorVO) {
        String[] stockList = bdIndicatorVO.getStockIds().split(",");
        for (String stockId : stockList) {
            List<TTMVo> ttmVoList = bdRestRequest.queryStockValuationFromBd(stockId, PCF_URL);
            if (ttmVoList.isEmpty()) {
                continue;
            }
            List<StockDailyVO> dbVoList = stockDailyDao.findByStockIdOrderByDayAsc(stockId, easymoneyRangeCount);
            if (dbVoList.isEmpty()) {
                continue;
            }
            Map<String, StockDailyVO> dbVoMap = new HashMap<>();
            dbVoList.forEach(dbVo -> {
                dbVoMap.put(dbVo.getDay().toString(), dbVo);
            });
            AtomicInteger count = new AtomicInteger();
            ttmVoList.forEach(ttmVo -> {
                StockDailyVO dbVo = dbVoMap.get(ttmVo.getDate().toString());
                if (dbVo != null) {
                    dbVo.setPcf(Double.valueOf(ttmVo.getValue()));
                    count.getAndIncrement();
                    stockDailyDao.save(dbVo);
                }
            });
            logger.info("getFromBdAndUpdatePCF ======stockId={}, ==inserted=count==={}", stockId, count);
        }
    }

    public void getFromBdAndUpdateIndicatorPBR(List<BdIndicatorVO> stockIdsAndIndicatorId) {
        for (BdIndicatorVO bdIndicatorVO : stockIdsAndIndicatorId) {
            extractedPBR(bdIndicatorVO);
        }
    }

    @Async
    private void extractedPBR(BdIndicatorVO bdIndicatorVO) {
        String[] stockList = bdIndicatorVO.getStockIds().split(",");
        for (String stockId : stockList) {
            List<TTMVo> ttmVoList = bdRestRequest.queryStockValuationFromBd(stockId, PBR_URL);
            if (ttmVoList.isEmpty()) {
                continue;
            }
            List<StockDailyVO> dbVoList = stockDailyDao.findByStockIdOrderByDayAsc(stockId, easymoneyRangeCount);
            if (dbVoList.isEmpty()) {
                continue;
            }
            Map<String, StockDailyVO> dbVoMap = new HashMap<>();
            dbVoList.forEach(dbVo -> {
                dbVoMap.put(dbVo.getDay().toString(), dbVo);
            });
            AtomicInteger count = new AtomicInteger();
            ttmVoList.forEach(ttmVo -> {
                StockDailyVO dbVo = dbVoMap.get(ttmVo.getDate().toString());
                if (dbVo != null) {
                    dbVo.setPbr(Double.valueOf(ttmVo.getValue()));
                    count.getAndIncrement();
                    stockDailyDao.save(dbVo);
                }
            });
            logger.info("getFromBdAndUpdatePBR ======stockId={}, ==inserted=count==={}", stockId, count);
        }
    }

    //to fix some ttm is null
    public void fixNullTtm() {
        logger.info("Enter method fixNullTtm ====");
        //backward to update null ttm
        //1.get all stockIds from table daily_price
        //2.get all daily_price by stockid
        //3.iterator to update ttm, if ttm is null, use previous ttm
        // 获取所有股票ID
        List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
        if (Utils.isWinSystem()) {
            stockIdsAndIndicatorId = new ArrayList<>();
            BdIndicatorVO e = new BdIndicatorVO("730200");
            e.setStockIds("sz000063");
            e.setStockIds("sh600498");
            stockIdsAndIndicatorId.add(e);
        }
        for (BdIndicatorVO vo : stockIdsAndIndicatorId) {
            String[] strings = vo.getStockIds().split(",");
            int length = strings.length;
            if (length == 0) {
                return;
            }
            Arrays.stream(strings).forEach(this::extractedFixNull);
        }
    }

    //    @Async
    private void extractedFixNull(String stockId) {
        if (stockId == null || stockId.isEmpty() || !stockId.toLowerCase().startsWith("s")) {
            return;
        }
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
        Double prePB = null;
        Double prePE = null;
        Double prePC = null;
        int countPE = 0;
        int countPB = 0;
        int countPC = 0;
        for (int i = 0; i < dailyList.size(); i++) {
            StockDailyVO dailyVO = dailyList.get(i);
            Double currentPE = dailyVO.getTtm();
            if (currentPE == null && prePE == null) {
                continue;
            } else if (currentPE != null) {
                prePE = currentPE;
            } else if (currentPE == null && prePE != null) {
                dailyVO.setTtm(prePE);
                stockDailyDao.save(dailyVO);
                countPE++;
            }
        }

        for (int i = 0; i < dailyList.size(); i++) {
            StockDailyVO dailyVO = dailyList.get(i);
            Double currentPB = dailyVO.getPbr();
            if (currentPB == null && prePB == null) {
                continue;
            } else if (currentPB != null) {
                prePB = currentPB;
            } else if (currentPB == null && prePB != null) {
                dailyVO.setPbr(prePB);
                stockDailyDao.save(dailyVO);
                countPB++;
            }
        }

        for (int i = 0; i < dailyList.size(); i++) {
            StockDailyVO dailyVO = dailyList.get(i);
            Double currentPC = dailyVO.getPcf();
            if (currentPC == null && prePC == null) {
                continue;
            } else if (currentPC != null) {
                prePC = currentPC;
            } else if (currentPC == null && prePC != null) {
                dailyVO.setPcf(prePC);
                stockDailyDao.save(dailyVO);
                countPC++;
            }
        }
        logger.info("fixNullTtm ===========stockId={}, stockName={}, countPE={}, countPB={}, countPC={}",
                stockId, holdingService.getStockIdOrNameByMap(stockId), countPE, countPB, countPC);
    }


    //update stock wave/range of ttm, pbr, pcf
    public void updateStockPercentile() {
        List<BdIndicatorVO> stockIdsAndIndicatorId = bdIndicatorDao.findStockIdsAndIndicatorId();
        if (Utils.isWinSystem()) {
            stockIdsAndIndicatorId = new ArrayList<>();
            BdIndicatorVO e = new BdIndicatorVO("730200");
            e.setStockIds("sz000063");
            e.setStockIds("sh600498");
            stockIdsAndIndicatorId.add(e);
        }
        for (BdIndicatorVO vo : stockIdsAndIndicatorId) {
            String[] strings = vo.getStockIds().split(",");
            int length = strings.length;
            if (length == 0) {
                return;
            }
            Arrays.stream(strings).filter(s -> s.toLowerCase().startsWith("s")).forEach(this::updatePct);
        }
    }

    //    @Async
    private void updatePct(String id) {
        calculatePCFPercentile(id);
        calculatePEPercentile(id);
        calculatePBRPercentile(id);
    }

    @Autowired
    private HoldingService holdingService;

    @Value("${notification.easymoney.band.range.count}")
    private Integer easymoneyRangeCount;

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
