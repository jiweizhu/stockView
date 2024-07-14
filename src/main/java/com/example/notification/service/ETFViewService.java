package com.example.notification.service;

import com.example.notification.repository.IntraDayPriceDao;
import com.example.notification.repository.StockDailyDao;
import com.example.notification.repository.StockDao;
import com.example.notification.util.Utils;
import com.example.notification.vo.IntradayPriceVO;
import com.example.notification.vo.StockDailyVO;
import com.example.notification.vo.StockNameVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static com.example.notification.constant.Constants.MARKETDAYCLOSEDJOB_QUERY_PRICE_DAY;

@Service
public class ETFViewService {
    private static final Logger logger = LoggerFactory.getLogger(ETFViewService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private StockDao stockDao;

    @Autowired
    private StockDailyDao stockDailyDao;

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Autowired
    private KLineService kLineService;

    @Autowired
    private HoldingService holdingService;

    @Autowired
    private IntraDayPriceDao intraDayPriceDao;


    public void generateReportEveryDay() throws JsonProcessingException, InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        //check if today market day open
//        Boolean ifMarketOpenToday = kLineService.checkIfMarketOpenToday();
//        if (!ifMarketOpenToday) {
//            return;
//        }
        //just need to get all stocks from stock table, and then get today's kline price to store in db and calculate avg data
        kLineMarketClosedService.getHistoryPriceOnLineAndStoreInDb(MARKETDAYCLOSEDJOB_QUERY_PRICE_DAY);
        Thread.sleep(15000);
        kLineMarketClosedService.handleStocksFlipDaysAndGainReport();
    }

