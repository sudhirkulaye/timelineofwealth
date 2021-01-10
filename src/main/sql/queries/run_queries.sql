select * from member a where a.first_name = 'Sindhu';
SELECT * from mutual_fund_stats a ORDER BY a.scheme_type, a.trailing_return_1yr desc;
SELECT * FROM stock_price_movement a ORDER BY a.ticker; 
SELECT * from nse_price_history a where date = '2019-09-19';
select * from index_valuation a where date = '2019-09-19';
select * from wealth_details a where memberid in (1000, 1011);
select * from portfolio_holdings a where memberid = 1008;
select * from wealth_details a where memberid in (1007,1015,1058);
select * from sip a where memberid in (1007,1015,1058);
select * from mutual_fund_universe a where a.scheme_name_part like 'Franklin India Bluechip%';
-- 120564 - Aditya Birla Life Equity Fund-Dir-G
-- 118531 - Franklin India Bluechip Fund-Dir-G
call ap_process_portfolio_returns;


SELECT round(EXP(SUM(LOG(coalesce(returns_calendar_year + 1, 1)))), 4) - 1
FROM   portfolio_twrr_monthly a
WHERE  a.memberid = 1026 AND a.portfolioid = 1;

call ap_process_portfolio_returns;

SELECT prod(returns_calendar_year + 1)
FROM   portfolio_twrr_monthly a
WHERE  a.memberid = 1026 AND a.portfolioid = 1;

call ap_process_benchmark_returns;
select * from benchmark; 
/* MF Vs Index Vs Portfolio */
select b.benchmark_name, b.benchmark_type, a.returns_twrr_since_current_month, a.returns_twrr_three_months, returns_twrr_half_year, returns_twrr_one_year, returns_twrr_two_year, returns_twrr_three_year, returns_twrr_five_year from benchmark_twrr_summary a, benchmark b Where a.benchmarkid = b.benchmarkid order by benchmark_type, a.benchmarkid, a.returns_twrr_one_year;
select 'Focus-Five', 'Multi-Cap', returns_twrr_since_current_month, returns_twrr_three_months, returns_twrr_half_year, returns_twrr_one_year, returns_twrr_two_year, returns_twrr_three_year, returns_twrr_five_year from portfolio_twrr_summary a WHERE  a.memberid = 1 AND a.portfolioid = 2;
SELECT b.benchmark_name, b.benchmark_type, a.*  from benchmark_twrr_monthly a, benchmark b Where a.benchmarkid = b.benchmarkid  order by benchmarkid, year desc;
select * from portfolio_twrr_summary a WHERE  a.memberid = 1 AND a.portfolioid = 2;
select * from portfolio_twrr_monthly a WHERE  a.memberid = 1 AND a.portfolioid = 2;

SELECT * from index_valuation where date > '2020-01-18' and ticker = 'NIFTY' ORDER BY date desc;
select * from mutual_fund_nav_history a WHERE date IN (SELECT max(date) FROM mutual_fund_nav_history WHERE scheme_code = '118531' group by year(date), month(date)) AND scheme_code = '118531' order by date desc;
select min(date) from mutual_fund_nav_history a WHERE scheme_code in ('125497');
SELECT date, value FROM index_valuation a WHERE date > '2019-01-01' and date IN (SELECT max(date) FROM index_valuation WHERE ticker = 'NIFTY' group by year(date), month(date)) AND ticker = 'NIFTY' order by date desc;
select * from benchmark;
select * from log_table;
truncate table log_table;
select * from portfolio_twrr_monthly a WHERE  a.memberid = 1000 AND a.portfolioid = 1;
select * from portfolio_twrr_summary a WHERE  a.memberid = 1000 AND a.portfolioid = 1;
select * from portfolio_returns_calculation_support a WHERE  a.memberid = 1026 AND a.portfolioid = 1;

