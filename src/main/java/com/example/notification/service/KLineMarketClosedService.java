package com.example.notification.service;

import com.example.notification.http.RestRequest;
import com.example.notification.repository.StockDailyDao;
import com.example.notification.repository.StockDao;
import com.example.notification.util.Utils;
import com.example.notification.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Service
public class KLineMarketClosedService {
    private static final Logger logger = LoggerFactory.getLogger(KLineMarketClosedService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static String localTestStockFile = "C:\\code\\tools\\notification\\src\\main\\resources\\sNum_test.txt";

    public static Boolean ifMarketOpen = Boolean.FALSE;

    private static ArrayList<StockNameVO> stockFileList = new ArrayList<>();
    private static ArrayList<StockNameVO> storedETFs = new ArrayList<>();

    private static Map<String, StockNameVO> exceedFiveDayMap = new HashMap<>();
    private static Map<String, StockNameVO> exceedTenDayMap = new HashMap<>();
    private static Map<String, StockNameVO> downFiveDayMap = new HashMap<>();
    private static Map<String, StockNameVO> downTenDayMap = new HashMap<>();
    private static Map<String, List<ArrayList<String>>> daysPriceMap = new HashMap<>();

    public static void clearCollect() {
        exceedTenDayMap.clear();
        downTenDayMap.clear();
    }

    @Value("${notification.monitor.file}")
    private String stockFile;

    @Autowired
    private RestRequest restRequest;

    @Autowired
    private StockDao stockDao;

    @Autowired
    private StockDailyDao stockDailyDao;


    // 1.importStocks
    public void importStocks() {
        readStockFile();
        stockDao.saveAll(stockFileList);
    }

    public List<StockNameVO> getAllEtfs() {
        if (CollectionUtils.isEmpty(storedETFs)) {
            List<StockNameVO> stockDaoAll = stockDao.findAll();
            for (StockNameVO id_name : stockDaoAll) {
                if (!id_name.getStockId().startsWith("s")) continue;
                if (id_name.getStockName().toLowerCase().contains("etf")) {
                    storedETFs.add(id_name);
                }
            }
        }
        return storedETFs;
    }


    //load stock list
    private void readStockFile() {
        if (!CollectionUtils.isEmpty(stockFileList)) {
            return;
        }
        stockFileList.clear();
        BufferedReader reader = null;
        String line;
        try {
            boolean winSystem = Utils.isWinSystem();
            if (winSystem) {
                stockFile = localTestStockFile;
            }
            FileReader fileReader = new FileReader(stockFile);
            reader = new BufferedReader(fileReader);
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                String[] split = line.split(",");
                for (int i = 0; i < split.length; i++) {
                    String str = split[i];
                    StockNameVO stockNameVO = new StockNameVO();
                    if (str.startsWith("s")) {
                        String[] id_name = str.split("_");
                        stockNameVO.setStockId(id_name[0].toLowerCase());
                        stockNameVO.setStockName(id_name[1]);
                    } else {
                        stockNameVO.setStockId(str.toLowerCase());
                    }
                    stockFileList.add(stockNameVO);
                }
            }
            logger.info("Successfully read stock name file !!");
        } catch (IOException e) {
            logger.error("Fail read stock name file !!", e);
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
                logger.error("error occurs. ", e);
            }
        }
    }

    //2.getDaysPriceOnLineAndStoreInDb
    public void getDaysPriceOnLineAndStoreInDb() throws InterruptedException, JsonProcessingException {
        readStockFile();
        //iterator to query 20day price history and calculate 10day 20day price, and store in db
        for (StockNameVO stockNameVO : stockFileList) {
            //slow down the speech, just intend to avoid website protection
            Thread.sleep(100);
            if (stockNameVO.getStockId().contains("sh") || stockNameVO.getStockId().contains("sz")) {
                WebQueryParam webQueryParam = new WebQueryParam();
                webQueryParam.setIdentifier(stockNameVO.getStockId());
                DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLine(webQueryParam);
                List<ArrayList<String>> dailyPriceList = storeInDbAndReturnKlines(dailyQueryResponse, stockNameVO);
            }
        }
    }

    private List<ArrayList<String>> storeInDbAndReturnKlines(DailyQueryResponseVO dailyQueryResponse, StockNameVO stockNameVO) throws JsonProcessingException {
        Map<String, Object> dataMap = (Map) dailyQueryResponse.getData().get(stockNameVO.getStockId().toLowerCase());
        Object dayListObj = dataMap.get("day");
        if (Objects.isNull(dayListObj)) {
            dayListObj = dataMap.get("qfqday");
        }
        List<ArrayList<String>> dayList = objectMapper.readValue(objectMapper.writeValueAsString(dayListObj), List.class);
        if (dayList.size() == 0) {
            logger.error("==============ERROR ===== Input Stock Name must be wrong: " + stockNameVO.getStockId());
            logger.error("==============ERROR ====try this url ==https://web.ifzq.gtimg.cn/appstock/app/fqkline/get?_var=kline_dayhfq&param=" + stockNameVO.getStockId() + ",day,,,10,qfq");
            return null;
        }
        extractStockName(stockNameVO, dataMap);
        daysPriceMap.put(stockNameVO.getStockId(), dayList);

        BigDecimal beforeDay_FiveDayAvgPrice = null;
        BigDecimal beforeDay_TenDayAvgPrice = null;
        for (int i = 0; i < dayList.size(); i++) {
            ArrayList<String> dayPrice = dayList.get(i);
            StockDailyVO stockDailyVO = new StockDailyVO(stockNameVO.getStockId(), dayPrice.get(0), dayPrice.get(1), dayPrice.get(2), dayPrice.get(3), dayPrice.get(4));
            //get 5 day avg, before 4 days, set to 0
            if (i >= 4) {
                BigDecimal fiveDayAvgPrice = calculateDayAvg(stockNameVO.getStockName(), i, dayList, 5);
                stockDailyVO.setFiveDayAvg(fiveDayAvgPrice);
                BigDecimal dayDiff = calculateDayGainPercentage(fiveDayAvgPrice, beforeDay_FiveDayAvgPrice);
                stockDailyVO.setFiveDayDiff(dayDiff);
                beforeDay_FiveDayAvgPrice = fiveDayAvgPrice;
            }
            //get 10 day avg
            if (i >= 9) {
                BigDecimal tenDayAvgPrice = calculateDayAvg(stockNameVO.getStockName(), i, dayList, 10);
                stockDailyVO.setTenDayAvg(tenDayAvgPrice);
                BigDecimal dayDiff = calculateDayGainPercentage(tenDayAvgPrice, beforeDay_TenDayAvgPrice);
                stockDailyVO.setTenDayDiff(dayDiff);
                beforeDay_TenDayAvgPrice = tenDayAvgPrice;
            }

            stockDailyDao.save(stockDailyVO);
        }


        return dayList;
    }

    private BigDecimal calculateDayGainPercentage(BigDecimal dayAvgPrice, BigDecimal beforeDayAvgPrice) {
        if (beforeDayAvgPrice == null) {
            return new BigDecimal(0);
        }
        BigDecimal subtract = dayAvgPrice.subtract(beforeDayAvgPrice).multiply(BigDecimal.valueOf(100));
        BigDecimal gainPercentage = subtract.divide(beforeDayAvgPrice, 2, BigDecimal.ROUND_HALF_UP);
        return gainPercentage;
    }

    private BigDecimal calculateDayAvg(String stockName, int index, List<ArrayList<String>> dailyPriceList, Integer dayCount) {
        if (dayCount > dailyPriceList.size()) {
            return BigDecimal.valueOf(0);
        }
        BigDecimal totalPrice = new BigDecimal(0);
        for (int y = 0; y < dayCount; y++) {
            List<String> day = dailyPriceList.get(index - y);
            BigDecimal dayEndPrice = new BigDecimal(day.get(2));
            totalPrice = totalPrice.add(dayEndPrice);
        }
        //keep three decimals
        BigDecimal avgPrice = totalPrice.divide(BigDecimal.valueOf(dayCount), 3, RoundingMode.HALF_UP);
        String lowerCase = stockName.toLowerCase();
        if (!lowerCase.contains("etf") && !lowerCase.contains("lof")) {
            avgPrice = avgPrice.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return avgPrice;
    }


    private static void extractStockName(StockNameVO stockNameVO, Map<String, Object> dataMap) {
        List identifierList = (List) ((Map) dataMap.get("qt")).get(stockNameVO.getStockId());
        Object stockChineseNameObject = identifierList.get(1);
        String stockChineseName = stockChineseNameObject.toString();
        stockNameVO.setStockName(stockChineseName);
    }

    @PersistenceContext
    private EntityManager entityManager;

    //3.calculate avg
    public void calculateAvg() {
        // query all stockIds from daily_price table
        List<String> stockIds = entityManager.createQuery("select distinct stock_id from daily_price;").getResultList();
        ;
        //query the newest one and check if avg exist, if yes, return.
        for (String stockId : stockIds) {
            String fiveDayPrice = (String) entityManager.createQuery("select five_day_avg from daily_price where stock_id=?1 and five_day_avg is null order by day limit 1 ;").setParameter(1, stockId).getSingleResult();
            if (!StringUtils.hasLength(fiveDayPrice)) {
                //need to calculate
            }
        }


        //if not, query last 5 or 10 days price, calculate and in db.


    }

    public Object stockJsonData(String stockId) throws JsonProcessingException {
        logger.debug("enter stockJsonData stockId=============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }
        List resultList = entityManager.createNativeQuery("select day, closing_price from daily_price where stock_id=?1 ").setParameter(1, stockId).getResultList();

        return objectMapper.writeValueAsString(resultList);
    }

    public Object listEtfs() throws JsonProcessingException {
        logger.debug("enter listEtfs ====");
        List<StockNameVO> resultList = stockDao.findAll();
        StringBuffer ret = new StringBuffer("");
        for (StockNameVO stockVo : resultList) {
            if(!stockVo.getStockName().toLowerCase().contains("etf")) continue;
            ret.append(stockVo.getStockId() + "_" + stockVo.getStockName() + "<br/>");
        }
        return ret;
    }

    public Object report() {
        logger.debug("enter report ====");
        List<StockNameVO> allEtfs = getAllEtfs();
        for (StockNameVO stockNameVO : allEtfs) {
            //loop to calculate each etf
            String stockId = stockNameVO.getStockId();
            List<StockDailyVO> etfPriceList = entityManager.createNativeQuery("select day, closing_price from daily_price where stock_id=?1 limit 60 ").setParameter(1, stockId).getResultList();
            analysisData(etfPriceList, 5);
        }
        return null;
    }

    private void analysisData(List<StockDailyVO> etfPriceList, Integer dayToAnalysis) {
        if (etfPriceList.size() == 0) return;
        BigDecimal daydiff;
        for (int i = etfPriceList.size() - 1; i > dayToAnalysis; i--) {
            StockDailyVO day = etfPriceList.get(i);
            StockDailyVO beforeDay = etfPriceList.get(i - 1);
            daydiff = day.getClosingPrice().subtract(beforeDay.getClosingPrice());
            if (daydiff.compareTo(BigDecimal.ZERO) <= 0) {

            }
        }
    }

}

