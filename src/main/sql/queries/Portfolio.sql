SET SQL_SAFE_UPDATES = 0;
Commit;
select * from asset_classification;
select * from user a where a.email = 'sudhirkulaye';
select * from user_members;
select * from member; 
select * from adviser_user_mapping;
select * from composite; -- 7 composites
select * from composite_constituents; 
select * from portfolio a order by start_date desc, a.memberid, a.portfolioid; -- total 26 portfolios 
select * from portfolio_holdings a where memberid in (1071) order by asset_classid, ticker, buy_date;
select compositeid, count(1) from portfolio a group by compositeid order by a.compositeid; -- (composite 1: 10, 2: 11)
select * from portfolio_cashflow where memberid in (1000) order by portfolioid, date desc;
select * from portfolio_value_history a where memberid in (1071) and  date >= '2023-03-01' order by memberid, date desc;
select * from portfolio_returns_calculation_support a where memberid in (1000);
select * from portfolio_twrr_summary a where memberid in (1, 1024);
select * from portfolio_twrr_monthly a where memberid in (1);
select * from benchmark;
UPDATE portfolio_holdings SET rate = '1', total_cost = '1', net_rate = '1', cmp = '1', market_value = '1' WHERE ticker = 'MOSL_CASH'  and total_cost = 0 and rate = 0 and net_rate = 0 and market_value = 0;


-- All portfolio holdings
SELECT * FROM portfolio_holdings a WHERE memberid = 1 order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
SELECT * FROM portfolio_historical_holdings a  WHERE memberid = 1 order by a.memberid, a.portfolioid, a.sell_date desc, a.asset_classid, a.ticker;

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
and b.compositeid in (1,2,3,4,7) -- (1,2,3,4,7) -- change to 1: for INTRO strategy 2: FOCUS-FIVE
GROUP BY a.memberid, a.portfolioid, a.ticker 
ORDER BY b.compositeid, memberid, portfolioid,sum(a.market_value) desc; 

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
and b.compositeid = 1 -- change to 1: for INTRO strategy 2: FOCUS-FIVE
ORDER BY memberid, portfolioid, a.sell_date desc; 

-- stock count match
select concat(moslcode, '-', first_name, ' ', last_name) name, short_name, sum(quantity) 
from portfolio_holdings a, member b, moslcode_memberid c
where a.memberid = b.memberid and
a.memberid = c.memberid and
b.memberid = c.memberid and 
c.moslcode != 'H20613'
group by a.memberid, name order by name, short_name;
-- cash balance
select concat(moslcode, '-', first_name, ' ', last_name) name, short_name, cmp
from portfolio_holdings a, member b, moslcode_memberid c
where a.memberid = b.memberid and
a.memberid = c.memberid and
b.memberid = c.memberid and 
c.moslcode != 'H20613' and
short_name = 'MOSL Cash'
order by name;


-- delete holdings of closed account esp. MOSL_CASH
select * from portfolio_holdings a order by memberid, asset_classid, short_name, buy_date;
-- delete from portfolio_holdings where memberid in (1009, 1010, 1051);
-- delete from portfolio_value_history where memberid in (1009, 1010, 1051);


-- model portfolio
SELECT 'H1', 'Model', b.description, a.memberid, a.portfolioid, 
a.short_name, sum(a.quantity), sum(a.total_cost), sum(a.market_value), sum(a.net_profit), 
sum(a.market_value)/avg(b.market_value), avg(b.market_value)
FROM portfolio_holdings a, portfolio b
WHERE a.portfolioid = b.portfolioid 
and a.memberid = b.memberid 
and a.memberid in (1)
and b.compositeid in (2) -- change to 1: for INTRO strategy 2: FOCUS-FIVE
GROUP BY a.memberid, a.portfolioid, a.ticker 
ORDER BY memberid, portfolioid,sum(a.market_value) desc; 

