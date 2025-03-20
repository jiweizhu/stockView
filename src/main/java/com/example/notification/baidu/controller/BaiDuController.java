package com.example.notification.baidu.controller;

import com.example.notification.baidu.respVo.IndicatorRespVO;
import com.example.notification.baidu.respVo.RangeSortRespVO;
import com.example.notification.baidu.service.BaiduInfoService;
import com.example.notification.baidu.vo.IndicatorVO;
import com.example.notification.constant.Constants;
import com.example.notification.repository.BdIndicatorDao;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.util.Utils;
import com.example.notification.vo.BdIndicatorVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RestController
public class BaiDuController {
    private static final Logger logger = LoggerFactory.getLogger(BaiDuController.class);

    @Autowired
    private BaiduInfoService baiduInfoService;

    @Autowired
    private BdIndicatorDao bdIndicatorDao;
    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;

    @RequestMapping(value = {"/bd/rangeSort/view"})
    @ResponseBody
    public Object rangeSortQuery() {
        logger.info("======Enter method rangeSortQuery========");
        List<RangeSortRespVO> retList = new ArrayList<>();
        baiduInfoService.rangeSortQuery().forEach(vo -> {
            RangeSortRespVO respVO = new RangeSortRespVO();
            BeanUtils.copyProperties(vo, respVO);
            String rangeId = respVO.getRangeId();
            rangeId = "<a href=http://" + Utils.getServerIp() + ":8888/rangeSort/" + rangeId + " >" + rangeId + "</a>";
            respVO.setRangeId(rangeId);
            retList.add(respVO);
        });
        return retList;
    }

    @RequestMapping(value = {"/bd/queryIndexDropRangeAll"})
    @ResponseBody
    public Object queryIndexDropRangeAll() {
        logger.info("======Enter method queryIndexDropRangeAll========");
        return baiduInfoService.queryIndexDropRangeAll();
    }

    @RequestMapping(value = {"/bd/DropRange/{stockId}"})
    @ResponseBody
    public Object queryIndexDropRangeByIndex(@PathVariable String stockId) {
        logger.info("======Enter method queryIndexDropRange========");
        return baiduInfoService.queryIndexDropRange(stockId);
    }


    @RequestMapping(value = {"/bd/financialList/{stockId}"})
    @ResponseBody
    public Object getBdFinancialList(@PathVariable String stockId) throws Exception {
        return baiduInfoService.readBdFinancialDataFromDbByStockId(stockId.split("_")[0]);
    }

    @RequestMapping(value = {"/bd/updateStockfinancialType"})
    @ResponseBody
    public Object updateStockfinancialType() throws Exception {
        return baiduInfoService.updateStockfinancialType();
    }

    @RequestMapping(value = {"/bd/financialSum"})
    @ResponseBody
    public Object financialSum() {
        return baiduInfoService.queryFinancialSum();
    }

    @RequestMapping(value = {"/bd/real"})
    @ResponseBody
    public Object getBaiduIndustriesRealInfo() throws Exception {
        List<IndicatorVO> list = baiduInfoService.queryBaiduIndustriesRealInfo();
        List<IndicatorRespVO> retList = new ArrayList<>();
        list.forEach(vo -> {
            IndicatorRespVO indicatorVO = new IndicatorRespVO();
            indicatorVO.setName(vo.getName());
            indicatorVO.setRaiseCount(vo.getRiseCount());
            indicatorVO.setFallCount(vo.getFallCount());
//            if (ratio.getStatus().equals("down")) {
//                dayGain = Double.parseDouble(ratio.getValue().replace("%", ""));
//            }
            indicatorVO.setZeroCount(vo.getMemberCount() - vo.getFallCount() - vo.getRiseCount());
            retList.add(indicatorVO);
        });
        return retList;
    }

    @RequestMapping(value = {"/bd/stock/{stockId}"})
    @ResponseBody
    public ResponseEntity stockDataById(@PathVariable String stockId) {
        Object body = baiduInfoService.getStockJsonDataDay(stockId);
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/bd/wk/stock/{stockId}"})
    @ResponseBody
    public ResponseEntity stockWeekDataById(@PathVariable String stockId) {
        Object body = baiduInfoService.getStockJsonDataWeek(stockId);
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/bd/indicatorsView/{isWeek}"})
    @ResponseBody
    public ResponseEntity indicatorsView(@PathVariable String isWeek) {
//        baiduInfoService.calculateIndicatorsAvg();
        Object body = baiduInfoService.indicatorsView(Boolean.parseBoolean(isWeek));
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/bd/indicatorStocksView"})
    @ResponseBody
    public ResponseEntity indicatorStocksView(@PathVariable String indicatorId) {
        logger.info("Enter method indicatorStocksView=========");
        Object body = baiduInfoService.indicatorStocksView(indicatorId);
        return ResponseEntity.ofNullable(body);
    }

    @Value("${notification.bd.indicators.file}")
    private String bd_import_file;

    @RequestMapping(value = {"/bd/import"})
    @ResponseBody
    public ResponseEntity<String> importFile() {
        List<String> stringList = Constants.getImportFileList(bd_import_file);
        stringList.forEach(vo -> {
            String[] split = vo.split("_");
            bdIndicatorDao.save(new BdIndicatorVO(split[0], split[1]));
        });
        return ResponseEntity.ok(Arrays.toString(stringList.toArray()));
    }

    @RequestMapping(value = {"/bd/init/getFromNetAndStore"})
    @ResponseBody
    public ResponseEntity getFromNetAndStore() {
        baiduInfoService.getFromNetAndStoreDay(250);
//        baiduInfoService.getFromNetAndStoreWeek(false);
        return ResponseEntity.ofNullable("finish getFromNetAndStore");
    }

    @RequestMapping(value = {"/bd/calculateRangeSort"})
    @ResponseBody
    public ResponseEntity calculateRangeSort() {
        baiduInfoService.calculateRangeSort();
        return ResponseEntity.ofNullable("finish calculateRangeSort");
    }


    @RequestMapping(value = {"/bd/updateFinancialReportSum"})
    @ResponseBody
    public ResponseEntity updateFinancialReportSum() {
        baiduInfoService.updateFinancialReportSum();
        return ResponseEntity.ofNullable("finish updateFinancialReportSum");
    }


    // manually update config data
    @RequestMapping(value = {"/bd/updateManually"})
    @ResponseBody
    public ResponseEntity updateIndicatorBelongStocks() {
        baiduInfoService.getFromNetAndStoreWeek(false);
        return ResponseEntity.ofNullable("finish updateIndicatorBelongStocks");
    }

    //test api
    @RequestMapping(value = {"/bd/test"})
    @ResponseBody
    public ResponseEntity test() throws Exception {
        baiduInfoService.calculateDropRange();
//        baiduInfoService.findStocksAmplitudeDuringIndicatorDown();
        return ResponseEntity.ok("SuccessFully done! ");
    }


}
