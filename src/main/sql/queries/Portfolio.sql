SET SQL_SAFE_UPDATES = 0;
Commit;

select * from composite; 

select * from mosl_code;

select * from portfolio a order by a.memberid, a.portfolioid;

select * from portfolio_cashflow;

SELECT * FROM portfolio_holdings a order by a.memberid, a.portfolioid, a.ticker;

SET SQL_SAFE_UPDATES = 0;
Commit;
update portfolio_holdings a, stock_universe b set a.name = b.name, a.short_name = b.short_name, a.asset_classid = b.asset_classid, a.subindustryid = b.subindustryid where a.ticker = b.ticker ;
update portfolio_holdings a set a.asset_classid = 101010, name = 'MOSL Cash', a.short_name = 'MOSL Cash' Where a.ticker = 'MOSL_CASH';
UPDATE portfolio_holdings a, nse_price_history b SET a.cmp = b.close_price WHERE  a.ticker = b.nse_ticker AND b.date = (select date_today from setup_dates);
UPDATE portfolio_holdings a, bse_price_history b SET a.cmp = b.close_price WHERE  a.ticker = b.bse_ticker AND b.date = (select date_today from setup_dates);
update portfolio_holdings a set total_cost = ((quantity * rate) + brokerage + tax), net_rate = round((total_cost/quantity),2), market_value = (cmp * quantity), net_profit = (market_value - total_cost), holding_period = ROUND((DATEDIFF((select date_today from setup_dates), buy_date) / 365.25), 2),absolute_return = round((market_value / total_cost) - 1, 2),annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 2);
commit;
