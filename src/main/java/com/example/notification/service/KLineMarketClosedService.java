package com.example.notification.service;

import com.example.notification.http.RestRequest;
import com.example.notification.repository.StockDailyDao;
import com.example.notification.repository.StockDao;
import com.example.notification.util.Utils;
import com.example.notification.vo.DailyQueryResponseVO;
import com.example.notification.vo.StockDailyVO;
import com.example.notification.vo.StockNameVO;
import com.example.notification.vo.WebQueryParam;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class KLineMarketClosedService {
    private static final Logger logger = LoggerFactory.getLogger(KLineMarketClosedService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    static String importStockFile = "C:\\code\\tools\\notification\\src\\main\\resources\\import_stock.txt";
    static String etfViewFile = "C:\\code\\tools\\notification\\src\\main\\resources\\etfs_view.txt";

    public static Boolean ifMarketOpen = Boolean.FALSE;

    private static ArrayList<String> importStockFileList = new ArrayList<>();
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


    @Value("${notification.import.file}")
    private String importFileInCloud;

    @Value("${notification.etfView.file}")
    private String etfViewFileInCloud;

    @Autowired
    private RestRequest restRequest;

    @Autowired
    private StockDao stockDao;

    @Autowired
    private StockDailyDao stockDailyDao;

    // 1.importStocks,
    // format: stockId_name or stockId

    public String importStocks() throws JsonProcessingException {
        readImportFileStocks();
        List<String> storedStocks = storedStockIds();
        List<String> newImportStock = importStockFileList.stream().filter(stock -> !storedStocks.contains(stock)).collect(Collectors.toList());

        ArrayList<StockNameVO> newToAdd = new ArrayList<>();
        ArrayList<String> wrongInputStockId = new ArrayList<>();
        StringBuilder ret = new StringBuilder("<h2>Input stock wrong, please check: ");
        for (String stockid : newImportStock) {
            StockNameVO stockIdVo = new StockNameVO(stockid);
            DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLine(new WebQueryParam(1, stockid));
            if (dailyQueryResponse == null) {
                wrongInputStockId.add(stockid);
                ret.append(stockid);
                return ret.append("</h2>").toString();
            }
            List<ArrayList<String>> dayPriceList = getDayPriceList(dailyQueryResponse, stockIdVo);
            if (CollectionUtils.isEmpty(dayPriceList)) {
                wrongInputStockId.add(stockid);
                ret.append(stockid);
                return ret.append("</h2>").toString();
            }
            newToAdd.add(stockIdVo);
        }
        if (CollectionUtils.isEmpty(newToAdd)) {
            return "<h2>All are imported, Please check!</h2>";
        }
        stockDao.saveAll(newToAdd);
        return "<h2>Success imported: " + Arrays.asList(newToAdd) + "</h2>";
    }
    public List<String> storedStockIds() {
        List<String> storedStockIds = stockDao.findStockIds();
        return storedStockIds;
    }

    public List<StockNameVO> storedStocks() {
        List<StockNameVO> storedStocks = stockDao.findAll();
        return storedStocks;
    }

    public List<StockNameVO> getAllEtfs() {
        if (CollectionUtils.isEmpty(storedETFs)) {
            List<StockNameVO> stockDaoAll = stockDao.findAll();
            for (StockNameVO id_name : stockDaoAll) {
                if (!id_name.getStockId().toLowerCase().startsWith("s")) continue;
                if (id_name.getStockName().toLowerCase().contains("etf")) {
                    storedETFs.add(id_name);
                }
            }
        }
        return storedETFs;
    }

    private static ArrayList<String> importedFileLine = new ArrayList<>();
    //load stock list
    private void readImportFileStocks() {
        importStockFileList.clear();
        BufferedReader reader = null;
        String line;
        try {
            boolean winSystem = Utils.isWinSystem();
            if (winSystem) {
                importFileInCloud = importStockFile;
            }
            FileReader fileReader = new FileReader(importFileInCloud);
            reader = new BufferedReader(fileReader);
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                importedFileLine.add(line);
                String[] split = line.split(",");
                for (int i = 0; i < split.length; i++) {
                    String str = split[i];
                    String stock_id;
                    if (str.startsWith("s")) {
                        String[] id_name = str.split("_");
                        stock_id = id_name[0].toLowerCase();
//                   }
                        importStockFileList.add(stock_id);
                    }
                }
                logger.info("Successfully read stock name file !!");
            }
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

    private static ArrayList<String> etfViewLine = new ArrayList<>();
    private void readETFFile() {
        BufferedReader reader = null;
        String line;
        try {
            boolean winSystem = Utils.isWinSystem();
            if (winSystem) {
                etfViewFileInCloud = etfViewFile;
            }
            FileReader fileReader = new FileReader(etfViewFileInCloud);
            reader = new BufferedReader(fileReader);
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                etfViewLine.add(line);
                logger.info("Successfully read etfView file !!");
            }
        } catch (IOException e) {
            logger.error("Fail read etfView file !!", e);
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

    public String getHistoryPriceOnLineAndStoreInDb(Integer daysToGet) throws InterruptedException, JsonProcessingException {
        ArrayList<StockNameVO> getHistoryPriceStocks = new ArrayList<>();

        //iterator to query 50day price history and calculate 10day price, and store in db
        List<StockNameVO> storedStocks = storedStocks();
        for (StockNameVO stockNameVO : storedStocks) {
            if (stockNameVO.getGainPercentFive() != null) {
                continue;
            }

            getHistoryPriceStocks.add(stockNameVO);
            //slow down the speech, just intend to avoid website protection
            Thread.sleep(100);
            if (stockNameVO.getStockId().contains("sh") || stockNameVO.getStockId().contains("sz")) {
                WebQueryParam webQueryParam = new WebQueryParam();
                if (daysToGet == null) {
                    daysToGet = 50;
                }
                webQueryParam.setDaysToQuery(daysToGet);
                webQueryParam.setIdentifier(stockNameVO.getStockId());
                DailyQueryResponseVO dailyQueryResponse = restRequest.queryKLine(webQueryParam);
                List<ArrayList<String>> dailyPriceList = storeInDbAndReturnKlines(dailyQueryResponse, stockNameVO);
            }
        }
        return "getHistoryPriceStocks: " + getHistoryPriceStocks.toString();
    }

    private List<ArrayList<String>> storeInDbAndReturnKlines(DailyQueryResponseVO dailyQueryResponse, StockNameVO stockNameVO) throws JsonProcessingException {
        List<ArrayList<String>> dayList = getDayPriceList(dailyQueryResponse, stockNameVO);
        if (dayList == null) return null;

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
                stockDailyVO.setDayGainOfFive(dayDiff);
                beforeDay_FiveDayAvgPrice = fiveDayAvgPrice;
            }
            //get 10 day avg
            if (i >= 9) {
                BigDecimal tenDayAvgPrice = calculateDayAvg(stockNameVO.getStockName(), i, dayList, 10);
                stockDailyVO.setTenDayAvg(tenDayAvgPrice);
                BigDecimal dayDiff = calculateDayGainPercentage(tenDayAvgPrice, beforeDay_TenDayAvgPrice);
                stockDailyVO.setDayGainOfTen(dayDiff);
                beforeDay_TenDayAvgPrice = tenDayAvgPrice;
            }
            stockDailyDao.save(stockDailyVO);
        }


        return dayList;
    }

    private static List<ArrayList<String>> getDayPriceList(DailyQueryResponseVO dailyQueryResponse, StockNameVO stockNameVO) throws JsonProcessingException {
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
        StringBuilder ret = new StringBuilder("");
        for (StockNameVO stockVo : resultList) {
            if (!stockVo.getStockName().toLowerCase().contains("etf")) continue;
            ret.append(stockVo.getStockId() + "_" + stockVo.getStockName() + "<br/>");
        }
        return ret;
    }

    public Object handleStocksAvg() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        logger.debug("enter report ====");
        StringBuilder retStr = new StringBuilder("<h2>Calculated All Stocks: </h2></br>");
        List<StockNameVO> stocks = storedStocks();
        for (StockNameVO stockNameVO : stocks) {
            //loop to calculate each etf
            String stockId = stockNameVO.getStockId();
//            List<Object[]> etfPriceList = entityManager.createNativeQuery("select five_day_avg, five_day_diff, ten_day_avg, ten_day_diff from daily_price where stock_id=?1 limit 60 ").setParameter(1, stockId).;
            List<StockDailyVO> etfPriceList = stockDailyDao.findByStockId(stockId, Pageable.ofSize(60)).stream().toList();
            List<Integer> flipDayFive = analysisFlipDay(etfPriceList, "getDayGainOfFive");
            List<Integer> flipDayTen = analysisFlipDay(etfPriceList, "getDayGainOfTen");
            setUpwardDaysAndGain(etfPriceList, flipDayFive.get(0), flipDayFive.get(1), stockNameVO, "Five");
            setUpwardDaysAndGain(etfPriceList, flipDayTen.get(0), flipDayTen.get(1), stockNameVO, "Ten");
            stockDao.save(stockNameVO);
            retStr.append(stockNameVO.getStockId() + "_" + stockNameVO.getStockName() + "</br>");
        }
        return retStr;
    }

    private List<Integer> analysisFlipDay(List<StockDailyVO> etfPriceList, String methodName) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        if (etfPriceList.size() == 0) return null;
        Integer flipBeginIndex = 0;
        Integer flipEndIndex = 0;
        Integer loopCount = 0;
        Boolean countFlip = Boolean.FALSE;
        Boolean todayKLineUpward = Boolean.TRUE;

        StockDailyVO today = etfPriceList.get(etfPriceList.size() - 1);
        Method getDayGainMethod = today.getClass().getMethod(methodName);
        BigDecimal gainValue = (BigDecimal) getDayGainMethod.invoke(today);
        if (gainValue.compareTo(BigDecimal.ZERO) < 0) {
            //today is downward!
            todayKLineUpward = Boolean.FALSE;
        }
        //set today upwardDay count = 0!
        for (int index = etfPriceList.size() - 1; index > 0; index--) {
            StockDailyVO indexDay = etfPriceList.get(index);
            loopCount++;
            BigDecimal dayGainPercent = (BigDecimal) getDayGainMethod.invoke(indexDay);
            if (dayGainPercent == null) {
                flipBeginIndex = loopCount;
                break;
            }
            boolean indexDayPositive = dayGainPercent.compareTo(BigDecimal.ZERO) >= 0;
            if(countFlip == Boolean.TRUE){
                indexDayPositive = dayGainPercent.compareTo(BigDecimal.ZERO) > 0;
            }
            if (!todayKLineUpward.equals(indexDayPositive)) {
                //start to find adjustment days, turn the opposite
                if (countFlip.equals(Boolean.FALSE)) {
                    flipBeginIndex = loopCount - 1;
                }
                if (countFlip.equals(Boolean.TRUE)) {
                    flipEndIndex = loopCount - 1;
                    break;
                }
                countFlip = Boolean.TRUE;
                if (todayKLineUpward.equals(Boolean.TRUE)) {
                    todayKLineUpward = Boolean.FALSE;
                } else {
                    todayKLineUpward = Boolean.TRUE;
                }
            }
        }
        List<Integer> flipDayList = Arrays.asList(flipBeginIndex, flipEndIndex);
        return flipDayList;
    }

    private void setUpwardDaysAndGain(List<StockDailyVO> etfPriceList, Integer flipBeginIndex, Integer flipEndIndex, StockNameVO stockNameVO, String dayIdentify) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        System.out.println("flipBeginIndex = " + flipBeginIndex);
        System.out.println("flipEndIndex = " + flipEndIndex);
        System.out.println("dayIdentify = " + dayIdentify);
        int listSize = etfPriceList.size();
        StockDailyVO today = etfPriceList.get(etfPriceList.size() - 1);
        StockDailyVO flipDay = etfPriceList.get(listSize - flipBeginIndex - 1);
        BigDecimal gainPercent = getGainPercent(today, flipDay);

        StockDailyVO flipEndDay = etfPriceList.get(listSize - flipEndIndex - 1);
        BigDecimal gainPercentFlip = getGainPercent(flipDay, flipEndDay);

        String methodName = "getDayGainOf" + dayIdentify;
        Method getDayGainMethod = today.getClass().getMethod(methodName);
        BigDecimal gainValue = (BigDecimal) getDayGainMethod.invoke(today);
        boolean todayUpward = gainValue.compareTo(BigDecimal.ZERO) >= 0;

        int flipUpwardDays = flipBeginIndex - flipEndIndex;
        if (!todayUpward) {
            flipBeginIndex = -flipBeginIndex;
            flipUpwardDays = -flipUpwardDays;
        }

        Class<? extends StockNameVO> aClass = stockNameVO.getClass();
        aClass.getMethod("setUpwardDays" + dayIdentify, Integer.class).invoke(stockNameVO, flipBeginIndex);
        aClass.getMethod("setGainPercent" + dayIdentify, BigDecimal.class).invoke(stockNameVO, gainPercent);
        aClass.getMethod("setFlipUpwardDays" + dayIdentify, Integer.class).invoke(stockNameVO, flipUpwardDays);
        aClass.getMethod("setFlipGainPercent" + dayIdentify, BigDecimal.class).invoke(stockNameVO, gainPercentFlip);
    }

    private BigDecimal getGainPercent(StockDailyVO newPrice, StockDailyVO oldPrice) {
        BigDecimal subtract = newPrice.getFiveDayAvg().subtract(oldPrice.getFiveDayAvg()).multiply(BigDecimal.valueOf(100));
        System.out.println("newPrice ========== " + newPrice);
        System.out.println("oldPrice ========== " + oldPrice);
        BigDecimal gainPercentage = subtract.divide(oldPrice.getFiveDayAvg(), 2, BigDecimal.ROUND_HALF_UP);
        return gainPercentage;
    }

    private BigDecimal getGainPercent(List<StockDailyVO> etfPriceList, int index) {
        StockDailyVO todayPrice = etfPriceList.get(etfPriceList.size() - 1);
        StockDailyVO beforePrice = etfPriceList.get(etfPriceList.size() - 1 - index);
        if (beforePrice.getFiveDayAvg().equals(BigDecimal.ZERO)) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtract = todayPrice.getFiveDayAvg().subtract(beforePrice.getFiveDayAvg()).multiply(BigDecimal.valueOf(100));
        System.out.println("todayPrice ========== " + todayPrice);
        System.out.println("beforePrice ========== " + beforePrice);
        BigDecimal gainPercentage = subtract.divide(beforePrice.getFiveDayAvg(), 2, BigDecimal.ROUND_HALF_UP);
        return gainPercentage;
    }


    public Object etfsCurveView() {
        logger.debug("enter listImport ====");
        if (CollectionUtils.isEmpty(etfViewLine)) {
            readETFFile();
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String fileLine : etfViewLine) {
            stringBuilder.append("<tr>");
            String[] split = fileLine.split(",");
            for (int index = 0; index < split.length; index++) {
//                stringBuilder.append("<td><a href=\"javascript:addOrRemove(").append(split[index]).append(")\">").append(split[index]).append("</a>").append(",").append("</td>");
                String str = split[index];
                int eftIndex = str.indexOf("ETF");
                if (eftIndex > 0) {
                    str = str.substring(0, eftIndex + 3);
                }
                stringBuilder.append("<span class=\"cell\" onclick=\"changeColor(this)\">").append(str).append("</span>");
            }
            stringBuilder.append("</tr>");
            stringBuilder.append("</br>");
        }
        return stringBuilder;
    }
}

