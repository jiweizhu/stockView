package com.example.notification.easymoney;

import com.example.notification.easymoney.netVo.EmBandNetVO;
import com.example.notification.http.RestRequestFromDongCai;
import com.example.notification.repository.EmBandDailyDao;
import com.example.notification.repository.EmIndicatorDao;
import com.example.notification.util.Utils;
import com.example.notification.vo.EmBandDailyVO;
import com.example.notification.vo.EmIndicatorVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.util.*;

@Service
public class EasyMoneyService {
    private static final Logger logger = LoggerFactory.getLogger(EasyMoneyService.class);
    @Autowired
    private RestRequestFromDongCai restRequestFromDongCai;
    @Autowired
    private EmIndicatorDao emIndicatorDao;
    @Autowired
    private EmBandDailyDao emBandDailyDao;

    public void updateBandDailyDet() {
        List<String> ids = emIndicatorDao.findIds();
        if (Utils.isWinSystem()) {
            ids = new ArrayList<>();
            ids.add("016029");
            ids.add("016028");
        }
        logger.info("====Enter==EasyMoneyService updateBandDailyDet====ids size={}",ids.size());
        ids.forEach(id -> {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            List<EmBandNetVO> emBandDailyVOS = restRequestFromDongCai.queryValueIndustryDet(id);
            Set<String> existTradeDates = new HashSet<>();
            emBandDailyDao.findTradeDatesByBoardCode(id).forEach(tradeDate -> {
                existTradeDates.add(tradeDate.toString());
            });
            for (EmBandNetVO netVo : emBandDailyVOS) {
                if (existTradeDates.contains(netVo.getTradeDate().
                        toString())) {
                    continue;
                }
                EmBandDailyVO emBandDailyVO = new EmBandDailyVO();
                BeanUtils.copyProperties(netVo, emBandDailyVO);
                emBandDailyVO.setTradeDate(Date.valueOf(netVo.getTradeDate().split(" ")[0]));
                emBandDailyDao.save(emBandDailyVO);
            }
        });
    }

    public Object eMoneyNetView() {
        List<EmIndicatorVO> all = emIndicatorDao.findAll();
        return buildHtml(all);
    }

    private Object buildHtml(List<EmIndicatorVO> bandVOs) {
        logger.info("====Enter==EasyMoneyService buildHtml=====");
        String serverIp = Utils.getServerIp();
        StringBuilder tdHtml = new StringBuilder();
        for (int index = 0; index < bandVOs.size(); index++) {
            EmIndicatorVO vo = bandVOs.get(index);

            String stockId = vo.getBoardId();
            String id_name = stockId + "_" + vo.getBoardName();

            int columnSize = 8;
            int count = index % columnSize;
            if (count == 0) {
                tdHtml.append("<tr>");
            }

            tdHtml.append("<td><a href=\"http://").append(serverIp).append(":8888/listTargetFileStocks/").append("bd_").append(stockId).append("\">")
                    .append(stockId).append("</a>#").append("<a href=\"https://gushitong.baidu.com/block/ab-").append(stockId).append("\">");
            tdHtml.append("<b style=font-size:20px >").append(id_name.split("_")[1]);
            tdHtml.append("</b></a>(").append(vo.getUpwardDaysFive()).append("|").append(vo.getGainPercentFive() + ")");

            tdHtml.append("<div class=\"band-container\" ").append("id = \"").append("span_").append(id_name).append("\" ></div>").append("</td>");
            if (count == 7) {
                tdHtml.append("</tr>");
            }

        }
        return tdHtml.toString();
    }

    @Value("${notification.easymoney.band.range.count}")
    private String rangeCount;
    public Object getStockJsonDataDay(String stockId) {
        ArrayList<String[]> result = new ArrayList<>();
        logger.info("enter EasyMoneyService getStockJsonDataDay stockId =============" + stockId);
        if (stockId.contains("_")) {
            stockId = stockId.split("_")[0];
        }


        List<EmBandDailyVO> voList = emBandDailyDao.findByIndexStockIdOrderByDay(stockId, Integer.valueOf(rangeCount))
                .stream().sorted(Comparator.comparing(EmBandDailyVO::getTradeDate)).toList();
        //as baidu restrict to query
        // return db data
        for (EmBandDailyVO vo : voList) {
            String[] strings = new String[7];
            strings[0] = Utils.getFormat(vo.getTradeDate());
            String format = String.format("%.2f", vo.getPeTtm());
            strings[1] = format;
            strings[2] = format;
            strings[3] = format;
            strings[4] = format;
            result.add(strings);
        }
        return result;
    }
}
