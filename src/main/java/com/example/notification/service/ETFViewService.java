package com.example.notification.service;

import com.example.notification.constant.Constants;
import com.example.notification.controller.Controller;
import com.example.notification.http.RestRequest;
import com.example.notification.repository.*;
import com.example.notification.responseVo.EtfFlowVO;
import com.example.notification.util.FileUtil;
import com.example.notification.util.Utils;
import com.example.notification.vo.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.example.notification.constant.Constants.*;

@Service
public class ETFViewService {
    private static final Logger logger = LoggerFactory.getLogger(ETFViewService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    @Autowired
    private StockDao stockDao;

    @Autowired
    private StockDailyDao stockDailyDao;

    @Autowired
    private BdIndicatorDao bdIndicatorDao;

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Autowired
    private RestRequest restRequest;

    @Autowired
    private HoldingService holdingService;

    @Autowired
    private IntraDayPriceDao intraDayPriceDao;

    @Autowired
    private RangeSortIdDao rangeSortIdDao;

    @Autowired
    private RangeSortGainDao rangeSortGainDao;

    public void generateReportEveryDay() throws JsonProcessingException, InterruptedException, InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        //check if today market day open
//        Boolean ifMarketOpenToday = kLineService.checkIfMarketOpenToday();
//        if (!ifMarketOpenToday) {
//            return;
//        }
        //just need to get all stocks from stock table, and then get today's kline price to store in db and calculate avg data
        kLineMarketClosedService.getHistoryPriceOnLineAndStoreInDb(MARKETDAYCLOSEDJOB_QUERY_PRICE_DAY);
        Thread.sleep(25000);
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


    @Value("${notification.mainboard300.file}")
    private String mainboard300_path;

    private Integer fiveDayExceedTen_num = 0;


    public Object findAllEtfSortView_new(String num) {
        List<StockNameVO> downwardDaysStock = stockDao.findDownwardDaysStock();
        List<StockNameVO> fiveDayUpwardDays = stockDao.findupwardDaysStock();
        boolean b = fiveDayUpwardDays.addAll(downwardDaysStock);
        if (CollectionUtils.isEmpty(fiveDayUpwardDays)) {
            return "No data found!";
        }

        List<StockNameVO> commonStockList = new ArrayList<>();
        List<StockNameVO> mainEtfs = new ArrayList<>();

        //group main index Kline
        ArrayList<String> mainKlineIds = kLineMarketClosedService.getMainKlineIds();
        Set<String> mainIndexSet = new HashSet<>(mainKlineIds);
        for (StockNameVO fiveDayUpwardDay : fiveDayUpwardDays) {
            if (!mainIndexSet.contains(fiveDayUpwardDay.getStockId())) {
                commonStockList.add(fiveDayUpwardDay);
            } else {
                mainEtfs.add(fiveDayUpwardDay);
            }
        }
        String industryEtfsTable = "";
        if (num.equals("3")) {
            industryEtfsTable = industryEtfsView(commonStockList);
        }
        if (num.equals("4")) {
            //return etfs has stock_ids
            List<StockNameVO> industryEtfs = new ArrayList<>();
            commonStockList.forEach(vo -> {
                if (StringUtils.hasLength(vo.getStockIds())) {
                    industryEtfs.add(vo);
                }
            });
            industryEtfsTable = industryEtfsView(industryEtfs);
        }

        Boolean returnSortOfFiveDay = true;
        if (num.split("_").length > 1) {
            returnSortOfFiveDay = false;
        }
        if (num.contains("wholeEtfsView")) {
            List<StockNameVO> upWardIndustryEtfs = new ArrayList<>();
            List<StockNameVO> downWardIndustryEtfs = new ArrayList<>();
            commonStockList.forEach(vo -> {
                if (vo.getStockName().toLowerCase().contains("etf")) {
                    if (vo.getUpwardDaysFive() >= 0 || vo.getUpwardDaysTen() >= 0) {
                        upWardIndustryEtfs.add(vo);
                    } else {
                        downWardIndustryEtfs.add(vo);
                    }
                }
            });
            upWardIndustryEtfs.addAll(downWardIndustryEtfs);
            industryEtfsTable = buildHtml(upWardIndustryEtfs, returnSortOfFiveDay);
        }
        if (num.contains("300mainBoard")) {
            Set<String> threeHundredSet = new HashSet<>();
            Constants.getMainBoard300(mainboard300_path).forEach(vo -> {
                threeHundredSet.add(vo.getStockId());
            });
            List<StockNameVO> fiveDayExceedTenDay = new ArrayList<>();
            List<StockNameVO> upwardStocks = new ArrayList<>();
            List<StockNameVO> downWardIndustryEtfs = new ArrayList<>();
            commonStockList.forEach(vo -> {
                if (threeHundredSet.contains(vo.getStockId())) {
                    //check if 5day excced 10 day!!
                    List<StockDailyVO> lastTwoDayPrice = stockDailyDao.findLastTwoDayPriceByStockId(vo.getStockId());
                    StockDailyVO yesterdayVo = lastTwoDayPrice.get(1);
                    StockDailyVO todayVo = lastTwoDayPrice.get(0);
                    if (yesterdayVo.getDayAvgFive().compareTo(yesterdayVo.getDayAvgTen()) <= 0 && todayVo.getDayAvgFive().compareTo(todayVo.getDayAvgTen()) >= 0) {
                        fiveDayExceedTenDay.add(vo);
                        fiveDayExceedTen_num++;
                    }
                    if (vo.getUpwardDaysFive() >= 0 || vo.getUpwardDaysTen() >= 0) {
                        upwardStocks.add(vo);
                    } else {
                        downWardIndustryEtfs.add(vo);
                    }
                }
            });
            fiveDayExceedTenDay.addAll(upwardStocks);
            fiveDayExceedTenDay.addAll(downWardIndustryEtfs);

            industryEtfsTable = buildHtml(fiveDayExceedTenDay, true);
        }

        if (num.contains("targetList")) {
            String targetFile = Controller.getTargetFile();
            Set<String> stockIdSet;
            List<StockNameVO> fiveDayExceedTenDay = new ArrayList<>();
            if (targetFile.contains(CONSTANT_BD)) {
                //here to judge if baidu indicator stockIds to show
                stockIdSet = Arrays.stream(bdIndicatorDao.findStockIdsByIndicatorId(targetFile.substring(3)).split(",")).collect(Collectors.toSet());
                stockIdSet.forEach(id -> {
                    Optional<StockNameVO> byId = stockDao.findById(id);
                    byId.ifPresent(vo -> fiveDayExceedTenDay.add(byId.get()));
                });

            } else if (targetFile.contains(CONSTANT_RANGE_SORT)) {
                List<BdIndicatorVO> list = bdIndicatorDao.findupwardDaysIndicator();
                list.addAll(bdIndicatorDao.findDownwardDaysIndicator());
                list.forEach(vo -> {
                    StockNameVO target = new StockNameVO();
                    BeanUtils.copyProperties(vo, target);
                    fiveDayExceedTenDay.add(target);
                });
            } else if (targetFile.contains(CONSTANT_ETF)) {
                stockIdSet = Arrays.stream(stockDao.findStockIdsByEtfId(targetFile.substring(4)).split(",")).collect(Collectors.toSet());
                stockIdSet.forEach(id -> {
                    Optional<StockNameVO> byId = stockDao.findById(id);
                    byId.ifPresent(vo -> fiveDayExceedTenDay.add(byId.get()));
                });
            } else {
                //read excel file
                stockIdSet = FileUtil.readTargetFileStocks(targetFile);
                List<StockNameVO> upwardStocks = new ArrayList<>();
                List<StockNameVO> downWardIndustryEtfs = new ArrayList<>();
                commonStockList.forEach(vo -> {
                    if (stockIdSet.contains(vo.getStockId())) {
                        //check if 5day excced 10 day!!
                        List<StockDailyVO> lastTwoDayPrice = stockDailyDao.findLastTwoDayPriceByStockId(vo.getStockId());
                        StockDailyVO yesterdayVo = lastTwoDayPrice.get(1);
                        StockDailyVO todayVo = lastTwoDayPrice.get(0);
                        if (yesterdayVo.getDayAvgFive().compareTo(yesterdayVo.getDayAvgTen()) <= 0 && todayVo.getDayAvgFive().compareTo(todayVo.getDayAvgTen()) >= 0) {
                            fiveDayExceedTenDay.add(vo);
                            fiveDayExceedTen_num++;
                        }
                        if (vo.getUpwardDaysFive() >= 0 || vo.getUpwardDaysTen() >= 0) {
                            upwardStocks.add(vo);
                        } else {
                            downWardIndustryEtfs.add(vo);
                        }
                    }
                });
                fiveDayExceedTenDay.addAll(upwardStocks);
                fiveDayExceedTenDay.addAll(downWardIndustryEtfs);
            }

            Controller.setTargetFileSize(String.valueOf(fiveDayExceedTenDay.size()));
            industryEtfsTable = buildHtml(fiveDayExceedTenDay, true);
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
            int columnSize = 5;
            if (i % columnSize == 0) {
                html.append("<tr>");
            }
            String id_name = stock.getStockId() + "_" + stock.getStockName();
            String backGroudColor = "#C0C0C0";
            if (stock.getUpwardDaysFive() >= 0) {
                backGroudColor = "#00FF00";
            }
            html.append("<td><div style=\"background-color:").append(backGroudColor).append("\">").append(id_name).append("(" + stock.getUpwardDaysFive()).append("|").append(stock.getGainPercentFive() + ")").append("(" + stock.getFlipUpwardDaysFive()).append("|").append(stock.getFlipGainPercentFive() + ")").append("</br>").append("(" + stock.getUpwardDaysTen()).append("|").append(stock.getGainPercentTen() + ")").append("(" + stock.getFlipUpwardDaysTen()).append("|").append(stock.getFlipGainPercentTen() + ")")
                    .append("</div>").append("<div class=\"index-container\" ").append("id = \"").append("span_").append(id_name).append("\"").append("</td>");
            if (i % columnSize == columnSize) {
                html.append("</tr>");
            }
        }
        return html.toString();
    }

    private static Map<Integer, List<String>> stocksFlowMap = new LinkedHashMap<>();
    private static List<Integer> stocksFlowIndexList = new ArrayList<>();

    private void constructMap() {
        stocksFlowMap.put(-3, new ArrayList<>());
        stocksFlowMap.put(-2, new ArrayList<>());
        stocksFlowMap.put(-1, new ArrayList<>());
        stocksFlowMap.put(1, new ArrayList<>());
        stocksFlowMap.put(2, new ArrayList<>());
        stocksFlowMap.put(3, new ArrayList<>());
        stocksFlowMap.put(4, new ArrayList<>());
        stocksFlowMap.put(5, new ArrayList<>());
        stocksFlowIndexList.clear();
        stocksFlowIndexList.add(-1);
        stocksFlowIndexList.add(-2);
        stocksFlowIndexList.add(-3);
        stocksFlowIndexList.add(1);
        stocksFlowIndexList.add(2);
        stocksFlowIndexList.add(3);
        stocksFlowIndexList.add(4);
        stocksFlowIndexList.add(5);
    }

    public String buildHtml(List<StockNameVO> industryEtfs, Boolean returnFiveSort) {
        //sort 10day avg desc
        industryEtfs = industryEtfs.stream().sorted(Comparator.comparing(StockNameVO::getGainPercentTen)).toList();

        //process
        constructMap();
        String serverIp = Utils.getServerIp();

        HashMap<String, StockNameVO> etsMapForRangeSort = new HashMap<>();
        HashMap<String, Double> etsMapForRangeSortGain = new HashMap<>();
        industryEtfs.forEach(vo -> {
            etsMapForRangeSort.put(vo.getStockId(), vo);
        });

        boolean isRangeSort = false;
        if (StringUtils.hasLength(Constants.getRangeSortDay())) {
            isRangeSort = true;
            //sort by range gain

            List<StockNameVO> sortedList = new ArrayList<>();
            String rangeSortDay = getRangeSortDay();
            List<RangeSortGainVO> findAllByRangeId = rangeSortGainDao.findAllByRangeId(rangeSortDay);
            findAllByRangeId.forEach(rangeVo -> {
                etsMapForRangeSortGain.put(rangeVo.getStockId(), rangeVo.getRangeGain());
                sortedList.add(etsMapForRangeSort.get(rangeVo.getStockId()));
            });
            industryEtfs = sortedList;
        }

        for (int index = 0; index < industryEtfs.size(); index++) {

            StringBuilder tdHtml = new StringBuilder();
            StockNameVO stock = industryEtfs.get(index);

            boolean isETF = false;
            if (stock.getStockName().contains("ETF")) {
                isETF = true;
            }

            String stockId = stock.getStockId();
            String id_name = stockId + "_" + stock.getStockName();

            String fiveBackGroudColor = "#C0C0C0";
            String tenBackGroudColor = "#C0C0C0";

            Integer upwardDaysNum = stock.getUpwardDaysFive();
            Integer capitalType = stock.getCapitalType();
            if (!returnFiveSort) {
                upwardDaysNum = stock.getUpwardDaysTen();
            }

            if (capitalType != null && capitalType == YangQi) {
                //pink 央企
                fiveBackGroudColor = YangQi_Color;
            }
            if (capitalType != null && capitalType == GuoQi) {
                //yellow 国企
                fiveBackGroudColor = GuoQi_Color;
            }

            if (stock.getUpwardDaysTen() >= 0) {
                tenBackGroudColor = "#00FF00";
            }


            tdHtml.append("<td><div style=\"background-color:").append(fiveBackGroudColor).append("\">");
            if (stock.getStockId().startsWith("sh") || stockId.startsWith("sz")) {
                String urlPrefix = "<a href=\"https://gushitong.baidu.com/stock/ab-";
                if (isETF) {
                    urlPrefix = "<a href=\"https://gushitong.baidu.com/fund/ab-";
                    // format is sh1596110#电力ETF
                    tdHtml.append("<a href=\"http://").append(serverIp).append(":8888/listTargetFileStocks/").append("etf_").append(stockId).append("\">").append(stockId).append("</a>#").append(urlPrefix).append(stockId.substring(2)).append("\">");
                } else {
                    tdHtml.append(urlPrefix).append(stockId.substring(2)).append("\">");
                }
            } else {
                // here stockId is bdIndictorId
                tdHtml.append("<a href=\"http://").append(serverIp).append(":8888/listTargetFileStocks/").append("bd_").append(stockId).append("\">").append(stockId).append("</a>#").append("<a href=\"https://gushitong.baidu.com/block/ab-").append(stockId).append("\">");
            }
            String stockIds = stock.getStockIds();
            int belongStockNum = 0;
            if (StringUtils.hasLength(stockIds)) {
                belongStockNum = stockIds.split(",").length;
            }
            tdHtml.append("<b style=font-size:15px >").append(id_name.split("_")[1]);
            if (!stock.getStockId().startsWith("s") || stock.getStockName().contains("ETF")) {
                tdHtml.append("(").append(belongStockNum).append(")");
            }
            tdHtml.append("</b></a>(").append(stock.getUpwardDaysFive()).append("|").append(stock.getGainPercentFive() + ")").append("(" + stock.getFlipUpwardDaysFive()).append("|").append(stock.getFlipGainPercentFive() + ")").append("<br>").append("</div>").append("<div style=\"background-color:").append(tenBackGroudColor).append("\">").append("10Day(" + stock.getUpwardDaysTen()).append("|").append(stock.getGainPercentTen()).append(")").append("(" + stock.getFlipUpwardDaysTen()).append("|").append(stock.getFlipGainPercentTen() + ")");
            // add rangeSort gain
            if (isRangeSort) {
                tdHtml.append("| RangeGain = ").append(etsMapForRangeSortGain.get(stockId));
            }


            tdHtml.append("<div class=\"income-container\" ").append("id = \"").append("income_").append(id_name).append("\" ></div>")
                    .append("</div>").append("<div class=\"index-container\" ").append("id = \"").append("span_").append(id_name).append("\" ></div>")
                    .append("</td>");



            if (isRangeSort) {
                //do stocksFlowMap iteration
                int columnNum = index % 5;
                stocksFlowMap.get(columnNum + 1).add(tdHtml.toString());
            } else {
                if (stocksFlowMap.get(upwardDaysNum) == null) {
                    if (upwardDaysNum < -3) {
                        stocksFlowMap.get(-3).add(tdHtml.toString());
                    } else {
                        stocksFlowMap.get(5).add(tdHtml.toString());
                    }
                } else {
                    stocksFlowMap.get(upwardDaysNum).add(tdHtml.toString());
                }
            }
        }
        if (!isRangeSort) {
            //sort 国企 in the top
            //if it is range_sort, no need to run
            stocksFlowMap.keySet().forEach(key -> {
                List<String> yangQi = new ArrayList<>();
                List<String> guoQi = new ArrayList<>();
                List<String> minQi = new ArrayList<>();
                stocksFlowMap.get(key).forEach(line -> {
                    if (line.contains(YangQi_Color)) {
                        yangQi.add(line);
                    } else if (line.contains(GuoQi_Color)) {
                        guoQi.add(line);
                    } else {
                        minQi.add(line);
                    }
                });
                yangQi.addAll(guoQi);
                yangQi.addAll(minQi);
                stocksFlowMap.put(key, yangQi);

            });
        }

        //calculate max number in column
        List<Integer> intList = new ArrayList<>();
        stocksFlowMap.keySet().forEach(key -> {
            intList.add(stocksFlowMap.get(key).size());
        });
        int trLineSize = intList.stream().sorted(Comparator.reverseOrder()).toList().get(0);

        //build html
        StringBuilder retHtml = new StringBuilder();
        retHtml.append("<tr>");

        for (int i = 0; i < stocksFlowIndexList.size(); i++) {
            List<String> tdList = stocksFlowMap.get(stocksFlowIndexList.get(i));
            int size = tdList.size();
            retHtml.append("<td >").append(size);
            if (isRangeSort && size != 0) {
                RangeSortIDVO rangeSortIDVO = rangeSortIdDao.findById(getRangeSortDay()).get();
                retHtml.append("|RangeSort ").append(rangeSortIDVO.getDayStart()).append(" to ").append(rangeSortIDVO.getDayEnd());
            }
            retHtml.append("</td>");
        }

        retHtml.append("</tr>");
        for (int i = 0; i < trLineSize; i++) {
            StringBuilder trString = new StringBuilder();
            trString.append("<tr>");
            for (Integer integer : stocksFlowIndexList) {
                List<String> tdList = stocksFlowMap.get(integer);
                int size = tdList.size();
                if (i < size) {
                    trString.append(tdList.get(i));
                } else {
                    trString.append("<td>null</td>");
                }
            }
            trString.append("</tr>");
            retHtml.append(trString);
        }
       if(!Utils.isWinSystem()){
           setRangeSortDay(null);
           Controller.setTargetFile(null);
       }
        return retHtml.toString();
    }


    private String dayLineEtfsView(List<StockNameVO> industryEtfs) {
        StringBuilder html = new StringBuilder();
        for (int i = 0; i < industryEtfs.size(); i++) {
            StockNameVO stock = industryEtfs.get(i);
            int columnSizw = 6;
            if (i % columnSizw == 0) {
                html.append("<tr>");
            }

            String id_name = stock.getStockId() + "_" + stock.getStockName();
            String fiveBackGroudColor = "#C0C0C0";
            String tenBackGroudColor = "#C0C0C0";

            if (stock.getUpwardDaysFive() >= 0) {
                fiveBackGroudColor = "#00FF00";
            }

            if (i < fiveDayExceedTen_num) {
                fiveBackGroudColor = " pink";
            }
            if (stock.getUpwardDaysTen() >= 0) {
                tenBackGroudColor = "#00FF00";
            }

            html.append("<td><div style=\"background-color:").append(fiveBackGroudColor).append("\">").append("<a href=\"https://gushitong.baidu.com/fund/ab-").append(stock.getStockId().substring(2)).append("\">").append(id_name).append("</a>").append("(" + stock.getUpwardDaysFive()).append("|").append(stock.getGainPercentFive() + ")").append("(" + stock.getFlipUpwardDaysFive()).append("|").append(stock.getFlipGainPercentFive() + ")").append("<br>").append("</div>").append("<div style=\"background-color:").append(tenBackGroudColor).append("\">").append("(" + stock.getUpwardDaysTen()).append("|").append(stock.getGainPercentTen()).append(")").append("10Day(" + stock.getFlipUpwardDaysTen()).append("|").append(stock.getFlipGainPercentTen() + ")").append("</div>").append("<div class=\"index-container\" ").append("id = \"")
                    .append("span_").append(id_name).append("\"").append("</td>");
            if (i % columnSizw == columnSizw) {
                html.append("</tr>");
            }
        }
        return html.toString();
    }

    private void dayLineEtfs_graph(StringBuilder html, StockNameVO stockVo) {

        Integer upwardDaysTen = stockVo.getUpwardDaysTen();
        Integer upwardDaysFive = stockVo.getUpwardDaysFive();

        Timestamp lastUpdatedTime = stockVo.getLastUpdatedTime();

        Date customized_date_1 = stockVo.getFlipEndDayTen();
        Date customized_date_2 = stockVo.getFlipDayTen();
        Date customized_date_3 = null;
        String customerRange = stockVo.getCustomerRange();
        if (StringUtils.hasLength(customerRange)) {
            String[] ranges = customerRange.split(";");
            for (String range : ranges) {
                String[] split = range.split(",");
                LocalDate customized_1 = LocalDate.parse(split[0], yyyyMMdd_Formatter);
                LocalDate customized_2 = LocalDate.parse(split[1], yyyyMMdd_Formatter);
                customized_date_1 = Date.valueOf(customized_1);
                customized_date_2 = Date.valueOf(customized_2);
                if (split.length == 3 && StringUtils.hasLength(split[2])) {
                    LocalDate customized_3 = LocalDate.parse(split[2], yyyyMMdd_Formatter);
                    customized_date_3 = Date.valueOf(customized_3);
                }
            }
        }

        String updatedDay = lastUpdatedTime == null ? "null" : MMdd_Formatter.format(lastUpdatedTime);
        String cust_day_3 = customized_date_3 == null ? "null" : MMdd_Formatter.format(customized_date_3);
        String cust_day_2 = customized_date_2 == null ? "null" : MMdd_Formatter.format(customized_date_2);
        String cust_day_1 = customized_date_1 == null ? "null" : MMdd_Formatter.format(customized_date_1);

        String eftStockGainTenList = getEftStockGainList(stockVo, customized_date_1, customized_date_2, customized_date_3);

        html.append("</td>");


        String stockId = stockVo.getStockId();
        String stockIds = stockVo.getStockIds();
        int belongStockNum = (StringUtils.hasLength(stockIds)) ? stockIds.split(",").length : 0;

        html.append("<td class=\"hide-column\"><div class=\"chart-container\" id=\"").append("week_").append(stockId).append("\"></div></td>");

        //customized day gain
        html.append("<td><div class=\"chart-container\" id=\"").append("span_").append(stockId).append("\"></div></td>");

        if (upwardDaysFive > 1 && upwardDaysTen >= 0) {
            html.append("<td>");
        } else if (upwardDaysFive < 0 || upwardDaysTen < 0) {
            html.append("<td style=\"background-color: #DEB887;\">");
        } else {
            html.append("<td>");
        }
        html.append(String.format("%-6s", stockVo.getUpwardDaysFive())).append("|").append(String.format("%02d", stockVo.getFlipUpwardDaysFive())).append("|5Day").append("</br>").append(stockVo.getGainPercentFive()).append("%").append("|").append(stockVo.getFlipGainPercentFive()).append("%").append("</br>").append("-----------------<br/>")

                .append(String.format("%02d", upwardDaysTen)).append("|").append(String.format("%02d", stockVo.getFlipUpwardDaysTen())).append("|10Day").append("</br>").append(stockVo.getGainPercentTen()).append("%").append("|").append(stockVo.getFlipGainPercentTen()).append("%").append("</br>").append("-----------------<br/>");

        html.append("<b style=\"font-size:15px\">").append(stockVo.getStockName().replace("ETF", "#")).append("</b>").append(belongStockNum).append("</br>").append(stockId).append("</br>");
        if (StringUtils.hasLength(customerRange)) {
            html.append("<b>#########</b><br/>");
        } else {
            html.append("<b>---------</b><br/>");
        }
        if (!"null".equals(cust_day_3)) {
            updatedDay = cust_day_3;
        }
        html.append(cust_day_1).append("|").append(cust_day_2).append("|").append(updatedDay).append("</br>").append(eftStockGainTenList).append("</br>").append("</td>");
        html.append("</tr>\n");

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
            html.append("<tr style=\"background-color:").append(color).append("\">");

            dayWeekRange_graph(html, stockVo);
        }
        return html.toString();
    }


