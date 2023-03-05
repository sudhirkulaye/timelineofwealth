DROP PROCEDURE IF EXISTS ap_process_stat_calculation;
CREATE PROCEDURE ap_process_stat_calculation()
BEGIN

  -- DECLARE var_ticker, var_ticker_b VARCHAR(30);

  DECLARE var_date_today, var_date_last_trading_day, var_date_start_current_fin_year,
  var_date_start_week1, var_date_start_week2, var_date_month_before, var_date_quarter_before,
  var_date_half_year_before, var_date_year_before, var_date_3years_before,
  var_date_5years_before, var_date_10years_before DATE;

  DECLARE var_finished, var_count INT DEFAULT 0;

  SET SQL_SAFE_UPDATES = 0;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_stat_calculation: Begin');

  SELECT date_today, date_last_trading_day, date_start_current_fin_year
  INTO   var_date_today, var_date_last_trading_day, var_date_start_current_fin_year
  FROM   setup_dates;


  SET var_date_start_week1      = date_sub(var_date_today, INTERVAL 7 DAY);
  SET var_date_start_week2      = date_sub(var_date_today, INTERVAL 14 DAY);
  SET var_date_month_before     = date_sub(var_date_today, INTERVAL 1 MONTH);
  SET var_date_quarter_before   = date_sub(var_date_today, INTERVAL 3 MONTH);
  SET var_date_half_year_before = date_sub(var_date_today, INTERVAL 6 MONTH);
  SET var_date_year_before      = date_sub(var_date_today, INTERVAL 12 MONTH);
  SET var_date_3years_before    = date_sub(var_date_today, INTERVAL 36 MONTH);
  SET var_date_5years_before    = date_sub(var_date_today, INTERVAL 60 MONTH);
  SET var_date_10years_before    = date_sub(var_date_today, INTERVAL 120 MONTH);

  -- set market cap ranks (NOTE: NO need now daily_data_b is now not available)
  -- CALL ap_set_market_cap_rank(var_date_today, var_date_last_trading_day);
  -- Update rank and marketcap data in stock universe
  UPDATE stock_universe a, daily_data_s b
  SET a.marketcap = b.market_cap, a.marketcap_rank = rank, a.pe_ttm = b.pe_ttm
  WHERE a.ticker5 = b.name
  AND b.date = var_date_today;

  -- Find any new Stock Entry
  -- Same TICKERs but different ISIN_CODES, update to new ISIN_CODE, no need to update wealth_details or portfolio_details
  UPDATE stock_universe a, nse_price_history b
  SET a.listing_date = var_date_today, a.isin_code = b.isin_code
  WHERE b.date = var_date_today
  AND a.ticker = b.nse_ticker
  AND a.isin_code <> b.isin_code
  AND b.isin_code <> '';

  -- Same ISINN but different TICKERS, update to new TICKER, need to update wealth_details or portfolio_details
  UPDATE stock_universe a, nse_price_history b
  SET a.ticker_old = a.ticker, a.listing_date = var_date_today, a.ticker = b.nse_ticker
  WHERE b.date = var_date_today
  AND a.isin_code = b.isin_code
  AND a.ticker <> b.nse_ticker
  AND b.isin_code <> '';

  -- update wealth_details
  UPDATE wealth_details a, stock_universe b
  SET a.ticker = b.ticker
  WHERE a.ticker = b.ticker_old
  AND b.listing_date = var_date_today;

  INSERT INTO stock_universe
  ( SELECT nse_ticker, '','','','','', nse_ticker, '', isin_code, nse_ticker, nse_ticker,
      CASE when nse_ticker like '%GOLD%' then '502010' when nse_ticker like '%NIF%' then '401010'
           when nse_ticker like '%JUNIOR%' then '401030' when nse_ticker like '%ETF%' then '401010' else '406040' END,
      '', 0, close_price, date, 0,0,0,0,0,0,0,0,0,0,0,0,0, '', date
      FROM nse_price_history a
      WHERE date = var_date_today
      AND a.series = 'EQ'
      AND a.nse_ticker NOT IN (SELECT ticker FROM stock_universe)
      AND a.isin_code <> '' -- (since isin_code is unique so cannot be null)
  );

  -- Log Stocks with Splits/Bonus Probability
  DELETE FROM stock_split_probability WHERE date = var_date_today;
  INSERT INTO stock_split_probability (
    SELECT nse_ticker, var_date_today, close_price, previous_close_price, (close_price/previous_close_price)-1, 'NO', ''
    FROm nse_price_history a
    WHERE date = var_date_today
    AND (close_price/previous_close_price)-1 < -0.3
    AND close_price > 5
  );

  -- Compute Mutual Fund Stats
  call ap_process_mf_returns();

  -- Compute Stock Pirce Returns
  call ap_process_stock_returns();

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_stat_calculation: End');

  commit;
END