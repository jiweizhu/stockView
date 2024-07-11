package com.example.notification.controller;

import com.example.notification.repository.HoldingStockDao;
import com.example.notification.repository.StockDao;
import com.example.notification.requestVO.HoldingStockRequestVO;
import com.example.notification.service.HoldingService;
import com.example.notification.service.IntraDayService;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.vo.HoldingStockVO;
import com.example.notification.vo.StockNameVO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
public class HoldingController {
    private static final Logger logger = LoggerFactory.getLogger(HoldingController.class);

    @Autowired
    private HoldingService holdingService;

    @Autowired
    private HoldingStockDao holdingStockDao;

    @Autowired
    private StockDao stockDao;

    @Autowired
    private IntraDayService intraDayService;

    @Autowired
    private KLineMarketClosedService kLineMarketClosedService;


    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy");
        dateFormat.setLenient(false);

        binder.registerCustomEditor(java.sql.Date.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) {
                try {
                    java.util.Date parsedDate = dateFormat.parse(text);
                    setValue(new java.sql.Date(parsedDate.getTime()));
                } catch (ParseException e) {
                    setValue(null);
                }
            }
        });
    }


    @RequestMapping(value = {"/holdingStock/save"}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String saveNewStock(@ModelAttribute HoldingStockRequestVO stockVO) throws JsonProcessingException, ParseException {
        List<String> list = new ArrayList<>();
        list.add(stockVO.getStockId());
        String ret = kLineMarketClosedService.addNewInTable(list);
        if (ret.contains("check")) {
            return ret;
        }
        HoldingStockVO holdingStockVO = new HoldingStockVO();
        holdingStockVO.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
        BeanUtils.copyProperties(stockVO, holdingStockVO);
        String stockName = holdingService.getStockIdOrNameByMap(holdingStockVO.getStockId());
        holdingStockVO.setStockName(stockName);
        holdingService.save(holdingStockVO);
        intraDayService.getPriceByminute();
        return "ok";
    }

    @RequestMapping(value = {"/holdingStock/update"}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String update(@ModelAttribute HoldingStockRequestVO requestVO) {
        Optional<HoldingStockVO> byId = holdingStockDao.findById(requestVO.getStockId());
        byId.ifPresent(vo -> {
            BeanUtils.copyProperties(requestVO, vo);
            vo.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
            holdingService.save(vo);
        });
        // save belongEtf to stock table, not holding_stock table.
        boolean hasLength = StringUtils.hasLength(requestVO.getBelongEtf());
        if(hasLength){
            StockNameVO stock = stockDao.findById(requestVO.getStockId()).get();
            stock.setBelongEtf(requestVO.getBelongEtf());
            stockDao.save(stock);
        }
        return "ok";
    }

    @RequestMapping(value = {"/holdingStock/etfList"})
    @ResponseBody
    public Object holdingStockEtfList() {
        Object o = holdingService.listEtfsForComboBox();
        return o;
    }

    @RequestMapping(value = {"/holdingStock/delete"})
    @ResponseBody
    public Object delete(@ModelAttribute HoldingStockRequestVO stockVO) {
        holdingService.delete(stockVO.getId());
        return "ok";
    }


    @RequestMapping(value = {"/holdingStock/stockList"})
    @ResponseBody
    public Object holdingStockList() {
        Object o = holdingService.stockList();
        return o;
    }


    @RequestMapping(value = {"/holdingStock/datagridList"})
    @ResponseBody
    public Object datagridList() {
        Object list = holdingService.datagridList();
        return list;
    }


    @RequestMapping(value = {"/getPriceByminute"})
    @ResponseBody
    public Object getPriceByminute() throws ParseException {
        logger.info("======= Enter HoldingController getPriceByminute =========");
        Object list = intraDayService.getPriceByminute();
        return list;
    }

}
