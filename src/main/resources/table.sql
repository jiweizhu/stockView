SELECT table_schema AS 'Database', SUM(data_length+index_length)/ 1024 / 1024 AS 'Size (MB)' FROM information_schema.tables GROUP BY table_schema;
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


Create schema stock;
-- if indicator drop 10%, filter stable stock
---choose 5 day avg drop more than 10%
CREATE TABLE indicator_drop (
indicator_id VARCHAR(12),
day_start DATE,
day_end DATE,
last_updated_time TIMESTAMP ,
drop_percent DECIMAL(10,2),
stock_ids TEXT,
PRIMARY KEY(indicator_id,day_start)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE favorite (
stock_id VARCHAR(12),
indicator_id VARCHAR(12),
description VARCHAR(500),
PRIMARY KEY(stock_id)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
insert into favorite (stock_id, indicator_id) values ('sh600487','730200');

CREATE TABLE bd_indicator_financial_summary (
indicator_id VARCHAR(12),
report_day VARCHAR(12),
profit_gain_asc_num int,
profit_gain_desc_num int,
gross_gain_asc_num int,
gross_gain_desc_num int,
last_updated_time TIMESTAMP,
profit_gain_asc_ids TEXT,
profit_gain_desc_ids TEXT,
gross_gain_asc_ids TEXT,
gross_gain_desc_ids TEXT,
PRIMARY KEY(indicator_id,report_day)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE bd_financial (
stock_id VARCHAR(12),
report_day VARCHAR(12),
content TEXT,
gross_income VARCHAR(12),
gross_income_gain DECIMAL(10,2),
gross_profit VARCHAR(12),
gross_profit_gain DECIMAL(10,2),
last_updated_time TIMESTAMP ,
holder_num VARCHAR(12),
top_holders TEXT,
PRIMARY KEY(stock_id,report_day)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- this table is for 300 index range
CREATE TABLE range_sort_id (
range_id VARCHAR(20) primary key,
day_start DATE,
day_end DATE,
last_updated_time TIMESTAMP ,
description VARCHAR(20)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
insert into range_sort_id (range_id, day_start, day_end, description) values ('z1','20250319','20250321', 'latest tracing');
insert into range_sort_id (range_id, day_start, day_end, description) values ('aaa','20220701','20221028', '300 descreased 21%');
insert into range_sort_id (range_id, day_start, day_end, description) values ('bbb','20221031','20230131', '300 increased 17.38%');
insert into range_sort_id (range_id, day_start, day_end, description) values ('ccc','20230721','20240202', '300 decreased 16.5%');
insert into range_sort_id (range_id, day_start, day_end, description) values ('ddd','20240202','20240311', '300 increased 10.7%');
insert into range_sort_id (range_id, day_start, day_end, description) values ('eee','20240521','20240913', '300 decreased ');
insert into range_sort_id (range_id, day_start, day_end, description) values ('fff','20240913','20250221', '2025 开年涨势 ');
insert into range_sort_id (range_id, day_start, day_end, description) values ('ff1','20241112','20250110', '2025 开年涨势 ');
insert into range_sort_id (range_id, day_start, day_end, description) values ('ggg','20250113','20250319', '2025 3月 ');
insert into range_sort_id (range_id, day_start, day_end, description) values ('hhh','','20250113', '2025 March down');

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
last_updated_time TIMESTAMP
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
last_updated_time TIMESTAMP
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE  easy_indicator(
board_id VARCHAR(12) primary key,
board_name VARCHAR(20),
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
last_updated_time TIMESTAMP
)ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE easy_band_daily (
    BOARD_CODE VARCHAR(50) ,
    ORIGINALCODE VARCHAR(50) COMMENT '原始代码',
    TRADE_DATE DATE NOT NULL COMMENT '交易日期',
    PE_TTM DOUBLE COMMENT '市盈率TTM',
    PE_LAR DOUBLE COMMENT '市盈率LAR',
    PB_MRQ DOUBLE COMMENT '市净率MRQ',
    PCF_OCF_TTM DOUBLE COMMENT '市现率OCF_TTM',
    PS_TTM DOUBLE COMMENT '市销率TTM',
    PEG_CAR DOUBLE COMMENT 'PEG比率CAR',
    TOTAL_MARKET_CAP DOUBLE COMMENT '总市值',
    MARKET_CAP_VAG DOUBLE COMMENT '流通市值',
    NOTLIMITED_MARKETCAP_A DOUBLE COMMENT '非限售A股市值',
    NOMARKETCAP_A_VAG DOUBLE COMMENT '非限售A股流通市值',
    TOTAL_SHARES BIGINT COMMENT '总股本',
    TOTAL_SHARES_VAG DOUBLE COMMENT '总流通股本',
    FREE_SHARES_VAG DOUBLE COMMENT '自由流通股本',
    NUM INT COMMENT '公司数量',
    LOSS_COUNT INT COMMENT '亏损公司数量',
    PRIMARY KEY(BOARD_CODE, TRADE_DATE)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
last_updated_time TIMESTAMP
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
day DATE NOT NULL,
opening_price DECIMAL(10,3),
closing_price DECIMAL(10,3),
intraday_high DECIMAL(10,3),
intraday_low DECIMAL(10,3),
day_avg_five DECIMAL(10,3),
day_avg_ten DECIMAL(10,3),
day_gain_of_five DECIMAL(10,2),
day_gain_of_ten DECIMAL(10,2),
ttm DECIMAL(10,2),
pbr DECIMAL(10,2),
price_book_ratio DECIMAL(10,2),
price_sale_ratio DECIMAL(10,2)
PRIMARY KEY(stock_id, day)
)ENGINE=InnoDB DEFAULT CHARSET=utf8;
alter table daily_price add column pbr DECIMAL(10,2);

CREATE TABLE  stock(
stock_id VARCHAR(12) primary key,
stock_name VARCHAR(20),
financial_type INT,
market_value VARCHAR(12),
gross_profit_gain DECIMAL(10,2),

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
last_updated_time TIMESTAMP ,
belong_etf VARCHAR(12),
belong_bd_indicator VARCHAR(12),
stock_ids TEXT,
customer_range VARCHAR(50), -- 20240601_20240615_20240620
customer_range_gain_pre DECIMAL(10,2),
customer_range_gain_post DECIMAL(10,2)

)ENGINE=InnoDB DEFAULT CHARSET=utf8;

