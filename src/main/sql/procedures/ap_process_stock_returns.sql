DROP PROCEDURE IF EXISTS ap_process_stock_returns;
CREATE PROCEDURE `ap_process_stock_returns`()
BEGIN

  DECLARE var_finished, var_count INT DEFAULT 0;
  DECLARE var_price_today, var_price_history DECIMAL(20,3);
  DECLARE var_date_today, var_date_last_trading_day, var_date_current_year_start,
		  var_date_1_w_before, var_date_2_w_before, var_date_1_mt_before,
          var_date_2_mt_before, var_date_3_mt_before, var_date_6_mt_before, var_date_9_mt_before,
		  var_date_1_yr_before, var_date_2_yr_before, var_date_3_yr_before, var_date_5_yr_before, var_date_10_yr_before DATE;
  DECLARE var_ticker VARCHAR(30);
  DECLARE var_is_nse_ticker VARCHAR(1);

  DECLARE stock_price_movement_cursor CURSOR FOR
    SELECT ticker
    FROM   stock_price_movement
    ORDER BY ticker;

  DECLARE CONTINUE HANDLER
  FOR NOT FOUND
    SET var_finished = 1;


  SET SQL_SAFE_UPDATES          = 0;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_stock_returns: Begin');

  SELECT date_today, date_last_trading_day
  INTO   var_date_today, var_date_last_trading_day
  FROM   setup_dates;

  INSERT INTO stock_price_movement (ticker)
  (SELECT ticker FROM stock_universe WHERE (is_bse500 = 1 OR is_nse500 = 1) AND
   ticker NOT IN (SELECT ticker FROM stock_price_movement));

  UPDATE stock_price_movement
  SET return_1D = 0, return_1W = 0, return_2W = 0, return_1M = 0, return_2M = 0,
      return_3M = 0, return_6M = 0, return_9M = 0, return_1Y = 0, return_2Y = 0,
      return_3Y = 0, return_5Y = 0, return_10Y = 0, return_YTD = 0;

  SET var_date_1_w_before = date_sub(var_date_today, INTERVAL 7 DAY);
  SET var_date_2_w_before = date_sub(var_date_today, INTERVAL 14 DAY);
  SET var_date_1_mt_before = date_sub(var_date_today, INTERVAL 1 MONTH);
  SET var_date_2_mt_before = date_sub(var_date_today, INTERVAL 2 MONTH);
  SET var_date_3_mt_before = date_sub(var_date_today, INTERVAL 3 MONTH);
  SET var_date_6_mt_before = date_sub(var_date_today, INTERVAL 6 MONTH);
  SET var_date_9_mt_before = date_sub(var_date_today, INTERVAL 9 MONTH);
  SET var_date_1_yr_before = date_sub(var_date_today, INTERVAL 12 MONTH);
  SET var_date_2_yr_before = date_sub(var_date_today, INTERVAL 24 MONTH);
  SET var_date_3_yr_before = date_sub(var_date_today, INTERVAL 36 MONTH);
  SET var_date_5_yr_before = date_sub(var_date_today, INTERVAL 60 MONTH);
  SET var_date_10_yr_before = date_sub(var_date_today, INTERVAL 120 MONTH);
  SET var_date_current_year_start = Makedate(Year(var_date_today), 1);

  OPEN stock_price_movement_cursor;

  SET var_finished              = 0;

 FETCH_DATA:
  LOOP
    FETCH stock_price_movement_cursor INTO var_ticker;

    IF var_finished = 1
    THEN
      LEAVE fetch_data;
    END IF;

	SELECT count(1)
	INTO var_count
	FROM nse_price_history
	WHERE nse_ticker = var_ticker;

    IF (var_count > 0) THEN
		SET var_is_nse_ticker = 'Y';
	ELSE
		SET var_is_nse_ticker = 'N';
    END IF;

    SET var_count = 0;

	SELECT latest_price
	INTO var_price_today
	FROM stock_universe
	WHERE ticker = var_ticker;

	IF (var_is_nse_ticker = 'Y') THEN
		SELECT close_price
		INTO var_price_history
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date = (SELECT MAX(date)
				FROM nse_price_history
				WHERE nse_ticker = var_ticker AND
				date <= var_date_last_trading_day);
	ELSE
		SELECT close_price
		INTO var_price_history
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date = (SELECT MAX(date)
				FROM bse_price_history
				WHERE bse_ticker = var_ticker AND
				date <= var_date_last_trading_day);
    END IF;

	-- INSERT INTO log_table
	-- VALUES      (now(), concat('ap_process_stock_returns: var_ticker-', var_ticker, '-IsNSE?-',var_is_nse_ticker, '-CMP-', var_price_today, '-var_price_history-',var_price_history ));

	IF var_price_history <> 0 THEN
		UPDATE stock_price_movement
		SET return_1D = ((var_price_today/var_price_history) - 1)*100
		WHERE ticker = var_ticker;
    END IF;

    -- 1 Week Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_1_w_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_1_w_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_1_w_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_1_w_before);
        END IF;

		UPDATE stock_price_movement
        SET return_1W = ((var_price_today/var_price_history) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- 2 Week Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_2_w_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_2_w_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_2_w_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_2_w_before);
        END IF;

		UPDATE stock_price_movement
        SET return_2W = ((var_price_today/var_price_history) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- 1 Month Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_1_mt_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_1_mt_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_1_mt_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_1_mt_before);
        END IF;

		UPDATE stock_price_movement
        SET return_1M = ((var_price_today/var_price_history) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- 2 Month Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_2_mt_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_2_mt_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_2_mt_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_2_mt_before);
        END IF;

		UPDATE stock_price_movement
        SET return_2M = ((var_price_today/var_price_history) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- 3 Month Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_3_mt_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_3_mt_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_3_mt_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_3_mt_before);
        END IF;

		UPDATE stock_price_movement
        SET return_3M = ((var_price_today/var_price_history) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- 6 Month Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_6_mt_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_6_mt_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_6_mt_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_6_mt_before);
        END IF;

		UPDATE stock_price_movement
        SET return_6M = ((var_price_today/var_price_history) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- 9 Month Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_9_mt_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_9_mt_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_9_mt_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_9_mt_before);
        END IF;

		UPDATE stock_price_movement
        SET return_9M = ((var_price_today/var_price_history) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- 1 Year Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_1_yr_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_1_yr_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_1_yr_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_1_yr_before);
        END IF;

		UPDATE stock_price_movement
        SET return_1Y = ((var_price_today/var_price_history) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- 2 Year Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_2_yr_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_2_yr_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_2_yr_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_2_yr_before);
        END IF;

		UPDATE stock_price_movement
        SET return_2Y = (pow((var_price_today/var_price_history), 0.5) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- 3 Year Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_3_yr_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_3_yr_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_3_yr_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_3_yr_before);
        END IF;

		UPDATE stock_price_movement
        SET return_3Y = (pow((var_price_today/var_price_history), (1/3)) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- 5 Year Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_5_yr_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_5_yr_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_5_yr_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_5_yr_before);
        END IF;

		UPDATE stock_price_movement
        SET return_5Y = (pow((var_price_today/var_price_history), 0.2) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- 10 Year Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_10_yr_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_10_yr_before;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_10_yr_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_10_yr_before);
        END IF;

		UPDATE stock_price_movement
        SET return_10Y = (pow((var_price_today/var_price_history), 0.1) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

    -- YTD Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_current_year_start;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_current_year_start;
    END IF;

	IF(var_count > 0) THEN
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date < var_date_current_year_start);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date < var_date_current_year_start);
        END IF;

		UPDATE stock_price_movement
        SET return_YTD = ((var_price_today/var_price_history) - 1)*100
        WHERE ticker = var_ticker;

    END IF;

  END LOOP fetch_data;

  CLOSE stock_price_movement_cursor;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_stock_returns: End');

  commit;

END