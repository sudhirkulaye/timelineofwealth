SET SQL_SAFE_UPDATES = 0;
Commit;
select * from asset_classification;
select * from user a where a.email = 'sudhirkulaye';
select * from user_members;
select * from member; 
select * from adviser_user_mapping;
select * from composite; -- 5 composites
select * from composite_constituents; 
select * from portfolio a order by a.memberid, a.portfolioid; -- total 21 portfolios 
select compositeid, count(1) from portfolio a group by compositeid order by a.compositeid; -- (composite 1: 11, 2: 10)
select * from portfolio_cashflow where memberid in (1051) order by portfolioid, date desc;
select * from portfolio_value_history a where memberid in (1026) and  date >= '2019-12-01' order by date desc;
select * from portfolio_returns_calculation_support a where memberid in (1000);
select * from portfolio_twrr_summary a where memberid in (1, 1007);
select * from portfolio_twrr_monthly a where memberid in (1);
select * from benchmark;

-- All portfolio holdings
SELECT * FROM portfolio_holdings a  WHERE memberid = 1060 order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
SELECT * FROM portfolio_historical_holdings a  WHERE memberid = 1001 order by a.memberid, a.portfolioid, a.sell_date desc, a.asset_classid, a.ticker;
-- query to find wt of each security to compare with model portfolio
SELECT c.moslcode, d.first_name, d.last_name, a.memberid, a.portfolioid, 
a.short_name, sum(a.quantity), sum(a.total_cost), sum(a.market_value), sum(a.net_profit), 
sum(a.market_value)/avg(b.market_value), avg(b.market_value)
FROM portfolio_holdings a, portfolio b, moslcode_memberid c, member d
WHERE a.portfolioid = b.portfolioid 
and a.memberid = b.memberid 
and a.memberid = c.memberid
and c.moslcode != 'H20613'
and a.memberid = d.memberid
and b.compositeid = 2 -- change to 1: for INTRO strategy 2: FOCUS-FIVE
GROUP BY a.memberid, a.portfolioid, a.ticker 
ORDER BY memberid, portfolioid,sum(a.market_value) desc; 
-- Realized Profit & Loss
SELECT c.moslcode, d.first_name, d.last_name, a.memberid, a.portfolioid, 
a.short_name, a.buy_date, a.total_cost, a.sell_date, a.net_sell, a.holding_period, a.net_profit
FROM portfolio_historical_holdings a, portfolio b, moslcode_memberid c, member d
WHERE a.portfolioid = b.portfolioid 
and a.memberid = b.memberid 
and a.memberid = c.memberid
and c.moslcode != 'H20613'
and a.memberid = d.memberid
and a.short_name != 'LIQD BeES ETF'
and a.sell_date >= '2019-07-01'
and b.compositeid = 2 -- change to 1: for INTRO strategy 2: FOCUS-FIVE
ORDER BY memberid, portfolioid, a.sell_date desc; 
-- model portfolio
SELECT 'NA', 'Model', b.description, a.memberid, a.portfolioid, 
a.short_name, sum(a.quantity), sum(a.total_cost), sum(a.market_value), sum(a.net_profit), 
sum(a.market_value)/avg(b.market_value), avg(b.market_value)
FROM portfolio_holdings a, portfolio b
WHERE a.portfolioid = b.portfolioid 
and a.memberid = b.memberid 
and a.memberid in (1)
and b.compositeid in (2) -- change to 1: for INTRO strategy 2: FOCUS-FIVE
GROUP BY a.memberid, a.portfolioid, a.ticker 
ORDER BY memberid, portfolioid,sum(a.market_value) desc; 
-- Portfolio returns
SELECT  c.moslcode, d.first_name, d.last_name, a.*  
from portfolio_twrr_summary a, portfolio b, moslcode_memberid c, member d 
WHERE a.portfolioid = b.portfolioid 
and a.memberid = b.memberid 
and a.memberid = c.memberid
and c.moslcode != 'H20613'
and a.memberid = d.memberid
and b.compositeid = 2 -- change to 1: for INTRO strategy 2: FOCUS-FIVE
ORDER BY a.memberid, a.portfolioid;
select c.moslcode, d.first_name, d.last_name, a.* 
from portfolio_twrr_monthly a, portfolio b, moslcode_memberid c, member d
WHERE a.portfolioid = b.portfolioid 
and a.memberid = b.memberid 
and a.memberid = c.memberid
and c.moslcode != 'H20613'
and a.memberid = d.memberid
and b.compositeid = 1 -- change to 1: for INTRO strategy 2: FOCUS-FIVE
ORDER BY a.memberid, a.portfolioid;
Select c.moslcode, d.first_name, d.last_name, a.date, a.cashflow, a.value, a.description  
from portfolio_returns_calculation_support a, portfolio b, moslcode_memberid c, member d
WHERE a.portfolioid = b.portfolioid 
and a.memberid = b.memberid 
and a.memberid = c.memberid
and c.moslcode != 'H20613'
and a.memberid = d.memberid
and b.compositeid = 1 -- change to 1: for INTRO strategy 2: FOCUS-FIVE
Order By a.memberid, a.portfolioid, a.date DESC;

