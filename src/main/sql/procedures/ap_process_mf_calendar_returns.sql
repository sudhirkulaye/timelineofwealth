DROP PROCEDURE IF EXISTS ap_process_mf_calendar_returns;
DELIMITER $$
CREATE PROCEDURE ap_process_mf_calendar_returns()
BEGIN

  DECLARE var_finished, var_count, var_scheme_code INT DEFAULT 0;
  DECLARE var_nav_end_date, var_nav_start_date DECIMAL(20,3);
  DECLARE var_date_today, var_end_date, var_start_date DATE;

  DECLARE mutual_fund_stats_cursor CURSOR FOR
    SELECT scheme_code
    FROM   mutual_fund_stats
    ORDER BY scheme_code;

  DECLARE CONTINUE HANDLER
  FOR NOT FOUND
    SET var_finished = 1;

  SET SQL_SAFE_UPDATES          = 0;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_mf_calendar_returns: Begin');

  SELECT date_today
  INTO   var_date_today
  FROM   setup_dates;

  OPEN mutual_fund_stats_cursor;

  SET var_finished = 0;

  FETCH_DATA: LOOP

    FETCH mutual_fund_stats_cursor INTO var_scheme_code;
    IF var_finished = 1 THEN
      LEAVE FETCH_DATA;
    END IF;

	/*INSERT INTO log_table
	VALUES      (now(), concat('ap_process_mf_calendar_returns: var_scheme_code - ', var_scheme_code));*/

    -- Calculate total_returns_y0 (YTD returns)
    SELECT MAX(date) INTO var_end_date
    FROM mutual_fund_nav_history
    WHERE scheme_code = var_scheme_code
    AND date <= var_date_today;

    SELECT MAX(date) INTO var_start_date
    FROM mutual_fund_nav_history
    WHERE scheme_code = var_scheme_code
    AND date < DATE_SUB(var_date_today, INTERVAL 1 YEAR);

    IF(var_end_date IS NOT NULL AND var_start_date IS NOT NULL) THEN
		SELECT nav INTO var_nav_end_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_end_date;

		SELECT nav INTO var_nav_start_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_start_date;

		IF var_nav_end_date IS NOT NULL AND var_nav_start_date IS NOT NULL THEN
		  UPDATE mutual_fund_stats
		  SET total_returns_y0 = ((var_nav_end_date / var_nav_start_date) - 1)*100
		  WHERE scheme_code = var_scheme_code;
		ELSE
		  UPDATE mutual_fund_stats
		  SET total_returns_y0 = 0
		  WHERE scheme_code = var_scheme_code;
		END IF;
	ELSE
		UPDATE mutual_fund_stats
		SET total_returns_y0 = 0
		WHERE scheme_code = var_scheme_code;
    END IF;

	-- Calculate total_returns_y1 (returns on the calendar year before the current year)
	SELECT MAX(date) INTO var_end_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= var_date_today
	AND YEAR(date) < YEAR(var_date_today);

	SELECT MAX(date) INTO var_start_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 1 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 1;

	IF(var_end_date IS NOT NULL AND var_start_date IS NOT NULL) THEN
		SELECT nav INTO var_nav_end_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_end_date;

		SELECT nav INTO var_nav_start_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_start_date;

		IF var_nav_end_date IS NOT NULL AND var_nav_start_date IS NOT NULL THEN
		  UPDATE mutual_fund_stats
		  SET total_returns_y1 = ((var_nav_end_date / var_nav_start_date) - 1)*100
		  WHERE scheme_code = var_scheme_code;
		ELSE
		  UPDATE mutual_fund_stats
		  SET total_returns_y1 = 0
		  WHERE scheme_code = var_scheme_code;
		END IF;
	ELSE
		UPDATE mutual_fund_stats
		SET total_returns_y1 = 0
		WHERE scheme_code = var_scheme_code;
    END IF;

	-- Calculate total_returns_y2 (returns on the calendar year two years before the current year)
	SELECT MAX(date) INTO var_end_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 1 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 1;

	SELECT MAX(date) INTO var_start_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 2 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 2;

	IF(var_end_date IS NOT NULL AND var_start_date IS NOT NULL) THEN
		SELECT nav INTO var_nav_end_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_end_date;

		SELECT nav INTO var_nav_start_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_start_date;

		IF var_nav_end_date IS NOT NULL AND var_nav_start_date IS NOT NULL THEN
		  UPDATE mutual_fund_stats
		  SET total_returns_y2 = ((var_nav_end_date / var_nav_start_date) - 1)*100
		  WHERE scheme_code = var_scheme_code;
		ELSE
		  UPDATE mutual_fund_stats
		  SET total_returns_y2 = 0
		  WHERE scheme_code = var_scheme_code;
		END IF;
	ELSE
		UPDATE mutual_fund_stats
		SET total_returns_y2 = 0
		WHERE scheme_code = var_scheme_code;
    END IF;

	-- Calculate total_returns_y3 (returns on the calendar year three years before the current year)
	SELECT MAX(date) INTO var_end_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 2 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 2;

	SELECT MAX(date) INTO var_start_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 3 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 3;

	IF(var_end_date IS NOT NULL AND var_start_date IS NOT NULL) THEN
		SELECT nav INTO var_nav_end_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_end_date;

		SELECT nav INTO var_nav_start_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_start_date;

		IF var_nav_end_date IS NOT NULL AND var_nav_start_date IS NOT NULL THEN
		  UPDATE mutual_fund_stats
		  SET total_returns_y3 = ((var_nav_end_date / var_nav_start_date) - 1)*100
		  WHERE scheme_code = var_scheme_code;
		ELSE
		  UPDATE mutual_fund_stats
		  SET total_returns_y3 = 0
		  WHERE scheme_code = var_scheme_code;
		END IF;
	ELSE
		UPDATE mutual_fund_stats
		SET total_returns_y3 = 0
		WHERE scheme_code = var_scheme_code;
    END IF;

	-- Calculate total_returns_y4 (returns on the calendar year three years before the current year)
	SELECT MAX(date) INTO var_end_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 3 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 3;

	SELECT MAX(date) INTO var_start_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 4 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 4;

	IF(var_end_date IS NOT NULL AND var_start_date IS NOT NULL) THEN
		SELECT nav INTO var_nav_end_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_end_date;

		SELECT nav INTO var_nav_start_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_start_date;

		IF var_nav_end_date IS NOT NULL AND var_nav_start_date IS NOT NULL THEN
		  UPDATE mutual_fund_stats
		  SET total_returns_y4 = ((var_nav_end_date / var_nav_start_date) - 1)*100
		  WHERE scheme_code = var_scheme_code;
		ELSE
		  UPDATE mutual_fund_stats
		  SET total_returns_y4 = 0
		  WHERE scheme_code = var_scheme_code;
		END IF;
	ELSE
		UPDATE mutual_fund_stats
		SET total_returns_y4 = 0
		WHERE scheme_code = var_scheme_code;
    END IF;

	-- Calculate total_returns_y5 (returns on the calendar year three years before the current year)
	SELECT MAX(date) INTO var_end_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 4 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 4;

	SELECT MAX(date) INTO var_start_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 5 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 5;

	IF(var_end_date IS NOT NULL AND var_start_date IS NOT NULL) THEN
		SELECT nav INTO var_nav_end_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_end_date;

		SELECT nav INTO var_nav_start_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_start_date;

		IF var_nav_end_date IS NOT NULL AND var_nav_start_date IS NOT NULL THEN
		  UPDATE mutual_fund_stats
		  SET total_returns_y5 = ((var_nav_end_date / var_nav_start_date) - 1)*100
		  WHERE scheme_code = var_scheme_code;
		ELSE
		  UPDATE mutual_fund_stats
		  SET total_returns_y5 = 0
		  WHERE scheme_code = var_scheme_code;
		END IF;
	ELSE
		UPDATE mutual_fund_stats
		SET total_returns_y5 = 0
		WHERE scheme_code = var_scheme_code;
    END IF;

	-- Calculate total_returns_y6 (returns on the calendar year three years before the current year)
	SELECT MAX(date) INTO var_end_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 5 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 5;

	SELECT MAX(date) INTO var_start_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 6 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 6;

	IF(var_end_date IS NOT NULL AND var_start_date IS NOT NULL) THEN
		SELECT nav INTO var_nav_end_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_end_date;

		SELECT nav INTO var_nav_start_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_start_date;

		IF var_nav_end_date IS NOT NULL AND var_nav_start_date IS NOT NULL THEN
		  UPDATE mutual_fund_stats
		  SET total_returns_y6 = ((var_nav_end_date / var_nav_start_date) - 1)*100
		  WHERE scheme_code = var_scheme_code;
		ELSE
		  UPDATE mutual_fund_stats
		  SET total_returns_y6 = 0
		  WHERE scheme_code = var_scheme_code;
		END IF;
	ELSE
		UPDATE mutual_fund_stats
		SET total_returns_y6 = 0
		WHERE scheme_code = var_scheme_code;
    END IF;

	-- Calculate total_returns_y7 (returns on the calendar year three years before the current year)
	SELECT MAX(date) INTO var_end_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 6 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 6;

	SELECT MAX(date) INTO var_start_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 7 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 7;

	IF(var_end_date IS NOT NULL AND var_start_date IS NOT NULL) THEN
		SELECT nav INTO var_nav_end_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_end_date;

		SELECT nav INTO var_nav_start_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_start_date;

		IF var_nav_end_date IS NOT NULL AND var_nav_start_date IS NOT NULL THEN
		  UPDATE mutual_fund_stats
		  SET total_returns_y7 = ((var_nav_end_date / var_nav_start_date) - 1)*100
		  WHERE scheme_code = var_scheme_code;
		ELSE
		  UPDATE mutual_fund_stats
		  SET total_returns_y7 = 0
		  WHERE scheme_code = var_scheme_code;
		END IF;
	ELSE
		UPDATE mutual_fund_stats
		SET total_returns_y7 = 0
		WHERE scheme_code = var_scheme_code;
    END IF;

	-- Calculate total_returns_y8 (returns on the calendar year three years before the current year)
	SELECT MAX(date) INTO var_end_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 7 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 7;

	SELECT MAX(date) INTO var_start_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 8 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 8;

	IF(var_end_date IS NOT NULL AND var_start_date IS NOT NULL) THEN
		SELECT nav INTO var_nav_end_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_end_date;

		SELECT nav INTO var_nav_start_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_start_date;

		IF var_nav_end_date IS NOT NULL AND var_nav_start_date IS NOT NULL THEN
		  UPDATE mutual_fund_stats
		  SET total_returns_y8 = ((var_nav_end_date / var_nav_start_date) - 1)*100
		  WHERE scheme_code = var_scheme_code;
		ELSE
		  UPDATE mutual_fund_stats
		  SET total_returns_y8 = 0
		  WHERE scheme_code = var_scheme_code;
		END IF;
	ELSE
		UPDATE mutual_fund_stats
		SET total_returns_y8 = 0
		WHERE scheme_code = var_scheme_code;
    END IF;

	-- Calculate total_returns_y9 (returns on the calendar year three years before the current year)
	SELECT MAX(date) INTO var_end_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 8 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 8;

	SELECT MAX(date) INTO var_start_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 9 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 9;

	IF(var_end_date IS NOT NULL AND var_start_date IS NOT NULL) THEN
		SELECT nav INTO var_nav_end_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_end_date;

		SELECT nav INTO var_nav_start_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_start_date;

		IF var_nav_end_date IS NOT NULL AND var_nav_start_date IS NOT NULL THEN
		  UPDATE mutual_fund_stats
		  SET total_returns_y9 = ((var_nav_end_date / var_nav_start_date) - 1)*100
		  WHERE scheme_code = var_scheme_code;
		ELSE
		  UPDATE mutual_fund_stats
		  SET total_returns_y9 = 0
		  WHERE scheme_code = var_scheme_code;
		END IF;
	ELSE
		UPDATE mutual_fund_stats
		SET total_returns_y9 = 0
		WHERE scheme_code = var_scheme_code;
    END IF;

	-- Calculate total_returns_y10 (returns on the calendar year three years before the current year)
	SELECT MAX(date) INTO var_end_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 9 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 9;

	SELECT MAX(date) INTO var_start_date
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code
	AND date <= DATE_SUB(var_date_today, INTERVAL 10 YEAR)
	AND YEAR(date) < YEAR(var_date_today) - 10;

	IF(var_end_date IS NOT NULL AND var_start_date IS NOT NULL) THEN
		SELECT nav INTO var_nav_end_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_end_date;

		SELECT nav INTO var_nav_start_date
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_start_date;

		IF var_nav_end_date IS NOT NULL AND var_nav_start_date IS NOT NULL THEN
		  UPDATE mutual_fund_stats
		  SET total_returns_y10 = ((var_nav_end_date / var_nav_start_date) - 1)*100
		  WHERE scheme_code = var_scheme_code;
		ELSE
		  UPDATE mutual_fund_stats
		  SET total_returns_y10 = 0
		  WHERE scheme_code = var_scheme_code;
		END IF;
	ELSE
		UPDATE mutual_fund_stats
		SET total_returns_y10 = 0
		WHERE scheme_code = var_scheme_code;
    END IF;

  END LOOP FETCH_DATA;

  CLOSE mutual_fund_stats_cursor;

  INSERT INTO log_table
  VALUES (now(), 'ap_process_mf_calendar_returns: End');

  COMMIT;


END$$
DELIMITER ;
