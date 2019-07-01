SET SQL_SAFE_UPDATES = 0;
Commit;
select * from asset_classification;
select * from composite; -- 5 composites
select * from portfolio a order by a.memberid, a.portfolioid; -- total 21 portfolios 
select compositeid, count(1) from portfolio a group by compositeid order by a.memberid, a.portfolioid; -- (composite 1: 11, 2: 10)
select * from portfolio_cashflow where memberid in (1000) order by portfolioid, date desc;
select * from portfolio_value_history a where memberid in (1000);
-- All portfolio holdings
SELECT * FROM portfolio_holdings a  /*WHERE memberid = 1000*/ order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
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
and b.compositeid = 1 -- change to 1: for INTRO strategy 2: FOCUS-FIVE
GROUP BY a.memberid, a.portfolioid, a.ticker 
ORDER BY memberid, portfolioid,sum(a.market_value) desc; 
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
select * from log_table order by TIMESTAMP desc;
truncate log_table; 
-- DELETE from mosl_transaction where moslcode = 'H20488';
select * from mosl_transaction where date > '2019-06-24' and is_processed = 'N' order by date;
SELECT * FROM stock_universe a WHERE ticker in ('TVSMOTOR','');
select * from portfolio_holdings a where a.memberid = 1060 order by asset_classid, ticker, buy_date;
select * from portfolio_historical_holdings a where a.memberid = 1010 order by sell_date, ticker;
-- DELETE from portfolio_holdings where memberid = 1026;
-- DELETE from portfolio_historical_holdings where memberid = 1026;
-- UPDATE mosl_transaction set is_processed = 'N' where moslcode = 'H22295';
-- DELETE from mosl_transaction where moslcode = 'H22295';
select * from moslcode_memberid a where moslcode = 'H22295';

SELECT * from portfolio_value_history a where a. memberid = 1026 and a.date = (select date_today from setup_dates) order by memberid, portfolioid, date; 
select * from portfolio_irr_summary a where a. memberid = 1026;
select * from portfolio_twrr_monthly a where a. memberid = 1026;
SELECT * from portfolio_twrr_summary a where a. memberid = 1026;
select * from portfolio_asset_allocation a where a. memberid = 1026;
select * from mutual_fund_stats;
SELECT * from stock_price_movement;

UPDATE portfolio_historical_holdings SET fin_year = 'FY2020' WHERE sell_date >= '2019-04-01';
UPDATE portfolio_historical_holdings SET fin_year = 'FY2019' WHERE sell_date >= '2018-04-01' AND sell_date < '2019-04-01';
SELECT memberid, portfolioid, fin_year, sum(net_profit) from portfolio_historical_holdings a where holding_period >= 1 group by memberid, portfolioid, fin_year ORDER BY memberid, portfolioid, fin_year desc;
SELECT memberid, portfolioid, fin_year, sum(net_profit) from portfolio_historical_holdings a where holding_period < 1 group by memberid, portfolioid, fin_year ORDER BY memberid, portfolioid, fin_year desc;

SELECT * FROM setup_dates;
select month('2019-04-01');
select year('2019-04-01');

select a.memberid, a.portfolioid, a.ticker, sum(a.quantity), avg(d.latest_price), sum(a.market_value), avg(b.value), round((avg(0.15*b.value) - sum(a.market_value))/(avg(d.latest_price)), 2) 
from portfolio_holdings a, portfolio_value_history b, portfolio c, stock_universe d
where a.ticker = 'BRITANNIA' 
and a.portfolioid = b.portfolioid 
and a.memberid = b.memberid 
and a.memberid = c.memberid
and b.portfolioid = c.portfolioid
and a.ticker = d.ticker
and b.date = '2019-06-28'
and c.compositeid = 2
group by a.memberid, a.portfolioid, a.ticker;

SELECT * from wealth_details a where memberid in (1000, 1011) order by a.memberid, a.asset_classid, a.ticker, a.buy_date;
