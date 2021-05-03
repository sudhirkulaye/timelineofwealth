/*
ALTER TABLE `timelineofwealth`.`daily_data_s` 
ADD COLUMN `fcf_s` DECIMAL(20,3) NULL DEFAULT '0.000' AFTER `sales`,
ADD COLUMN `sales_growth_5years` DECIMAL(10,4) NULL DEFAULT '0.0000' AFTER `sales_growth_3years`,
ADD COLUMN `sales_growth_10years` DECIMAL(10,4) NULL DEFAULT '0.0000' AFTER `sales_growth_5years`;

ALTER TABLE `timelineofwealth`.`daily_data_s` 
ADD COLUMN `noplat` DECIMAL(20,3) NULL DEFAULT '0.000' AFTER `mcap_to_sales`,
ADD COLUMN `capex` DECIMAL(20,3) NULL DEFAULT '0.000' AFTER `noplat`,
ADD COLUMN `fcff` DECIMAL(20,3) NULL DEFAULT '0.000' AFTER `capex`,
ADD COLUMN `invested_capital` DECIMAL(20,3) NULL DEFAULT '0.000' AFTER `fcff`,
ADD COLUMN `roic` DECIMAL(10,4) NULL DEFAULT '0.0000' AFTER `invested_capital`;

ALTER TABLE `timelineofwealth`.`daily_data_s` 
ADD COLUMN `invested_capital` DECIMAL(20,3) NULL DEFAULT '0.000' AFTER `fcff`;
select * from daily_data_s a where a.date = '2020-02-17' order by rank;



-- update HDFC Bank PB
select * from daily_data_s a where a.name = 'HDFC Bank' and date BETWEEN '2019-04-20' and '2019-07-01' order by date desc;
update daily_data_s a set pb_ttm = (cmp/548) where a.name = 'HDFC Bank' and date BETWEEN '2019-04-20' and '2019-07-01'; -- TODO: update second date
update daily_data_s a set pb_ttm = (cmp/522) where a.name = 'HDFC Bank' and date BETWEEN '2019-01-19' and '2019-04-19'; -- TODO: update second date
update daily_data_s a set pb_ttm = (cmp/507) where a.name = 'HDFC Bank' and date BETWEEN '2018-10-20' and '2019-01-18'; -- TODO: update second date
update daily_data_s a set pb_ttm = (cmp/409) where a.name = 'HDFC Bank' and date BETWEEN '2018-07-21' and '2018-10-19'; -- TODO: update second date
update daily_data_s a set pb_ttm = (cmp/405) where a.name = 'HDFC Bank' and date BETWEEN '2018-04-21' and '2018-07-20'; -- TODO: update second date
-- 
*/

select * from daily_data_s a where date = '2020-04-30' or name = 'Avenue Super.';
select * from stock_universe a where bse_industry = 'Other Financial Services' order by marketcap desc;
select distinct ticker, short_name, asset_classid from wealth_details a where asset_classid > 401010 and asset_classid < 500000 and short_name not like '%FOCUS-FIVE%';
select * from asset_classification;
select b.benchmark_type, b.benchmark_name, a.* from benchmark_twrr_summary a, benchmark b  Where a.benchmarkid = b.benchmarkid order by b.benchmark_type, b.is_mutual_fund, b.benchmark_name;

select * from asset_classification; 
select * from subindustry;
select * from stock_universe order by marketcap desc;
-- update stock_universe set marketcap = 0; 
-- update stock_universe a, daily_data_s b set a.marketcap = b.market_cap where a.ticker5 = b.name and b.date = (select max(date) from daily_data_s);
select * from nse_price_history a where date > '2020-09-06' and a.nse_ticker = 'INFY';

  UPDATE portfolio_holdings a 
  SET market_value = cmp * quantity, 
      net_profit = market_value - total_cost, 
      absolute_return = round((market_value / total_cost) - 1, 4), 
      annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 4)
  WHERE asset_classid not in ('101010', '101020', '201010', '202010', '203010', '203020', '203050')
  AND holding_period > 1;

