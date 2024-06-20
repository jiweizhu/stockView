package com.example.notification.service;

import com.example.notification.repository.HoldingStockDao;
import com.example.notification.repository.IntraDayPriceDao;
import com.example.notification.repository.StockDailyDao;
import com.example.notification.repository.StockDao;
import com.example.notification.responseVo.EtfsRespVO;
import com.example.notification.responseVo.HoldingStockViewRespVO;
import com.example.notification.responseVo.StockRespVO;
import com.example.notification.util.Utils;
import com.example.notification.vo.HoldingStockVO;
import com.example.notification.vo.IntradayPriceVO;
import com.example.notification.vo.StockDailyVO;
import com.example.notification.vo.StockNameVO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
public class HoldingService {
    private static final Logger logger = LoggerFactory.getLogger(HoldingService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static Map<String, String> STOCK_ID_NAME_MAP = new HashMap<>();

    @Autowired
    private IntraDayPriceDao intraDayPriceDao;

    @Autowired
    private StockDailyDao stockDailyDao;

    @Autowired
    private StockDao stockDao;

    @Autowired
    private HoldingStockDao holdingStockDao;


    public String getStockIdOrNameByMap(String id_or_name) {
        String ret = STOCK_ID_NAME_MAP.get(id_or_name);
        if (StringUtils.hasText(ret)) {
            return ret;
        }
        List<StockNameVO> voList = stockDao.findAll();
        voList.stream().filter(vo -> !STOCK_ID_NAME_MAP.containsKey(vo.getStockId()) || !STOCK_ID_NAME_MAP.containsKey(vo.getStockName()))
                .forEach(vo -> {
                    STOCK_ID_NAME_MAP.put(vo.getStockId(), vo.getStockName());
                    STOCK_ID_NAME_MAP.put(vo.getStockName(), vo.getStockId());
                });
        return STOCK_ID_NAME_MAP.get(id_or_name);
    }


    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd HH:mm:ss");
    DateTimeFormatter FORMATTER_MMDD = DateTimeFormatter.ofPattern("MM-dd");

    public Object datagridList() {
        logger.debug("enter HoldingService list ====");
        List<HoldingStockVO> resultList = holdingStockDao.findAll();
        List<HoldingStockViewRespVO> retList = new ArrayList<>();
        for (HoldingStockVO holdingStockVO : resultList) {
            IntradayPriceVO newestPriceVo = intraDayPriceDao.findLastestPriceById(holdingStockVO.getStockId());
            if (newestPriceVo == null) {
                continue;
            }
            BigDecimal newestPrice = newestPriceVo.getPrice();
            holdingStockVO.setNowPrice(newestPrice);
            BigDecimal gainPercentage = Utils.calculateDayGainPercentage(newestPrice, holdingStockVO.getCostPrice());
            holdingStockVO.setGainPercent(gainPercentage);
            List<StockDailyVO> lastDayPriceByStockId = stockDailyDao.findLastTwoDayPriceByStockId(holdingStockVO.getStockId());
            String lastCloseDay = null;
            for (StockDailyVO stockDailyVO : lastDayPriceByStockId) {
                if (!stockDailyVO.getDay().equals(newestPriceVo.getDay())) {
                    holdingStockVO.setLastClosePrice(stockDailyVO.getClosingPrice());
                    lastCloseDay = stockDailyVO.getDay().toLocalDate().format(FORMATTER_MMDD);
                    break;
                }
            }
            BigDecimal oneDayGain = Utils.calculateDayGainPercentage(newestPrice, holdingStockVO.getLastClosePrice());
            holdingStockVO.setOneDayGain(oneDayGain);
            holdingStockDao.save(holdingStockVO);

            // to response
            HoldingStockViewRespVO target = new HoldingStockViewRespVO();
            BeanUtils.copyProperties(holdingStockVO, target);
            LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(holdingStockVO.getLastUpdatedTime().getTime()), ZoneOffset.of("+8"));
            target.setLastUpdatedTime(localDateTime.format(formatter));
            target.setNowPrice(newestPriceVo.getPrice() + "(" + newestPriceVo.getMinute() + ")");
            target.setLastClosePrice(holdingStockVO.getLastClosePrice() + "|" + lastCloseDay);
            target.setOneDayGain(oneDayGain);
            StockNameVO stock = stockDao.findById(holdingStockVO.getStockId()).get();
            if (stock.getBelongEtf() != null) {
                Optional<StockNameVO> belongEtfOpt = stockDao.findById(stock.getBelongEtf());
                StockNameVO belongEtfVo = belongEtfOpt.get();
                String belongEtfName = getStockIdOrNameByMap(belongEtfVo.getStockId());
                Integer upwardDaysFive = belongEtfVo.getUpwardDaysFive();
                Integer flipUpwardDaysFive = belongEtfVo.getFlipUpwardDaysFive();
                belongEtfName = belongEtfName + "(" + upwardDaysFive + "|" + flipUpwardDaysFive + ")";
                belongEtfName = belongEtfName.toLowerCase().replace("etf", "");
                target.setBelongEtf(belongEtfName);
            }
            retList.add(target);
        }
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("total", retList.size());
        retMap.put("rows", retList);
        return retMap;
    }

    public Object listEtfsForComboBox() {
        logger.debug("enter HoldingService listEtfsForComboBox ====");
        List<StockNameVO> resultList = stockDao.findAll();
        List<EtfsRespVO> retList = new ArrayList<>();
        for (StockNameVO stockVo : resultList) {
            if (!stockVo.getStockName().toLowerCase().contains("etf")) continue;
            String stockIds = stockVo.getStockIds();
            if (StringUtils.hasLength(stockIds)) {
                StringBuilder sb = new StringBuilder();
                String[] split = stockIds.split(",");
                Arrays.stream(split).forEach(
                        stockId -> {
                            String stockName = getStockIdOrNameByMap(stockId);
                            sb.append(stockName).append(",");
                        }
                );
                stockIds = sb.toString();
            }
            retList.add(new EtfsRespVO(stockVo.getStockId(), stockVo.getStockName(), stockIds));
        }
        return retList;
    }

    public void save(HoldingStockVO stockVO) {
        stockVO.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
        holdingStockDao.save(stockVO);

    }


    public Object stockList() {
        logger.debug("enter HoldingService stockList ====");
        Set<String> holdingStockVOS = holdingStockDao.findAllStockIds();
        List<StockNameVO> resultList = stockDao.findAll();
        List<StockRespVO> retList = new ArrayList<>();
        for (StockNameVO stockVo : resultList) {
            if (holdingStockVOS.contains(stockVo.getStockId()) || stockVo.getStockName().toLowerCase().contains("etf"))
                continue;
            retList.add(new StockRespVO(stockVo.getStockId(), stockVo.getStockName()));
        }
        return retList;
    }

    public void delete(String stockId) {
        holdingStockDao.deleteById(stockId);
    }
}