    public Object findAllEtfSortView(String num) {
        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "upwardDaysFive"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "gainPercentFive"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "upwardDaysTen"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "gainPercentTen"));
        List<StockNameVO> downwardDaysStock = stockDao.findDownwardDaysStock();
        List<StockNameVO> fiveDayUpwardDays = stockDao.findupwardDaysStock();
        boolean b = fiveDayUpwardDays.addAll(downwardDaysStock);
        if (CollectionUtils.isEmpty(fiveDayUpwardDays)) {
            return "No data found!";
        }
        StringBuilder html = new StringBuilder();
        html.append("<table border=\"1\">\n");

        if (num.equals("1")) {
            html.append("<tr><th>ETF Name</th><th>5日上涨天数</th><th>割</th><th>10日上涨天数</th></tr>\n");
        }
        if (num.equals("2")) {
            html.append("<tr><th>ETF Name</th>" + "<th>5线</th>" + "<th>8</th>" + "<th>10线</th>" + "</tr>\n");
        }

        List<String> lightColors = generateLightColors();
        Integer loopCount = 0;
        String color = lightColors.get(loopCount);
        Integer temp = 0;

        for (StockNameVO stockVo : fiveDayUpwardDays) {
            String stockName = stockVo.getStockName();
            if (stockName == null || !stockName.toLowerCase().contains("etf")) continue;
            if (!stockVo.getUpwardDaysFive().equals(temp)) {
                loopCount++;
                temp = stockVo.getUpwardDaysFive();
                if (loopCount >= lightColors.size()) {
                    loopCount = 0;
                }
                color = lightColors.get(loopCount);
            }

            html.append("<tr style=\"background-color:").append(color).append("\">");
            html.append("<td>").append("<a href=\"https://gushitong.baidu.com/fund/ab-" + stockVo.getStockId().substring(2) + "\">").append(stockVo.getStockId() + "_" + stockName).append("</a></td>");
            if (num.equals("1")) {
                view_one(html, stockVo);
            }
            if (num.equals("2")) {
                view_two(html, stockVo);
            }
        }
        html.append("</table>");
        return html;
    }

    public Object findAllEtfSortView_new(String num) {
        List<StockNameVO> downwardDaysStock = stockDao.findDownwardDaysStock();
        List<StockNameVO> fiveDayUpwardDays = stockDao.findupwardDaysStock();
        boolean b = fiveDayUpwardDays.addAll(downwardDaysStock);
        if (CollectionUtils.isEmpty(fiveDayUpwardDays)) {
            return "No data found!";
        }

        List<StockNameVO> commonEtfs = new ArrayList<>();
        List<StockNameVO> mainEtfs = new ArrayList<>();

        //group main index Kline
        ArrayList<String> mainKlineIds = kLineMarketClosedService.getMainKlineIds();
        Set<String> stringSet = new HashSet<>(mainKlineIds);
        for (StockNameVO fiveDayUpwardDay : fiveDayUpwardDays) {
            if (!stringSet.contains(fiveDayUpwardDay.getStockId())) {
                commonEtfs.add(fiveDayUpwardDay);
            } else {
                mainEtfs.add(fiveDayUpwardDay);
            }
        }
        String industryEtfsTable = "";
        if (num.equals("3")) {
            industryEtfsTable = industryEtfsView(commonEtfs);
        }
        if (num.equals("4")) {
            //return etfs has stock_ids
            List<StockNameVO> industryEtfs = new ArrayList<>();
            commonEtfs.forEach(vo -> {
                if (StringUtils.hasLength(vo.getStockIds())) {
                    industryEtfs.add(vo);
                }
            });
            industryEtfsTable = industryEtfsView(industryEtfs);
        }
        if (num.equals("main")) {
            industryEtfsTable = mainEtfsView(mainEtfs);
        }
        return industryEtfsTable;
    }

    private static String mainEtfsView(List<StockNameVO> mainEtfs) {
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < mainEtfs.size(); i++) {
            StockNameVO stock = mainEtfs.get(i);
            if (i % 4 == 0) {
                html.append("<tr>");
            }
            String id_name = stock.getStockId() + "_" + stock.getStockName();
            String backGroudColor = "#C0C0C0";
            if (stock.getUpwardDaysFive() >= 0) {
                backGroudColor = "#00FF00";
            }
            html.append("<td><div style=\"background-color:").append(backGroudColor).append("\">").append(id_name).append("(" + stock.getUpwardDaysFive()).append("|").append(stock.getGainPercentFive() + ")").append("(" + stock.getFlipUpwardDaysFive()).append("|").append(stock.getFlipGainPercentFive() + ")").append("</div>").append("<div class=\"index-container\" ").append("id = \"").append("span_").append(id_name).append("\"").append("</td>");
            if (i % 4 == 4) {
                html.append("</tr>");
            }
        }
        return html.toString();
    }

    private String industryEtfsView(List<StockNameVO> industryEtfs) {
        List<String> lightColors = generateLightColors();
        Integer loopCount = 0;
        String color = lightColors.get(loopCount);
        StringBuilder html = new StringBuilder();

        html.append("<tr><th>ETF Name</th>" + "<th>5线</th>" + "<th>8</th>" + "<th>10线</th>" + "</tr>\n");
        Integer temp = 0;
        for (StockNameVO stockVo : industryEtfs) {
            String stockName = stockVo.getStockName();
            if (stockName == null || !stockName.toLowerCase().contains("etf")) continue;
//            if (!stockVo.getUpwardDaysFive().equals(temp)) {
//                loopCount++;
//                temp = stockVo.getUpwardDaysFive();
//                if (loopCount >= lightColors.size()) {
//                    loopCount = 0;
//                }
//                color = lightColors.get(loopCount);
//            }

            html.append("<tr style=\"background-color:").append(color).append("\">");

            avg_graph(html, stockVo);
        }
        return html.toString();
    }


    private static final SimpleDateFormat MMdd_Formatter = new SimpleDateFormat("MMdd");
    DateTimeFormatter yyyyMMdd_Formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private void avg_graph(StringBuilder html, StockNameVO stockVo) {

        Integer upwardDaysTen = stockVo.getUpwardDaysTen();
        Integer upwardDaysFive = stockVo.getUpwardDaysFive();
        if (upwardDaysFive > 1 && upwardDaysTen >= 0) {
//            html.append("<td style=\"background-color: #00FFB0;\">");
            html.append("<td>");
        } else if (upwardDaysFive < 0) {
            html.append("<td style=\"background-color: #DEB887;\">");
        } else {
            html.append("<td>");
        }
        Timestamp lastUpdatedTime = stockVo.getLastUpdatedTime();
        Date flipDayFive = stockVo.getFlipDayFive();
        Date flipEndDayFive = stockVo.getFlipEndDayFive();

        Date flipDayTen = stockVo.getFlipDayTen();
        Date flipEndDayTen = stockVo.getFlipEndDayTen();
        String customerRange = stockVo.getCustomerRange();
        if (StringUtils.hasLength(customerRange)) {
            String[] split = customerRange.split(",");
            LocalDate flipEnd = LocalDate.parse(split[0], yyyyMMdd_Formatter);
            LocalDate flipStart = LocalDate.parse(split[1], yyyyMMdd_Formatter);
            flipEndDayTen = Date.valueOf(flipEnd);
            flipDayTen = Date.valueOf(flipStart);
        }

        String updatedDay = lastUpdatedTime == null ? "null" : MMdd_Formatter.format(lastUpdatedTime);
        String flipDayFiveDay = flipDayFive == null ? "null" : MMdd_Formatter.format(flipDayFive);
        String flipEndDayFiveDay = flipEndDayFive == null ? "null" : MMdd_Formatter.format(flipEndDayFive);
        String flipDayTenDay = flipDayTen == null ? "null" : MMdd_Formatter.format(flipDayTen);
        String flipEndDayTenDay = flipEndDayTen == null ? "null" : MMdd_Formatter.format(flipEndDayTen);

        String eftStockGainFiveList = getEftStockGainList(stockVo, flipDayFive, flipEndDayFive);
        String eftStockGainTenList = getEftStockGainList(stockVo, flipDayTen, flipEndDayTen);

        html.append("5Day|").append(String.format("%02d", stockVo.getUpwardDaysFive())).append("|").append(String.format("%02d", stockVo.getFlipUpwardDaysFive())).append("</br>")
            .append(stockVo.getGainPercentFive()).append("%").append("|").append(stockVo.getFlipGainPercentFive()).append("%").append("</br>")
            .append("-----------------<br/>")
            .append("10Day|")
            .append(String.format("%02d", upwardDaysTen))
//                    .append("|").append(formatter.format(stockVo.getFlipDayTen()))
            .append("|").append(String.format("%02d", stockVo.getFlipUpwardDaysTen())).append("</br>").append(stockVo.getGainPercentTen()).append("%")
//                    .append("|").append(formatter.format(stockVo.getFlipEndDayTen()))
            .append("|").append(stockVo.getFlipGainPercentTen()).append("%").append("</br>")
            .append("</td>");


        if (upwardDaysTen < 0) {
            html.append("<td style=\"background-color: #DEB887;\">");
        } else {
            html.append("<td>");
        }
        //customized day gain
        html.append(flipEndDayTenDay).append("|").append(flipDayTenDay).append("|").append(updatedDay).append("</br>")
                .append(eftStockGainTenList).append("</br>")
                .append("</td>");
        String stockId = stockVo.getStockId();
        String stockIds = stockVo.getStockIds();
        int belongStockNum = (StringUtils.hasLength(stockIds)) ? stockIds.split(",").length : 0;
        html.append("<td>").append(stockId).append("</br><span class=\"vertical-stockName\" >")
                .append(stockVo.getStockName().replace("ETF", "#"))
                .append(belongStockNum)
                .append("</span></td>");
        html.append("<td><div class=\"chart-container\" style=\"background-color:#FFFFFF\" id=\"").append("week_").append(stockId).append("\"></div></td>");
        html.append("<td><div class=\"chart-container\" style=\"background-color:#FFFFFF\" id=\"").append("span_").append(stockId).append("\"></div></td>");
        html.append("<td><div class=\"multiLine-container\" style=\"background-color:#FFFFFF\" id=\"").append("multi_").append(stockId).append("\"></div></td>");
        html.append("</tr>\n");
    }

    private String getEftStockGainList(StockNameVO stockVo, Date flipDay, Date flipEndDayDay) {
        String stockIds = stockVo.getStockIds();
        StringBuilder htmlStr = new StringBuilder();
        htmlStr.append("-----------------<br/>");
        if (!StringUtils.hasLength(stockIds)) {
            return htmlStr.toString();
        }
        String[] split = stockIds.split(",");
        List<StockNameVO> list = new ArrayList<>();
        for (String stockId : split) {
            String stockIdOrNameByMap = holdingService.getStockIdOrNameByMap(stockId);
            if (!StringUtils.hasLength(stockId) || !StringUtils.hasLength(stockIdOrNameByMap)) {
                continue;
            }
            StockDailyVO tempDailyVo = new StockDailyVO();
            tempDailyVo.setClosingPrice(new BigDecimal(0));
            StockDailyVO flipEndVo = stockDailyDao.findDayPriceByStockIdAndDay(stockId, flipEndDayDay);
            flipEndVo = flipEndVo == null ? tempDailyVo : flipEndVo;
            StockDailyVO flipVo = stockDailyDao.findDayPriceByStockIdAndDay(stockId, flipDay);
            flipVo = flipVo == null ? tempDailyVo : flipVo;
            StockDailyVO todayVo = stockDailyDao.findLastOneDayPriceByStockId(stockId);
            todayVo = todayVo == null ? tempDailyVo : todayVo;

            BigDecimal todayClosePrice = todayVo.getClosingPrice();
            BigDecimal flipClosingPrice = flipVo.getClosingPrice();
            BigDecimal flipGainPercent = Utils.calculateDayGainPercentage(todayClosePrice, flipClosingPrice);
            BigDecimal flipEndGainPercent = Utils.calculateDayGainPercentage(flipClosingPrice, flipEndVo.getClosingPrice());
            StockNameVO stockNameVO = new StockNameVO();
            stockNameVO.setStockId(stockId);
            stockNameVO.setStockName(stockIdOrNameByMap);
            stockNameVO.setGainPercentFive(flipGainPercent);
            stockNameVO.setFlipGainPercentFive(flipEndGainPercent);
            list.add(stockNameVO);
        }
        List<StockNameVO> collect = list.stream().sorted(Comparator.comparing(StockNameVO::getFlipGainPercentFive).reversed()).toList();
        for (StockNameVO stockNameVO : collect) {
            htmlStr.append(stockNameVO.getStockName()).append("|")
                    .append(stockNameVO.getFlipGainPercentFive()).append("%|").append(stockNameVO.getGainPercentFive()).append("%</br>");
        }
        return htmlStr.toString();
    }

    private static void view_one(StringBuilder html, StockNameVO stockVo) {
        String stockName = stockVo.getStockName();
        if (stockName != null && stockName.toLowerCase().contains("etf")) {
            html.append("<td>").append(stockVo.getUpwardDaysFive()).append("|").append(MM_dd_Formatter.format(stockVo.getFlipDayFive())).append("|").append(stockVo.getGainPercentFive()).append("%").append("</br>").append(stockVo.getFlipUpwardDaysFive()).append("|").append(MM_dd_Formatter.format(stockVo.getFlipEndDayFive())).append("|").append(stockVo.getFlipGainPercentFive()).append("%").append("</td>");
            html.append("<td style=\"background-color: #708090;\"|").append("8").append("</td>");
            html.append("<td>").append(stockVo.getUpwardDaysTen()).append("|").append(MM_dd_Formatter.format(stockVo.getFlipDayTen())).append("|").append(stockVo.getGainPercentTen()).append("%</br>");
            html.append(stockVo.getFlipUpwardDaysTen()).append("|").append(MM_dd_Formatter.format(stockVo.getFlipEndDayTen())).append("|").append(stockVo.getFlipGainPercentTen()).append("%").append("(").append(stockVo.getLastUpdatedTime()).append(")").append("</td>");
            html.append("</tr>\n");
        }
    }

    //#DEB887
    private static void view_two(StringBuilder html, StockNameVO stockVo) {
        Integer upwardDaysTen = stockVo.getUpwardDaysTen();
        Integer upwardDaysFive = stockVo.getUpwardDaysFive();
        if (upwardDaysFive > 1 && upwardDaysTen >= 0) {
            html.append("<td style=\"background-color: #00FFB0;\">");
        } else if (upwardDaysFive < 0 || upwardDaysTen < 0) {
            html.append("<td style=\"background-color: #DEB887;\">");
        } else {
            html.append("<td>");
        }
        html.append(String.format("%02d", stockVo.getUpwardDaysFive()))
//                    .append("|").append(formatter.format(stockVo.getFlipDayFive()))
                .append("|").append(String.format("%02d", stockVo.getFlipUpwardDaysFive())).append("</br>").append("<span >").append(stockVo.getGainPercentFive()).append("%")
//                    .append("|").append(formatter.format(stockVo.getFlipEndDayFive()))
                .append("|").append(stockVo.getFlipGainPercentFive()).append("%").append("(").append(MM_dd_Formatter.format(stockVo.getLastUpdatedTime())).append(")").append("</span>").append("</td>");


        html.append("<td style=\"background-color: #708090;\">").append("").append("</td>");
        if (upwardDaysFive < 0 || upwardDaysTen < 0) {
            html.append("<td style=\"background-color: #DEB887;\">");
        } else {
            html.append("<td>");
        }
        html.append(String.format("%02d", upwardDaysTen))
//                    .append("|").append(formatter.format(stockVo.getFlipDayTen()))
                .append("|").append(String.format("%02d", stockVo.getFlipUpwardDaysTen())).append("</br>").append(stockVo.getGainPercentTen()).append("%")
//                    .append("|").append(formatter.format(stockVo.getFlipEndDayTen()))
                .append("|").append(stockVo.getFlipGainPercentTen()).append("%");
        html.append("</td></tr>\n");
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

        //light colors
//        colors.add("#FFCCCC");
//        colors.add("#FFE5CC");
//        colors.add("#FFFFCC");
//        colors.add("#E5FFCC");
//        colors.add("#CCFFCC");
//        colors.add("#CCFFE5");
//        colors.add("#CCFFFF");
//        colors.add("#CCE5FF");
//        colors.add("#CCCCFF");
//        colors.add("#E5CCFF");
//        colors.add("#FFCCFF");
//        colors.add("#FFCCE5");


//        colors.add("#99FFCC");
        colors.add("#FFFFCC");
//        colors.add("#FFFFFF");
//        colors.add("#CCCCFF");
//        colors.add("#CCFFFF");
        return colors;
    }

    public Object fiveDayAdjustedView() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        orders.add(new Sort.Order(Sort.Direction.ASC, "upwardDaysFive"));
        orders.add(new Sort.Order(Sort.Direction.ASC, "gainPercentFive"));
        List<StockNameVO> all = stockDao.findAll(Sort.by(orders));
