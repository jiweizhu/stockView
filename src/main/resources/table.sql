SELECT table_schema AS 'Database', SUM(data_length+index_length)/ 1024 / 1024 AS 'Size (MB)' FROM information_schema.tables GROUP BY table_schema;

Create schema stock;
CREATE TABLE  imported_stock(
stock_id VARCHAR(12) primary key,
stock_name VARCHAR(20)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  holding_stock(
stock_id VARCHAR(12) primary key,
stock_name VARCHAR(20),
costPrice DECIMAL(10,3),
now_price DECIMAL(10,3),
buy_in_lot INT,
gain_percent DECIMAL(10,2),
today_gain DECIMAL(10,2),
one_day_gain DECIMAL(10,2),
last_close_price DECIMAL(10,3),

-- belong_etf VARCHAR(12),
buy_day DATE,
last_updated_time time
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE intraday_price(
stock_id VARCHAR(12),
stock_name VARCHAR(20),
day DATE,
minute VARCHAR(10),
price DECIMAL(10,3),

PRIMARY KEY (stock_id, day, minute)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE range_gain(
stock_id VARCHAR(12),
start_day DATE,
end_day DATE,
market_days_num INT,
gain_percent_five DECIMAL(10,3),
gain_percent DECIMAL(10,3)

)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE week_price (
stock_id VARCHAR(12) NOT NULL,
day DATE NOT NULL,
opening_price DECIMAL(10,3),
closing_price DECIMAL(10,3),
week_high DECIMAL(10,3),
week_low DECIMAL(10,3),
PRIMARY KEY(stock_id, day)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE daily_price (
stock_id VARCHAR(12) NOT NULL,
day DATE NOT NULL,
opening_price DECIMAL(10,3),
closing_price DECIMAL(10,3),
intraday_high DECIMAL(10,3),
intraday_low DECIMAL(10,3),
day_avg_five DECIMAL(10,3),
day_avg_ten DECIMAL(10,3),
day_gain_of_five DECIMAL(10,2),
day_gain_of_ten DECIMAL(10,2),
PRIMARY KEY(stock_id, day)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  stock(
stock_id VARCHAR(12) primary key,
stock_name VARCHAR(20),
upward_days_five INT,
gain_percent_five DECIMAL(10,2),
flip_upward_days_five INT,
flip_gain_percent_five DECIMAL(10,2),
flip_day_five DATE,
flip_end_day_five DATE,

upward_days_ten INT,
gain_percent_ten DECIMAL(10,2),
flip_upward_days_ten INT,
flip_gain_percent_ten DECIMAL(10,2),
flip_day_ten DATE,
flip_end_day_ten DATE,
last_updated_time time,

stock_ids VARCHAR(200),
belong_etf VARCHAR(12),

customer_range VARCHAR(50), -- 20240601_20240615_20240620
customer_range_gain_pre DECIMAL(10,2),
customer_range_gain_post DECIMAL(10,2)

)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  bd_indicator(
indicator_id VARCHAR(12) primary key,
indicator_name VARCHAR(20),
upward_days_five INT,
gain_percent_five DECIMAL(10,2),
flip_upward_days_five INT,
flip_gain_percent_five DECIMAL(10,2),
flip_day_five DATE,
flip_end_day_five DATE,

upward_days_ten INT,
gain_percent_ten DECIMAL(10,2),
flip_upward_days_ten INT,
flip_gain_percent_ten DECIMAL(10,2),
flip_day_ten DATE,
flip_end_day_ten DATE,

stock_ids VARCHAR(1000),
last_updated_time time

)ENGINE=InnoDB DEFAULT CHARSET=utf8;