select * from subindustry a order by a.subindustryid;
select * from stock_universe a where a.name like 'Dhani%';
select * from stock_universe a where a.ticker IN ('EMBASSY');
select * from daily_data_s a where a.name = 'deepak nitrite' order by date desc;

select * from mutual_fund_universe a where a.scheme_code = 118531;
select count(1) from mutual_fund_nav_history where scheme_code = 118531;  -- 13221422

select count(1) from daily_data_s where date >= '2020-10-01';
select count(1) from nse_price_history where date >= '2021-01-07';
select count(1) from bse_price_history where date >= '2020-10-01';
select date, count(1) from mutual_fund_nav_history where date >= '2021-04-01' group by date desc;
select * from mutual_fund_nav_history where date = '2021-03-24' ;

select * from wealth_details a where a.memberid in (1000, 1011) order by a.asset_classid, a.ticker, a.buy_date;
call ap_process_eod;
select * from log_table;
truncate table log_table;
select date, count(1) from index_valuation a where date > '2020-12-18' group by date order by date desc;

-- new IPO
/*
Mrs. Bectors Food
Burger King India
Gland Pharma
Equitas Small Finance Bank
Angel Broking
Mazagon Dock Shipbuilders
UTI Asset Management
CAMS
Route Mobile
Happiest Minds
Mindspace Business Parks REIT
Rossari Biotech
Affle (India)

*/

select count(1), date from daily_data_s where date > '2021-03-22' group by date order by date desc; 
select count(1), date from nse_price_history where date > '2021-03-22' group by date order by date desc;
select count(1), date from bse_price_history where date > '2021-03-22' group by date order by date desc;
select count(1), date from mutual_fund_nav_history where date > '2021-03-22' group by date order by date desc;
select name from daily_data_s where date = '2020-01-14' and name not in (select name from daily_data_s where date = '2020-01-15');
select date, count(1) from mutual_fund_nav_history where date >= '2021-03-31' group by date desc;
select * from mutual_fund_nav_history a where date in ('2021-01-04','2021-01-05','2021-01-07','2021-01-14','2021-01-15');

select * from stock_price_movement_history a where ticker = 'MFSL';
select * from benchmark_twrr_monthly a where benchmarkid = 'NIFTY';

select nse_ticker, a.date, open_price, high_price, low_price, close_price, b.return_1D, b.return_1W, b.return_2W, b.return_1M 
from nse_price_history a, stock_price_movement_history b 
where nse_ticker = b.ticker and
a.date = b.date and
a.date between '2020-07-27' and '2020-10-20' and 
nse_ticker = 'MARICO' order by a.date desc; 

select * from benchmark;
select * from benchmark_twrr_monthly where benchmarkid = 1;
select * from portfolio_twrr_monthly where memberid = 1;
select * from portfolio_twrr_summary where memberid = 1;

select * from mutual_fund_universe a where a.fund_house like 'Parag%' and direct_regular = 'Direct' and dividend_growth = 'Growth';

SELECT memberid, portfolioid, date date, 0 cashflow, value, 'EOM Value'
      FROM   portfolio_value_history a
      WHERE  a.memberid = 1
      AND a.portfolioid = 2
      AND a.date NOT IN
					   (SELECT date
					   FROM   portfolio_cashflow g
					   WHERE  g.memberid = 1
					   AND g.portfolioid = 2) -- AND a.date NOT IN (SELECT date_today FROM setup_dates)
       AND date IN (SELECT   MAX(date)
					  FROM     portfolio_value_history h
					  WHERE    h.memberid = 1
					  AND h.portfolioid = 2
					  GROUP BY YEAR(date),
					  MONTH (date));
                      
select * from mutual_fund_nav_history a where a.scheme_code = '118825' and date IN ('2021-04-30','2021-03-31','2021-02-26','2021-01-29','2020-12-31','2020-11-27','2020-10-30','2020-09-30','2020-08-31','2020-07-31','2020-06-30','2020-05-29','2020-04-30','2020-03-31','2020-02-28','2020-01-31','2019-12-31','2019-11-29','2019-10-31','2019-09-28','2019-08-30','2019-07-31','2019-06-28') order by date desc;