-- Portfolio Vs Benchmark Performance
select b.benchmark_name, b.benchmark_type, a.returns_twrr_since_current_month, a.returns_twrr_three_months, returns_twrr_half_year, returns_twrr_one_year, returns_twrr_ytd, returns_twrr_two_year, returns_twrr_three_year, returns_twrr_five_year from benchmark_twrr_summary a, benchmark b Where a.benchmarkid = b.benchmarkid order by benchmark_type, a.benchmarkid, a.returns_twrr_one_year desc;
select 'Focus-Five', 'Multi-Cap', returns_twrr_since_current_month, returns_twrr_three_months, returns_twrr_half_year, returns_twrr_one_year, returns_twrr_ytd, returns_twrr_two_year, returns_twrr_three_year, returns_twrr_five_year from portfolio_twrr_summary a WHERE  a.memberid = 1 AND a.portfolioid = 2;
select d.first_name, d.last_name, a.memberid /* 'Focus-Five'*/, 'Multi-Cap', returns_twrr_since_current_month, returns_twrr_three_months, returns_twrr_half_year, returns_twrr_one_year, returns_twrr_ytd, returns_twrr_two_year, returns_twrr_three_year, returns_twrr_five_year from portfolio_twrr_summary a, portfolio b, member d where a.memberid = b.memberid and b.memberid = d.memberid and a.portfolioid = b.portfolioid and b.compositeid in (2,3) order by  a.memberid;

-- Portfolio returns
SELECT  c.moslcode, d.first_name, d.last_name, a.*  
from portfolio_twrr_summary a, portfolio b, moslcode_memberid c, member d 
WHERE a.portfolioid = b.portfolioid 
and a.memberid = b.memberid 
and a.memberid = c.memberid
and c.moslcode != 'H20613'
and a.memberid = d.memberid
and b.compositeid = 3 -- change to 1: for INTRO strategy 2: FOCUS-FIVE
ORDER BY a.memberid, a.portfolioid;
select c.moslcode, d.first_name, d.last_name, a.* 
from portfolio_twrr_monthly a, portfolio b, moslcode_memberid c, member d
WHERE a.portfolioid = b.portfolioid 
and a.memberid = b.memberid 
and a.memberid = c.memberid
and c.moslcode != 'H20613'
and a.memberid = d.memberid
and b.compositeid = 3 -- change to 1: for INTRO strategy 2: FOCUS-FIVE
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

-- Fee Calculaiton
select concat(d.first_name, ' ', d.last_name) name, a.memberid, a.portfolioid, a.date, b.date, TIMESTAMPDIFF(MONTH, a.date, b.date) duration, a.value, b.value 
from portfolio_value_history a, portfolio_value_history b, portfolio c, member d where
a.memberid = b.memberid and
b.memberid = c.memberid and
c.memberid = d.memberid and
a.portfolioid = b.portfolioid and
b.portfolioid = c.portfolioid and
c.status = 'Active' and
c.compositeid = 2 and -- (1: INTRO, 2: FOCUS-FIVE)
a.date = '2019-06-28' and -- (Start Date)
b.date = '2020-12-31' -- (End Date)
order by memberid, portfolioid;


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
-- update portfolio_holdings a, stock_universe b set a.name = b.name, a.short_name = b.short_name, a.asset_classid = b.asset_classid, a.subindustryid = b.subindustryid where a.ticker = b.ticker ;
-- update portfolio_holdings a set a.asset_classid = 101010, name = 'MOSL Cash', a.short_name = 'MOSL Cash' Where a.ticker = 'MOSL_CASH';
-- UPDATE portfolio_holdings a, nse_price_history b SET a.cmp = b.close_price WHERE  a.ticker = b.nse_ticker AND b.date = (select date_today from setup_dates);
-- UPDATE portfolio_holdings a, bse_price_history b SET a.cmp = b.close_price WHERE  a.ticker = b.bse_ticker AND b.date = (select date_today from setup_dates);
-- update portfolio_holdings a set total_cost = ((quantity * rate) + brokerage + tax), net_rate = round((total_cost/quantity),2), market_value = (cmp * quantity), net_profit = (market_value - total_cost), holding_period = ROUND((DATEDIFF((select date_today from setup_dates), buy_date) / 365.25), 2),absolute_return = round((market_value / total_cost) - 1, 2),annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 2);
commit;