-- Benchmark returns
select b.benchmark_type, b.benchmark_name, a.* from benchmark_twrr_summary a, benchmark b
Where a.benchmarkid = b.benchmarkid order by b.benchmark_type, benchmarkid;
select b.benchmark_type, b.benchmark_name, a.* from benchmark_twrr_monthly a, benchmark b
Where a.benchmarkid = b.benchmarkid order by year desc, b.benchmark_type, benchmarkid;
select * from benchmark;

-- update portfolio_holdings set buy_date = (select date_today from setup_dates) where ticker = 'MOSL_CASH';
-- UPDATE portfolio_holdings a, stock_universe b SET a.asset_classid = b.asset_classid, a.name = b.name, a.short_name = b.short_name, a.subindustryid = b.subindustryid WHERE a.ticker = b.ticker;
-- UPDATE portfolio_historical_holdings a, stock_universe b SET a.asset_classid = b.asset_classid, a.name = b.name, a.short_name = b.short_name, a.subindustryid = b.subindustryid WHERE a.ticker = b.ticker;
-- UPDATE portfolio_holdings a SET total_cost = (quantity * rate) + brokerage + tax, net_rate = total_cost/quantity, market_value = cmp * quantity, net_profit = market_value - total_cost, holding_period = ROUND((DATEDIFF((SELECT date_today FROM setup_dates), buy_date) / 365.25), 2), absolute_return = round((market_value / total_cost) - 1, 4), annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 4);
-- UPDATE portfolio_historical_holdings a SET total_cost = (quantity * rate) + brokerage + tax, net_rate = total_cost/quantity, net_sell = (quantity *sell_rate) - brokerage_sell - tax_sell, net_sell_rate = net_sell/quantity, net_profit = net_sell - total_cost, holding_period = ROUND((DATEDIFF(sell_date, buy_date) / 365.25), 2), absolute_return = round((net_sell / total_cost) - 1, 4), annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 4);
-- truncate table portfolio_holdings;
SELECT a.memberid, a.portfolioid, sum(market_value) from portfolio_holdings a group by a.memberid, a.portfolioid order by a.memberid, a.portfolioid;
SELECT a.memberid, a.portfolioid, b.asset_class_group, sum(a.market_value) from portfolio_holdings a, asset_classification b where a.asset_classid = b.classid GROUP BY a.memberid, a.portfolioid, b.asset_class_group order by a.memberid, a.portfolioid, asset_class_group;

SET SQL_SAFE_UPDATES = 0;
Commit;
update portfolio_holdings a, stock_universe b set a.name = b.name, a.short_name = b.short_name, a.asset_classid = b.asset_classid, a.subindustryid = b.subindustryid where a.ticker = b.ticker ;
update portfolio_holdings a set a.asset_classid = 101010, name = 'MOSL Cash', a.short_name = 'MOSL Cash' Where a.ticker = 'MOSL_CASH';
UPDATE portfolio_holdings a, nse_price_history b SET a.cmp = b.close_price WHERE  a.ticker = b.nse_ticker AND b.date = (select date_today from setup_dates);
UPDATE portfolio_holdings a, bse_price_history b SET a.cmp = b.close_price WHERE  a.ticker = b.bse_ticker AND b.date = (select date_today from setup_dates);
update portfolio_holdings a set total_cost = ((quantity * rate) + brokerage + tax), net_rate = round((total_cost/quantity),2), market_value = (cmp * quantity), net_profit = (market_value - total_cost), holding_period = ROUND((DATEDIFF((select date_today from setup_dates), buy_date) / 365.25), 2),absolute_return = round((market_value / total_cost) - 1, 2),annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 2);
commit;

