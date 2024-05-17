package com.example.notification.service;

import com.example.notification.repository.StockDao;
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

import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.example.notification.constant.Constants.MARKETDAYCLOSEDJOB_QUERY_PRICE_DAY;

@Service
public class ETFViewService {
    private static final Logger logger = LoggerFactory.getLogger(ETFViewService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private StockDao stockDao;

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Autowired
    private KLineService kLineService;

    public void generateReportEveryDay() throws JsonProcessingException, InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        //check if today market day open
        Boolean ifMarketOpenToday = kLineService.checkIfMarketOpenToday();
        if (!ifMarketOpenToday) {
            return;
        }
        //just need to get all stocks from stock table, and then get today's kline price to store in db and calculate avg data
        kLineMarketClosedService.getHistoryPriceOnLineAndStoreInDb(MARKETDAYCLOSEDJOB_QUERY_PRICE_DAY);
        kLineMarketClosedService.handleStocksAvg();
    }

    public Object findAllEtfSortView() {
        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "upwardDaysFive"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "gainPercentFive"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "upwardDaysTen"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "gainPercentTen"));
        List<StockNameVO> fiveDayUpwardDays = stockDao.findAll(Sort.by(orders));
        if (CollectionUtils.isEmpty(fiveDayUpwardDays)) {
            return "No data found!";
        }
        StringBuilder html = new StringBuilder();
        html.append("<table border=\"1\">\n");
        html.append("<tr><th>ETF Name</th>" + "<th>5日上涨天数</th><th>5日上涨(%)</th>" + "<th>5日回调天数</th><th>5日回调(%)</th>" + "<th>分割</th>" + "<th>10日上涨天数</th><th>10日上涨(%)</th>" + "<th>10日回调天数</th><th>10日回调(%)</th>" + "</tr>\n");
        List<String> lightColors = generateLightColors();
        Integer loopCount = 0;
        String color = lightColors.get(loopCount);
        Integer temp = 0;

        SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");

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
            html.append("<td>").append(stockVo.getUpwardDaysFive()).append("(").append(formatter.format(stockVo.getFlipDayFive())).append(")").append("</td>");
            html.append("<td>").append(stockVo.getGainPercentFive()).append("</td>");
            html.append("<td>").append(stockVo.getFlipUpwardDaysFive()).append("(").append(formatter.format(stockVo.getFlipEndDayFive())).append(")").append("</td>");
            html.append("<td>").append(stockVo.getFlipGainPercentFive()).append("</td>");
            html.append("<td style=\"background-color: #708090;\"").append("8888").append("</td>");

            html.append("<td>").append(stockVo.getUpwardDaysTen()).append("(").append(formatter.format(stockVo.getFlipDayTen())).append(")").append("</td>");
            html.append("<td>").append(stockVo.getGainPercentTen()).append("</td>");
            html.append("<td>").append(stockVo.getFlipUpwardDaysTen()).append("(").append(formatter.format(stockVo.getFlipEndDayTen())).append(")").append("</td>");
            html.append("<td>").append(stockVo.getFlipGainPercentTen()).append("</td>");
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


        colors.add("#99FFCC");
        colors.add("#FFFFCC");
        colors.add("#FFFFFF");
        colors.add("#CCFFFF");
        colors.add("#CCCCFF");
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

    private static SimpleDateFormat formatter = new SimpleDateFormat("MM-dd");

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
            fieldValues[0] = "<a href=\"https://gushitong.baidu.com/fund/ab-" + stockId_name + "\">" + stockId_name + "</a>";

            fieldValues[1] = formatter.format(day.getFlipDayFive()) + "|" + day.getUpwardDaysFive().toString();
            fieldValues[2] = day.getGainPercentFive().toString();
            fieldValues[3] = formatter.format(day.getFlipEndDayFive()) + "|" + day.getFlipUpwardDaysFive();
            fieldValues[4] = day.getFlipGainPercentFive().toString();

            fieldValues[5] = formatter.format(day.getFlipDayTen()) + "|" + day.getUpwardDaysTen().toString();
            fieldValues[6] = day.getGainPercentTen().toString();
            fieldValues[7] = formatter.format(day.getFlipEndDayTen()) + "|" + day.getFlipUpwardDaysTen();
            fieldValues[8] = day.getFlipGainPercentTen().toString();

            list.add(fieldValues);
        }
        result.put("data", list);
        return result;
    }
}