//        List<StockNameVO> fiveDayUpwardDays = all.stream().filter(vo -> (vo.getUpwardDaysFive() >= 1)).toList();
        List<StockNameVO> fiveDayUpwardDays = all;
        if (CollectionUtils.isEmpty(fiveDayUpwardDays)) {
            result.put("rows", Arrays.asList());
            return result;
        }
        result.put("rows", fiveDayUpwardDays);
        return result;
    }

    public Object tenDayAdjustedView() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "upwardDaysTen"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "gainPercentTen"));
        List<StockNameVO> all = stockDao.findAll(Sort.by(orders));
//        List<StockNameVO> fiveDayUpwardDays = all.stream().filter(vo -> (vo.getUpwardDaysFive() >= 1)).toList();
        List<StockNameVO> fiveDayUpwardDays = all;
        if (CollectionUtils.isEmpty(fiveDayUpwardDays)) {
            result.put("rows", Arrays.asList());
            return result;
        }
        result.put("rows", fiveDayUpwardDays);
        return result;
    }

    private static final SimpleDateFormat MM_dd_Formatter = new SimpleDateFormat("MM-dd");

    public Object findAllEtfsForTable(int i) {
        List<StockNameVO> fiveDayUpwardDays = Collections.emptyList();
        if (i == 1) {
            //find 5day 10 day both upward
            fiveDayUpwardDays = stockDao.findupwardDaysStock();
        } else {
            //findDownwardDaysStock
            fiveDayUpwardDays = stockDao.findDownwardDaysStock();
        }
        Map<String, Object> result = new HashMap<>();
        ArrayList<String[]> list = new ArrayList<>();
        for (StockNameVO day : fiveDayUpwardDays) {
            String[] fieldValues = new String[9];
            String stockId_name = day.getStockId() + "_" + day.getStockName();
            fieldValues[0] = stockId_name;

            fieldValues[1] = MM_dd_Formatter.format(day.getFlipDayFive()) + "|" + day.getUpwardDaysFive().toString();
            fieldValues[2] = day.getGainPercentFive().toString();
            fieldValues[3] = MM_dd_Formatter.format(day.getFlipEndDayFive()) + "|" + day.getFlipUpwardDaysFive();
            fieldValues[4] = day.getFlipGainPercentFive().toString();

            fieldValues[5] = MM_dd_Formatter.format(day.getFlipDayTen()) + "|" + day.getUpwardDaysTen().toString();
            fieldValues[6] = day.getGainPercentTen().toString();
            fieldValues[7] = MM_dd_Formatter.format(day.getFlipEndDayTen()) + "|" + day.getFlipUpwardDaysTen();
            fieldValues[8] = day.getFlipGainPercentTen().toString();

            list.add(fieldValues);
        }
        result.put("data", list);
        return result;
    }

    public List<String> getStocksBelongEtf(String etfId) {
        if (etfId.contains("_")) {
            etfId = etfId.split("_")[0];
        }
        StockNameVO etf = stockDao.findById(etfId).get();
        String etfStockIds = etf.getStockIds();
        List<String> stockList = new ArrayList<>();
        if (StringUtils.hasLength(etfStockIds)) {
            stockList.add(etfId);
            String[] stockIds = etfStockIds.split(",");
            for (String stockId : stockIds) {
                String stockName = holdingService.getStockIdOrNameByMap(stockId);
                stockList.add(stockId + "_" + stockName);
            }
            return stockList;
        }
        return Collections.emptyList();
    }

    public String[] belongStockIds(String etfId) {
        if (etfId.contains("_")) {
            etfId = etfId.split("_")[0];
        }
        StockNameVO etf = stockDao.findById(etfId).get();
        String etfStockIds = etf.getStockIds();
        List<String> stockList = new ArrayList<>();
        if (StringUtils.hasLength(etfStockIds)) {
            stockList.add(etfId);
            String[] stockIds = etfStockIds.split(",");
            return stockIds;
        }
        return null;
    }

    public Set<IntradayPriceVO> getIntradayPrice(String etfId) {
        IntradayPriceVO lastestPriceById = intraDayPriceDao.findLastestPriceById(etfId);
        if (lastestPriceById == null) return null;
        Set<IntradayPriceVO> minuteVOs = intraDayPriceDao.findMinutesByIdAndToday(etfId, lastestPriceById.getDay());
        return minuteVOs;
    }
}
