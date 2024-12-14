package com.example.notification.baidu.service;

import com.example.notification.baidu.vo.IndicatorDayVO;
import com.example.notification.baidu.vo.IndicatorVO;
import com.example.notification.http.RestRequest;
import com.example.notification.repository.*;
import com.example.notification.service.ETFViewService;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.util.Utils;
import com.example.notification.vo.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.example.notification.constant.Constants.getRangeSize;

@Service
public class BaiduInfoService {
    private static final Logger logger = LoggerFactory.getLogger(BaiduInfoService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    @Autowired
    private RestRequest restRequest;

    @Autowired
    private ETFViewService etfViewService;

    @Autowired
    private ThreadPoolTaskExecutor executorService;

    @Autowired
    private BdIndicatorDao bdIndicatorDao;

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Autowired
    private StockDailyDao stockDailyDao;

    @Autowired
    private WeeklyPriceDao weeklyPriceDao;

    @Autowired
    private BdIndicatorDailyDao bdIndicatorDailyDao;

    @Autowired
    private BdIndicatorWeeklyDao bdIndicatorWeeklyDao;


    public List<IndicatorVO> queryBaiduIndustriesRealInfo() {
        List<IndicatorVO> list = restRequest.queryBaiduIndustriesRealInfo();
        return list;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public Object getStockJsonDataDay(String stockId) {
        ArrayList<String[]> result = new ArrayList<>();
        logger.info("enter BaiduInfoService getStockJsonDataDay stockId =============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }

        Integer rangeSize = getRangeSize();

        List<BdIndicatorDailyVO> voList = bdIndicatorDailyDao.findByIndexStockIdOrderByDay(stockId, rangeSize).stream().sorted(Comparator.comparing(BdIndicatorDailyVO::getDay)).toList();
        //as baidu restrict to query
        // return db data
        for (BdIndicatorDailyVO vo : voList) {
            String[] strings = new String[7];
            strings[0] = Utils.getFormat(vo.getDay());
            strings[1] = vo.getOpeningPrice().toString();
            strings[2] = vo.getClosingPrice().toString();
            strings[3] = vo.getIntradayHigh().toString();
            strings[4] = vo.getIntradayLow().toString();
            result.add(strings);
        }
        return result;
    }

    public Object getStockJsonDataWeek(String stockId) {
        ArrayList<String[]> result = new ArrayList<>();
        logger.info("enter BaiduInfoService getStockJsonDataWeek stockId =============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }

        Integer rangeSize = getRangeSize();

        List<BdIndicatorWeeklyVO> voList = bdIndicatorWeeklyDao.findByIndexStockIdOrderByDay(stockId, rangeSize).stream().sorted(Comparator.comparing(BdIndicatorWeeklyVO::getDay)).toList();
        //as baidu restrict to query
        // return db data
        for (BdIndicatorWeeklyVO vo : voList) {
            String[] strings = new String[7];
            strings[0] = Utils.getFormat(vo.getDay());
            strings[1] = vo.getOpeningPrice().toString();
            strings[2] = vo.getClosingPrice().toString();
            strings[3] = vo.getIntradayHigh().toString();
            strings[4] = vo.getIntradayLow().toString();
            result.add(strings);
        }
        return result;
    }


    public void calculateIndicatorsAvg() {
        logger.debug("enter BaiduInfoService calculateIndicatorsAvg =============");
        List<BdIndicatorVO> ids = bdIndicatorDao.findAll();
        LocalDate today = LocalDate.now();
        LocalDate date = today.minusDays(100);
        String formattedDate = date.format(formatter);
        List<Callable<Void>> tasks = new ArrayList<>();
        ids.forEach(id -> {
            tasks.add(() -> {
                //get from baidu net, 200 days
                List<IndicatorDayVO> dayLine = restRequest.queryBaiduIndustriesKline(id.getStockId(), "day", formattedDate);
                logger.info("Finish queryBaiduIndustriesKline======" + id.getStockId() + "========" + dayLine.size());
                //calculate avg
                setUpwardDaysFive(id, dayLine);
                setUpwardDaysTen(id, dayLine);
                bdIndicatorDao.save(id);
                return null;
            });
        });

        try {
            List<Future<Void>> futures = executorService.getThreadPoolExecutor().invokeAll(tasks);
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    logger.error("Error executing task", e.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Task execution interrupted", e);
        }

    }

    private static void setUpwardDaysFive(BdIndicatorVO id, List<IndicatorDayVO> dayLine) {
        int upwardDaysFive = 0;
        for (int i = dayLine.size() - 1; i > 5; i--) {
            if (dayLine.get(i).getMa5avgprice() >= dayLine.get(i - 1).getMa5avgprice()) {
                if (upwardDaysFive < 0) break;
                upwardDaysFive++;
            } else {
                if (upwardDaysFive > 0) break;
                upwardDaysFive--;
            }
        }
        id.setUpwardDaysFive(upwardDaysFive);
    }

    private static void setUpwardDaysTen(BdIndicatorVO id, List<IndicatorDayVO> dayLine) {
        int upwardDaysTen = 0;
        for (int i = dayLine.size() - 1; i > 10; i--) {
            if (dayLine.get(i).getMa10avgprice() >= dayLine.get(i - 1).getMa10avgprice()) {
                if (upwardDaysTen < 0) break;
                upwardDaysTen++;
            } else {
                if (upwardDaysTen > 0) break;
                upwardDaysTen--;
            }
        }
        id.setUpwardDaysTen(upwardDaysTen);
    }

    public String indicatorsView() {
        List<BdIndicatorVO> list = bdIndicatorDao.findupwardDaysIndicator();
        list.addAll(bdIndicatorDao.findDownwardDaysIndicator());
//        Comparator<BdIndicatorVO> comparator = Comparator.comparing(BdIndicatorVO::getUpwardDaysFive);
//        List<BdIndicatorVO> resp = list.stream().sorted(comparator).toList();
        List<StockNameVO> industryEtfs = new ArrayList<>();
        list.forEach(vo -> {
            StockNameVO target = new StockNameVO();
            BeanUtils.copyProperties(vo, target);
            industryEtfs.add(target);
        });
        String html = etfViewService.dayLineStocksFlowView(industryEtfs, true);
        return html;
    }

    public Object indicatorStocksView(String indicatorId) {

        return null;
    }

    public void getFromNetAndStoreDay() {
        List<String> ids = bdIndicatorDao.findIds();
        logger.info("========getFromNetAndStoreDay ========ids.size ={}====={}", ids.size(), ids);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (String stockId : ids) {
            tasks.add(() -> {
                Set<String> exsitingDaySet = new HashSet<>();
                List<BdIndicatorDailyVO> allByStockId = bdIndicatorDailyDao.findAllByStockId(stockId);

                for (BdIndicatorDailyVO dailyVO : allByStockId) {
                    exsitingDaySet.add(dailyVO.getDay().toString());
                }
                if (exsitingDaySet.contains(Date.valueOf(LocalDate.now()).toString())) {
                    return null;
                }
//                Thread.sleep(new Random().nextInt(2000));
                List<IndicatorDayVO> fromNetList = restRequest.queryBaiduIndustriesKline(stockId, "day", "2019-01-01");
                // save new in db
                int loopNum = 0;
                for (IndicatorDayVO dayVO : fromNetList) {
                    if (exsitingDaySet.contains(dayVO.getDay())) {
                        continue;
                    }
                    loopNum++;
                    Date dateFromNet = Date.valueOf(dayVO.getDay());
                    BdIndicatorDailyVO newVo = new BdIndicatorDailyVO();
                    newVo.setStockId(stockId);
                    newVo.setDay(dateFromNet);
                    newVo.setOpeningPrice(BigDecimal.valueOf(dayVO.getOpen()));
                    newVo.setClosingPrice(BigDecimal.valueOf(dayVO.getClose()));
                    newVo.setIntradayHigh(BigDecimal.valueOf(dayVO.getHigh()));
                    newVo.setIntradayLow(BigDecimal.valueOf(dayVO.getLow()));
                    bdIndicatorDailyDao.save(newVo);
                }
                logger.info("========getFromNetAndStoreDay =====stockId={}===exsitingDaySet=={}=fromNetList size={}===loopNum=={}", stockId, exsitingDaySet.size(), fromNetList.size(), loopNum);
                return null;
            });
        }
        try {
            logger.info("========getFromNetAndStoreDay ========start to invokeAll =tasks.size ={}", tasks.size());
            List<Future<Void>> futures = executorService.getThreadPoolExecutor().invokeAll(tasks);
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    logger.error("Error executing task", e.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Task execution interrupted", e);
        }
    }

    public void getFromNetAndStoreWeek() {
        List<String> ids = bdIndicatorDao.findIds();
        logger.info("========getFromNetAndStoreWeek ========ids.size ={}====={}", ids.size(), ids);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (String stockId : ids) {
            tasks.add(() -> {
                Set<String> exsitingDaySet = new HashSet<>();
                List<BdIndicatorWeeklyVO> allByStockId = bdIndicatorWeeklyDao.findAllByStockId(stockId);

                for (BdIndicatorWeeklyVO dailyVO : allByStockId) {
                    exsitingDaySet.add(dailyVO.getDay().toString());
                }
                if (exsitingDaySet.contains(Date.valueOf(LocalDate.now()).toString())) {
                    return null;
                }
//                Thread.sleep(new Random().nextInt(2000));
                List<IndicatorDayVO> fromNetList = restRequest.queryBaiduIndustriesKline(stockId, "week", "2018-01-01");
                // save new in db
                int loopNum = 0;
                for (IndicatorDayVO dayVO : fromNetList) {
                    if (exsitingDaySet.contains(dayVO.getDay())) {
                        continue;
                    }
                    loopNum++;
                    Date dateFromNet = Date.valueOf(dayVO.getDay());
                    BdIndicatorWeeklyVO newVo = new BdIndicatorWeeklyVO();
                    newVo.setStockId(stockId);
                    newVo.setDay(dateFromNet);
                    newVo.setOpeningPrice(BigDecimal.valueOf(dayVO.getOpen()));
                    newVo.setClosingPrice(BigDecimal.valueOf(dayVO.getClose()));
                    newVo.setIntradayHigh(BigDecimal.valueOf(dayVO.getHigh()));
                    newVo.setIntradayLow(BigDecimal.valueOf(dayVO.getLow()));
                    bdIndicatorWeeklyDao.save(newVo);
                }
                logger.info("========getFromNetAndStoreWeek =====stockId={}===exsitingDaySet=={}=fromNetList size={}===loopNum=={}", stockId, exsitingDaySet.size(), fromNetList.size(), loopNum);
                return null;
            });
        }
        try {
            logger.info("========getFromNetAndStoreWeek ========start to invokeAll =tasks.size ={}", tasks.size());
            List<Future<Void>> futures = executorService.getThreadPoolExecutor().invokeAll(tasks);
            for (Future<Void> future : futures) {
                try {
                    future.get();
                } catch (ExecutionException e) {
                    logger.error("Error executing task", e.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Task execution interrupted", e);
        }
    }

    public void updateIndicatorBelongStocks() {
        // iterate indicators to get stockids from net
        List<BdIndicatorVO> indicatorIds = bdIndicatorDao.findAll();
        indicatorIds.forEach(vo -> {
            //update to bd_indicator stock_ids
            JsonNode jsonNode = restRequest.queryBaiduIndustryStocks(vo.getStockId());
            if (jsonNode != null && jsonNode.isArray()) {
                StringBuilder stockIdsLine = new StringBuilder();
                int loopCount = 0;
                for (JsonNode node : jsonNode) {
                    String market = node.get("exchange").asText().toLowerCase();
                    String code = node.get("code").asText();
                    stockIdsLine.append(market).append(code).append(",");
                    loopCount++;
                }
                vo.setStockIds(stockIdsLine.toString());
                bdIndicatorDao.save(vo);
            }
        });
    }
}
