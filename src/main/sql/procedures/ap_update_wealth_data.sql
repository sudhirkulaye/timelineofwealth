DROP PROCEDURE IF EXISTS ap_update_wealth_data;
CREATE PROCEDURE ap_update_wealth_data()
BEGIN

  DECLARE var_date_today DATE;

  SET SQL_SAFE_UPDATES          = 0;

  INSERT INTO log_table
  VALUES      (now(), 'ap_update_wealth_data: Begin');

  UPDATE setup_dates
  SET    date_last_trading_day            = (SELECT Max(date)
                                             FROM   nse_price_history
                                             WHERE  date < (SELECT Max(date) FROM nse_price_history)),
         date_today                       = (SELECT Max(date) FROM nse_price_history),
         date_start_current_month         = Date_sub(date_today, INTERVAL Dayofmonth(date_today) - 1 DAY),
         date_start_current_quarter       = Makedate(Year(date_today), 1) + INTERVAL Quarter(date_today) QUARTER - INTERVAL 1 QUARTER,
         date_start_current_fin_year      = CASE Quarter(date_today)
                                              WHEN 1 THEN Makedate(Year(date_today) - 1, 1) + INTERVAL 1 QUARTER
                                              ELSE Makedate(Year(date_today), 1) + INTERVAL 1 QUARTER
                                            END,
         date_start_1_quarter             = date_start_current_quarter,
         date_start_2_quarter             = date_start_current_quarter - INTERVAL 1 QUARTER,
         date_start_3_quarter             = date_start_current_quarter - INTERVAL 2 QUARTER,
         date_start_4_quarter             = date_start_current_quarter - INTERVAL 3 QUARTER,
         date_start_next_fin_year         = date_start_current_fin_year + INTERVAL 4 QUARTER,
         current_fin_year                 = CASE Quarter(date_today) WHEN 1 THEN Year(date_today) ELSE Year(date_today) + 1 END,
         current_quarter                  = CASE Quarter(date_today) WHEN 1 THEN 4 ELSE Quarter(date_today) - 1 END;

  SELECT date_today -- , date_last_trading_day, date_start_current_fin_year
  INTO   var_date_today -- , var_date_last_trading_day, var_date_start_current_fin_year
  FROM   setup_dates;

  -- Call process SIPs
  call ap_process_sips;
  -- end call process SIPs

  -- update CMP of FD
  UPDATE wealth_details
  SET    cmp = total_cost + (maturity_value - total_cost) * (DATEDIFF(var_date_today, buy_date) / DATEDIFF(maturity_date, buy_date))
  WHERE  maturity_date > var_date_today
  AND asset_classid in (201010, 202010, 203010);

  -- CMP of stocks
  UPDATE wealth_details a, nse_price_history b
  SET    a.cmp = b.close_price
  WHERE  a.ticker = b.nse_ticker AND b.date = var_date_today;

  UPDATE wealth_details a, bse_price_history b
  SET    a.cmp = b.close_price
  WHERE  a.ticker = b.bse_ticker AND b.date = var_date_today;

  UPDATE wealth_details a, mutual_fund_nav_history b
  SET    a.cmp = b.nav
  WHERE  a.ticker = b.scheme_code AND b.date = var_date_today;

  -- query to update CMP, market_vaue, net_profit & absolute_return
  UPDATE wealth_details
  SET    market_value = cmp * quantity,
         net_profit = market_value - total_cost,
         holding_period = ROUND((DATEDIFF(var_date_today, buy_date) / 365.25), 2),
         absolute_return = round((market_value / total_cost) - 1, 2),
         annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 2)
  WHERE asset_classid not in ('101010', '101020', '201010', '202010', '203010', '203020', '203050');

  UPDATE mutual_fund_universe a, mutual_fund_nav_history b
  SET a.latest_nav = b.nav
  WHERE a.scheme_code = b.scheme_code and b.date = var_date_today;

  UPDATE stock_universe a, nse_price_history b
  SET a.latest_price = b.close_price, a.date_latest_price = var_date_today
  WHERE a.ticker = b.nse_ticker
  AND b.date = var_date_today;

  UPDATE stock_universe a, bse_price_history b
  SET a.latest_price = b.close_price, a.date_latest_price = var_date_today
  WHERE a.ticker = b.bse_ticker
  AND b.date = var_date_today;

  DELETE FROM wealth_history
  WHERE  date = var_date_today;

  INSERT INTO wealth_history
    (SELECT   memberid, var_date_today, TRUNCATE(SUM(market_value), 2)
      FROM     wealth_details
      GROUP BY memberid);

  DELETE FROM wealth_asset_allocation_history
  WHERE  date = var_date_today;

  INSERT INTO wealth_asset_allocation_history
    SELECT   memberid, var_date_today, b.asset_class_group, round(sum(market_value), 2), 0.0
    FROM     wealth_details a, asset_classification b
    WHERE a.asset_classid = b.classid
    GROUP BY a.memberid, b.asset_class_group;

  INSERT INTO log_table
  VALUES      (now(), 'ap_update_wealth_data: End');

  commit;

END;