-- ap_process_mosl_transactions to be called after uploading transactions
-- call ap_process_mosl_transactions();
select * from log_table;
truncate log_table; 
-- DELETE from mosl_transaction where moslcode = 'H20488';
select * from mosl_transaction where date >= '2020-02-27' and script_name != 'MOSL_CASH' AND moslcode not in ('-H20404', '-1') and is_processed != 'X' order by date, moslcode;
SELECT * FROM stock_universe a WHERE ticker in ('YESBANK','');
update mosl_transaction set portfolioid = 1 where date = '2019-11-18';
select * from portfolio_holdings a where a.memberid in (1) order by portfolioid, asset_classid, ticker, buy_date;
select * from portfolio_historical_holdings a where a.memberid in (1) order by sell_date desc, ticker;
select b.moslcode, a.memberid, a.portfolioid, a.ticker, a.total_cost, a.cmp, a.market_value, b.net_amount 
from portfolio_holdings a, moslcode_memberid c, mosl_transaction b
WHERE a.memberid = c.memberid and a.portfolioid = b.portfolioid and b.moslcode = c.moslcode 
and b.script_name = 'MOSL_CASH' and a.ticker = 'MOSL_CASH' and b.is_processed = 'N';
-- DELETE from portfolio_holdings where memberid = 1026;
-- DELETE from portfolio_historical_holdings where memberid = 1026;
-- UPDATE mosl_transaction set is_processed = 'N' where moslcode = 'H22295';
-- DELETE from mosl_transaction where moslcode = 'H22295';
select * from moslcode_memberid a where moslcode = 'H22295';
SELECT * from portfolio a where a. memberid = 1 and portfolioid = 1;
select * from portfolio_cashflow a where a. memberid = 1003 and portfolioid = 1;
select * from portfolio_holdings a where a. memberid = 1 and portfolioid = 2 order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
SELECT * from portfolio_value_history a where a.date >= '2020-02-27' and a.memberid in (1) order by memberid, portfolioid, date desc; 
SELECT * from portfolio_returns_calculation_support a where a. memberid = 1026 and portfolioid = 1 ORDER BY a.memberid, a.portfolioid, a.date;
select * from portfolio_twrr_monthly a where a. memberid = 1026 and portfolioid = 1;
SELECT * from portfolio_twrr_summary a where a. memberid = 1026 and portfolioid = 1;
select * from portfolio_asset_allocation a where a. memberid = 1026 and portfolioid = 1;
select * from portfolio_irr_summary a where a. memberid = 1026 and portfolioid = 1;
select * from temp_irr_calculation a where a. memberid = 1026 and portfolioid = 1;
select * from mutual_fund_stats;
SELECT * from stock_price_movement;

UPDATE portfolio_historical_holdings SET fin_year = 'FY2020' WHERE sell_date >= '2019-04-01';
UPDATE portfolio_historical_holdings SET fin_year = 'FY2019' WHERE sell_date >= '2018-04-01' AND sell_date < '2019-04-01';
SELECT memberid, portfolioid, fin_year, sum(net_profit) from portfolio_historical_holdings a where holding_period >= 1 group by memberid, portfolioid, fin_year ORDER BY memberid, portfolioid, fin_year desc;
SELECT memberid, portfolioid, fin_year, sum(net_profit) from portfolio_historical_holdings a where holding_period < 1 group by memberid, portfolioid, fin_year ORDER BY memberid, portfolioid, fin_year desc;

SELECT * FROM setup_dates;
select month('2019-04-01');
select year('2019-04-01');


select classid, asset_class_group from asset_classification a;
SELECT * from wealth_details a where memberid in (1001,1002,1003,1026) order by a.memberid, a.asset_classid, a.ticker, a.buy_date;
SELECT * FROM wealth_asset_allocation_history a where a.memberid = 1026 and date = (SELECT min(date) FROM wealth_asset_allocation_history a where a.memberid = 1026);
select * from portfolio_holdings a where memberid in (1026) order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
select * from portfolio_historical_holdings a where memberid in (1026) order by a.memberid, a.portfolioid, a.sell_date desc, a.asset_classid, a.ticker, a.buy_date;
SELECT a.*, b.value from portfolio_cashflow a, portfolio_value_history b 
where a.memberid in (1026) and a.memberid = b.memberid and a.portfolioid = b.portfolioid and a.date = b.date order by a.date desc;
SELECT * from portfolio_value_history a where memberid in (1026) and date >= '2019-05-31' order by date desc;


select * from mutual_fund_nav_history a WHERE a.date = '2019-08-30' and a.scheme_code in ('120465','119018','120152','118825','129046','119718','118533','119807','120505','129220','130503','118525');
