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
import org.springframework.data.domain.Sort;
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

    public Object report() {
        logger.debug("enter report ====");
        List<StockNameVO> allEtfs = getAllEtfs();
        for (StockNameVO stockNameVO : allEtfs) {
            //loop to calculate each etf
            String stockId = stockNameVO.getStockId();
//            List<Object[]> etfPriceList = entityManager.createNativeQuery("select five_day_avg, five_day_diff, ten_day_avg, ten_day_diff from daily_price where stock_id=?1 limit 60 ").setParameter(1, stockId).;
            List<StockDailyVO> etfPriceList = stockDailyDao.findByStockId(stockId, Pageable.ofSize(60)).stream().toList();
            List<Integer> flipDay = analysisFlipDay(etfPriceList, "getDayGainOfFive");
            setUpwardDaysAndGainFive(etfPriceList, stockNameVO, flipDay.get(0), flipDay.get(1));
            stockDao.save(stockNameVO);
        }
        return "ok";
    }

    private List<Integer> analysisFlipDay(List<StockDailyVO> etfPriceList, String methodName) {
        if (etfPriceList.size() == 0) return null;
        Integer flipBeginIndex = 0;
        Integer flipEndIndex = 0;
        Integer loopCount = 0;
        Boolean countFlip = Boolean.FALSE;
        Boolean todayKLineUpward = Boolean.TRUE;

        StockDailyVO today = etfPriceList.get(etfPriceList.size() - 1);
        if (today.getDayGainOfFive().compareTo(BigDecimal.ZERO) < 0) {
            //today is downward!
            todayKLineUpward = Boolean.FALSE;
        }
        //set today upwardDay count = 0!
        for (int index = etfPriceList.size() - 1; index > 0; index--) {
            StockDailyVO indexDay = etfPriceList.get(index);
            if (indexDay.getDayGainOfFive() == null) {
                break;
            }
            loopCount++;
            boolean indexDayPositive = indexDay.getDayGainOfFive().compareTo(BigDecimal.ZERO) >= 0;
            System.out.println("indexDay = " + indexDay);
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
        List<Integer> flipDayList = new ArrayList<>();
        flipDayList.add(flipBeginIndex);
        flipDayList.add(flipEndIndex);
        return flipDayList;
    }

    private static Integer upwardDayPlus(Integer fiveDayUpwardDays, Boolean todayKLineUpward, StockDailyVO beforeDay) {
        if (beforeDay.getDayGainOfFive().compareTo(BigDecimal.ZERO) >= 0 && todayKLineUpward == Boolean.TRUE) {
            fiveDayUpwardDays++;
        }
//            else if (todayKLineUpward == Boolean.FALSE && beforeDay.getFiveDayDiff().compareTo(BigDecimal.ZERO) >= 0) {
//                ifNeedToBreak = Boolean.TRUE;
//            } else if (todayKLineUpward == Boolean.TRUE && beforeDay.getFiveDayDiff().compareTo(BigDecimal.ZERO) < 0) {
//                ifNeedToBreak = Boolean.TRUE;
        if (beforeDay.getDayGainOfFive().compareTo(BigDecimal.ZERO) < 0 && todayKLineUpward == Boolean.FALSE) {
            fiveDayUpwardDays--;
        }
        return fiveDayUpwardDays;
    }

    private void setUpwardDaysAndGainFive(List<StockDailyVO> etfPriceList, StockNameVO stockNameVO, Integer flipBeginIndex, Integer flipEndIndex) {
        System.out.println("flipBeginIndex = " + flipBeginIndex);
        System.out.println("flipEndIndex = " + flipEndIndex);
        int listSize = etfPriceList.size();
        StockDailyVO today = etfPriceList.get(etfPriceList.size() - 1);
        StockDailyVO flipDay = etfPriceList.get(listSize - flipBeginIndex - 1);
        BigDecimal gainPercent = getGainPercent(today, flipDay);

        StockDailyVO flipEndDay = etfPriceList.get(listSize - flipEndIndex - 1);
        BigDecimal gainPercentFlip = getGainPercent(flipDay, flipEndDay);

        boolean todayUpward = today.getDayGainOfFive().compareTo(BigDecimal.ZERO) >= 0;
        if (todayUpward) {
            stockNameVO.setUpwardDaysFive(flipBeginIndex);
            stockNameVO.setGainPercentFive(gainPercent);

            stockNameVO.setFlipUpwardDaysFive(flipBeginIndex - flipEndIndex);
            stockNameVO.setFlipGainPercentFive(gainPercentFlip);
        } else {
            stockNameVO.setUpwardDaysFive(-flipBeginIndex);
            stockNameVO.setGainPercentFive(gainPercent);

            stockNameVO.setFlipUpwardDaysFive(flipEndIndex - flipBeginIndex);
            stockNameVO.setFlipGainPercentFive(gainPercentFlip);
        }
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


    public Object findAllEtfSort(String avgDay, String flip) {
        String sort_1 = avgDay;
        String sort_2 = flip;

        List<StockNameVO> fiveDayUpwardDays = stockDao.findAll(Sort.by("flipUpwardDaysFive", "flipGainPercentFive").descending());
        if (CollectionUtils.isEmpty(fiveDayUpwardDays)) {
            return "No data found!";
        }
        StringBuilder html = new StringBuilder();
        html.append("<table border=\"1\">\n");
        html.append("<tr><th>ETF Name</th><th>Upward Days of Five</th><th>Gain Percent of Five(%)</th>" + "<th>Flip Upward Days of Five</th><th>Flip Gain Percent of Five(%)</th></tr>\n");
        List<String> lightColors = generateLightColors();
        Integer loopCount = 0;
        String color = lightColors.get(loopCount);
        Integer temp = 0;
        for (StockNameVO stockVo : fiveDayUpwardDays) {
            if (!stockVo.getStockName().toLowerCase().contains("etf")) continue;
            if (!stockVo.getUpwardDaysFive().equals(temp)) {
                loopCount++;
                temp = stockVo.getUpwardDaysFive();
                if (loopCount >= lightColors.size()) {
                    loopCount = 0;
                }
                color = lightColors.get(loopCount);
            }
            html.append("<tr style=\"background-color:").append(color).append("\">");
            html.append("<td>").append(stockVo.getStockId() + "_" + stockVo.getStockName()).append("</td>");
            html.append("<td>").append(stockVo.getUpwardDaysFive()).append("</td>");
            html.append("<td>").append(stockVo.getGainPercentFive()).append("</td>");
            html.append("<td>").append(stockVo.getFlipUpwardDaysFive()).append("</td>");
            html.append("<td>").append(stockVo.getFlipGainPercentFive()).append("</td>");
            html.append("</tr>\n");
        }
        html.append("</table>");
        return html;
    }


    private static String generateRandomColor() {
        Random random = new Random();
        int r, g, b;

        // Ensure that each RGB component is greater than 200
        do {
            r = random.nextInt(256);
            g = random.nextInt(256);
            b = random.nextInt(256);
        } while (r < 200 || g < 200 || b < 200);

        // Format the RGB components into hexadecimal and return the color string
        return String.format("#%02X%02X%02X", r, g, b);
    }

    private static List<String> generateLightColors() {
        List<String> colors = new ArrayList<>();
        colors.add("#FFCCCC");
        colors.add("#FFE5CC");
        colors.add("#FFFFCC");
        colors.add("#E5FFCC");
        colors.add("#CCFFCC");
        colors.add("#CCFFE5");
        colors.add("#CCFFFF");
        colors.add("#CCE5FF");
        colors.add("#CCCCFF");
        colors.add("#E5CCFF");
        colors.add("#FFCCFF");
        colors.add("#FFCCE5");
        return colors;
    }
}

