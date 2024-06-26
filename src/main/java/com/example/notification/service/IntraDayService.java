package com.example.notification.service;

import com.example.notification.http.RestRequest;
import com.example.notification.repository.HoldingStockDao;
import com.example.notification.repository.IntraDayPriceDao;
import com.example.notification.repository.StockDao;
import com.example.notification.util.Utils;
import com.example.notification.vo.DailyQueryResponseVO;
import com.example.notification.vo.HoldingStockVO;
import com.example.notification.vo.IntradayPriceVO;
import com.example.notification.vo.WebQueryParam;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class IntraDayService {
    private static final Logger logger = LoggerFactory.getLogger(IntraDayService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    @Autowired
    private StockDao stockDao;

    @Autowired
    private RestRequest restRequest;

    @Autowired
    private IntraDayPriceDao intraDayPriceDao;

    @Autowired
    private HoldingStockDao holdingStockDao;


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
        Date today = new Date(System.currentTimeMillis());
        for (HoldingStockVO stockVO : holdingStockDaoAll) {
            WebQueryParam webQueryParam = new WebQueryParam();
            webQueryParam.setIdentifier(stockVO.getStockId());
            DailyQueryResponseVO intraDayData = restRequest.getIntraDayData(webQueryParam);
            if (null == intraDayData) {
                continue;
            }
            Map data = (Map) ((Map) intraDayData.getData().get(stockVO.getStockId())).get("data");

            List<String> minutePriceList = (List) data.get("data");
            String date = (String) data.get("date");
            java.util.Date parse = dateFormat.parse(date);
            Date remoteRetureDate = new Date(parse.getTime());

            Set<IntradayPriceVO> voSet = intraDayPriceDao.findMinutesByIdAndToday(webQueryParam.getIdentifier(), today);
            Set<String> storedSet = new HashSet<>();
            voSet.forEach(vo -> {
                storedSet.add(vo.getDay() + vo.getMinute());
            });
            for (String line : minutePriceList) {
                String[] split = line.split("\\s+");
                if (storedSet.contains(remoteRetureDate + split[0])) {
                    continue;
                }
                IntradayPriceVO vo = new IntradayPriceVO();
                vo.setDay(today);
                vo.setMinute(split[0]);
                vo.setPrice(new BigDecimal(split[1]));
                vo.setStockId(webQueryParam.getIdentifier());
                intraDayPriceDao.save(vo);
            }
        }
        return null;
    }

    public void removeOneWeekAgoData(String oneWeekAgeDay) {
        intraDayPriceDao.removeOneWeekAgoData(oneWeekAgeDay);
    }

    public void clearTodayIntraPrice() {
        String todayDate = Utils.getTodayDate();
        intraDayPriceDao.clearTodayIntraPrice(todayDate);
    }
}

