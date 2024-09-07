package com.example.notification.baidu.service;

import com.example.notification.baidu.vo.IndicatorDayVO;
import com.example.notification.baidu.vo.IndicatorVO;
import com.example.notification.http.RestRequest;
import com.example.notification.repository.BdIndicatorDailyDao;
import com.example.notification.repository.BdIndicatorDao;
import com.example.notification.repository.StockDailyDao;
import com.example.notification.service.ETFViewService;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.util.Utils;
import com.example.notification.vo.BdIndicatorDailyVO;
import com.example.notification.vo.BdIndicatorVO;
import com.example.notification.vo.StockDailyVO;
import com.example.notification.vo.StockNameVO;
import com.fasterxml.jackson.databind.DeserializationFeature;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

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
    private BdIndicatorDailyDao bdIndicatorDailyDao;


    public List<IndicatorVO> queryBaiduIndustriesRealInfo() {
        List<IndicatorVO> list = restRequest.queryBaiduIndustriesRealInfo();
        return list;
    }

    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public Object stockJsonData(String stockId) {
        ArrayList<String[]> result = new ArrayList<>();
        logger.info("enter BaiduInfoService stockJsonData stockId =============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }

        Integer rangeSize = getRangeSize();
        List<StockDailyVO> sh510300 = stockDailyDao.findByIndexStockIdOrderByDay("sh510300", rangeSize);
        Date day = sh510300.get(sh510300.size() - 1).getDay();
        String formattedDate = day.toLocalDate().format(formatter);

        ArrayList<String> mainKlineIds = kLineMarketClosedService.getMainKlineIds();
        Set<String> stringSet = mainKlineIds.stream().collect(Collectors.toSet());
        List<IndicatorDayVO> kLineList = restRequest.queryBaiduIndustriesKline(stockId, "day", formattedDate);
        List<BdIndicatorDailyVO> voList = bdIndicatorDailyDao.findByIndexStockIdOrderByDay(stockId, rangeSize);
        if (kLineList.isEmpty()) {
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
        // save new in db
        Set<String> dayLinesInDbSet = new HashSet<>();
        for (BdIndicatorDailyVO vo : voList) {
            dayLinesInDbSet.add(Utils.getFormat_YYYY_MM_DD(vo.getDay()));
        }
        for (int i = kLineList.size() - 1; i >= 0; i--) {
            IndicatorDayVO dayVO = kLineList.get(i);
            if (dayLinesInDbSet.contains(dayVO.getTime())) {
                break;
            }
            BdIndicatorDailyVO newVo = new BdIndicatorDailyVO();
            newVo.setStockId(stockId);
            newVo.setDay(new Date(dayVO.getTimestamp()));
            newVo.setOpeningPrice(BigDecimal.valueOf(dayVO.getOpen()));
            newVo.setClosingPrice(BigDecimal.valueOf(dayVO.getClose()));
            newVo.setIntradayHigh(BigDecimal.valueOf(dayVO.getHigh()));
            newVo.setIntradayLow(BigDecimal.valueOf(dayVO.getLow()));
            bdIndicatorDailyDao.save(newVo);
        }
        //return
        for (IndicatorDayVO vo : kLineList) {
            String[] strings = new String[7];
            strings[0] = vo.getTime();
            strings[1] = vo.getOpen().toString();
            strings[2] = vo.getClose().toString();
            strings[3] = vo.getHigh().toString();
            strings[4] = vo.getLow().toString();
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
}
