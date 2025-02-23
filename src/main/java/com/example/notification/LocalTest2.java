package com.example.notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class LocalTest2 {
    static class DailyPrice {
        String date;
        double open, high, low, close;

        public DailyPrice(String date, double open, double high, double low, double close) {
            this.date = date;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }

        public String getDate() {
            return date;
        }
    }

    public static List<DailyPrice> fetchDailyPricesFromDB() throws SQLException, ClassNotFoundException {
        List<DailyPrice> data = new ArrayList<>();
        Class.forName("com.mysql.cj.jdbc.Driver");
        String url = "jdbc:mysql://localhost:3306/stock?useSSL=false&serverTimezone=UTC";
        String username = "root";
        String password = "123456";
        String sql = "SELECT day, opening_price, intraday_high, intraday_low, closing_price " +
                "FROM daily_price " +
                "WHERE stock_id ='sh600009' " +
                "ORDER BY day desc " +
                "LIMIT 236";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                String date = rs.getString("day");
                double open = rs.getDouble("opening_price");
                double high = rs.getDouble("intraday_high");
                double low = rs.getDouble("intraday_low");
                double close = rs.getDouble("closing_price");
                data.add(new DailyPrice(date, open, high, low, close));
            }
        }
        return data.stream().sorted(Comparator.comparing(DailyPrice::getDate)).toList();
    }

    public static double simulateGridStrategy(List<DailyPrice> data, double gridPercent, double capital) {
        if (data.isEmpty()) return capital;

        double basePrice = data.get(0).close; // 设定基准价格
        double cash = capital;
        int asset = 0;
        int buyCount = 0, sellCount = 0;
        List<String> buyDates = new ArrayList<>(), sellDates = new ArrayList<>();
        double totalCost = 0;

        double transactionFeeRate = 0.00025; // 交易手续费 0.025%
        double stampDutyRate = 0.001; // 印花税 0.1%（仅卖出时收取）

        for (DailyPrice dp : data) {
            // 计算买入和卖出价格
            double buyThreshold = basePrice * (1 - gridPercent);
            double sellThreshold = basePrice * (1 + gridPercent);

            if (dp.low <= buyThreshold && cash >= buyThreshold) {
                double cost = buyThreshold * (1 + transactionFeeRate);
                cash -= cost;
                asset++;
                buyCount++;
                buyDates.add(String.format("%s(%.2f)", dp.date, buyThreshold));
                totalCost += buyThreshold * transactionFeeRate;
            }

            if (dp.high >= sellThreshold && asset > 0) {
                double revenue = sellThreshold * (1 - transactionFeeRate - stampDutyRate);
                cash += revenue;
                asset--;
                sellCount++;
                sellDates.add(String.format("%s(%.2f)", dp.date, sellThreshold));
                totalCost += sellThreshold * (transactionFeeRate + stampDutyRate);
            }
        }

        double finalValue = cash + asset * data.get(data.size() - 1).close;
        System.out.printf("网格间距=%.2f%%, 最终市值=%.2f, 收益=%.2f, 交易成本=%.2f, 买入次数=%d, 卖出次数=%d, 买入日期=%s, 卖出日期=%s\n",
                gridPercent * 100, finalValue, finalValue - capital, totalCost, buyCount, sellCount, buyDates, sellDates);
        return finalValue;
    }

    public static void main(String[] args) {
        try {
            List<DailyPrice> data = fetchDailyPricesFromDB();
            double initialCapital = 100000;
            double bestProfit = -Double.MAX_VALUE;
            double bestPercent = 0;
            double bestFinalValue = 0;

            double minGridPercent = 0.001; // 0.1%
            double maxGridPercent = 0.20; // 20%
            double stepGridPercent = 0.001; // 0.1%

            for (double percent = minGridPercent; percent <= maxGridPercent; percent += stepGridPercent) {
                double finalValue = simulateGridStrategy(data, percent, initialCapital);
                double profit = finalValue - initialCapital;
                if (profit > bestProfit) {
                    bestProfit = profit;
                    bestPercent = percent;
                    bestFinalValue = finalValue;
                }
            }

            System.out.printf("最优策略: 网格间距=%.2f%%, 最终市值=%.2f, 总收益=%.2f\n",
                    bestPercent * 100, bestFinalValue, bestProfit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}