package com.example.notification;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class LocalTest {

    // 每日价格数据类，注意：日期字段对应数据库中的 day
    static class DailyPrice {
        java.sql.Date tradeDate;  // 数据库字段 day
        double open;
        double high;
        double low;
        double close;

        public DailyPrice(java.sql.Date tradeDate, double open, double high, double low, double close) {
            this.tradeDate = tradeDate;
            this.open = open;
            this.high = high;
            this.low = low;
            this.close = close;
        }

        @Override
        public String toString() {
            return "DailyPrice{" +
                    "tradeDate=" + tradeDate +
                    ", open=" + open +
                    ", high=" + high +
                    ", low=" + low +
                    ", close=" + close +
                    '}';
        }
    }

    // 交易记录类：记录每笔买卖的类型、日期及成交价格
    static class Transaction {
        String type; // "BUY" 或 "SELL"
        java.sql.Date tradeDate;
        double price;

        public Transaction(String type, java.sql.Date tradeDate, double price) {
            this.type = type;
            this.tradeDate = tradeDate;
            this.price = price;
        }

        @Override
        public String toString() {
            return "Transaction{" +
                    "type='" + type + '\'' +
                    ", tradeDate=" + tradeDate +
                    ", price=" + price +
                    '}';
        }
    }

    // 回测结果类，包含最终市值和所有交易记录
    static class SimulationResult {
        double finalValue;
        List<Transaction> transactions;

        public SimulationResult(double finalValue, List<Transaction> transactions) {
            this.finalValue = finalValue;
            this.transactions = transactions;
        }
    }

    /**
     * 从本地 MySQL 数据库中查询每日价格数据
     * 注意：日期字段使用数据库中的字段名 day
     *
     * @return 按日期升序排列的每日价格数据列表
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public static List<DailyPrice> fetchDailyPricesFromDB() throws SQLException, ClassNotFoundException {
        List<DailyPrice> data = new ArrayList<>();
        // 加载 MySQL JDBC 驱动
        Class.forName("com.mysql.cj.jdbc.Driver");

        // 修改为你的数据库连接信息
        String url = "jdbc:mysql://localhost:3306/stock?useSSL=false&serverTimezone=UTC";
        String username = "root";
        String password = "123456";

        // SQL 查询：字段 day, opening_price, intraday_high, intraday_low, closing_price
        String sql = "SELECT day, opening_price, intraday_high, intraday_low, closing_price " +
                "FROM daily_price " +
                "WHERE stock_id ='sh601127' " +
                "ORDER BY day ASC " +
                "LIMIT 100";

        try (Connection conn = DriverManager.getConnection(url, username, password);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                java.sql.Date tradeDate = rs.getDate("day");
                double open = rs.getDouble("opening_price");
                double high = rs.getDouble("intraday_high");
                double low = rs.getDouble("intraday_low");
                double close = rs.getDouble("closing_price");
                data.add(new DailyPrice(tradeDate, open, high, low, close));
            }
        }

        return data;
    }

    /**
     * 模拟基于百分比的网格策略回测
     * 使用几何网格：以首日开盘价为基准（0 层），向上/向下每一层价格分别为
     * basePrice * (1 + gridPercent)^(n)   (n 为整数，可正、负)
     *
     * 日内价格走势假定为：开盘 -> 最低 -> 最高 -> 收盘
     * 当价格下破当前层下一低一层时（即：低于 basePrice * (1 + gridPercent)^(currentGrid - 1)），
     * 则按该价格买入 1 份资产；上破当前层下一高一层时，则按该价格卖出 1 份资产。
     *
     * @param data         每日价格数据列表（按时间顺序）
     * @param gridPercent  百分比参数（例如 0.01 表示 1%）
     * @param totalCapital 初始资金
     * @return 回测结果，包含最终市值和交易记录
     */
    public static SimulationResult simulateGridStrategyPercentage(List<DailyPrice> data, double gridPercent, double totalCapital) {
        if (data == null || data.isEmpty()) {
            throw new IllegalArgumentException("数据为空");
        }
        // 基准价：首日开盘价
        double basePrice = data.get(0).open;
        // 初始时以基准价所在层记为 0 层
        int currentGrid = 0;
        int asset = 0;        // 初始仓位为 0
        double cash = totalCapital;
        List<Transaction> transactions = new ArrayList<>();

        // 对每个交易日进行模拟
        for (DailyPrice dp : data) {
            // 模拟下跌过程：从开盘跌至最低价
            // 当前层下方一层的价格：
            double lowerGridPrice = basePrice * Math.pow(1 + gridPercent, currentGrid - 1);
            while (dp.low < lowerGridPrice) {
                // 检查资金是否充足
                if (cash >= lowerGridPrice) {
                    cash -= lowerGridPrice;
                    asset++;
                    currentGrid--; // 更新当前网格下移 1 层
                    transactions.add(new Transaction("BUY", dp.tradeDate, lowerGridPrice));
                    // 更新新的下方一层价格
                    lowerGridPrice = basePrice * Math.pow(1 + gridPercent, currentGrid - 1);
                } else {
                    break;
                }
            }

            // 模拟上行过程：从最低价回升到最高价
            double upperGridPrice = basePrice * Math.pow(1 + gridPercent, currentGrid + 1);
            while (dp.high > upperGridPrice) {
                if (asset > 0) {
                    cash += upperGridPrice;
                    asset--;
                    currentGrid++; // 更新当前网格上移 1 层
                    transactions.add(new Transaction("SELL", dp.tradeDate, upperGridPrice));
                    // 更新新的上方一层价格
                    upperGridPrice = basePrice * Math.pow(1 + gridPercent, currentGrid + 1);
                } else {
                    break;
                }
            }
        }

        // 回测结束时，按照最后一日收盘价估值未平仓资产
        double lastClose = data.get(data.size() - 1).close;
        double totalValue = cash + asset * lastClose;
        return new SimulationResult(totalValue, transactions);
    }

    public static void main(String[] args) {
        try {
            // 从数据库中获取历史数据
            List<DailyPrice> data = fetchDailyPricesFromDB();
            if (data.isEmpty()) {
                System.out.println("未查询到数据，请检查数据库表和查询条件。");
                return;
            }
            System.out.println("查询到 " + data.size() + " 条数据。");

            // 初始资金
            double totalCapital = 100000.0;
            // 基准价（首日开盘价）
            double basePrice = data.get(0).open;

            // 准备图表数据：横轴为绝对网格间距 = basePrice * gridPercent，纵轴记录收益和最终市值
            XYSeries profitSeries = new XYSeries("收益");
            XYSeries totalValueSeries = new XYSeries("最终市值");

            double bestValue = -Double.MAX_VALUE;
            double bestPercent = 0;
            SimulationResult bestResult = null;

            // 遍历候选百分比（1% 到 10%）
            for (double percent = 0.01; percent <= 0.10 + 1e-6; percent += 0.01) {
                SimulationResult result = simulateGridStrategyPercentage(data, percent, totalCapital);
                double finalValue = result.finalValue;
                double profit = finalValue - totalCapital;
                // 统计买卖次数及日期
                int buyCount = 0, sellCount = 0;
                StringBuilder buyDates = new StringBuilder();
                StringBuilder sellDates = new StringBuilder();
                for (Transaction tx : result.transactions) {
                    if ("BUY".equals(tx.type)) {
                        buyCount++;
                        buyDates.append(tx.tradeDate).append(" ");
                    } else if ("SELL".equals(tx.type)) {
                        sellCount++;
                        sellDates.append(tx.tradeDate).append(" ");
                    }
                }
                // 计算绝对网格间距（用于输出和图表横轴）：基准价 * percent
                double absoluteGridSpacing = basePrice * percent;
                // 输出当前候选的结果（格式如：网格间距=11.90, 最终市值=101564.64, 收益=1564.64, 买入次数=..., 卖出次数=..., 买入日期=[...], 卖出日期=[...]）
                System.out.printf("网格间距=%.2f, 最终市值=%.2f, 收益=%.2f, 买入次数=%d, 卖出次数=%d, 买入日期=[%s], 卖出日期=[%s]%n",
                        absoluteGridSpacing, finalValue, profit, buyCount, sellCount,
                        buyDates.toString().trim(), sellDates.toString().trim());

                // 添加数据到图表数据集中
                profitSeries.add(absoluteGridSpacing, profit);
                totalValueSeries.add(absoluteGridSpacing, finalValue);

                if (finalValue > bestValue) {
                    bestValue = finalValue;
                    bestPercent = percent;
                    bestResult = result;
                }
            }

            System.out.println("======================================");
            System.out.printf("最佳网格策略：百分比=%.2f%%, 对应绝对网格间距=%.2f, 最终市值=%.2f, 总收益=%.2f%n",
                    bestPercent * 100, basePrice * bestPercent, bestValue, bestValue - totalCapital);

            // 输出最佳策略下的买卖统计信息
            if (bestResult != null) {
                int bestBuyCount = 0, bestSellCount = 0;
                StringBuilder bestBuyDates = new StringBuilder();
                StringBuilder bestSellDates = new StringBuilder();
                for (Transaction tx : bestResult.transactions) {
                    if ("BUY".equals(tx.type)) {
                        bestBuyCount++;
                        bestBuyDates.append(tx.tradeDate).append(" ");
                    } else if ("SELL".equals(tx.type)) {
                        bestSellCount++;
                        bestSellDates.append(tx.tradeDate).append(" ");
                    }
                }
                System.out.println("最佳策略买入次数: " + bestBuyCount);
                System.out.println("最佳策略卖出次数: " + bestSellCount);
                System.out.println("最佳策略买入日期: " + bestBuyDates.toString().trim());
                System.out.println("最佳策略卖出日期: " + bestSellDates.toString().trim());
            }

            // 生成图表数据集
            XYSeriesCollection dataset = new XYSeriesCollection();
            dataset.addSeries(profitSeries);
            dataset.addSeries(totalValueSeries);

            // 利用 JFreeChart 生成折线图
            JFreeChart chart = ChartFactory.createXYLineChart(
                    "基于百分比网格策略回测结果",   // 图表标题
                    "绝对网格间距",             // x 轴标签
                    "收益 / 最终市值",         // y 轴标签
                    dataset,                   // 数据集
                    PlotOrientation.VERTICAL,
                    true,                      // 是否显示图例
                    true,                      // 是否生成提示工具
                    false                      // 是否生成 URL 链接
            );

            // 保存图表到 PNG 文件
            int width = 800;
            int height = 600;
            File chartFile = new File("grid_strategy_results.png");
            ChartUtils.saveChartAsPNG(chartFile, chart, width, height);
            System.out.println("图表已保存到 " + chartFile.getAbsolutePath());

        } catch (SQLException e) {
            System.err.println("数据库操作异常：" + e.getMessage());
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.err.println("找不到 MySQL JDBC 驱动，请检查依赖：" + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("发生异常：" + e.getMessage());
            e.printStackTrace();
        }
    }
}
