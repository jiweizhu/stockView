#!/bin/bash

# 股票代码
STOCK="AAPL"

# 获取股票价格
get_stock_price() {
    curl -s "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=${STOCK}&interval=1min&apikey=YOUR_API_KEY" | jq -r '.["Time Series (1min)"] | to_entries | .[0].value["1. open"]'
}
curl -s "https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=AAPL&interval=1min&apikey=YOUR_API_KEY"
# 显示股票价格走势
while true; do
    price=$(get_stock_price)
    echo "$(date '+%Y-%m-%d %H:%M:%S') - $STOCK: $price"
    sleep 60  # 每分钟更新一次
done