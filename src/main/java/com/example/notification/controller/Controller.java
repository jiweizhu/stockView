package com.example.notification.controller;

import com.example.notification.constant.Constants;
import com.example.notification.repository.BdIndicatorDao;
import com.example.notification.repository.StockDao;
import com.example.notification.service.ETFViewService;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.service.KLineService;
import com.example.notification.util.EmailUtil;
import com.example.notification.util.Utils;
import com.example.notification.vo.BdIndicatorVO;
import com.example.notification.vo.StockNameVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

import static com.example.notification.constant.Constants.*;

@RestController
public class Controller {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);
    @Value("${notification.init.history.price.day}")
    private Integer initHistoryPriceDay;


    @Value("${notification.stock.folder}")
    private String stockFolder;

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @Autowired
    private KLineService kLineService;
    @Autowired
    private ETFViewService etfViewService;

    @Autowired
    private BdIndicatorDao bdIndicatorDao;

    @Autowired
    private StockDao stockDao;


    @RequestMapping(value = {"/listStockFiles"})
    @ResponseBody
    public String listStockFiles() throws Exception {
        return kLineService.listStockFiles(stockFolder);
    }

    //370400#医药商业 for debug locally
    @Getter
    private static String targetFile = "bd_730200";

    public static void setTargetFileSize(String targetFileSize) {
        Controller.targetFileSize = targetFileSize;
    }

    @Getter
    private static String targetFileSize;

    public static void setTargetFile(String targetFile) {
        Controller.targetFile = targetFile;
    }

    @RequestMapping(value = {"/rangeSort/{range}"})
    @ResponseBody
    public ModelAndView rangeSort(@PathVariable String range) {
        logger.info("Enter method rangeSort========range={}", range);
        setRangeSortDay(range);
        setTargetFile(CONSTANT_RANGE_SORT);
        return new ModelAndView("redirect:/bdIndicatorsKlineView.html");
    }

    @RequestMapping(value = {"/listTargetFileStocks/{target}"})
    public ModelAndView listTargetFileStocks(@PathVariable String target) throws Exception {
        if (Utils.isWinSystem() && target == null) {
            target = "bd_730200";
        }
        if (target.contains(CONSTANT_BD) || target.contains(CONSTANT_ETF)) {
            // return bd indicator or ETF stocks
            targetFile = target;
        } else {
            targetFile = stockFolder + "/" + target;
        }
        logger.info("========= listTargetFileStocks ===========" + targetFile);
        setTargetFile(targetFile);
        return new ModelAndView("redirect:/stocksDayView.html");
    }

    @RequestMapping(value = {"/getPageTitle"})
    public String getPageTitle() {

        String targetFileValue = getTargetFile();
        if (targetFileValue == null) {
            return "No Title";
        } else if (targetFileValue.startsWith(CONSTANT_BD)) {
            BdIndicatorVO bdIndicatorVO = bdIndicatorDao.findById(targetFileValue.substring(3)).get();
            int length = bdIndicatorVO.getStockIds().split(",").length;
            String stockName = bdIndicatorVO.getStockName();
            return length + "|" + stockName + "_" + bdIndicatorVO.getStockId();
        } else if (targetFileValue.startsWith(CONSTANT_ETF)) {
            StockNameVO stockNameVO = stockDao.findById(targetFileValue.substring(4)).get();
            int length = stockNameVO.getStockIds().split(",").length;
            String stockName = stockNameVO.getStockName();
            return length + "|" + stockName + "_" + stockNameVO.getStockId();
        } else if (targetFileValue.startsWith(CONSTANT_RANGE_SORT)) {
            return "Range Sort";
        } else {
            StringBuilder stringBuilder = new StringBuilder("(").append(getTargetFileSize()).append(")");
            int lastIndexOf = targetFileValue.lastIndexOf("/");
            stringBuilder.append(targetFileValue.substring(lastIndexOf, targetFileValue.length() - 1).replace(".xls|.txt", ""));
            return stringBuilder.toString();
        }
    }


    @RequestMapping(value = {"/real", "/index"})
    @ResponseBody
    public String realTimeQuery() throws Exception {
        kLineService.realTimeQuery();
        return "ok";
    }

    @RequestMapping(value = {"/range/{size}"})
    @ResponseBody
    public String updateGraphRange(@PathVariable String size) {
        Constants.rangeSize = Integer.parseInt(size);
        return "ok";
    }

    @RequestMapping(value = {"/stock/list"})
    @ResponseBody
    public ResponseEntity listAllETFs() throws JsonProcessingException {
        logger.info("Enter method listAllETFs=========");
        Object body = kLineMarketClosedService.listEtfs();
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/delete_HistoryData"})
    @ResponseBody
    public ResponseEntity deleteHistoryData() {
        Object body = kLineMarketClosedService.deleteDayHistoryData();
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/etfsCurveView"})
    @ResponseBody
    public ResponseEntity etfsCurveView() {
        logger.info("Enter method etfsCurveView=========");
        Object body = kLineMarketClosedService.etfsCurveView();
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/stock/{stockId}"})
    @ResponseBody
    public ResponseEntity stockDataById(@PathVariable String stockId) throws JsonProcessingException, ParseException {
        Object body = kLineMarketClosedService.stockJsonData(stockId);
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/stock/weekly/{stockId}"})
    @ResponseBody
    public ResponseEntity stockWeeklyDataById(@PathVariable String stockId) {
        Object body = kLineMarketClosedService.stockWeeklyJsonData(stockId);
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/multiK/{stockId}"})
    @ResponseBody
    public ResponseEntity multiK(@PathVariable String stockId) {
        logger.info("Enter method multiK====stockId====" + stockId);
        Object body = kLineMarketClosedService.multiK(stockId);
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/etfs/{num}"})
    @ResponseBody
    public ResponseEntity findAllEtfSort(@PathVariable String num) {
        logger.info("Enter method findAllEtfSort====" + num);
        Object body = "";
        if (num.equals("3") || num.equals("4")) {
            body = etfViewService.findAllEtfSortView_new(num);
        } else if (num.contains("targetList")) {
            //get target file xls to list stocks
            // and bd_indicator/etf belong stocks
            body = etfViewService.findAllEtfSortView_new(num);
        } else if (num.contains("300mainBoard")) {
            body = etfViewService.findAllEtfSortView_new(num);
        } else if (num.contains("wholeEtfsView")) {
            body = etfViewService.findAllEtfSortView_new(num);
        } else if (num.contains("main")) {
            body = etfViewService.findAllEtfSortView_new(num);
        } else {
            body = etfViewService.findAllEtfSortView(num);
        }
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/etfs/table1"})
    @ResponseBody
    public ResponseEntity findAllEtfsForTable_1() {
        logger.info("Enter method findAllEtfsForTable_1====");
        Object body = etfViewService.findAllEtfsForTable(1);
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/etfs/table2"})
    @ResponseBody
    public ResponseEntity findAllEtfsForTable_2() {
        logger.info("Enter method findAllEtfsForTable_2====");
        Object body = etfViewService.findAllEtfsForTable(2);
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/etfs/five/noflip"})
    @ResponseBody
    public ResponseEntity fiveDayAdjusted() {
        Object body = etfViewService.fiveDayAdjustedView();
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/etfs/ten"})
    @ResponseBody
    public ResponseEntity tenDayAdjusted() {
        Object body = etfViewService.tenDayAdjustedView();
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/import"})
    @ResponseBody
    public String importETF() throws JsonProcessingException {
        logger.info("Enter method importETF====");
        String result = kLineMarketClosedService.importStocks();
        return result;
    }

    @RequestMapping(value = {"/manuallyUpdate"})
    @ResponseBody
    public String manuallyUpdate() {
        logger.info("Enter method manuallyUpdate ====");
        String result = kLineMarketClosedService.manuallyUpdateStock();
        return result;
    }

    @RequestMapping(value = {"/generateReportEveryDay"})
    @ResponseBody
    public String generateReportEveryDay() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException, JsonProcessingException, InterruptedException {
        logger.info("Enter method generateReportEveryDay====");
        Object body = kLineMarketClosedService.deleteDayHistoryData();
        kLineMarketClosedService.getWeekHistoryPriceAndStoreInDb(2);
        etfViewService.generateReportEveryDay();
        return "ok";
    }


    @RequestMapping(value = {"/getWeekHistoryPriceAndStoreInDb"})
    @ResponseBody
    public String getWeekHistoryPriceAndStoreInDb() {
        logger.info("Enter method getWeekHistoryPriceAndStoreInDb====");
        kLineMarketClosedService.getWeekHistoryPriceAndStoreInDb(initHistoryPriceDay);
        return "ok";
    }


    @RequestMapping(value = {"/storeHistoryData"})
    @ResponseBody
    public String storeHistoryData() {
        logger.info("Enter method storeHistoryData====");
        String historyPriceOnLineAndStoreInDb = kLineMarketClosedService.getHistoryPriceOnLineAndStoreInDb(initHistoryPriceDay);
        return historyPriceOnLineAndStoreInDb;
    }

    @RequestMapping(value = {"/mail"})
    @ResponseBody
    public String sendEmailTest() throws Exception {
        StockNameVO stockNameVO = new StockNameVO();
        stockNameVO.setStockId("testId");
        stockNameVO.setStockName("testStockName");
        EmailUtil.sendMailSingle(stockNameVO);
        return "ok";
    }

    @RequestMapping(value = {"/handleEtfsAvg"})
    @ResponseBody
    public ResponseEntity handleEtfsAvg() throws Exception {
        Object body = kLineMarketClosedService.handleStocksFlipDaysAndGainReport();
        return ResponseEntity.ofNullable(body);
    }

}
