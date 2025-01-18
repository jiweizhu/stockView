SELECT table_schema AS 'Database', SUM(data_length+index_length)/ 1024 / 1024 AS 'Size (MB)' FROM information_schema.tables GROUP BY table_schema;

Create schema stock;

CREATE TABLE bd_financial (
stock_id VARCHAR(12),
report_day VARCHAR(12),
content TEXT,
PRIMARY KEY(stock_id,report_day)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE range_sort_id (
range_id VARCHAR(20) primary key,
day_start DATE,
day_end DATE,
description VARCHAR(20)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
insert into range_sort_id (range_id, day_start, day_end, description) values ('aaa','20220701','20221028', '300 descreased 21%');
insert into range_sort_id (range_id, day_start, day_end, description) values ('bbb','20221031','20230131', '300 increased 17.38%');
insert into range_sort_id (range_id, day_start, day_end, description) values ('ccc','20230721','20240202', '300 decreased 16.5%');
insert into range_sort_id (range_id, day_start, day_end, description) values ('ddd','20240202','20240311', '300 increased 10.7%');

CREATE TABLE range_sort_gain (
range_id VARCHAR(12),
stock_id VARCHAR(12),
range_gain DECIMAL(10,2),
PRIMARY KEY(range_id,stock_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


CREATE TABLE  cn_indicator(
index_code VARCHAR(12) primary key,
index_name_cn VARCHAR(20),
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
stock_ids TEXT,
last_updated_time time
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE cn_daily_price (
index_code VARCHAR(12) NOT NULL,
trade_date VARCHAR(12) NOT NULL,
index_name_cn_all VARCHAR(50),
index_name_cn VARCHAR(50),
index_name_en_all VARCHAR(200),
index_name_en VARCHAR(50),
open_val DECIMAL(10,3),
high_val DECIMAL(10,3),
low_val DECIMAL(10,3),
close_val DECIMAL(10,3),
change_val DECIMAL(10,2),
change_pct DECIMAL(10,2),
trading_vol DECIMAL(10,2),
trading_value DECIMAL(10,2),
cons_number INT,
peg DECIMAL(10,2),
day_avg_five DECIMAL(10,3),
day_avg_ten DECIMAL(10,3),
day_gain_of_five DECIMAL(10,2),
day_gain_of_ten DECIMAL(10,2),
PRIMARY KEY(index_code, trade_date)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE bd_indicator_wk_price (
stock_id VARCHAR(12) NOT NULL,
day DATE NOT NULL,
opening_price DECIMAL(10,3),
closing_price DECIMAL(10,3),
intraday_high DECIMAL(10,3),
intraday_low DECIMAL(10,3),
day_avg_five DECIMAL(10,3),
day_avg_ten DECIMAL(10,3),
day_avg_twenty DECIMAL(10,3),
day_gain_of_five DECIMAL(10,2),
day_gain_of_ten DECIMAL(10,2),
day_gain_of_twenty DECIMAL(10,2),
PRIMARY KEY(stock_id, day)
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
stock_ids TEXT,
last_updated_time time
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE bd_daily_price (
stock_id VARCHAR(12) NOT NULL,
day DATE NOT NULL,
opening_price DECIMAL(10,3),
closing_price DECIMAL(10,3),
intraday_high DECIMAL(10,3),
intraday_low DECIMAL(10,3),
day_avg_five DECIMAL(10,3),
day_avg_ten DECIMAL(10,3),
day_avg_twenty DECIMAL(10,3),
day_gain_of_five DECIMAL(10,2),
day_gain_of_ten DECIMAL(10,2),
day_gain_of_twenty DECIMAL(10,2),
PRIMARY KEY(stock_id, day)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;


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

-- this to store data from tencent
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
capital_type INT,
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
belong_etf VARCHAR(12),
stock_ids TEXT,

customer_range VARCHAR(50), -- 20240601_20240615_20240620
customer_range_gain_pre DECIMAL(10,2),
customer_range_gain_post DECIMAL(10,2)

)ENGINE=InnoDB DEFAULT CHARSET=utf8;
alter table stock add column favorite INT;

SELECT
    TABLE_SCHEMA AS '数据库名',
    TABLE_NAME AS '表名',
    ROUND(DATA_LENGTH / 1024 / 1024, 2) AS '数据大小(MB)',
    ROUND(INDEX_LENGTH / 1024 / 1024, 2) AS '索引大小(MB)',
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) AS '总大小(MB)'
FROM
    information_schema.TABLES where TABLE_SCHEMA = 'stock'
ORDER BY
    (DATA_LENGTH + INDEX_LENGTH) DESC;
