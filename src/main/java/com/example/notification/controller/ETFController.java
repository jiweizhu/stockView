package com.example.notification.controller;

import com.example.notification.repository.StockDao;
import com.example.notification.requestVO.ETFRequestVO;
import com.example.notification.service.ETFViewService;
import com.example.notification.service.HoldingService;
import com.example.notification.service.IntraDayService;
import com.example.notification.service.KLineMarketClosedService;
import com.example.notification.vo.IntradayPriceVO;
import com.example.notification.vo.StockNameVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.beans.PropertyEditorSupport;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
public class ETFController {
    private static final Logger logger = LoggerFactory.getLogger(ETFController.class);

    @Autowired
    private HoldingService holdingService;

    @Autowired
    private StockDao stockDao;

    @Autowired
    private ETFViewService etfViewService;

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

    @RequestMapping(value = {"/etf/flow/{way}"})
    @ResponseBody
    public ResponseEntity findAllEtfFlow(@PathVariable String way) {
        logger.info("Enter method findAllEtfFlow====" + way);
        Object body = "";
        body = etfViewService.findAllEtfFlowView(way);
        return ResponseEntity.ofNullable(body);
    }


    @RequestMapping(value = {"/etf/flow/main/{way}"})
    @ResponseBody
    public ResponseEntity findMainEtfFlow(@PathVariable String way) {
        logger.info("Enter method findMainEtfFlow====" + way);
        Object body = "";
        body = etfViewService.findMainEtfFlow(way);
        return ResponseEntity.ofNullable(body);
    }

    @RequestMapping(value = {"/etf/belongStocks/{etfId}"})
    @ResponseBody
    public List<String> getStocksBelongEtf(@PathVariable String etfId) {
        return etfViewService.getStocksBelongEtf(etfId);
    }

    @RequestMapping(value = {"/etf/belongStockIds/{etfId}"})
    @ResponseBody
    public Object belongStockIds(@PathVariable String etfId) {
        return etfViewService.belongStockIds(etfId);
    }

    @RequestMapping(value = {"/etf/getIntradayPrice/{etfId}"})
    @ResponseBody
    public ResponseEntity getIntradayPrice(@PathVariable String etfId) {
        logger.info("Enter ETFController getIntradayPrice======" + etfId);
        Set<IntradayPriceVO> intradayPrice = etfViewService.getIntradayPrice(etfId);
        if (intradayPrice == null) {
            return ResponseEntity.ok(Collections.emptyList());
        }
        ArrayList<Double[]> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");
        DateTimeFormatter minuteSec = DateTimeFormatter.ofPattern("yyyyMMddHHmm");
        ZoneId zoneId = ZoneId.systemDefault();
        for (IntradayPriceVO vo : intradayPrice) {
            LocalDate localDate = vo.getDay().toLocalDate();
            String formattedDate = localDate.format(formatter);
            String timeStr = formattedDate + vo.getMinute();
            LocalDateTime localDateTime = LocalDateTime.parse(timeStr, minuteSec);
            ZonedDateTime zonedDateTime = localDateTime.atZone(zoneId);

            Instant instant = zonedDateTime.toInstant();
            long timestamp = instant.toEpochMilli();
            Double[] strings = new Double[2];
            strings[0] = Double.valueOf(Long.toString(timestamp));
            strings[1] = Double.valueOf(vo.getPrice().toString());
            result.add(strings);
        }
        return ResponseEntity.ofNullable(result);
    }


    @RequestMapping(value = {"/etf/update"}, consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    @ResponseBody
    public String update(@ModelAttribute ETFRequestVO requestVO) {
        Optional<StockNameVO> byId = stockDao.findById(requestVO.getEtfId());
        byId.ifPresent(storedEtf -> {
            String stockIds = requestVO.getStockIds();
            if (StringUtils.hasLength(stockIds)) {
                String[] split = stockIds.split(",");
                if (split.length > 0) {
                    Set<String> set = new HashSet<>();
                    for (String idOrName : split) {
                        if (!StringUtils.hasLength(idOrName) || "null".equals(idOrName)) {
                            continue;
                        }
                        if (!isValidStockCode(idOrName)) {
                            idOrName = holdingService.getStockIdOrNameByMap(idOrName);
                        }
                        set.add(idOrName);
                    }
                    StringBuilder sb = new StringBuilder();
                    set.forEach(line -> sb.append(line + ","));
                    stockIds = sb.toString();
                }
            }
            requestVO.setStockIds(stockIds);
            BeanUtils.copyProperties(requestVO, storedEtf);
            storedEtf.setLastUpdatedTime(new Timestamp(System.currentTimeMillis()));
            stockDao.save(storedEtf);
        });
        return "ok";
    }

    private static boolean isChinese(char c) {
        return c >= '\u4E00' && c <= '\u9FA5';
    }

    public static boolean isValidStockCode(String code) {
        if (code == null || code.isEmpty()) {
            return false;
        }
        // 定义正则表达式，匹配以 "sh" 或 "sz" 开头，且后面都是数字的字符串
        String regex = "^(sh|sz)\\d+$";
        // 使用正则表达式匹配字符串
        return code.matches(regex);
    }


}
