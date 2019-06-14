DROP PROCEDURE IF EXISTS ap_process_stock_returns_history;
CREATE PROCEDURE ap_process_stock_returns_history()
BEGIN

  DECLARE var_finished, var_count INT DEFAULT 0;
  DECLARE var_price_today, var_price_history, var_52w_min, var_52w_max DECIMAL(20,3);
  DECLARE var_date_today, var_date_1_w_before, var_date_2_w_before, var_date_1_mt_before, var_date_2_mt_before DATE;
  DECLARE var_ticker VARCHAR(30);
  DECLARE var_is_nse_ticker VARCHAR(1);

  DECLARE nse_price_history_cursor CURSOR FOR
    SELECT DISTINCT date
    FROM   nse_price_history
    WHERE date > '2018-01-01'
    AND date <= '2019-05-06'
    ORDER BY date desc;

  DECLARE stock_price_movement_cursor CURSOR FOR
    SELECT ticker
    FROM   stock_price_movement
    -- WHERE ticker NOT in ('DENABANK','IL&FSTRANS','KPIT','TIFIN','VIJAYABANK','IDFCFIRSTB','500033', '500285','502865','505533','533033','AAVAS','ABCAPITAL')
    ORDER BY ticker;

   DECLARE CONTINUE HANDLER
    FOR NOT FOUND
    SET var_finished = 1;

  SET SQL_SAFE_UPDATES          = 0;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_stock_returns_history: Begin');

  OPEN nse_price_history_cursor;
  SET var_finished = 0;

FETCH_DATE: LOOP
	FETCH nse_price_history_cursor INTO var_date_today;

	  IF var_finished = 1
	  THEN
		  LEAVE fetch_date;
	  END IF;

	  DELETE FROM stock_price_movement_history WHERE date = var_date_today;

	  INSERT INTO stock_price_movement_history (date, ticker, return_1D, return_1W, return_2W, return_1M)
	  (SELECT var_date_today, ticker, 0, 0, 0, 0 FROM stock_price_movement );

	  SET var_date_1_w_before = date_sub(var_date_today, INTERVAL 7 DAY);
	  SET var_date_2_w_before = date_sub(var_date_today, INTERVAL 14 DAY);
	  SET var_date_1_mt_before = date_sub(var_date_today, INTERVAL 1 MONTH);
      SET var_date_2_mt_before = date_sub(var_date_today, INTERVAL 2 MONTH);

	OPEN stock_price_movement_cursor;
	-- SET var_finished              = 0;

 FETCH_DATA: LOOP
    FETCH stock_price_movement_cursor INTO var_ticker;

    IF var_finished = 1
    THEN
      LEAVE fetch_data;
    END IF;

	SELECT count(1)
	INTO var_count
	FROM stock_universe
	WHERE nse_code = var_ticker;

    IF (var_count > 0) THEN
		SET var_is_nse_ticker = 'Y';
	ELSE
		SET var_is_nse_ticker = 'N';
    END IF;

	IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date = var_date_today;
	ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date = var_date_today;
	END IF;

    IF (var_count > 0 AND var_is_nse_ticker = 'Y') THEN
		SELECT  close_price
		INTO var_price_today
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date = var_date_today;

		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date < var_date_today AND
        date > var_date_1_w_before;
    END IF;
    IF (var_count > 0 AND var_is_nse_ticker = 'N') THEN
		SELECT  close_price
		INTO var_price_today
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date = var_date_today;

		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date < var_date_today AND
        date > var_date_1_w_before;
    END IF;

	IF (var_count > 0 AND var_is_nse_ticker = 'Y') THEN
		SELECT close_price
		INTO var_price_history
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date = (SELECT MAX(date)
				FROM nse_price_history
				WHERE nse_ticker = var_ticker AND
				date < var_date_today AND
                date > var_date_1_w_before);
	END IF;
    IF (var_count > 0 AND var_is_nse_ticker = 'N') THEN
		SELECT close_price
		INTO var_price_history
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date = (SELECT MAX(date)
				FROM bse_price_history
				WHERE bse_ticker = var_ticker AND
				date < var_date_today AND
                date > var_date_1_w_before);
	END IF;
	IF (var_ticker = 'TCS') THEN
	  INSERT INTO log_table
	  VALUES      (now(), concat(var_date_today, '- TCS var_price_history -',var_price_history));
	END IF;
	IF var_price_history <> 0 AND var_price_today <> 0 THEN
		UPDATE stock_price_movement_history
		SET return_1D = (round((var_price_today/var_price_history),3) - 1)*100
		WHERE ticker = var_ticker
		AND date = var_date_today;
	END IF;


    -- 1 Week Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date <= var_date_1_w_before AND
        date > var_date_2_w_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date <= var_date_1_w_before AND
        date > var_date_2_w_before;
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
					date <= var_date_1_w_before AND
                    date > var_date_2_w_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date <= var_date_1_w_before AND
                    date > var_date_2_w_before);
        END IF;

		UPDATE stock_price_movement_history
        SET return_1W = (round((var_price_today/var_price_history), 3) - 1)*100
        WHERE ticker = var_ticker
        AND date = var_date_today;

    END IF;

    -- 2 Week Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date <= var_date_2_w_before AND
        date > var_date_1_mt_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date <= var_date_2_w_before AND
        date > var_date_1_mt_before;
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
					date <= var_date_2_w_before AND
                    date > var_date_1_mt_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date <= var_date_2_w_before AND
                    date > var_date_1_mt_before);
        END IF;

		UPDATE stock_price_movement_history
        SET return_2W = (round((var_price_today/var_price_history), 3) - 1)*100
        WHERE ticker = var_ticker
        AND date = var_date_today;

    END IF;

    -- 1 Month Change
	SET var_count = 0;
    IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date <= var_date_1_mt_before AND
        date > var_date_2_mt_before;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date <= var_date_1_mt_before AND
        date > var_date_2_mt_before;
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
					date <= var_date_1_mt_before AND
                    date > var_date_2_mt_before);
		ELSE
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date <= var_date_1_mt_before AND
                    date > var_date_2_mt_before);
        END IF;

		UPDATE stock_price_movement_history
        SET return_1M = (round((var_price_today/var_price_history), 3) - 1)*100
        WHERE ticker = var_ticker
        AND date = var_date_today;

    END IF;
  END LOOP fetch_data;
  CLOSE stock_price_movement_cursor;
  SET var_finished = 0;
END LOOP fetch_date;

  close nse_price_history_cursor;
  INSERT INTO log_table
  VALUES      (now(), 'ap_process_stock_returns_history: End');

  commit;

END