-- ap_process_mosl_transactions to be called after uploading transactions
-- call ap_process_mosl_transactions();
select * from log_table;
truncate log_table; 
-- DELETE from mosl_transaction where moslcode = 'H20488'; 
-- update mosl_transaction set script_name = '516030' where script_name = 'YASHPAKKA';
select * from mosl_transaction where is_processed = 'N' order by date desc;
select * from mosl_transaction where /*quantity < 0 and*/ date >= '2025-04-02' and script_name not in ('MOSL_CASH', 'LIQUIDBEES') AND moslcode not in ('-H20404', '-1') and is_processed != '-Y' order by date, moslcode;
select moslcode, date, script_name, sell_buy, sum(quantity), sum(brokerage), sum(net_amount) from mosl_transaction where date >= '2024-01-01' and moslcode in ('H1', '-H20404', '-1') and script_name not in ('MOSL_CASH', 'LIQUIDBEES') group by moslcode, date, script_name, sell_buy order by moslcode, date desc, script_name;
-- update mosl_transaction set portfolioid = 1 where date = '2019-11-18';
select * from portfolio_holdings a where a.memberid in (1) order by portfolioid, asset_classid, ticker, buy_date;
select * from portfolio_historical_holdings a where a.memberid in (1) and sell_date >= '2025-01-01' order by sell_date desc, ticker;
select * from portfolio_historical_holdings a where sell_date >= '2021-11-26';
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
select * from portfolio_cashflow a where a. memberid = 1000 and portfolioid = 1;
select * from portfolio_holdings a where a. memberid in (1) order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
SELECT * from portfolio_value_history a where a.date >= '2024-09-01' and a.memberid in (1) order by memberid, portfolioid, date desc; 
SELECT * from portfolio_returns_calculation_support a where a. memberid = 1026 and portfolioid = 1 ORDER BY a.memberid, a.portfolioid, a.date;
select * from portfolio_twrr_monthly a where a.memberid IN (1026, 1, 1003, 1001, 1024);
SELECT * from portfolio_twrr_summary a where a.memberid IN (1026, 1, 1003, 1001, 1024);
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

select * from asset_classification a;
SELECT * from wealth_details a where memberid in (1000, 1011) order by a.memberid, a.asset_classid, a.ticker, a.buy_date;
SELECT b.asset_class_group, sum(a.market_value)  from wealth_details a, asset_classification b where a.asset_classid = b.classid and a.memberid in (1000, 1011) group by b.asset_class_group order by b.asset_class_group;
select * from wealth_history a where memberid in (1000) order by a.date desc;
SELECT * FROM wealth_asset_allocation_history a where a.memberid = 1026 and date = (SELECT min(date) FROM wealth_asset_allocation_history a where a.memberid = 1026);
select * from portfolio_holdings a where memberid in (1000) order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
select * from portfolio_historical_holdings a where memberid in (1026) order by a.memberid, a.portfolioid, a.sell_date desc, a.asset_classid, a.ticker, a.buy_date;
SELECT a.*, b.value from portfolio_cashflow a, portfolio_value_history b 
where a.memberid in (1026) and a.memberid = b.memberid and a.portfolioid = b.portfolioid and a.date = b.date order by a.date desc;
SELECT * from portfolio_value_history a where memberid in (1026) and date >= '2019-05-31' order by date desc;


select * from mutual_fund_nav_history a WHERE a.date = '2019-08-30' and a.scheme_code in ('120465','119018','120152','118825','129046','119718','118533','119807','120505','129220','130503','118525');

-- update portfolio_holdings set rate = 692156.6, total_cost = 692156.6, net_rate = 692156.6, cmp = 692156.6, market_value = 692156.6, buy_date = (select date_today from setup_dates) where memberid in (1000) and ticker = 'MOSL_CASH';
-- update portfolio_holdings set rate = -118246, total_cost = -118246, net_rate = -118246, cmp = -118246, market_value = -118246, buy_date = (select date_today from setup_dates) where memberid in (1000) and ticker = 'FutureOptions';


