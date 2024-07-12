package com.example.notification.service;

import com.example.notification.http.RestRequest;
import com.example.notification.repository.HoldingStockDao;
import com.example.notification.repository.IntraDayPriceDao;
import com.example.notification.repository.StockDailyDao;
import com.example.notification.repository.StockDao;
import com.example.notification.util.Utils;
import com.example.notification.vo.*;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Service
public class IntraDayService {
    private static final Logger logger = LoggerFactory.getLogger(IntraDayService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    @Autowired
    private StockDao stockDao;

    @Autowired
    private StockDailyDao stockDailyDao;

    @Autowired
    private RestRequest restRequest;

    @Autowired
    private IntraDayPriceDao intraDayPriceDao;

    @Autowired
    private HoldingStockDao holdingStockDao;

    @Autowired
    private ThreadPoolTaskExecutor executorService;

    public void save(HoldingStockVO stockVO) {
        stockVO.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
        holdingStockDao.save(stockVO);

    }

    public Object removedStock() {
        return null;
    }

    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

    public Object getPriceByminute() throws ParseException {
        List<HoldingStockVO> holdingStockDaoAll = holdingStockDao.findAll();

        //etfs need to get minute data as well
        List<StockNameVO> stockDaoAll = stockDao.findAll();
        stockDaoAll.stream().filter(vo -> vo.getStockName().toLowerCase().contains("etf")).forEach(
                etfVo -> {
                    HoldingStockVO holdingStockVO = new HoldingStockVO();
                    holdingStockVO.setStockId(etfVo.getStockId());
                    holdingStockDaoAll.add(holdingStockVO);
                }
        );

        Date today = new Date(System.currentTimeMillis());
        String formattedToday = dateFormat.format(today);
        List<Callable<Void>> tasks = getCallables(holdingStockDaoAll, formattedToday, today);
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
        return null;
    }

    private List<Callable<Void>> getCallables(List<HoldingStockVO> holdingStockDaoAll, String formattedToday, Date today) {
        List<Callable<Void>> tasks = new ArrayList<>();
        for (HoldingStockVO stockVO : holdingStockDaoAll) {
            tasks.add(() -> {
                WebQueryParam webQueryParam = new WebQueryParam();
                webQueryParam.setIdentifier(stockVO.getStockId());
                QueryFromTencentResponseVO intraDayData = restRequest.getIntraDayData(webQueryParam);
                if (null == intraDayData) {
                    return null;
                }
                Map data = (Map) ((Map) intraDayData.getData().get(stockVO.getStockId())).get("data");

                List<String> minutePriceList = (List) data.get("data");
                String date = (String) data.get("date");

                if (!formattedToday.equals(date)) {
                    return null;
                }

                Set<IntradayPriceVO> voSet = intraDayPriceDao.findMinutesByIdAndToday(webQueryParam.getIdentifier(), today);
                Set<String> storedSet = new HashSet<>();
                voSet.forEach(vo -> {
                    storedSet.add(vo.getMinute());
                });
                for (String line : minutePriceList) {
                    String[] split = line.split("\\s+");
                    if (storedSet.contains(split[0])) {
                        continue;
                    }
                    IntradayPriceVO vo = new IntradayPriceVO();
                    vo.setDay(today);
                    vo.setMinute(split[0]);
                    vo.setPrice(new BigDecimal(split[1]));
                    vo.setStockId(webQueryParam.getIdentifier());
                    intraDayPriceDao.save(vo);
                }
                //save latest price in stockDaily table.
                String latestPriceLine = minutePriceList.get(minutePriceList.size() - 1);
                String[] split = latestPriceLine.split("\\s+");
                StockDailyVO stockDailyVO = new StockDailyVO();
                stockDailyVO.setStockId(stockVO.getStockId());
                stockDailyVO.setDay(today);
                stockDailyVO.setClosingPrice(BigDecimal.valueOf(Double.valueOf(split[1])));
                stockDailyDao.save(stockDailyVO);
                return null;
            });
        }
        return tasks;
    }

    public void removeOneWeekAgoData(String oneWeekAgeDay) {
        intraDayPriceDao.removeOneWeekAgoData(oneWeekAgeDay);
    }

    public void clearTodayIntraPrice() {
        String todayDate = Utils.getTodayDate();
        intraDayPriceDao.clearTodayIntraPrice(todayDate);
    }
}