    private static final SimpleDateFormat MMdd_Formatter = new SimpleDateFormat("MMdd");
    DateTimeFormatter yyyyMMdd_Formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

    private void dayWeekRange_graph(StringBuilder html, StockNameVO stockVo) {

        Integer upwardDaysTen = stockVo.getUpwardDaysTen();
        Integer upwardDaysFive = stockVo.getUpwardDaysFive();
        if (upwardDaysFive > 1 && upwardDaysTen >= 0) {
//            html.append("<td style=\"background-color: #00FFB0;\">");
            html.append("<td>");
        } else if (upwardDaysFive < 0 || upwardDaysTen < 0) {
            html.append("<td style=\"background-color: #DEB887;\">");
        } else {
            html.append("<td>");
        }
        Timestamp lastUpdatedTime = stockVo.getLastUpdatedTime();

        Date customized_date_1 = stockVo.getFlipEndDayTen();
        Date customized_date_2 = stockVo.getFlipDayTen();
        Date customized_date_3 = null;
        String customerRange = stockVo.getCustomerRange();
        if (StringUtils.hasLength(customerRange)) {
            String[] ranges = customerRange.split(";");
            for (String range : ranges) {
                String[] split = range.split(",");
                LocalDate customized_1 = LocalDate.parse(split[0], yyyyMMdd_Formatter);
                LocalDate customized_2 = LocalDate.parse(split[1], yyyyMMdd_Formatter);
                customized_date_1 = Date.valueOf(customized_1);
                customized_date_2 = Date.valueOf(customized_2);
                if (split.length == 3 && StringUtils.hasLength(split[2])) {
                    LocalDate customized_3 = LocalDate.parse(split[2], yyyyMMdd_Formatter);
                    customized_date_3 = Date.valueOf(customized_3);
                }
            }
        }

        String updatedDay = lastUpdatedTime == null ? "null" : MMdd_Formatter.format(lastUpdatedTime);
        String cust_day_3 = customized_date_3 == null ? "null" : MMdd_Formatter.format(customized_date_3);
        String cust_day_2 = customized_date_2 == null ? "null" : MMdd_Formatter.format(customized_date_2);
        String cust_day_1 = customized_date_1 == null ? "null" : MMdd_Formatter.format(customized_date_1);

        String eftStockGainTenList = getEftStockGainList(stockVo, customized_date_1, customized_date_2, customized_date_3);

        html.append(String.format("%-6s", stockVo.getUpwardDaysFive())).append("|").append(String.format("%02d", stockVo.getFlipUpwardDaysFive())).append("</br>").append(stockVo.getGainPercentFive()).append("%").append("|").append(stockVo.getFlipGainPercentFive()).append("%").append("</br>").append("-----------------<br/>")

                .append(String.format("%02d", upwardDaysTen))
//                    .append("|").append(formatter.format(stockVo.getFlipDayTen()))
                .append("|").append(String.format("%02d", stockVo.getFlipUpwardDaysTen())).append("</br>").append(stockVo.getGainPercentTen()).append("%")
//                    .append("|").append(formatter.format(stockVo.getFlipEndDayTen()))
                .append("|").append(stockVo.getFlipGainPercentTen()).append("%").append("</br>").append("</td>");


        String stockId = stockVo.getStockId();
        String stockIds = stockVo.getStockIds();
        int belongStockNum = (StringUtils.hasLength(stockIds)) ? stockIds.split(",").length : 0;
        html.append("<td class=\"hide-column\"><div class=\"chart-container\" style=\"background-color:#FFFFFF\" id=\"").append("week_").append(stockId).append("\"></div></td>");
        //customized day gain
        html.append("<td><div class=\"chart-container\" style=\"background-color:#FFFFFF\" id=\"").append("span_").append(stockId).append("\"></div></td>");
        html.append("<td>");
        html.append("<b style=\"font-size:15px\">").append(stockVo.getStockName().replace("ETF", "#")).append("</b>").append(belongStockNum).append("</br>").append(stockId).append("</br>");
        if (StringUtils.hasLength(customerRange)) {
            html.append("<b>#########</b><br/>");
        } else {
            html.append("<b>---------</b><br/>");
        }
        if (!"null".equals(cust_day_3)) {
            updatedDay = cust_day_3;
        }
        html.append(cust_day_1).append("|").append(cust_day_2).append("|").append(updatedDay).append("</br>").append(eftStockGainTenList).append("</br>").append("</td>");
        html.append("<td><div class=\"multiLine-container\" style=\"background-color:#FFFFFF\" id=\"").append("multi_").append(stockId).append("\"></div></td>");
        html.append("</tr>\n");
    }

