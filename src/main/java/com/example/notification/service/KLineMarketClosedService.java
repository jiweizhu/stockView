package com.example.notification.service;

import com.example.notification.constant.Constants;
import com.example.notification.http.RestRequest;
import com.example.notification.repository.StockDailyDao;
import com.example.notification.repository.StockDao;
import com.example.notification.repository.WeeklyPriceDao;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Service
public class KLineMarketClosedService {
    private static final Logger logger = LoggerFactory.getLogger(KLineMarketClosedService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private static ArrayList<String> importStockFileList = new ArrayList<>();
    private static ArrayList<StockNameVO> storedETFs = new ArrayList<>();

    @Value("${notification.import.file}")
    private String importFileInCloud;

    @Autowired
    private HoldingService holdingService;

    @Autowired
    private RestRequest restRequest;

    @Autowired
    private StockDao stockDao;

    @Autowired
    private ThreadPoolTaskExecutor executorService;

    @Autowired
    private StockDailyDao stockDailyDao;

    @Autowired
    private WeeklyPriceDao weeklyPriceDao;


    // 1.importStocks,
    // format: stockId_name or stockId
    public String importStocks() throws JsonProcessingException {
        readImportFileStocks();
        List<String> storedStocks = storedStockIds();
        List<String> newImportStock = importStockFileList.stream().filter(stock -> !storedStocks.contains(stock)).collect(Collectors.toList());

        return addNewInTable(newImportStock);
    }

    public String addNewInTable(List<String> newImportStock) throws JsonProcessingException {
        ArrayList<StockNameVO> newToAdd = new ArrayList<>();
        ArrayList<String> wrongInputStockId = new ArrayList<>();
        StringBuilder ret = new StringBuilder("<h2>Input stock wrong, please check: ");
        for (String stockid : newImportStock) {
            stockid = stockid.toLowerCase();
            StockNameVO stockIdVo = new StockNameVO(stockid);
            QueryFromTencentResponseVO dailyQueryResponse = restRequest.queryKLine(new WebQueryParam(1, stockid));
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
        return stockDao.findStockIds();
    }

    public List<StockNameVO> storedStocks() {
        return stockDao.findAll();
    }

    public List<StockNameVO> getAllEtfs() {
        storedETFs.clear();
        List<StockNameVO> stockDaoAll = stockDao.findAll();
        for (StockNameVO id_name : stockDaoAll) {
            if (!id_name.getStockId().toLowerCase().startsWith("s")) continue;
            if (id_name.getStockName().toLowerCase().contains("etf")) {
                storedETFs.add(id_name);
            }
            storedETFs.add(id_name);
        }
        return storedETFs;
    }

    private static ArrayList<String> importedFileLine = new ArrayList<>();

    public ArrayList<String> getMainKlineIds() {
        if (CollectionUtils.isEmpty(importedFileLine)) {
            readImportFileStocks();
        }
        String line = importedFileLine.get(0);
        String[] split = line.split(",");
        ArrayList<String> stockIds = new ArrayList<>();
        for (String id_name : split) {
            String stockId = id_name.split("_")[0];
            stockIds.add(stockId);
        }
        return stockIds;
    }

    //load stock list
    private void readImportFileStocks() {
        importStockFileList.clear();
        importedFileLine.clear();
        BufferedReader reader = null;
        String line;
        try {
            FileReader fileReader = new FileReader(importFileInCloud);
            reader = new BufferedReader(fileReader);
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("#")) continue;
                importedFileLine.add(line);
                String stock_id;
                if (line.toLowerCase().startsWith("s") && !line.startsWith("#")) {
                    //split by 
                    String[] commaSplit = line.split(",");
                    if (commaSplit.length > 1) {
                        for (String comma_split_str : commaSplit) {
                            String[] id_name = comma_split_str.split("_");
                            stock_id = id_name[0].toLowerCase();
                            importStockFileList.add(stock_id);
                        }
                    } else {
                        String[] id_name = line.split("_");
                        stock_id = id_name[0].toLowerCase();
                        importStockFileList.add(stock_id);
                    }
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

    public void getWeekHistoryPriceAndStoreInDb(Integer days) {
        logger.info("Enter getWeekHistoryPriceAndStoreInDb method ========days=" + days);
        final Integer daysToGet = days;
        List<Callable<Void>> tasks = new ArrayList<>();
        //iterator to query 50day price history and calculate 10day price, and store in db
//        List<StockNameVO> etfs = getAllEtfs();
        List<StockNameVO> etfs = stockDao.findAll();
        for (StockNameVO etfVO : etfs) {
            tasks.add(() -> {
                // 降低速度，避免网站保护
                Thread.sleep(10);
                WebQueryParam webQueryParam = new WebQueryParam();
                webQueryParam.setDaysToQuery(daysToGet);
                webQueryParam.setIdentifier(etfVO.getStockId());
                storeHistoryWeeklyPrice(webQueryParam, etfVO);
                return null;
            });
        }

        try {
            List<Future<Void>> futures = executorService.getThreadPoolExecutor().invokeAll(tasks);
            for (Future<Void> future : futures) {
                try {
                    future.get(); // 检查每个任务的执行情况
                } catch (ExecutionException e) {
                    logger.error("Error executing task", e.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Task execution interrupted", e);
        }
    }

    public String getHistoryPriceOnLineAndStoreInDb(Integer days) {
        logger.info("Enter getHistoryPriceOnLineAndStoreInDb method ========days=" + days);
        ArrayList<StockNameVO> getHistoryPriceStocks = new ArrayList<>();
        final Integer daysToGet = days;
        List<Callable<Void>> tasks = new ArrayList<>();
        //iterator to query 50day price history and calculate 10day price, and store in db
        List<StockNameVO> storedStocks = storedStocks();
        for (StockNameVO stockNameVO : storedStocks) {
            tasks.add(() -> {
                if (stockNameVO.getGainPercentFive() != null) {
                    logger.info("stockNameVO.getGainPercentFive ========return=");
                    return null;
                }

                synchronized (getHistoryPriceStocks) {
                    getHistoryPriceStocks.add(stockNameVO);
                }

                // 降低速度，避免网站保护
                Thread.sleep(10);

                if (stockNameVO.getStockId().startsWith("sh") || stockNameVO.getStockId().startsWith("sz")) {
                    WebQueryParam webQueryParam = new WebQueryParam();
                    webQueryParam.setDaysToQuery(daysToGet);
                    webQueryParam.setIdentifier(stockNameVO.getStockId());
                    QueryFromTencentResponseVO dailyQueryResponse = restRequest.queryKLine(webQueryParam);
                    storeHistoryDailyPrice(dailyQueryResponse, stockNameVO);
                }
                return null;
            });
        }

        try {
            List<Future<Void>> futures = executorService.getThreadPoolExecutor().invokeAll(tasks);
            for (Future<Void> future : futures) {
                try {
                    future.get(); // 检查每个任务的执行情况
                } catch (ExecutionException e) {
                    logger.error("Error executing task", e.getCause());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Task execution interrupted", e);
        }

        return "getHistoryPriceStocks: " + getHistoryPriceStocks;
    }

    private List<ArrayList<String>> storeHistoryWeeklyPrice(WebQueryParam webQueryParam, StockNameVO stockNameVO) throws JsonProcessingException {
        webQueryParam.setIdentifier(stockNameVO.getStockId());
        webQueryParam.setToQueryDailyPrice(false);
        QueryFromTencentResponseVO weeklyResponse = restRequest.queryKLine(webQueryParam);
        List<ArrayList<String>> dayList = getWeeklyPriceList(weeklyResponse, stockNameVO);

        //use hashmap to judge if the day is stored or not
        List<String> days = weeklyPriceDao.findWeekDaysByStockId(stockNameVO.getStockId());
        Set<String> daysSet = days.stream().collect(Collectors.toSet());

        if (dayList == null) return null;
        int size = dayList.size();
        logger.info("==storeHistoryWeeklyPrice==={} ==got {} week, == lastest week is {}", stockNameVO, size, dayList.get(size - 1).get(0));
        for (int i = 0; i < size; i++) {
            ArrayList<String> dayPrice = dayList.get(i);
            String stockName = stockNameVO.getStockName();
            if (stockName == null) {
                continue;
            }
            if (daysSet.contains(dayPrice.get(0))) {
                //history day already stored
                continue;
            }
            WeekPriceVO weekPriceVO = new WeekPriceVO(stockNameVO.getStockId(), dayPrice.get(0), dayPrice.get(1), dayPrice.get(2), dayPrice.get(3), dayPrice.get(4));
            weeklyPriceDao.save(weekPriceVO);
        }
        return null;
    }

    private static List<ArrayList<String>> getWeeklyPriceList(QueryFromTencentResponseVO responseVO, StockNameVO stockNameVO) throws JsonProcessingException {
        Map<String, Object> dataMap = (Map) responseVO.getData().get(stockNameVO.getStockId().toLowerCase());
        Object dayListObj = dataMap.get("week");
        if (Objects.isNull(dayListObj)) {
            dayListObj = dataMap.get("qfqweek");
        }
        List<ArrayList<String>> dayList = objectMapper.readValue(objectMapper.writeValueAsString(dayListObj), List.class);
        if (dayList.isEmpty()) {
            logger.error("==============ERROR ===== Input Stock Name must be wrong: " + stockNameVO.getStockId());
            logger.error("==============ERROR ====Response is =====" + responseVO);
            return null;
        }
        return dayList;
    }

    private List<ArrayList<String>> storeHistoryDailyPrice(QueryFromTencentResponseVO dailyQueryResponse, StockNameVO stockNameVO) throws JsonProcessingException {
        List<ArrayList<String>> dayList = getDayPriceList(dailyQueryResponse, stockNameVO);

        //use hashmap to judge if the day is stored or not
        List<String> days = stockDailyDao.findStockDaysByStockId(stockNameVO.getStockId());
        Set<String> daysSet = days.stream().collect(Collectors.toSet());

        if (dayList == null) return null;
        BigDecimal beforeDay_FiveDayAvgPrice = null;
        BigDecimal beforeDay_TenDayAvgPrice = null;
        int size = dayList.size();
        logger.info("==storeHistoryPrice==={} ==got {} days, == lastest day is {}", stockNameVO, size, dayList.get(size - 1).get(0));
        for (int i = 0; i < size; i++) {
            ArrayList<String> dayPrice = dayList.get(i);

            String stockName = stockNameVO.getStockName();
            if (stockName == null || i < 9) {
                //i < 9 just ignore to store avg price = null
                continue;
            }
            StockDailyVO stockDailyVO = new StockDailyVO(stockNameVO.getStockId(), dayPrice.get(0), dayPrice.get(1), dayPrice.get(2), dayPrice.get(3), dayPrice.get(4));

            //calculate 5 day avg, need at least 5 day price list
            BigDecimal fiveDayAvgPrice = Utils.calculateDayAvg(stockName, i, dayList, 5);
            stockDailyVO.setDayAvgFive(fiveDayAvgPrice);
            BigDecimal dayGainFive = Utils.calculateDayGainPercentage(fiveDayAvgPrice, beforeDay_FiveDayAvgPrice);
            stockDailyVO.setDayGainOfFive(dayGainFive);
            beforeDay_FiveDayAvgPrice = fiveDayAvgPrice;

            //get 10 day avg
            BigDecimal tenDayAvgPrice = Utils.calculateDayAvg(stockName, i, dayList, 10);
            stockDailyVO.setDayAvgTen(tenDayAvgPrice);
            BigDecimal dayGainTen = Utils.calculateDayGainPercentage(tenDayAvgPrice, beforeDay_TenDayAvgPrice);
            stockDailyVO.setDayGainOfTen(dayGainTen);
            beforeDay_TenDayAvgPrice = tenDayAvgPrice;


            if (daysSet.contains(dayPrice.get(0))) {
                //history day already stored
                continue;
            }
            stockDailyDao.save(stockDailyVO);
        }
//        String today = dayList.get(dayList.size() - 1).get(0);
//        Date date = Date.valueOf(today);
//        StockNameVO lastUpdateDay = new StockNameVO(stockNameVO.getStockId(), stockNameVO.getStockName(), date);
//        logger.info("storeHistoryPrice ====stockDao save ================= {} ,date=={}", stockNameVO, formatter.format(date));
//        stockDao.save(lastUpdateDay);
        return dayList;
    }

    private static List<ArrayList<String>> getDayPriceList(QueryFromTencentResponseVO responseVO, StockNameVO stockNameVO) throws JsonProcessingException {
        Map<String, Object> dataMap = (Map) responseVO.getData().get(stockNameVO.getStockId().toLowerCase());
        Object dayListObj = dataMap.get("day");
        if (Objects.isNull(dayListObj)) {
            dayListObj = dataMap.get("qfqday");
        }
        List<ArrayList<String>> dayList = objectMapper.readValue(objectMapper.writeValueAsString(dayListObj), List.class);
        if (dayList.isEmpty()) {
            logger.error("==============ERROR ===== Input Stock Name must be wrong: " + stockNameVO.getStockId());
            logger.error("==============ERROR ====Response is =====" + responseVO);
            return null;
        }
        extractStockName(stockNameVO, dataMap);
        return dayList;
    }


    private static void extractStockName(StockNameVO stockNameVO, Map<String, Object> dataMap) {
        List identifierList = (List) ((Map) dataMap.get("qt")).get(stockNameVO.getStockId());
        if (identifierList.size() < 2) {
            return;
        }
        Object stockChineseNameObject = identifierList.get(1);
        String stockChineseName = stockChineseNameObject.toString();
        stockNameVO.setStockName(stockChineseName);
    }


    private static SimpleDateFormat formatter_yyyy_mm_day = new SimpleDateFormat("YYYY-MM-dd");

    public Object multiK(String stockId) {
        logger.debug("enter multiK stockId=============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }
        List<StockDailyVO> resultList = stockDailyDao.multiKFindByStockIdOrderByDay(stockId);
        Collections.reverse(resultList);
        ArrayList<Double[]> result = new ArrayList<>();
        for (StockDailyVO stockNameVO : resultList) {
            BigDecimal dayAvgFive = stockNameVO.getDayAvgFive();
            BigDecimal dayAvgTen = stockNameVO.getDayAvgTen();
            if (dayAvgFive == null || dayAvgTen == null) {
                continue;
            }
            Double[] strings = new Double[2];
            strings[0] = Double.valueOf(stockNameVO.getDay().getTime());
            strings[1] = Double.valueOf(stockNameVO.getClosingPrice().toString());
            result.add(strings);
        }
        return result;
    }

    public Object stockJsonData(String stockId) throws JsonProcessingException, ParseException {
        logger.debug("enter stockJsonData stockId=============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }

        ArrayList<String> mainKlineIds = getMainKlineIds();
        Set<String> stringSet = mainKlineIds.stream().collect(Collectors.toSet());
        List<StockDailyVO> resultList;
        if (stringSet.contains(stockId)) {
            resultList = stockDailyDao.findByIndexStockIdOrderByDay(stockId, Constants.rangeSize);
        } else {
            resultList = stockDailyDao.findByStockIdOrderByDay(stockId, Constants.rangeSize);
        }
        Collections.reverse(resultList);
        ArrayList<String[]> result = new ArrayList<>();
        for (StockDailyVO stockNameVO : resultList) {
            BigDecimal dayAvgFive = stockNameVO.getDayAvgFive();
            BigDecimal dayAvgTen = stockNameVO.getDayAvgTen();
            if (dayAvgFive == null || dayAvgTen == null) {
                continue;
            }
            String[] strings = new String[7];
            Date day = stockNameVO.getDay();
            strings[0] = Utils.getFormat(day);
            strings[1] = stockNameVO.getOpeningPrice().toString();
            strings[2] = stockNameVO.getClosingPrice().toString();
            strings[3] = stockNameVO.getIntradayHigh().toString();
            strings[4] = stockNameVO.getIntradayLow().toString();

            result.add(strings);
        }
        return result;
    }

    public Object listEtfs() throws JsonProcessingException {
        logger.info("enter listEtfs ====");
        List<StockNameVO> resultList = stockDao.findAll();
        StringBuilder ret = new StringBuilder("");
        for (StockNameVO stockVo : resultList) {
            if (!stockVo.getStockName().toLowerCase().contains("etf")) continue;
            ret.append(stockVo.getStockId() + "_" + stockVo.getStockName() + "<br/>");
        }
        return ret;
    }

    public Object handleStocksFlipDaysAndGainReport() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        logger.info("Enter handleStocksFlipDaysAndGainReport ====");
        StringBuilder retStr = new StringBuilder("<h2>Calculated All Stocks: </h2></br>");
        List<StockNameVO> stocks = storedStocks();
        for (StockNameVO stockNameVO : stocks) {
            //loop to calculate each etf
            String stockId = stockNameVO.getStockId();
            String stockName = stockNameVO.getStockName();
            if (stockName == null) {
                continue;
            }
            Pageable pageable = PageRequest.of(0, 60, Sort.by(Sort.Direction.DESC, "day"));
//            List<StockDailyVO> etfPriceList = stockDailyDao.findByStockId(stockId, Pageable.ofSize(60)).stream().toList();
            List<StockDailyVO> etfPriceList = stockDailyDao.findByStockId(stockId, pageable).stream().sorted(Comparator.comparing(StockDailyVO::getDay)).collect(Collectors.toList());
            List<Integer> flipDayFive = Utils.analysisFlipDay(etfPriceList, "getDayGainOfFive");
            List<Integer> flipDayTen = Utils.analysisFlipDay(etfPriceList, "getDayGainOfTen");
            setUpwardDaysAndGain(etfPriceList, flipDayFive.get(0), flipDayFive.get(1), stockNameVO, "Five");
            setUpwardDaysAndGain(etfPriceList, flipDayTen.get(0), flipDayTen.get(1), stockNameVO, "Ten");
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            stockNameVO.setLastUpdatedTime(timestamp);
            stockDao.save(stockNameVO);
            retStr.append(stockNameVO.getStockId() + "_" + stockNameVO.getStockName() + "</br>");
        }
        return retStr;
    }


    private void setUpwardDaysAndGain(List<StockDailyVO> etfPriceList, Integer flipBeginIndex, Integer flipEndIndex, StockNameVO stockNameVO, String dayIdentify) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        logger.debug("Enter setUpwardDaysAndGain = " + etfPriceList.size());
        int listSize = etfPriceList.size();
        StockDailyVO today = etfPriceList.get(etfPriceList.size() - 1);

        StockDailyVO flipDay = etfPriceList.get(listSize - flipBeginIndex - 1);
        BigDecimal gainPercent = getGainPercent(today, flipDay, dayIdentify);

        StockDailyVO flipEndDay = etfPriceList.get(listSize - flipEndIndex - 1);
        BigDecimal gainPercentFlip = getGainPercent(flipDay, flipEndDay, dayIdentify);

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
        aClass.getMethod("setFlipDay" + dayIdentify, java.sql.Date.class).invoke(stockNameVO, flipDay.getDay());
        aClass.getMethod("setFlipEndDay" + dayIdentify, java.sql.Date.class).invoke(stockNameVO, flipEndDay.getDay());
    }

    private BigDecimal getGainPercent(StockDailyVO newPrice, StockDailyVO oldPrice, String dayIdentify) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Class<? extends StockDailyVO> aClass = newPrice.getClass();
        BigDecimal newPriceValue = ((BigDecimal) aClass.getMethod("getDayAvg" + dayIdentify).invoke(newPrice));
        BigDecimal oldPriceValue = ((BigDecimal) aClass.getMethod("getDayAvg" + dayIdentify).invoke(oldPrice));
        if (newPriceValue == null || oldPriceValue == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal subtract = newPriceValue.subtract(oldPriceValue).multiply(BigDecimal.valueOf(100));
        BigDecimal gainPercentage = subtract.divide(oldPrice.getDayAvgFive(), 2, BigDecimal.ROUND_HALF_UP);
        return gainPercentage;
    }

    public Object etfsCurveView() {
        StringBuilder sb = new StringBuilder();
        List<StockNameVO> etfs = getAllEtfs();
        ArrayList<String> mainKlineIds = getMainKlineIds();
        for (StockNameVO etfVo : etfs) {
            String stockIds = etfVo.getStockIds();
            if (!StringUtils.hasLength(stockIds)) {
                continue;
            }
            String[] stockIdSplit = stockIds.split(",");
            StringBuilder htmlAppend = new StringBuilder();
            StringBuilder divStockIds = new StringBuilder();
            divStockIds.append(etfVo.getStockId()).append(",");
            for (int index = 0; index < stockIdSplit.length; index++) {
                String str = stockIdSplit[index];
                str = str + "_" + holdingService.getStockIdOrNameByMap(str);
                if (!StringUtils.hasLength(str)) {
                    continue;
                }
                divStockIds.append(str).append(",");
                htmlAppend.append("<td class=\"cell\" onclick=\"changeColor(this)\">").append(str).append("</td>");
            }
            sb.append("<tr>");
            sb.append("<td class=\"cell\" style=\"background-color: lightGreen;\" onclick=\"showEtf(this)\" stockIds=\"").append(divStockIds).append("\">");
            sb.append(etfVo.getStockId()).append("_").append(etfVo.getStockName());
            sb.append("</td>");
            sb.append(htmlAppend);
            sb.append("</tr>");
            sb.append("</br>");
        }
        return sb;
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Object delete_HistoryData() {
        // also delete today's daily price as sometimes i need to know the real price while the market opening.
        String dayStr = formatter_yyyy_mm_day.format((new Date(System.currentTimeMillis())));
        entityManager.createNativeQuery("delete from daily_price where day = '" + dayStr + "'").executeUpdate();

        entityManager.createNativeQuery("delete from daily_price where day_avg_five is null or day_avg_ten is null or day_gain_of_five is null").executeUpdate();

        entityManager.createNativeQuery("update stock set gain_percent_five = null,  last_updated_time = null ;").executeUpdate();

        return "ok";
    }

    public Object addNewStock(String stockId) {
        return null;
    }

    public Object stockWeeklyJsonData(String stockId) {
        logger.info("enter stockWeeklyJsonData stockId=============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }
        List<WeekPriceVO> resultList = weeklyPriceDao.findAllByStockId(stockId, Constants.rangeSize);
        Collections.reverse(resultList);
        ArrayList<String[]> result = new ArrayList<>();
        for (WeekPriceVO weekPriceVO : resultList) {
            String[] strings = new String[7];
            strings[0] = Utils.getFormat(weekPriceVO.getDay());
            strings[1] = weekPriceVO.getOpeningPrice().toString();
            strings[2] = weekPriceVO.getClosingPrice().toString();
            strings[3] = weekPriceVO.getWeekHigh().toString();
            strings[4] = weekPriceVO.getWeekLow().toString();

            result.add(strings);
        }
        return result;
    }
}

