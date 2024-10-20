package com.example.notification.cnIndex.controller;

import com.example.notification.cnIndex.service.CNService;
import com.example.notification.constant.Constants;
import com.example.notification.repository.CNIndicatorDao;
import com.example.notification.service.ETFViewService;
import com.example.notification.vo.CNIndicatorVO;
import com.example.notification.vo.StockNameVO;
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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
public class CNController {
    private static final Logger logger = LoggerFactory.getLogger(CNController.class);

    @Autowired
    private CNService cnService;

    @Autowired
    private CNIndicatorDao cnIndicatorDao;

    @Autowired
    private ETFViewService etfViewService;

    @RequestMapping(value = {"/cn/indicatorsView"})
    @ResponseBody
    public String indicatorsView() {
        List<CNIndicatorVO> list = cnIndicatorDao.findupwardDaysIndicator();
        list.addAll(cnIndicatorDao.findDownwardDaysIndicator());
        List<StockNameVO> industryEtfs = new ArrayList<>();
        list.forEach(vo -> {
            StockNameVO target = new StockNameVO();
            BeanUtils.copyProperties(vo, target);
            target.setStockId(vo.getIndexCode());
            target.setStockName(vo.getIndexNameCn());
            industryEtfs.add(target);
        });
        String html = etfViewService.dayLineStocksFlowView(industryEtfs, true);
        return html;
    }

    @Value("${notification.cn.indicators.file}")
    private String cn_import_file;

    @RequestMapping(value = {"/cn/import"})
    @ResponseBody
    public ResponseEntity<String> importFile() throws InvocationTargetException, NoSuchMethodException, IllegalAccessException {
        List<String> stringList = Constants.getImportFileList(cn_import_file);
        stringList.forEach(vo -> {
            String[] split = vo.split("_");
            cnIndicatorDao.save(new CNIndicatorVO(split[0], split[1]));
        });

        //init indicator daily price from 20140101
        cnService.initDailyPrice();
        cnService.calculateAvgPrice();
        return ResponseEntity.ok(Arrays.toString(stringList.toArray()));
    }

    @RequestMapping(value = {"/cn/index/{indexId}"})
    @ResponseBody
    public ResponseEntity<Object> queryIndex(@PathVariable String indexId) {
        if (indexId.startsWith("s")) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        Object body = cnService.queryIndicatorDailyData(indexId);
        return ResponseEntity.ofNullable(body);
    }


}
