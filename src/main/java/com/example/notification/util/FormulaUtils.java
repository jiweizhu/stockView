package com.example.notification.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class FormulaUtils {

    public static BigDecimal calculateWavePercentile(List<BigDecimal> historicalValuations, BigDecimal todayNet) {
        if (historicalValuations == null || historicalValuations.isEmpty()) {
            // 如果历史数据为空，无法计算百分位
            return BigDecimal.ZERO;
        }
        // 1. 创建一个可修改的列表副本，并进行排序
        List<BigDecimal> sortedHistoricalValuations = new ArrayList<>(historicalValuations);
        Collections.sort(sortedHistoricalValuations); // 升序排序

        // 2. 统计有多少估值小于或等于 todayNet
        long countLessThanOrEqualToTodayNet = 0;
        for (BigDecimal valuation : sortedHistoricalValuations) {
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
        BigDecimal totalCount = new BigDecimal(sortedHistoricalValuations.size());
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
}
