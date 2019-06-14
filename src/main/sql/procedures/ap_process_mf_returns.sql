DROP PROCEDURE IF EXISTS ap_process_mf_returns;
CREATE PROCEDURE ap_process_mf_returns()
BEGIN

  DECLARE var_finished, var_count, var_scheme_code INT DEFAULT 0;
  DECLARE var_nav_today, var_nav_current_year_start, var_nav_history DECIMAL(20,3);
  DECLARE var_date_today, var_date_current_year_start, var_date_1_yr_before,
		  var_date_3_yr_before, var_date_5_yr_before, var_date_10_yr_before DATE;

  DECLARE mutual_fund_stats_cursor CURSOR FOR
    SELECT scheme_code
    FROM   mutual_fund_stats
    ORDER BY scheme_code;

  DECLARE CONTINUE HANDLER
  FOR NOT FOUND
    SET var_finished = 1;


  SET SQL_SAFE_UPDATES          = 0;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_mf_returns: Begin');

  SELECT date_today
  INTO   var_date_today
  FROM   setup_dates;

  SET var_date_1_yr_before = date_sub(var_date_today, INTERVAL 12 MONTH);
  SET var_date_3_yr_before = date_sub(var_date_today, INTERVAL 36 MONTH);
  SET var_date_5_yr_before = date_sub(var_date_today, INTERVAL 60 MONTH);
  SET var_date_10_yr_before = date_sub(var_date_today, INTERVAL 72 MONTH); -- temp. setting to 6years
  SET var_date_current_year_start = Makedate(Year(var_date_today), 1);

  OPEN mutual_fund_stats_cursor;

  SET var_finished              = 0;

 FETCH_DATA:
  LOOP
    FETCH mutual_fund_stats_cursor INTO var_scheme_code;

    IF var_finished = 1
    THEN
      LEAVE fetch_data;
    END IF;

	SELECT nav
	INTO var_nav_today
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code AND
	date = (SELECT MAX(date)
			FROM mutual_fund_nav_history
			WHERE scheme_code = var_scheme_code AND
			date <= var_date_today);

	SELECT nav
	INTO var_nav_current_year_start
	FROM mutual_fund_nav_history
	WHERE scheme_code = var_scheme_code AND
	date = (SELECT MAX(date)
			FROM mutual_fund_nav_history
			WHERE scheme_code = var_scheme_code AND
			date < var_date_current_year_start);

	UPDATE mutual_fund_stats
	SET total_returns_y0 = ((var_nav_today/var_nav_current_year_start) - 1)*100
	WHERE scheme_code = var_scheme_code;

	SELECT COUNT(1)
    INTO var_count
    FROM mutual_fund_nav_history
    WHERE scheme_code = var_scheme_code AND
    date < var_date_1_yr_before;

	IF(var_count != 0) THEN
		SELECT nav
		INTO var_nav_history
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code AND
		date = (SELECT MAX(date)
				FROM mutual_fund_nav_history
				WHERE scheme_code = var_scheme_code AND
				date < var_date_1_yr_before);

		UPDATE mutual_fund_stats
        SET trailing_return_1yr = ((var_nav_today/var_nav_history) - 1)*100
        WHERE scheme_code = var_scheme_code;

		/* -- computing 2018 returns
        SELECT nav
		INTO var_nav_history
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code AND
		date = (SELECT MAX(date)
				FROM mutual_fund_nav_history
				WHERE scheme_code = var_scheme_code AND
				date < '2018-01-01');

		UPDATE mutual_fund_stats
        SET total_returns_y1 = ((var_nav_current_year_start/var_nav_history) - 1)*100
        WHERE scheme_code = var_scheme_code;
        */

    END IF;

	SELECT COUNT(1)
    INTO var_count
    FROM mutual_fund_nav_history
    WHERE scheme_code = var_scheme_code AND
    date < var_date_3_yr_before;

	IF(var_count != 0) THEN
		SELECT nav
		INTO var_nav_history
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code AND
		date = (SELECT MAX(date)
				FROM mutual_fund_nav_history
				WHERE scheme_code = var_scheme_code AND
				date < var_date_3_yr_before);

		UPDATE mutual_fund_stats
        SET trailing_return_3yr = ((var_nav_today/var_nav_history) - 1)*100
        WHERE scheme_code = var_scheme_code;
	END IF;

	SELECT COUNT(1)
    INTO var_count
    FROM mutual_fund_nav_history
    WHERE scheme_code = var_scheme_code AND
    date < var_date_5_yr_before;

	IF(var_count != 0) THEN
		SELECT nav
		INTO var_nav_history
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code AND
		date = (SELECT MAX(date)
				FROM mutual_fund_nav_history
				WHERE scheme_code = var_scheme_code AND
				date < var_date_5_yr_before);

		UPDATE mutual_fund_stats
        SET trailing_return_5yr = ((var_nav_today/var_nav_history) - 1)*100
        WHERE scheme_code = var_scheme_code;
	END IF;

	SELECT COUNT(1)
    INTO var_count
    FROM mutual_fund_nav_history
    WHERE scheme_code = var_scheme_code AND
    date < var_date_10_yr_before;

	IF(var_count != 0) THEN
		SELECT nav
		INTO var_nav_history
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code AND
		date = (SELECT MAX(date)
				FROM mutual_fund_nav_history
				WHERE scheme_code = var_scheme_code AND
				date < var_date_10_yr_before);

		UPDATE mutual_fund_stats
        SET trailing_return_10yr = ((var_nav_today/var_nav_history) - 1)*100
        WHERE scheme_code = var_scheme_code;
	END IF;

  END LOOP fetch_data;

  CLOSE mutual_fund_stats_cursor;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_mf_returns: End');

  commit;

END