DROP PROCEDURE IF EXISTS ap_process_portfolio_returns;
CREATE PROCEDURE ap_process_portfolio_returns( /*
    IN pi_memberid   INT,
    IN pi_portfolioid    INT*/
)
BEGIN

  DECLARE var_memberid, var_portfolioid, var_curr_year, var_min_year, var_returns_year, var_min_month, var_month INT;
  DECLARE var_cashflow, var_value_last, var_value, var_return_temp Decimal(20,4) DEFAULT 0.0000;
  DECLARE var_monthly_return_last, var_monthly_return, var_returns_twrr_since_inception, var_since_inception_holding_period decimal(20,4) DEFAULT 1.0000;
  DECLARE var_finished, var_count INT DEFAULT 0;
  DECLARE var_date_today, var_date_start_current_month, var_date_start_current_quarter,
  var_date_start_current_fin_year, var_min_date, var_date_1_yr_before, var_date_2_yr_before,
  var_date_3_yr_before, var_date_5_yr_before, var_date_10_yr_before, var_date_3_mt_before, var_date_6_mt_before DATE;
  DECLARE var_out_irr  decimal(20,4) DEFAULT 1.0000;

  DECLARE portfolio_cursor CURSOR FOR
  SELECT memberid, portfolioid
  FROM  portfolio
  WHERE status = 'Active'
  -- WHERE memberid = pi_memberid AND portfolioid = pi_portfolioid
  ORDER BY memberid, portfolioid;

  DECLARE portfolio_returns_calculation_support_cursor CURSOR FOR
  SELECT cashflow, value
  FROM  portfolio_returns_calculation_support
  WHERE memberid = var_memberid AND
  portfolioid = var_portfolioid AND
  year(date) = var_returns_year AND
  month(date) = var_month AND
  date != (SELECT min(date)
			FROM portfolio_returns_calculation_support
			WHERE memberid = var_memberid AND
			portfolioid = var_portfolioid)
  ORDER BY memberid, portfolioid, date;

  DECLARE CONTINUE HANDLER
  FOR NOT FOUND
    SET var_finished = 1;

  SET SQL_SAFE_UPDATES = 0;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_portfolio_returns: Begin');

  SELECT date_today, YEAR(date_today), date_start_current_month, date_start_current_quarter, date_start_current_fin_year
  INTO   var_date_today, var_curr_year, var_date_start_current_month, var_date_start_current_quarter,
         var_date_start_current_fin_year
  FROM   setup_dates;

  SET var_date_3_mt_before = date_sub(var_date_today, INTERVAL 3 MONTH);
  SET var_date_6_mt_before = date_sub(var_date_today, INTERVAL 6 MONTH);
  SET var_date_1_yr_before = date_sub(var_date_today, INTERVAL 12 MONTH);
  SET var_date_2_yr_before = date_sub(var_date_today, INTERVAL 24 MONTH);
  SET var_date_3_yr_before = date_sub(var_date_today, INTERVAL 36 MONTH);
  SET var_date_5_yr_before = date_sub(var_date_today, INTERVAL 60 MONTH);
  SET var_date_10_yr_before = date_sub(var_date_today, INTERVAL 120 MONTH);

  DELETE FROM portfolio_returns_calculation_support;

  /*WHERE     memberid = pi_memberid
        AND portfolioid = pi_portfolioid;*/

  DELETE FROM portfolio_twrr_monthly;
  /*WHERE     memberid = pi_memberid
        AND portfolioid = pi_portfolioid;*/

  -- DELETE FROM portfolio_irr_summary;
  /*WHERE     memberid = pi_memberid
        AND portfolioid = pi_portfolioid;*/

  DELETE FROM portfolio_twrr_summary;
  /*WHERE     memberid = pi_memberid
        AND portfolioid = pi_portfolioid;*/

  INSERT INTO portfolio_twrr_summary
    SELECT memberid, portfolioid, 0, var_date_today, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	FROM   portfolio b;
     /* WHERE  memberid NOT IN (SELECT memberid FROM portfolio_twrr_summary)
      AND portfolioid NOT IN (SELECT portfolioid FROM portfolio_twrr_summary);
      AND portfolio_benchmarkid NOT IN (SELECT benchmarkid FROM portfolio_twrr_summary);*/

  OPEN portfolio_cursor;

  SET var_finished     = 0;

 FETCH_DATA:
  LOOP
    FETCH portfolio_cursor INTO var_memberid, var_portfolioid;

    IF var_finished = 1
    THEN
      LEAVE fetch_data;
    END IF;

    -- Insert cashflow data
    INSERT INTO portfolio_returns_calculation_support
      SELECT   b.memberid, b.portfolioid, b.date date, b.cashflow cashflow, c.value, b.description
      FROM     portfolio_cashflow b
               LEFT JOIN portfolio_value_history c
                 ON b.memberid = c.memberid AND b.portfolioid = c.portfolioid AND b.date = c.date
      WHERE    b.memberid = var_memberid AND b.portfolioid = var_portfolioid
      ORDER BY b.memberid, b.portfolioid, date;

    -- insert end of month data exclude if cash flow is also on end of month
    INSERT INTO portfolio_returns_calculation_support
      SELECT memberid, portfolioid, date date, 0 cashflow, value, 'EOM Value'
      FROM   portfolio_value_history a
      WHERE  a.memberid = var_memberid
      AND a.portfolioid = var_portfolioid
      AND a.date NOT IN
					   (SELECT date
					   FROM   portfolio_cashflow g
					   WHERE  g.memberid = var_memberid
					   AND g.portfolioid = var_portfolioid) -- AND a.date NOT IN (SELECT date_today FROM setup_dates)
       AND date IN (SELECT   MAX(date)
					  FROM     portfolio_value_history h
					  WHERE    h.memberid = var_memberid
					  AND h.portfolioid = var_portfolioid
					  GROUP BY YEAR(date),
					  MONTH (date));

    -- DELETE FROM portfolio_twrr_summary WHERE benchmarkid = 0;
    -- DELETE FROM portfolio_irr_summary WHERE benchmarkid = 0;

    -- INSERT INTO portfolio_twrr_summary
    --  SELECT memberid, portfolioid, 0, var_date_today, 0.00001, 0, 0, 0, 0, 0
    --  FROM   portfolio b;
     /* WHERE  memberid NOT IN (SELECT memberid FROM portfolio_twrr_summary)
      AND portfolioid NOT IN (SELECT portfolioid FROM portfolio_twrr_summary);
      AND portfolio_benchmarkid NOT IN (SELECT benchmarkid FROM portfolio_twrr_summary);*/

    -- INSERT INTO portfolio_irr_summary
    --  SELECT memberid, portfolioid, 0, var_date_today, 0, 0, 0, 0, 0, 0
    --  FROM   portfolio b;
	/*  WHERE  memberid NOT IN (SELECT memberid FROM portfolio_irr_summary)
      AND portfolioid NOT IN (SELECT portfolioid FROM portfolio_irr_summary);
     -- AND portfolio_benchmarkid NOT IN (SELECT benchmarkid FROM portfolio_irr_summary); */

    SELECT MIN(YEAR(date))
    INTO   var_min_year
    FROM   portfolio_returns_calculation_support
    WHERE  memberid = var_memberid AND portfolioid = var_portfolioid;

    SELECT MIN(MONTH(date))
    INTO   var_min_month
    FROM   portfolio_returns_calculation_support
    WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND YEAR(date) = var_min_year;

    SELECT cashflow
    INTO   var_cashflow
    FROM   portfolio_returns_calculation_support
    WHERE  memberid = var_memberid
    AND portfolioid = var_portfolioid
    AND date =
             (SELECT min(date)
              FROM   portfolio_returns_calculation_support
              WHERE  memberid = var_memberid
              AND portfolioid = var_portfolioid);

    SET var_value_last = abs(var_cashflow);
    SET var_monthly_return_last           = 1.0000;

    SET var_returns_year                  = var_min_year;

    WHILE var_returns_year <= var_curr_year
    DO
      IF (var_returns_year = var_min_year)
      THEN
        SET var_month = var_min_month;
      ELSE
        SET var_month = 1;
      END IF;

      -- INSERT INTO log_table values (now(), concat('In ap_process_historical_returns, 1: ',var_memberid, '-',var_portfolioid,'-',var_returns_year));

      INSERT INTO portfolio_twrr_monthly
      VALUES      (var_memberid, var_portfolioid, var_returns_year, 0, 0, 0, 0, 0, 0, 0,
                   0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);

      -- INSERT INTO log_table values (now(), concat('In ap_process_historical_returns, 2: ',var_memberid, '-',var_portfolioid,'-',var_returns_year,'-',var_month));

      WHILE var_month <= 12
      DO
        OPEN portfolio_returns_calculation_support_cursor;

        SET var_finished            = 0;

       FETCH_SUPPORT_DATA:
        LOOP
          FETCH portfolio_returns_calculation_support_cursor INTO var_cashflow, var_value;

          IF var_finished = 1
          THEN
            LEAVE FETCH_SUPPORT_DATA;
          END IF;

          SET var_monthly_return                = ROUND((((var_value + var_cashflow) / var_value_last) * var_monthly_return_last), 5);

          -- INSERT INTO log_table values (now(), concat('In ap_process_historical_returns, 3: ',var_monthly_return));

          SET var_monthly_return_last           = var_monthly_return;
          SET var_value_last = var_value;
        END LOOP FETCH_SUPPORT_DATA;

        CLOSE portfolio_returns_calculation_support_cursor;

        CASE
          WHEN var_month = 1
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_jan = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
          WHEN var_month = 2
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_feb = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
          WHEN var_month = 3
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_mar = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
          WHEN var_month = 4
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_apr = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
          WHEN var_month = 5
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_may = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
          WHEN var_month = 6
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_jun = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
          WHEN var_month = 7
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_jul = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
          WHEN var_month = 8
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_aug = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
          WHEN var_month = 9
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_sep = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
          WHEN var_month = 10
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_oct = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
          WHEN var_month = 11
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_nov = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
          WHEN var_month = 12
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_dec = var_monthly_return_last - 1
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last - 1
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmarkid = 0;
            END IF;
        END CASE;

        SET var_monthly_return_last = 1.0000;
        SET var_month               = var_month + 1;
      END WHILE;

      SET var_returns_year = var_returns_year + 1;
    END WHILE;

    SET var_finished                      = 0;
  END LOOP fetch_data;

  CLOSE portfolio_cursor;

  UPDATE portfolio_twrr_monthly
  SET    returns_mar_ending_quarter = ((returns_jan + 1) * (returns_feb + 1) * (returns_mar + 1)) - 1;

  UPDATE portfolio_twrr_monthly
  SET    returns_jun_ending_quarter = ((returns_apr + 1) * (returns_may + 1) * (returns_jun + 1)) - 1;

  UPDATE portfolio_twrr_monthly
  SET    returns_sep_ending_quarter = ((returns_jul + 1) * (returns_aug + 1) * (returns_sep + 1)) - 1;

  UPDATE portfolio_twrr_monthly
  SET    returns_dec_ending_quarter = ((returns_oct + 1) * (returns_nov + 1) * (returns_dec + 1)) - 1;

  UPDATE portfolio_twrr_monthly a
  SET    a.returns_calendar_year = ((returns_mar_ending_quarter + 1) * (returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1;

  UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
  SET    returns_twrr_ytd = b.returns_calendar_year
  WHERE  a.memberid = b.memberid
  AND a.portfolioid = b.portfolioid
  AND a.benchmarkid = 0
  AND b.returns_year = var_curr_year;

  CASE
    WHEN (MONTH(var_date_today) = 1) -- Jan month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_mar_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = ((returns_twrr_since_current_quarter + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = ((returns_twrr_since_current_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = ((returns_twrr_since_current_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = (var_curr_year - 1);


      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 3);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 5);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 9);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 2) -- Feb month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_mar_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = ((returns_twrr_since_current_quarter + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = ((returns_twrr_since_current_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = ((returns_twrr_since_current_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 3);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 5);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 9);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 3) -- Mar month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_mar_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = returns_twrr_since_current_quarter
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = ((returns_twrr_since_current_quarter + 1)*(returns_dec_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = ((returns_twrr_since_current_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 3);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 5);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 9);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 4) -- Apr month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_jun_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = ((1 + returns_twrr_since_current_quarter)*(returns_mar + 1)*(returns_feb + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = ((returns_twrr_ytd + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)*(returns_may + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_may + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_may + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 3);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_may + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 5);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 9);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_may + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 5) -- May month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_jun_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = ((1 + returns_twrr_since_current_quarter)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = ((returns_twrr_ytd + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 3);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 5);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year <= (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year <= (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year <= (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year <= (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year <= (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year <= (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year <= (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year <= (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year <= (var_curr_year - 9);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 6) -- Jun month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_jun_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = returns_twrr_since_current_quarter
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = returns_twrr_ytd
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 3);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 5);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 9);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 7) -- Jul month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_sep_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = ((1 + returns_twrr_since_current_quarter)*(returns_may + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = ((returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 3);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 5);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 9);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 8) -- Aug month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_sep_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = ((1 + returns_twrr_since_current_quarter)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = ((returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 3);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 5);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 9);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 9) -- Sep month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_sep_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = returns_twrr_since_current_quarter
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = ((returns_jun_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 3);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 5);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 9);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 10) -- Oct month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_dec_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = ((1 + returns_twrr_since_current_quarter)*(returns_aug + 1)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = ((returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_may + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 3);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 5);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 9);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 11) -- Nov month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_dec_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = ((1 + returns_twrr_since_current_quarter)*(returns_sep + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = ((returns_sep_ending_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_jun + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 3);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 5);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 9);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 12) -- Dec month
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_dec_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_months = returns_twrr_since_current_quarter
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_half_year = ((returns_sep_ending_quarter + 1)*(returns_dec_ending_quarter + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_6_mt_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_one_year = returns_twrr_ytd
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_1_yr_before
      AND b.returns_year = var_curr_year;

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_2_yr_before
      AND b.returns_year = (var_curr_year - 1);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_3_yr_before
      AND b.returns_year = (var_curr_year - 2);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_5_yr_before
      AND b.returns_year = (var_curr_year - 4);

      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 1);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 2);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 3);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 4);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 5);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 6);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 7);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 8);
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b, portfolio c
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.memberid = b.memberid AND b.memberid = c.memberid
      AND a.portfolioid = b.portfolioid AND b.portfolioid = c.portfolioid
      AND c.status = 'Active'
      AND c.start_date <= var_date_10_yr_before
      AND b.returns_year = (var_curr_year - 9);

  END CASE;

  UPDATE portfolio_twrr_summary a SET returns_twrr_two_year = POWER((1 +  returns_twrr_two_year), 0.5) - 1 WHERE returns_twrr_two_year <> 0;
  UPDATE portfolio_twrr_summary a SET returns_twrr_three_year = POWER((1 +  returns_twrr_three_year), (1/3)) - 1 WHERE returns_twrr_three_year <> 0;
  UPDATE portfolio_twrr_summary a SET returns_twrr_five_year = POWER((1 +  returns_twrr_five_year), 0.2) - 1 WHERE returns_twrr_five_year <> 0;
  UPDATE portfolio_twrr_summary a SET returns_twrr_ten_year = POWER((1 +  returns_twrr_ten_year), 0.1) - 1 WHERE returns_twrr_ten_year <> 0;

  IF (MONTH(var_date_today) > 3)
  THEN
    UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
    SET    returns_twrr_since_fin_year = ((returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1
    WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0 AND b.returns_year = var_curr_year;

    UPDATE portfolio_twrr_monthly
    SET    returns_fin_year = ((returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1
    WHERE  returns_year = var_curr_year;
  ELSE
    -- first update with current March ending quarter
    UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
    SET    returns_twrr_since_fin_year = b.returns_mar_ending_quarter
    WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0 AND b.returns_year = var_curr_year;

    -- then multimply wiht last 3 quarters of previous year
    UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
    SET    returns_twrr_since_fin_year = ((returns_twrr_since_fin_year + 1) * (returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1
    WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmarkid = 0 AND b.returns_year = (
           var_curr_year - 1);
  END IF;

  OPEN portfolio_cursor;
  SET var_finished     = 0;

 FETCH_DATA:
  LOOP
    FETCH portfolio_cursor INTO var_memberid, var_portfolioid;

    IF var_finished = 1
    THEN
      LEAVE fetch_data;
    END IF;

    SELECT round(EXP(SUM(LOG(coalesce(returns_calendar_year + 1, 1)))), 4) - 1
    INTO   var_returns_twrr_since_inception
    FROM   portfolio_twrr_monthly a
    WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid;

    SELECT ROUND((DATEDIFF(var_date_today, start_date) / 365.25), 2)
    INTO   var_since_inception_holding_period
    FROM   portfolio a
    WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid;

    IF (var_since_inception_holding_period > 1) THEN
		UPDATE portfolio_twrr_summary a
		SET    returns_twrr_since_inception = round(pow(var_returns_twrr_since_inception + 1, (1 / var_since_inception_holding_period)), 4) - 1,
			   returns_date = var_date_today
		WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmarkid = 0;
	ELSE
		UPDATE portfolio_twrr_summary a
		SET    returns_twrr_since_inception = var_returns_twrr_since_inception,
               returns_date = var_date_today
		WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmarkid = 0;
    END IF;
    SET var_finished = 0;
  END LOOP fetch_data;

  CLOSE portfolio_cursor;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_portfolio_returns: End');

  commit;

END