    private String getEftStockGainList(StockNameVO stockVo, Date customized_date_1, Date customized_date_2, Date customized_date_3) {
        String stockIds = stockVo.getStockIds();
        StringBuilder htmlStr = new StringBuilder();
        htmlStr.append("-----------------<br/>");
        if (!StringUtils.hasLength(stockIds)) {
            return htmlStr.toString();
        }
        List<String> idList = new ArrayList<>(Arrays.stream(stockIds.split(",")).toList());
        idList.add(stockVo.getStockId());
        List<StockNameVO> list = new ArrayList<>();
        for (String stockId : idList) {
            String stockIdOrNameByMap = holdingService.getStockIdOrNameByMap(stockId);
            if (!StringUtils.hasLength(stockId) || !StringUtils.hasLength(stockIdOrNameByMap)) {
                continue;
            }
            StockDailyVO tempDailyVo = new StockDailyVO();
            tempDailyVo.setClosingPrice(new BigDecimal(0));
            StockDailyVO flipEndVo = stockDailyDao.findDayPriceByStockIdAndDay(stockId, customized_date_1);
            flipEndVo = flipEndVo == null ? tempDailyVo : flipEndVo;
            StockDailyVO flipVo = stockDailyDao.findDayPriceByStockIdAndDay(stockId, customized_date_2);
            flipVo = flipVo == null ? tempDailyVo : flipVo;
            StockDailyVO todayVo = null;
            if (customized_date_3 == null) {
                todayVo = stockDailyDao.findLastOneDayPriceByStockId(stockId);
            } else {
                todayVo = stockDailyDao.findDayPriceByStockIdAndDay(stockId, customized_date_3);
            }
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
        List<StockNameVO> descList = list.stream().sorted(Comparator.comparing(StockNameVO::getGainPercentFive).reversed()).toList();
        for (int i = 0; i < descList.size(); i++) {
            descList.get(i).setStockId(descList.get(i).getGainPercentFive() + "%_" + i);
        }
        List<StockNameVO> resultCollect = descList.stream().sorted(Comparator.comparing(StockNameVO::getFlipGainPercentFive).reversed()).toList();
        for (int i = 0; i < resultCollect.size(); i++) {
            StockNameVO stockNameVO = resultCollect.get(i);
            if (stockNameVO.getStockName().toLowerCase().contains("etf")) {
                htmlStr.append("<b style=\"background-color: #DEB887;\">").append(stockNameVO.getStockName()).append(i).append("</b>|").append(stockNameVO.getFlipGainPercentFive()).append("%").append("|").append(stockNameVO.getStockId()).append("</br>");
            } else {
                htmlStr.append(stockNameVO.getStockName()).append(i).append("|").append(stockNameVO.getFlipGainPercentFive()).append("%").append("|").append(stockNameVO.getStockId()).append("</br>");
            }
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
            result.put(ROWS, Arrays.asList());
            return result;
        }
        result.put(ROWS, fiveDayUpwardDays);
        return result;
    }

    public Object tenDayAdjustedView() {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "upwardDaysTen"));
        orders.add(new Sort.Order(Sort.Direction.DESC, "gainPercentTen"));
        List<StockNameVO> all = stockDao.findAll(Sort.by(orders));
        List<StockNameVO> fiveDayUpwardDays = all;
        if (CollectionUtils.isEmpty(fiveDayUpwardDays)) {
            result.put(ROWS, Arrays.asList());
            return result;
        }
        result.put(ROWS, fiveDayUpwardDays);
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
        return intraDayPriceDao.findMinutesByIdAndToday(etfId, lastestPriceById.getDay());
    }

    private static final Map<Integer, List<StockNameVO>> DayListMap = new HashMap<>();

    static {
        Integer daySize = 8;
        for (int i = 1; i <= daySize; i++) {
            DayListMap.put(i, new ArrayList<>());
            DayListMap.put(-i, new ArrayList<>());
        }
    }

    public Object findAllEtfFlowView(String way) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<Sort.Order> orders = new ArrayList<Sort.Order>();
        orders.add(new Sort.Order(Sort.Direction.DESC, "gainPercentFive"));
        List<StockNameVO> stockDaoAll = stockDao.findAll(Sort.by(orders));
        List<EtfFlowVO> dayList;
        if (way.equals("up")) {
            dayList = daysUp(stockDaoAll, false);
        } else {
            dayList = daysDown(stockDaoAll, false);
        }
        Map<String, Object> result = new HashMap<>();
        if (CollectionUtils.isEmpty(stockDaoAll)) {
            result.put(ROWS, List.of());
            return result;
        }
        result.put(TOTAL, dayList.size());
        result.put(ROWS, dayList);
        return result;
    }

    public Object findMainEtfFlow(String way) throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        ArrayList<String> mainKlineIds = kLineMarketClosedService.getMainKlineIds();
        Set<String> stringSet = new HashSet<>(mainKlineIds);
        List<StockNameVO> stockDaoAll = stockDao.findAllById(stringSet);
        List<EtfFlowVO> dayList;
        if (way.equals("up")) {
            dayList = daysUp(stockDaoAll, true);
        } else {
            dayList = daysDown(stockDaoAll, true);
        }
        Map<String, Object> result = new HashMap<>();
        if (CollectionUtils.isEmpty(stockDaoAll)) {
            result.put(ROWS, List.of());
            return result;
        }
        result.put(TOTAL, dayList.size());
        result.put(ROWS, dayList);
        return result;
    }

    private static final Integer upDayCount = 8;
    private static final Integer downDayCount = 5;


    private static final Map<Integer, String> indexSetMethodMap = new HashMap<>();

    static {
        indexSetMethodMap.put(0, "setOneDayUp");
        indexSetMethodMap.put(1, "setTwoDayUp");
        indexSetMethodMap.put(2, "setThreeDayUp");
        indexSetMethodMap.put(3, "setFourDayUp");
        indexSetMethodMap.put(4, "setFiveDayUp");
        indexSetMethodMap.put(5, "setSixDayUp");
        indexSetMethodMap.put(6, "setSevenDayUp");
        indexSetMethodMap.put(7, "setEightDayUp");
    }

    private List<EtfFlowVO> daysDown(List<StockNameVO> stockDaoAll, boolean isManuEtf) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<StockNameVO> oneDaydownVOs = new ArrayList<>();
        List<StockNameVO> twoDaydownVOs = new ArrayList<>();
        List<StockNameVO> threeDaydownVOs = new ArrayList<>();
        List<StockNameVO> fourDaydownVOs = new ArrayList<>();
        List<StockNameVO> fiveDaydownVOs = new ArrayList<>();
        List<List<StockNameVO>> arrayLists = new ArrayList<>();
        arrayLists.add(oneDaydownVOs);
        arrayLists.add(twoDaydownVOs);
        arrayLists.add(threeDaydownVOs);
        arrayLists.add(fourDaydownVOs);
        arrayLists.add(fiveDaydownVOs);

        ArrayList<String> mainKlineIds = kLineMarketClosedService.getMainKlineIds();
        Set<String> stringSet = new HashSet<>(mainKlineIds);
        stockDaoAll.stream().sorted(Comparator.comparing(StockNameVO::getGainPercentFive)).filter(vo -> vo.getStockName().toLowerCase().contains("etf") && vo.getUpwardDaysFive() != null).filter(vo -> isManuEtf || !stringSet.contains(vo.getStockId())).forEach(vo -> {
            vo.setStockName(vo.getStockName().replace("ETF", ""));
            if (vo.getUpwardDaysFive().equals(-1)) {
                oneDaydownVOs.add(vo);
            }
            if (vo.getUpwardDaysFive().equals(-2)) {
                twoDaydownVOs.add(vo);
            }
            if (vo.getUpwardDaysFive().equals(-3)) {
                threeDaydownVOs.add(vo);
            }
            if (vo.getUpwardDaysFive().equals(-4)) {
                fourDaydownVOs.add(vo);
            }
            if (vo.getUpwardDaysFive() <= -5) {
                vo.setStockName(vo.getStockName());
                if (vo.getUpwardDaysFive() < -5) {
                    vo.setStockName(vo.getStockName() + "#" + vo.getUpwardDaysFive());
                }
                fiveDaydownVOs.add(vo);
            }
        });
        int size = Stream.of(oneDaydownVOs.size(), twoDaydownVOs.size(), threeDaydownVOs.size(), fourDaydownVOs.size(), fiveDaydownVOs.size()).max(Comparator.naturalOrder()).get();
        List<EtfFlowVO> rets = new ArrayList<>(size);

        for (int i = 0; i < size; i++) {
            EtfFlowVO etfFlowVO = new EtfFlowVO();
            Class<? extends EtfFlowVO> aClass = etfFlowVO.getClass();

            for (int j = 0; j < downDayCount; j++) {
                List<StockNameVO> voList = arrayLists.get(j);
                if (voList.size() > i) {
                    StockNameVO vo = voList.get(i);
                    BigDecimal dayGain = getDayGain(vo);
                    String tdContent = getTdContent(dayGain, vo);
                    String methodName = indexSetMethodMap.get(j);
                    Method method = aClass.getMethod(methodName, String.class);
                    method.invoke(etfFlowVO, tdContent);
                }
            }
            rets.add(etfFlowVO);
        }
        return rets;
    }

    private static String getTdContent(BigDecimal dayGain, StockNameVO vo) {
        StringBuilder dayGainFont = new StringBuilder("<div class=");
        if (dayGain.compareTo(BigDecimal.ZERO) < 0) {
            dayGainFont.append("grey-color> ");
        } else {
            dayGainFont.append("white-color> ");
        }
        String hasStockIds = "";
        if (StringUtils.hasLength(vo.getStockIds())) {
            hasStockIds = "##";
        }
        boolean goodEtfExist = false;
        BigDecimal flipGain = vo.getFlipGainPercentFive().setScale(2, RoundingMode.HALF_UP);
        BigDecimal gainPercentFive = vo.getGainPercentFive();
        if (new BigDecimal(-3).compareTo(flipGain) > 0 || new BigDecimal(-3).compareTo(gainPercentFive) > 0) {
            goodEtfExist = true;
        }
        String stockName = vo.getStockName();
        dayGainFont.append(dayGain).append("#").append(vo.getStockId().substring(2)).append(hasStockIds).append("</br>");
        if (goodEtfExist) {
            dayGainFont.append("<div class=goodEft>").append(gainPercentFive.toString()).append("|").append(vo.getFlipUpwardDaysFive()).append("(").append(flipGain).append("|").append("<a href=\"https://gushitong.baidu.com/fund/ab-").append(vo.getStockId().substring(2)).append("\">").append(stockName).append("</a>").append("</div>");
        } else {
            dayGainFont.append(gainPercentFive).append("|").append(vo.getFlipUpwardDaysFive()).append("(").append(flipGain).append("|").append("<a href=\"https://gushitong.baidu.com/fund/ab-").append(vo.getStockId().substring(2)).append("\">").append(stockName).append("</a>");
        }
        dayGainFont.append("</div>");
        return dayGainFont.toString();
    }

    private BigDecimal getDayGain(StockNameVO vo) {
        List<StockDailyVO> lastTwoDayPriceByStockId = stockDailyDao.findLastTwoDayPriceByStockId(vo.getStockId());
        StockDailyVO lastPriceVo = lastTwoDayPriceByStockId.get(0);
        StockDailyVO yesterdayPriceVo = lastTwoDayPriceByStockId.get(1);
        BigDecimal bigDecimal = Utils.calculateDayGainPercentage(lastPriceVo.getClosingPrice(), yesterdayPriceVo.getClosingPrice());
        return bigDecimal;
    }

    private List<EtfFlowVO> daysUp(List<StockNameVO> stockDaoAll, boolean isManuEtf) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        List<StockNameVO> oneDayUpVOs = new ArrayList<>();
        List<StockNameVO> twoDayUpVOs = new ArrayList<>();
        List<StockNameVO> threeDayUpVOs = new ArrayList<>();
        List<StockNameVO> fourDayUpVOs = new ArrayList<>();
        List<StockNameVO> fiveDayUpVOs = new ArrayList<>();
        List<StockNameVO> sixDayUpVOs = new ArrayList<>();
        List<StockNameVO> sevenDayUpVOs = new ArrayList<>();
        List<StockNameVO> eightDayUpVOs = new ArrayList<>();

        List<List<StockNameVO>> arrayLists = new ArrayList<>();
        arrayLists.add(oneDayUpVOs);
        arrayLists.add(twoDayUpVOs);
        arrayLists.add(threeDayUpVOs);
        arrayLists.add(fourDayUpVOs);
        arrayLists.add(fiveDayUpVOs);
        arrayLists.add(sixDayUpVOs);
        arrayLists.add(sevenDayUpVOs);
        arrayLists.add(eightDayUpVOs);


        ArrayList<String> mainKlineIds = kLineMarketClosedService.getMainKlineIds();
        Set<String> stringSet = new HashSet<>(mainKlineIds);
        stockDaoAll.stream().sorted(Comparator.comparing(StockNameVO::getGainPercentFive).reversed()).filter(vo -> vo.getStockName().toLowerCase().contains("etf") && vo.getUpwardDaysFive() != null).filter(vo -> isManuEtf || !stringSet.contains(vo.getStockId())).forEach(vo -> {
            vo.setStockName(vo.getStockName().replace("ETF", ""));
            if (vo.getUpwardDaysFive().equals(1)) {
                oneDayUpVOs.add(vo);
            }
            if (vo.getUpwardDaysFive().equals(2)) {
                twoDayUpVOs.add(vo);
            }
            if (vo.getUpwardDaysFive().equals(3)) {
                threeDayUpVOs.add(vo);
            }
            if (vo.getUpwardDaysFive().equals(4)) {
                fourDayUpVOs.add(vo);
            }
            if (vo.getUpwardDaysFive().equals(5)) {
                fiveDayUpVOs.add(vo);
            }
            if (vo.getUpwardDaysFive().equals(6)) {
                sixDayUpVOs.add(vo);
            }
            if (vo.getUpwardDaysFive().equals(7)) {
                sevenDayUpVOs.add(vo);
            }
            if (vo.getUpwardDaysFive() >= 8) {
                vo.setStockName(vo.getStockName());
                if (vo.getUpwardDaysFive() > 8) {
                    vo.setStockName(vo.getStockName() + "#" + vo.getUpwardDaysFive());
                }
                eightDayUpVOs.add(vo);
            }
        });

        int size = Stream.of(oneDayUpVOs.size(), twoDayUpVOs.size(), threeDayUpVOs.size(), fourDayUpVOs.size(), fiveDayUpVOs.size(), sixDayUpVOs.size(), sevenDayUpVOs.size(), eightDayUpVOs.size()).max(Comparator.naturalOrder()).get();
        List<EtfFlowVO> rets = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            EtfFlowVO etfFlowVO = new EtfFlowVO();
            Class<? extends EtfFlowVO> aClass = etfFlowVO.getClass();

            for (int j = 0; j < upDayCount; j++) {
                List<StockNameVO> voList = arrayLists.get(j);
                if (voList.size() > i) {
                    StockNameVO vo = voList.get(i);
                    BigDecimal dayGain = getDayGain(vo);
                    String tdContent = getTdContent(dayGain, vo);
                    String methodName = indexSetMethodMap.get(j);
                    Method method = aClass.getMethod(methodName, String.class);
                    method.invoke(etfFlowVO, tdContent);
                }
            }
            rets.add(etfFlowVO);
        }
        return rets;
    }

    public void updateBelongStockIds() {
        stockDao.findEtfIds().forEach(stockNameVO -> {
            JsonNode jsonNode = restRequest.queryEtfInfoFromBd(stockNameVO.getStockId());
            if (jsonNode != null) {
                StringBuilder stockIds = new StringBuilder();
                for (JsonNode item : jsonNode) {
                    String tmpCode = item.get("code").textValue();
                    List<StockNameVO> stockList = stockDao.findByStockIdLike("%" + tmpCode);
                    if (!CollectionUtils.isEmpty(stockList) && stockList.size() == 1) {
                        String stockId = stockList.get(0).getStockId();
                        stockIds.append(stockId).append(",");
                    } else {
                        logger.info("=====updateBelongStockIds==Found more than two ==Stocks={}", stockList);
                        return;
                    }
                }
                String ret = stockIds.toString();
                if (ret.length() > 1) {
                    stockNameVO.setStockIds(ret);
                    stockDao.save(stockNameVO);
                    logger.info("=====updateBelongStockIds==save stock_ids from Baidu==etfName={}", stockNameVO.getStockName());
                }
            }
        });
    }
}