SELECT LAST_DAY('2003-04-01' - INTERVAL 1 MONTH);


  UPDATE portfolio a, portfolio_value_history b
  SET a.market_value = b.value,
	  a.net_profit = b.value - a.net_investment,
      a.holding_period = ROUND((DATEDIFF('2019-09-12', a.start_date) / 365.25), 2), 
      a.absolute_return = round((b.value / a.net_investment) - 1, 4)
  WHERE a.memberid = b.memberid 
  AND a.portfolioid = b.portfolioid
  AND b.date = '2019-09-12';
  
  UPDATE portfolio a, portfolio_twrr_summary b
  SET a.annualized_return = returns_twrr_since_inception
  WHERE a.memberid = b.memberid 
  AND a.portfolioid = b.portfolioid
  AND b.benchmarkid = 0;
  
SELECT * from portfolio a WHERE  a.memberid = 1000;
SELECT * from portfolio_holdings a WHERE  a.memberid = 1000 and portfolioid = 1;
select * from nse_price_history a where date = '2019-09-12';

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= '2016-09-16'
      AND b.returns_year = (2019 - 1);
      
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= '2016-09-16'
      AND b.returns_year = (2019 - 3);
      
    SELECT b.memberid, a.moslcode, a.portfolioid, date, script_name, sell_buy, sum(quantity), sum(amount), sum(brokerage), sum(txn_charges), sum(service_tax), sum(stamp_duty), sum(stt_ctt)
    FROM  mosl_transaction a, moslcode_memberid b
    WHERE (is_processed = NULL OR is_processed = 'N') 
    AND a.moslcode = b.moslcode
    GROUP BY b.memberid, a.moslcode, date, script_name, sell_buy
    ORDER BY date, a.moslcode, sell_buy, order_no, trade_no, script_name;
    
select * from daily_data_s a where name = 'Bandhan Bank' and (a.date in ('2019-10-24', '2019-07-19', '2019-05-02', '2019-01-10') or date > '2019-10-24') order by date desc;
    
select * from daily_data_s a where a.name = 'HDFC Bank' and date IN ('2018-07-23', '2018-04-24' );

SELECT b.memberid, a.moslcode, a.portfolioid, date, script_name, sell_buy, sum(quantity), sum(amount), sum(brokerage), sum(txn_charges), sum(service_tax), sum(stamp_duty), sum(stt_ctt)
    FROM  mosl_transaction a, moslcode_memberid b
    WHERE (is_processed = NULL OR is_processed = 'N') 
    AND a.moslcode = b.moslcode
    GROUP BY b.memberid, a.moslcode, date, script_name, sell_buy
    ORDER BY date, a.moslcode, sell_buy, order_no, trade_no, script_name;
    
SELECT * from wealth_asset_allocation_history
where date = (select date_today from setup_dates)
and memberid in (1000, 1011)
group by date order by date desc;

select * from daily_data_s a where a.date = '2018-10-26' and a.name = 'ITC';

update composite_constituents a, stock_universe b set a.name = b.name where a.ticker = b.ticker;

select distinct fund_house, count(1) from mutual_fund_universe a group by fund_house order by fund_house, scheme_name_full;
select * from mutual_fund_universe a order by fund_house, scheme_name_full;
select * from asset_classification;

select * from stock_universe a where ticker = 'IIFLWAM';
select * from nse_price_history a where nse_ticker = 'GUJFLUORO' and date >= '2018-01-01' order by date desc;
select * from bse_price_history a where bse_ticker = '540376' and date >= '2020-04-30' order by date desc;
insert into nse_price_history (
select 'DMART', 'EQ', open_price, high_price, low_price, close_price, last_price, previous_close_price, total_traded_quantity, total_traded_value, date, total_trades, isin_code 
from bse_price_history a where bse_ticker = '540376' and date > '2020-04-30' and date < '2020-05-27');
select * from stock_price_movement a where ticker = 'DMART';
-- update nse_price_history set nse_ticker = 'FLUOROCHEM' where nse_ticker = 'GUJFLUORO';

