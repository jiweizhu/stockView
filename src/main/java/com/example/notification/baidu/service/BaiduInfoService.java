package com.example.notification.baidu.service;

import com.example.notification.baidu.respVo.FinancialRespVO;
import com.example.notification.baidu.vo.BdFinancialNetVO;
import com.example.notification.baidu.vo.IndicatorDayVO;
import com.example.notification.baidu.vo.IndicatorVO;
import com.example.notification.constant.Constants;
import com.example.notification.controller.Controller;
import com.example.notification.http.RestRequest;
import com.example.notification.repository.*;
import com.example.notification.service.ETFViewService;
import com.example.notification.service.KLineMarketClosedService;
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
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static com.example.notification.constant.Constants.*;

@Service
public class BaiduInfoService {
    private static final Logger logger = LoggerFactory.getLogger(BaiduInfoService.class);
    private static ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);


    @Autowired
    private RestRequest restRequest;

    @Autowired
    private ETFViewService etfViewService;

    @Autowired
    private ThreadPoolTaskExecutor executorService;

    @Autowired
    private BdIndicatorDao bdIndicatorDao;

    @Autowired
    private BdFinancialSumDao bdFinancialSumDao;

    @Autowired
    private BdFinacialDao bdFinacialDao;

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Autowired
    private StockDailyDao stockDailyDao;

    @Autowired
    private StockDao stockDao;

    @Autowired
    private WeeklyPriceDao weeklyPriceDao;

    @Autowired
    private BdIndicatorDailyDao bdIndicatorDailyDao;

    @Autowired
    private BdIndicatorWeeklyDao bdIndicatorWeeklyDao;

    @Autowired
    private BdIndicatorDropDao bdIndicatorDropDao;

    @Autowired
    private RangeSortIdDao rangeSortIdDao;

    @Autowired
    private RangeSortGainDao rangeSortGainDao;

    public void calculateRangeSort() {
        //handle bd range gain
        rangeSortIdDao.findAll().forEach(rangeVo -> {
            bdIndicatorDao.findAll().forEach(idVo -> {
                BdIndicatorDailyVO startVo = bdIndicatorDailyDao.findByStockIdAndDay(idVo.getStockId(), rangeVo.getDayStart());
                BdIndicatorDailyVO endVo = bdIndicatorDailyDao.findByStockIdAndDay(idVo.getStockId(), rangeVo.getDayEnd());
                String gainPercent = Utils.calculateDayGainPercentage(endVo.getClosingPrice(), startVo.getClosingPrice()).toString();
                RangeSortGainVO rangeSortGainVO = new RangeSortGainVO();
                rangeSortGainVO.setRangeId(rangeVo.getRangeId());
                rangeSortGainVO.setStockId(idVo.getStockId());
                rangeSortGainVO.setRangeGain(Double.valueOf(gainPercent));
                rangeSortGainDao.save(rangeSortGainVO);
                logger.info("====save=stock={}==rangeSortGain={}", idVo.getStockName(), rangeSortGainVO);
            });
        });
    }

    public List<IndicatorVO> queryBaiduIndustriesRealInfo() {
        List<IndicatorVO> list = restRequest.queryBaiduIndustriesRealInfo();
        return list;
    }

    static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");


    public Object getStockJsonDataDay(String stockId) {
        ArrayList<String[]> result = new ArrayList<>();
        logger.info("enter BaiduInfoService getStockJsonDataDay stockId =============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }

        Integer rangeSize = getRangeSize();

        List<BdIndicatorDailyVO> voList = bdIndicatorDailyDao.findByIndexStockIdOrderByDay(stockId, rangeSize).stream().sorted(Comparator.comparing(BdIndicatorDailyVO::getDay)).toList();
        //as baidu restrict to query
        // return db data
        for (BdIndicatorDailyVO vo : voList) {
            String[] strings = new String[7];
            strings[0] = Utils.getFormat(vo.getDay());
            strings[1] = vo.getOpeningPrice().toString();
            strings[2] = vo.getClosingPrice().toString();
            strings[3] = vo.getIntradayHigh().toString();
            strings[4] = vo.getIntradayLow().toString();
            result.add(strings);
        }
        return result;
    }

    public Object getStockJsonDataWeek(String stockId) {
        ArrayList<String[]> result = new ArrayList<>();
        logger.info("enter BaiduInfoService getStockJsonDataWeek stockId =============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }

        Integer rangeSize = getRangeWkSize();

        List<BdIndicatorWeeklyVO> voList = bdIndicatorWeeklyDao.findByIndexStockIdOrderByDay(stockId, rangeSize).stream().sorted(Comparator.comparing(BdIndicatorWeeklyVO::getDay)).toList();
        //as baidu restrict to query
        // return db data
        for (BdIndicatorWeeklyVO vo : voList) {
            String[] strings = new String[7];
            strings[0] = Utils.getFormat(vo.getDay());
            strings[1] = vo.getOpeningPrice().toString();
            strings[2] = vo.getClosingPrice().toString();
            strings[3] = vo.getIntradayHigh().toString();
            strings[4] = vo.getIntradayLow().toString();
            result.add(strings);
        }
        return result;
    }


    public void calculateIndicatorsAvg() {
        logger.debug("enter BaiduInfoService calculateIndicatorsAvg =============");
        List<BdIndicatorVO> ids = bdIndicatorDao.findAll();
        // debug start
//        ids = new ArrayList<>();
//        ids.add(new BdIndicatorVO("750200"));
        //debug end
        List<Callable<Void>> tasks = new ArrayList<>();

        ids.forEach(id -> {
            tasks.add(() -> {
                //calculate avg of day
                List<BdIndicatorDailyVO> dayLine = bdIndicatorDailyDao.findLastDaysByNumAndId(id.getStockId(), 120);
                setUpwardDaysAndGainOfFive(id, dayLine);
                setUpwardDaysAndGainOfTen(id, dayLine);
                bdIndicatorDao.save(id);
                logger.info("Finish update ==" + id.getStockId() + "===Avg data=====");
                return null;
            });
        });

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

    }

    private static void setUpwardDaysAndGainOfFive(BdIndicatorVO id, List<BdIndicatorDailyVO> dayLine) {
        int upwardDays = 0;
        int i1 = dayLine.size() - 1;
        for (int i = 0; i < i1; i++) {
            if (dayLine.get(i).getDayAvgFive().compareTo(dayLine.get(i + 1).getDayAvgFive()) > 0) {
                if (upwardDays < 0) break;
                upwardDays++;
            } else {
                if (upwardDays > 0) break;
                upwardDays--;
            }
        }
        id.setUpwardDaysFive(upwardDays);
        // setGainPercentFive
        BigDecimal dayAvgFive = dayLine.get(0).getDayAvgFive();
        int abs = Math.abs(upwardDays);
        BigDecimal denominator = dayLine.get(abs).getDayAvgFive();
        BigDecimal gainPercentFive = Utils.calculateDayGainPercentage(dayAvgFive, denominator);
        id.setGainPercentFive(gainPercentFive);
        id.setFlipDayFive(dayLine.get(abs).getDay());

        // flip day avg
        int flipDays = 0;
        for (int i = abs; i < i1; i++) {
            if (dayLine.get(i).getDayAvgFive().compareTo(dayLine.get(i + 1).getDayAvgFive()) > 0) {
                if (flipDays < 0) break;
                flipDays++;
            } else {
                if (flipDays > 0) break;
                flipDays--;
            }
        }

        id.setFlipUpwardDaysFive(flipDays);
        BigDecimal flipDayAvgFive = dayLine.get(abs).getDayAvgFive();
        int flipAbs = Math.abs(upwardDays) + Math.abs(flipDays);
        BigDecimal filpDenominator = dayLine.get(flipAbs).getDayAvgFive();
        BigDecimal flipGainPercentFive = Utils.calculateDayGainPercentage(flipDayAvgFive, filpDenominator);
        id.setFlipGainPercentFive(flipGainPercentFive);
        id.setFlipEndDayFive(dayLine.get(flipAbs).getDay());
    }

    private static void setUpwardDaysAndGainOfTen(BdIndicatorVO id, List<BdIndicatorDailyVO> dayLine) {
        int upwardDays = 0;
        int i1 = dayLine.size() - 1;
        for (int i = 0; i < i1; i++) {
            if (dayLine.get(i).getDayAvgTen().compareTo(dayLine.get(i + 1).getDayAvgTen()) > 0) {
                if (upwardDays < 0) break;
                upwardDays++;
            } else {
                if (upwardDays > 0) break;
                upwardDays--;
            }
        }
        id.setUpwardDaysTen(upwardDays);
        // setGainPercentTen
        BigDecimal dayAvgTen = dayLine.get(0).getDayAvgTen();
        int abs = Math.abs(upwardDays);
        BigDecimal denominator = dayLine.get(abs).getDayAvgTen();
        BigDecimal gainPercentTen = Utils.calculateDayGainPercentage(dayAvgTen, denominator);
        id.setGainPercentTen(gainPercentTen);
        id.setFlipDayTen(dayLine.get(abs).getDay());

        // flip day avg
        int flipDays = 0;
        for (int i = abs; i < i1; i++) {
            if (dayLine.get(i).getDayAvgTen().compareTo(dayLine.get(i + 1).getDayAvgTen()) > 0) {
                if (flipDays < 0) break;
                flipDays++;
            } else {
                if (flipDays > 0) break;
                flipDays--;
            }
        }

        id.setFlipUpwardDaysTen(flipDays);
        BigDecimal flipDayAvg = dayLine.get(abs).getDayAvgTen();
        int flipAbs = Math.abs(upwardDays) + Math.abs(flipDays);
        BigDecimal filpDenominator = dayLine.get(flipAbs).getDayAvgTen();
        BigDecimal flipGainPercentFive = Utils.calculateDayGainPercentage(flipDayAvg, filpDenominator);
        id.setFlipGainPercentTen(flipGainPercentFive);
        id.setFlipEndDayTen(dayLine.get(flipAbs).getDay());
    }

    private static void setUpwardDaysTen(BdIndicatorVO id, List<IndicatorDayVO> dayLine) {
        int upwardDaysTen = 0;
        for (int i = dayLine.size() - 1; i > 10; i--) {
            if (dayLine.get(i).getMa10avgprice() >= dayLine.get(i - 1).getMa10avgprice()) {
                if (upwardDaysTen < 0) break;
                upwardDaysTen++;
            } else {
                if (upwardDaysTen > 0) break;
                upwardDaysTen--;
            }
        }
        id.setUpwardDaysTen(upwardDaysTen);
    }

    public String indicatorsView(boolean isWeek) {
        List<BdIndicatorVO> list = bdIndicatorDao.findupwardDaysIndicator();
        list.addAll(bdIndicatorDao.findDownwardDaysIndicator());
//        Comparator<BdIndicatorVO> comparator = Comparator.comparing(BdIndicatorVO::getUpwardDaysFive);
//        List<BdIndicatorVO> resp = list.stream().sorted(comparator).toList();
        List<StockNameVO> industryEtfs = new ArrayList<>();
        list.forEach(vo -> {
            StockNameVO target = new StockNameVO();
            BeanUtils.copyProperties(vo, target);
            industryEtfs.add(target);
        });
        String html = buildHtmlForBd(industryEtfs, true, isWeek);
        return html;
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
        stocksFlowMap.put(6, new ArrayList<>());
        stocksFlowMap.put(7, new ArrayList<>());
        stocksFlowMap.put(8, new ArrayList<>());
        stocksFlowIndexList.clear();
        stocksFlowIndexList.add(-1);
        stocksFlowIndexList.add(-2);
        stocksFlowIndexList.add(-3);
        stocksFlowIndexList.add(1);
        stocksFlowIndexList.add(2);
        stocksFlowIndexList.add(3);
        stocksFlowIndexList.add(4);
        stocksFlowIndexList.add(5);
        stocksFlowIndexList.add(6);
        stocksFlowIndexList.add(7);
        stocksFlowIndexList.add(8);
    }

    // todo optimize buildHtmlForBd
    public String buildHtmlForBd(List<StockNameVO> industryEtfs, Boolean returnFiveSort, boolean isWeek) {
        if (isWeek) {
            LinkedList<StockNameVO> sortedList = new LinkedList();
            industryEtfs.forEach(vo -> {
                int count = bdIndicatorWeeklyDao.lineCountByStockId(vo.getStockId());
                if (count > 300) {
                    //means it has data before 2022
                    //put to the top
                    sortedList.addFirst(vo);
                } else {
                    sortedList.addLast(vo);
                }
            });
            industryEtfs = sortedList;
        }

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

            String stockId = stock.getStockId();
            String id_name = stockId + "_" + stock.getStockName();

            String fiveBackGroudColor = "#C0C0C0";
            String tenBackGroudColor = "#C0C0C0";

            Integer upwardDaysNum = stock.getUpwardDaysFive();
            if (!returnFiveSort) {
                upwardDaysNum = stock.getUpwardDaysTen();
            }


            if (stock.getUpwardDaysTen() >= 0) {
                tenBackGroudColor = "#00FF00";
            }

            tdHtml.append("<td><div style=\"background-color:").append(fiveBackGroudColor).append("\">");
            // here stockId is bdIndictorId
            tdHtml.append("<a href=\"http://").append(serverIp).append(":8888/listTargetFileStocks/").append("bd_").append(stockId).append("\">").append(stockId).append("</a>#")
                    .append("<a href=\"https://gushitong.baidu.com/block/ab-").append(stockId).append("\">");
            String stockIds = stock.getStockIds();
            int belongStockNum = 0;
            if (StringUtils.hasLength(stockIds)) {
                belongStockNum = stockIds.split(",").length;
            }
            tdHtml.append("<b style=font-size:20px >").append(id_name.split("_")[1]);
            if (!stock.getStockId().startsWith("s") || stock.getStockName().contains("ETF")) {
                tdHtml.append("(").append(belongStockNum).append(")");
            }
            tdHtml.append("</b></a>(").append(stock.getUpwardDaysFive()).append("|").append(stock.getGainPercentFive() + ")")
                    .append("(" + stock.getFlipUpwardDaysFive()).append("|").append(stock.getFlipGainPercentFive() + ")");
            tdHtml.append("<br>")
                    .append("</div>")
                    .append("<div style=\"background-color:").append(tenBackGroudColor).append("\">")
                    .append("10Day(" + stock.getUpwardDaysTen()).append("|").append(stock.getGainPercentTen()).append(")")
                    .append("(" + stock.getFlipUpwardDaysTen()).append("|").append(stock.getFlipGainPercentTen() + ")");
            // add rangeSort gain
            if (isRangeSort) {
                tdHtml.append("| RangeGain = ").append(etsMapForRangeSortGain.get(stockId));
            }

            tdHtml.append("<div class=\"index-container\" ").append("id = \"").append("span_").append(id_name).append("\" ></div>").append("</td>");

            if (isRangeSort || isWeek) {
                //do stocksFlowMap iteration
                int columnNum = index % 8;
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
        if (!Utils.isWinSystem()) {
            setRangeSortDay(null);
            Controller.setTargetFile(null);
        }
        return retHtml.toString();
    }


    public Object indicatorStocksView(String indicatorId) {


        return null;
    }


    public List<FinancialRespVO> readBdFinancialDataFromDbByStockId(String stockId) {

        List<FinancialRespVO> ret = new ArrayList<>();
        if (stockId.startsWith("s")) {
            List<BdFinancialVO> byStockId = bdFinacialDao.findByStockId(stockId).stream().sorted(Comparator.comparing(BdFinancialVO::getReportDay)).toList();
            //read data
            byStockId.forEach(vo -> {
                String reportDay = vo.getReportDay();
                FinancialRespVO newVo = new FinancialRespVO(stockId, reportDay, vo.getGrossIncome(), vo.getGrossIncomeGain(), vo.getGrossProfit(), vo.getGrossProfitGain());
                ret.add(newVo);
            });
        } else {
            List<BdFinancialSumVO> byStockId = bdFinancialSumDao.findSumByIndicatorId(stockId).stream().sorted(Comparator.comparing(BdFinancialSumVO::getReportDay)).toList();
            //read data
            byStockId.forEach(vo -> {
                String reportDay = vo.getReportDay();
                Integer grossGainAscNum = vo.getGrossGainAscNum();
                Integer grossGainDescNum = vo.getGrossGainDescNum();
                Integer profitGainAscNum = vo.getProfitGainAscNum();
                Integer profitGainDescNum = vo.getProfitGainDescNum();
                int grossGainSum = grossGainAscNum + grossGainDescNum;
                int profitGainSum = profitGainAscNum + profitGainDescNum;

                double grossGainPercent = Utils.divideReturnPercent(grossGainAscNum, grossGainSum);
                double profitGainPercent = Utils.divideReturnPercent(profitGainAscNum, profitGainSum);
                FinancialRespVO newVo = new FinancialRespVO(stockId, reportDay, null, grossGainPercent, null, profitGainPercent);
                ret.add(newVo);
            });
        }

        return ret;
    }

    private static String changeReportDay(String reportDay) {
        if (reportDay.contains("一季报")) {
            reportDay = reportDay.replace("一季报", SEASON_DAY_0401);
        } else if (reportDay.contains("三季报")) {
            reportDay = reportDay.replace("三季报", SEASON_DAY_0901);
        } else if (reportDay.contains("中报")) {
            reportDay = reportDay.replace("中报", SEASON_DAY_0701);
        } else {
            reportDay = reportDay.replace("年报", SEASON_DAY_1231);
        }
        return reportDay;
    }


    public void queryBaiduIncomeDataFromNetForAllStocks() {
        List<StockNameVO> all = stockDao.findAll();
        if (Utils.isWinSystem()) {
            //for local test
            List<String> addList = new ArrayList<>();
            addList.add("sh600498");
            addList.add("sh600487");
            addList.add("sh600522");
            addList.add("sh603083");
            addList.add("sz002313");
            addList.add("sz000063");
            all = new ArrayList<>();
            for (String str : addList) {
                all.add(new StockNameVO(str, str));
            }
        }
        all.forEach(vo -> {
            try {
                if (!vo.getStockId().startsWith("s") || vo.getStockName().toLowerCase().contains("etf")) {
                    return;
                }
                queryBaiduIncomeDataFromNet(vo.getStockId());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public void queryBaiduIncomeDataFromNet(String stockId) throws JsonProcessingException {
        JsonNode jsonNode = restRequest.queryBaiduIncomeDataFromNet(stockId.replaceAll("sh|sz", ""));
        if (jsonNode != null && jsonNode.isArray()) {
            for (JsonNode node : jsonNode) {
                BdFinancialNetVO bdFinancialVO = objectMapper.treeToValue(node, BdFinancialNetVO.class);
                BdFinancialVO target = new BdFinancialVO();
                target.setStockId(stockId);
                String reportDay = bdFinancialVO.getText();
                reportDay = changeReportDay(reportDay);
                target.setReportDay(reportDay);
                String content = objectMapper.writeValueAsString(bdFinancialVO.getContent());
                target.setContent(content);

                JsonNode rootNode = objectMapper.readTree(content);
                for (JsonNode tmpNode : rootNode) {
                    if (tmpNode.get("data").get(0) == null || tmpNode.get("data").get(0).get("header") == null) {
                        continue;
                    }
                    String header = tmpNode.get("data").get(0).get("header").toString();
                    if (header.contains("综合收益总额")) {
                        //save in db
                        JsonNode jsonNode1 = objectMapper.readTree(header);
                        String gain = jsonNode1.get(1).textValue();
                        String total = jsonNode1.get(2).textValue();
                        if (gain.contains("--") || total.contains("--")) {
                            logger.info("=========data not fit=======stockId={}, reportDay={}", target.getStockId(), target.getReportDay());
                            continue;
                        }
                        target.setGrossProfit(total);
                        target.setGrossProfitGain(Double.parseDouble(gain.substring(0, gain.indexOf("%"))));
                    }
                    if (header.contains("总营收")) {
                        //save in db
                        JsonNode jsonNode1 = objectMapper.readTree(header);
                        String gain = jsonNode1.get(1).textValue();
                        String total = jsonNode1.get(2).textValue();
                        if (gain.contains("--") || total.contains("--")) {
                            logger.info("=========data not fit=======stockId={}, reportDay={} ", target.getStockId(), target.getReportDay());
                            continue;
                        }
                        target.setGrossIncome(total);
                        target.setGrossIncomeGain(Double.parseDouble(gain.substring(0, gain.indexOf("%"))));
                    }
                }
                if (target.getGrossProfit() == null) {
                    continue;
                }
                target.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
                bdFinacialDao.save(target);
            }
        }
    }

    public void getFromNetAndStoreDay(int daysBofore) {
        List<String> ids = bdIndicatorDao.findIds();
        if (Utils.isWinSystem()) {
            ids = new ArrayList<>();
            ids.add("730200");
            ids.add("770100");
        }
        String formattedDaysBefore = Utils.getFormattedDaysBefore(daysBofore);
        logger.info("========getFromNetAndStoreDay =====formattedDaysBefore={}==ids.size ={}====={}", formattedDaysBefore, ids.size(), ids);
        List<Callable<Void>> tasks = new ArrayList<>();
        for (String stockId : ids) {
            tasks.add(() -> {
                Set<String> exsitingDaySet = new HashSet<>();
                List<BdIndicatorDailyVO> allByStockId = bdIndicatorDailyDao.findAllByStockId(stockId);

                for (BdIndicatorDailyVO dailyVO : allByStockId) {
                    exsitingDaySet.add(dailyVO.getDay().toString());
                }
                if (exsitingDaySet.contains(Date.valueOf(LocalDate.now()).toString())) {
                    return null;
                }
//                Thread.sleep(new Random().nextInt(2000));
                List<IndicatorDayVO> fromNetList = restRequest.queryBaiduIndustriesKline(stockId, "day", formattedDaysBefore);
                // save new in db
                int loopNum = 0;
                for (IndicatorDayVO dayVO : fromNetList) {
                    if (exsitingDaySet.contains(dayVO.getDay()) || dayVO.getMa20avgprice() == null) {
                        continue;
                    }
                    loopNum++;
                    Date dateFromNet = Date.valueOf(dayVO.getDay());
                    BdIndicatorDailyVO newVo = new BdIndicatorDailyVO();
                    newVo.setStockId(stockId);
                    newVo.setDay(dateFromNet);
                    newVo.setOpeningPrice(BigDecimal.valueOf(dayVO.getOpen()));
                    newVo.setClosingPrice(BigDecimal.valueOf(dayVO.getClose()));
                    newVo.setIntradayHigh(BigDecimal.valueOf(dayVO.getHigh()));
                    newVo.setIntradayLow(BigDecimal.valueOf(dayVO.getLow()));
                    newVo.setDayAvgFive(BigDecimal.valueOf(dayVO.getMa5avgprice()));
                    newVo.setDayAvgTen(BigDecimal.valueOf(dayVO.getMa10avgprice()));
                    newVo.setDayAvgTwenty(BigDecimal.valueOf(dayVO.getMa20avgprice()));
                    bdIndicatorDailyDao.save(newVo);
                }
                logger.info("========getFromNetAndStoreDay =====stockId={}===exsitingDaySet=={}=fromNetList size={}===loopNum=={}", stockId, exsitingDaySet.size(), fromNetList.size(), loopNum);
                return null;
            });
        }
        try {
            logger.info("========getFromNetAndStoreDay ========start to invokeAll =tasks.size ={}", tasks.size());
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
    }

    public void getFromNetAndStoreWeek(boolean isInitData) {
        List<String> ids = bdIndicatorDao.findIds();
        ids.add("sh000300");
        List<Callable<Void>> tasks = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate fiveDaysAgo = today.minusWeeks(50);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String tenDaysBefore = fiveDaysAgo.format(formatter);
        if (isInitData) {
            tenDaysBefore = "2013-08-01";
        }
        logger.info("========getFromNetAndStoreWeek ========ids.size ={}==tenDaysBefore={}=={}", ids.size(), tenDaysBefore, ids);
        for (String stockId : ids) {
            String finalTenDaysBefore = tenDaysBefore;
            tasks.add(() -> {
                Set<String> exsitingDaySet = new HashSet<>();
                List<BdIndicatorWeeklyVO> allByStockId = bdIndicatorWeeklyDao.findAllByStockId(stockId);

                for (BdIndicatorWeeklyVO dailyVO : allByStockId) {
                    exsitingDaySet.add(dailyVO.getDay().toString());
                }

                if (exsitingDaySet.contains(Date.valueOf(LocalDate.now()).toString())) {
                    return null;
                }
                List<IndicatorDayVO> fromNetList = restRequest.queryBaiduIndustriesKline(stockId, "week", finalTenDaysBefore);
                // save new in db
                int loopNum = 0;
                for (IndicatorDayVO dayVO : fromNetList) {
                    if (exsitingDaySet.contains(dayVO.getDay()) || dayVO.getMa10avgprice() == null) {
                        continue;
                    }
                    loopNum++;
                    Date dateFromNet = Date.valueOf(dayVO.getDay());
                    BdIndicatorWeeklyVO newVo = new BdIndicatorWeeklyVO();
                    newVo.setStockId(stockId);
                    newVo.setDay(dateFromNet);
                    newVo.setOpeningPrice(BigDecimal.valueOf(dayVO.getOpen()));
                    newVo.setClosingPrice(BigDecimal.valueOf(dayVO.getClose()));
                    newVo.setIntradayHigh(BigDecimal.valueOf(dayVO.getHigh()));
                    newVo.setIntradayLow(BigDecimal.valueOf(dayVO.getLow()));
                    newVo.setDayAvgFive(BigDecimal.valueOf(dayVO.getMa5avgprice()));
                    newVo.setDayAvgTen(BigDecimal.valueOf(dayVO.getMa10avgprice()));
                    bdIndicatorWeeklyDao.save(newVo);
                }
                logger.info("========getFromNetAndStoreWeek =====stockId={}===exsitingDaySet=={}=fromNetList size={}===loopNum=={}", stockId, exsitingDaySet.size(), fromNetList.size(), loopNum);
                return null;
            });
        }
        try {
            logger.info("========getFromNetAndStoreWeek ========start to invokeAll =tasks.size ={}", tasks.size());
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
    }

    public void updateIndicatorBelongStocks() {
        // iterate indicators to get stockids from net
        List<BdIndicatorVO> indicatorIds = bdIndicatorDao.findAll();
        indicatorIds.forEach(vo -> {
            //update to bd_indicator stock_ids
            JsonNode jsonNode = restRequest.queryBaiduIndustryOwnedStocks(vo.getStockId());
            if (jsonNode != null && jsonNode.isArray()) {
                StringBuilder stockIdsLine = new StringBuilder();
                int loopCount = 0;
                for (JsonNode node : jsonNode) {
                    String market = node.get("exchange").asText().toLowerCase();
                    String code = node.get("code").asText();
                    stockIdsLine.append(market).append(code).append(",");
                    loopCount++;
                }
                vo.setStockIds(stockIdsLine.toString());
                bdIndicatorDao.save(vo);
            }
        });
    }

    public List<RangeSortIDVO> rangeSortQuery() {
        return rangeSortIdDao.findAll();
    }


    public Object queryFinancialSum() {
        List<BdIndicatorVO> indicators = bdIndicatorDao.findAll();
        if (Utils.isWinSystem()) {
            indicators = new ArrayList<>();
            indicators.add(new BdIndicatorVO("730200", "通信设备"));
            indicators.add(new BdIndicatorVO("770100", "个护用品"));
        }
        List<Map<String, Object>> retJsonList = new ArrayList<>();
        // format is like {"name":"电力","2024四收入Asc":"15","2024四收入Desc":"5","2024四利润Asc":"15","2024四利润Desc":"5"}
        // change to
        // format is like {"name":"电力","2024四收入Asc(%)":"15","2024四利润Asc(%)":"15"}
        indicators.forEach(indicator -> {
            String stockId = indicator.getStockId();
            List<BdFinancialSumVO> voList = bdFinancialSumDao.findByIndicatorId(stockId);
            int length = bdIndicatorDao.findStockIdsByIndicatorId(indicator.getStockId()).split(",").length;
            Map<String, Object> lineMap = new HashMap<>();
            for (BdFinancialSumVO vo : voList) {
                String stockName = indicator.getStockName();
                StringBuilder sb = new StringBuilder();
                sb.append("<a href=\"http://").append(Utils.getServerIp()).append(":8888/listTargetFileStocks/").append("bd_").append(stockId).append("\">").append(stockName).append("(").append(length).append(")").append("</a>");
                lineMap.put("name", sb.toString());
                String reportDay = vo.getReportDay();
                reportDay = changeReportDayToChinese(reportDay);
                Integer grossGainAscNum = vo.getGrossGainAscNum();
                Integer grossGainDescNum = vo.getGrossGainDescNum();
                Integer profitGainAscNum = vo.getProfitGainAscNum();
                Integer profitGainDescNum = vo.getProfitGainDescNum();
                int grossGainSum = grossGainAscNum + grossGainDescNum;
                int profitGainSum = profitGainAscNum + profitGainDescNum;

                String grossGainPercent = Utils.divideReturnPercentString(grossGainAscNum, grossGainSum);
                String profitGainPercent = Utils.divideReturnPercentString(profitGainAscNum, profitGainSum);
                lineMap.put("收Up" + reportDay, grossGainPercent);
                lineMap.put("利Up" + reportDay, profitGainPercent);
            }
            retJsonList.add(lineMap);
        });
        return retJsonList;
    }

    private static String changeReportDayToChinese(String reportDay) {
        reportDay = reportDay.substring(2);
        if (reportDay.contains("0401")) {
            reportDay = reportDay.replace("0401", "04");
        } else if (reportDay.contains("0901")) {
            reportDay = reportDay.replace("0901", "07");
        } else if (reportDay.contains("0701")) {
            reportDay = reportDay.replace("0701", "10");
        } else {
            reportDay = reportDay.replace("1231", "12");
        }
        return reportDay;
    }

    private static void updateStockHolderNum() {

    }


    public void updateFinancialReportSum() {
        List<BdIndicatorVO> indicators = bdIndicatorDao.findAll();
        indicators.forEach(vo -> {
            String voStockIds = vo.getStockIds();
            if (!StringUtils.hasLength(voStockIds)) {
                return;
            }
            String[] stockIds = voStockIds.split(",");
            Map<String, BdFinancialSumVO> indicatorReportDayMap = new HashMap<>();
            for (String stockId : stockIds) {
                List<BdFinancialVO> financialVOList = bdFinacialDao.findByStockId(stockId);
                financialVOList.forEach(financialVo -> {
                    BdFinancialSumVO finSumVO = indicatorReportDayMap.get(financialVo.getReportDay());
                    if (finSumVO == null) {
                        finSumVO = BdFinancialSumVO.getInitVO();
                        indicatorReportDayMap.put(financialVo.getReportDay(), finSumVO);
                        finSumVO.setReportDay(financialVo.getReportDay());
                        finSumVO.setIndicatorId(vo.getStockId());
                    }
                    if (financialVo.getGrossProfitGain() != null && financialVo.getGrossProfitGain() > 0) {
                        finSumVO.setGrossGainAscNum(finSumVO.getGrossGainAscNum() + 1);
                        finSumVO.setGrossGainAscIds(finSumVO.getGrossGainAscIds() + "," + stockId);
                    } else {
                        finSumVO.setGrossGainDescNum(finSumVO.getGrossGainDescNum() + 1);
                        finSumVO.setGrossGainDescIds(finSumVO.getGrossGainDescIds() + "," + stockId);
                    }
                    if (financialVo.getGrossIncomeGain() != null && financialVo.getGrossIncomeGain() > 0) {
                        finSumVO.setProfitGainAscNum(finSumVO.getProfitGainAscNum() + 1);
                        finSumVO.setProfitGainAscIds(finSumVO.getProfitGainAscIds() + "," + stockId);
                    } else {
                        finSumVO.setProfitGainDescNum(finSumVO.getProfitGainDescNum() + 1);
                        finSumVO.setProfitGainDescIds(finSumVO.getProfitGainDescIds() + "," + stockId);
                    }
                    finSumVO.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
                    bdFinancialSumDao.save(finSumVO);
                });
            }
        });
    }

    public Object updateStockfinancialType() {
        List<String> stockIds = stockDao.findStockIds();
        if (Utils.isWinSystem()) {
            stockIds = new ArrayList<>();
            stockIds.add("sh600487");
            stockIds.add("sh600522");
            stockIds.add("sh603083");
            stockIds.add("sh600498");
            stockIds.add("sz000063");
        }
        stockIds.forEach(stockId -> {

            BdFinancialVO lastByStockId = bdFinacialDao.findLastByStockId(stockId);
            if (lastByStockId == null || lastByStockId.getGrossProfit() == null || lastByStockId.getGrossIncome() == null) {
                logger.info("========updateStockfinancialType=====data error=====stockVo={}", lastByStockId);
                return;
            }
            // set stock profit type
            String grossProfit = lastByStockId.getGrossProfit();

            boolean profitNegative = grossProfit.contains("-");
            StockNameVO vo = stockDao.findById(stockId).get();
            if (!profitNegative && lastByStockId.getGrossProfitGain() > 0) {
                vo.setFinancialType(PROFIT_TYPE_400);
            } else if (!profitNegative && lastByStockId.getGrossProfitGain() < 0) {
                vo.setFinancialType(PROFIT_TYPE_300);
            } else if (profitNegative && lastByStockId.getGrossProfitGain() > 0) {
                vo.setFinancialType(PROFIT_TYPE_200);
            } else if (profitNegative && lastByStockId.getGrossProfitGain() < 0) {
                vo.setFinancialType(PROFIT_TYPE_100);
            }
            vo.setGrossProfitGain(lastByStockId.getGrossProfitGain());
            stockDao.save(vo);
        });
        return "update successfully";
    }
}
