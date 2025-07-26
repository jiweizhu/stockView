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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
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

    @Async
    public void updateBandDailyDet() {
        List<String> ids = emIndicatorDao.findIds();
        if (Utils.isWinSystem()) {
            ids = new ArrayList<>();
            ids.add("016029");
            ids.add("016028");
            ids.add("016020");
        }
        logger.info("====Enter==EasyMoneyService updateBandDailyDet====ids size={}", ids.size());
        ids.forEach(id -> {
            List<EmBandNetVO> emBandDailyVOS = restRequestFromDongCai.queryValueIndustryDet(id);
            if (emBandDailyVOS == null || emBandDailyVOS.size() == 0) {
                logger.info("=Error==Need to chk! =EasyMoneyService updateBandDailyDet====no data for id={}", id);
                return;
            }
            logger.info("====Enter==EasyMoneyService updateBandDailyDet==id= {}====get from easyMoney size={}", id, emBandDailyVOS.size());
            Set<String> existTradeDates = new HashSet<>();
            emBandDailyDao.findTradeDatesByBoardCode(id).forEach(tradeDate -> {
                existTradeDates.add(tradeDate.toString());
            });
            addNewDet(emBandDailyVOS, existTradeDates, id);
        });
    }

    @Async
    private void addNewDet(List<EmBandNetVO> emBandDailyVOS, Set<String> existTradeDates, String id) {
        logger.info("Enter method addNewDet ===id={}", id);
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
        updateBandPercentile(id);
    }


    @Async
    public void updateBandPercentile(String id) {
        logger.info("Enter method updateBandPercentile ===id={}", id);
        //find all daily data and update percentile of ttm, pb, pe, ps, pe_ttm, pe_lar, pb_mrq
        List<EmBandDailyVO> list = emBandDailyDao.findAllByBoardCode(id);
        List<BigDecimal> ttmList = new ArrayList<>();
        list.forEach(netVo -> {
            //add all of list ttm
            ttmList.add(new BigDecimal(netVo.getPeTtm()));
        });

        BigDecimal ttmPercentile = calculatePercentile(ttmList, ttmList.get(ttmList.size() - 1));
        EmIndicatorVO emIndicatorVO = emIndicatorDao.findById(id).get();
        emIndicatorVO.setTtmPercentile(ttmPercentile.doubleValue());
        BigDecimal rangePercentile = calculateRangePercentile(ttmList, ttmList.get(ttmList.size() - 1));
        emIndicatorVO.setTtmRangePct(rangePercentile.doubleValue());
        emIndicatorDao.save(emIndicatorVO);
    }

    public static BigDecimal calculateRangePercentile(List<BigDecimal> historicalValuations, BigDecimal todayNet) {
        if (historicalValuations == null || historicalValuations.isEmpty()) {
            return BigDecimal.ZERO; // 历史数据为空，无法计算
        }

        // 1. 找出历史估值列表中的最大值和最小值
        Optional<BigDecimal> minValOpt = historicalValuations.stream()
                .min(BigDecimal::compareTo);
        Optional<BigDecimal> maxValOpt = historicalValuations.stream()
                .max(BigDecimal::compareTo);

        // 如果列表不为空，minValOpt 和 maxValOpt 应该总是存在
        if (!minValOpt.isPresent() || !maxValOpt.isPresent()) {
            return BigDecimal.ZERO; // 不应该发生，但作为安全检查
        }

        BigDecimal minVal = minValOpt.get();
        BigDecimal maxVal = maxValOpt.get();

        // 2. 处理特殊情况：最大值和最小值相同
        if (maxVal.compareTo(minVal) == 0) {
            // 如果所有历史估值都相同，或者列表只有一个值
            // 今天的估值如果在范围内，可以认为是0%（或者根据业务逻辑返回100%或其他）
            // 这里我们返回0%，因为没有“范围”可供计算相对位置
            return BigDecimal.ZERO;
        }

        // 3. 计算范围（最大值 - 最小值）
        BigDecimal range = maxVal.subtract(minVal);

        // 4. 计算 todayNet 相对于最小值的偏移量
        BigDecimal offset = todayNet.subtract(minVal);

        // 5. 计算百分位
        // 百分位 = (todayNet - minVal) / (maxVal - minVal) * 100
        BigDecimal percentile = offset.divide(range, 4, RoundingMode.HALF_UP) // 暂时保留更多小数位
                .multiply(new BigDecimal("100"));

        // 6. 确保百分位在0到100之间（处理 todayNet 超出历史范围的情况）
        if (percentile.compareTo(BigDecimal.ZERO) < 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        } else if (percentile.compareTo(new BigDecimal("100")) > 0) {
            return new BigDecimal("100.00").setScale(2, RoundingMode.HALF_UP);
        } else {
            return percentile.setScale(2, RoundingMode.HALF_UP);
        }
    }


    public static BigDecimal calculatePercentile(List<BigDecimal> historicalValuations, BigDecimal todayNet) {
        if (historicalValuations == null || historicalValuations.isEmpty()) {
            // 如果历史数据为空，无法计算百分位
            return BigDecimal.ZERO;
        }
        // 1. 创建一个可修改的列表副本，并进行排序
        Collections.sort(historicalValuations); // 升序排序

        // 2. 统计有多少估值小于或等于 todayNet
        long countLessThanOrEqualToTodayNet = 0;
        for (BigDecimal valuation : historicalValuations) {
            // 使用compareTo进行数值比较
            if (valuation.compareTo(todayNet) <= 0) {
                countLessThanOrEqualToTodayNet++;
            } else {
                // 因为列表已排序，一旦遇到大于 todayNet 的值，后续的值也必然大于 todayNet
                break;
            }
        }

        // 3. 计算百分位
        // 百分位 = (小于等于 todayNet 的数据点数量 / 总数据点数量) * 100
        // 使用 BigDecimal 进行精确计算，避免浮点数误差
        BigDecimal totalCount = new BigDecimal(historicalValuations.size());
        BigDecimal countLTE = new BigDecimal(countLessThanOrEqualToTodayNet);

        // 如果 totalCount 为 0，避免除以零
        if (totalCount.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal percentile = countLTE.divide(totalCount, 4, RoundingMode.HALF_UP) // 暂时保留更多小数位
                .multiply(new BigDecimal("100"));

        // 4. 返回保留两位小数的百分位
        return percentile.setScale(2, RoundingMode.HALF_UP);
    }

    public Object eMoneyNetView() {
        List<EmIndicatorVO> all = emIndicatorDao.findAllOrderByTtmPercentileAsc();
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
            tdHtml.append("</b></a>(").append("ttmPct=").append(vo.getTtmPercentile()).append("|").append("rangPct=").append(vo.getTtmRangePct()).append(")");

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
