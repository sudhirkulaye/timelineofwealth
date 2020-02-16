select * from member a where a.first_name = 'Sindhu';
SELECT * from mutual_fund_stats a ORDER BY a.scheme_type, a.trailing_return_1yr desc;
SELECT * FROM stock_price_movement a ORDER BY a.ticker; 
SELECT * from nse_price_history a where date = '2019-09-19';
select * from index_valuation a where date = '2019-09-19';
select * from wealth_details a where memberid in (1000, 1011);
select * from portfolio_holdings a where memberid = 1000;
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
select * from benchmark_twrr_summary order by benchmarkid;
SELECT * from benchmark_twrr_monthly order by benchmarkid, year desc;
SELECT * from index_valuation where date > '2020-01-18' and ticker = 'NIFTY' ORDER BY date desc;
select * from mutual_fund_nav_history a WHERE date IN (SELECT max(date) FROM mutual_fund_nav_history WHERE scheme_code = '118525' group by year(date), month(date)) AND scheme_code = '118525' order by date desc;
SELECT date, value FROM index_valuation a WHERE date > '2019-01-01' and date IN (SELECT max(date) FROM index_valuation WHERE ticker = 'NIFTY' group by year(date), month(date)) AND ticker = 'NIFTY' order by date desc;
select * from benchmark;
select * from log_table;
truncate table log_table;
select * from portfolio_twrr_monthly a WHERE  a.memberid = 1026 AND a.portfolioid = 1;
select * from portfolio_twrr_summary a WHERE  a.memberid = 1026 AND a.portfolioid = 1;
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
SELECT * from portfolio_holdings a WHERE  a.memberid = 1000 and portfolioid = 2;
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