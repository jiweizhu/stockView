package com.example.notification.baidu.controller;

import com.example.notification.baidu.respVo.IndicatorRespVO;
import com.example.notification.baidu.service.BaiduInfoService;
import com.example.notification.baidu.vo.IndicatorVO;
import com.example.notification.constant.Constants;
import com.example.notification.repository.BdIndicatorDao;
import com.example.notification.vo.BdIndicatorVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            IndicatorVO.Ratio ratio = vo.getRatio();
            double dayGain = Double.parseDouble(ratio.getValue().replace("%", ""));
//            if (ratio.getStatus().equals("down")) {
//                dayGain = Double.parseDouble(ratio.getValue().replace("%", ""));
//            }
            indicatorVO.setDayGain(dayGain);
            indicatorVO.setZeroCount(vo.getMemberCount() - vo.getFallCount() - vo.getRiseCount());
            List<IndicatorVO.Stock> riseFirst = vo.getRise_first();
            StringBuilder stocks = new StringBuilder();
            riseFirst.forEach(stockVo -> {
                stocks.append(stockVo.getName()).append(",");
            });
            indicatorVO.setStocks(stocks.toString());
            retList.add(indicatorVO);
        });
        return retList;
    }

    @RequestMapping(value = {"/bd/stock/{stockId}"})
    @ResponseBody
    public ResponseEntity stockDataById(@PathVariable String stockId) {
        Object body = baiduInfoService.stockJsonData(stockId);
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/bd/wk/stock/{stockId}"})
    @ResponseBody
    public ResponseEntity stockWeekDataById(@PathVariable String stockId) {
        Object body = baiduInfoService.stockWeekJsonData(stockId);
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/bd/indicatorsView"})
    @ResponseBody
    public ResponseEntity indicatorsView() {
//        baiduInfoService.calculateIndicatorsAvg();
        Object body = baiduInfoService.indicatorsView();
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
        List<String> stringList = Constants.getBaiduIndictorImportList(bd_import_file);
        stringList.forEach(vo -> {
            String[] split = vo.split("_");
            bdIndicatorDao.save(new BdIndicatorVO(split[0],split[1]));
        });
        return ResponseEntity.ok(Arrays.toString(stringList.toArray()));
    }

    @RequestMapping(value = {"/bd/test"})
    @ResponseBody
    public ResponseEntity test() {
        baiduInfoService.calculateIndicatorsAvg();
        return ResponseEntity.ofNullable("finish calculateIndicatorsAvg");
    }


}