-- Queries to update Screener Short Name (In case of Name Change or ticker Change)
select * from stock_universe a where a.ticker = 'ADANIGAS' or a.ticker5 = 'Adani Gas'; -- Bharat Forge, 25101010
select * from daily_data_s a where date >= '2020-01-10' and name like 'Coforge' order by date desc;
select * from stock_price_movement_history where ticker = 'NIITTECH';

-- update daily_data_s set name = 'Adani Total Gas' where name = 'Adani Gas';
-- update daily_data_s set name = 'Indus Towers' where name = 'Bharti Infratel';
-- update daily_data_s set name = 'Coforge' where name = 'NIIT Tech.';
-- update daily_data_s set name = 'Dhani Services' where name = 'Indiabulls Vent.';

-- update nse_price_history a set nse_ticker = 'INDUSTOWER' where nse_ticker = 'INFRATEL';
-- update nse_price_history a set nse_ticker = 'COFORGE' where nse_ticker = 'NIITTECH';
-- update nse_price_history a set nse_ticker = 'DHANI' where nse_ticker = 'IBVENTURES';

-- update stock_price_movement set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_price_movement set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_price_movement set ticker = 'DHANI' where ticker = 'IBVENTURES' and date < '2020-11-06';

-- update stock_price_movement_history set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_price_movement_history set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_price_movement_history set ticker = 'DHANI' where ticker = 'IBVENTURES' and date < '2020-11-06';

-- update stock_quarter set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_quarter set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_quarter set ticker = 'DHANI' where ticker = 'IBVENTURES';

-- update stock_pnl set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_pnl set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_pnl set ticker = 'DHANI' where ticker = 'IBVENTURES';

-- update stock_cashflow set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_cashflow set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_cashflow set ticker = 'DHANI' where ticker = 'IBVENTURES';

-- update stock_balancesheet set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_balancesheet set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_balancesheet set ticker = 'DHANI' where ticker = 'IBVENTURES';

select count(1), name from daily_data_s a group by name order by count(1); 

select if(is_sensex = 1, 'SENSEX', if(is_nifty50 = 1, 'NIFTY', if(is_nse100 = 1 or is_bse100 = 1, 'NSE-BSE100', if(is_nse200 = 1 or is_bse200 = 1, 'NSE-BSE200', 'NSE-BSE500'))) ) index1, 
b.short_name, c.sector_name_display, c.industry_name_display, c.sub_industry_name_display, a.cmp, a.market_cap, rank, 
last_result_date, sales, net_profit, (opm_latest_quarter/100), (npm_latest_quarter/100), (opm_last_year/100), (npm_last_year/100),
debt, debt_3years_back, debt_to_equity, (roce/100), (avg_roce_3years/100), (roe/100), (avg_roe_3years/100), 
(yoy_quarterly_sales_growth/100), (yoy_quarterly_profit_growth/100), (sales_growth_3years/100), (profit_growth_3years/100), 
a.pe_ttm, a.pb_ttm, (a.market_cap/sales), 
(return_1D/100), (return_1W/100), (return_2W/100), (return_1M/100), (return_2M/100), (return_3M/100), (return_6M/100), (return_9M/100), 
(return_YTD/100), (return_1Y/100), (up_52w_min/100), (down_52w_max/100), (return_2Y/100), (return_3Y/100), 
(sales_growth_5years/100), (sales_growth_10years/100), noplat, capex, fcff, invested_capital, (roic/100)
from daily_data_s a, stock_universe b, subindustry c, stock_price_movement d
where 
a.date = (select date_today from setup_dates) and 
a.name = b.ticker5 and 
b.subindustryid = c.subindustryid and 
(b.is_bse500 = 1 or b.is_nse500 = 1) and
b.ticker = d.ticker
order by a.market_cap desc;