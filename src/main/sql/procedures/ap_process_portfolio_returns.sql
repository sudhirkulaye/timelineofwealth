DROP PROCEDURE IF EXISTS ap_process_portfolio_returns;
CREATE PROCEDURE ap_process_portfolio_returns( /*
    IN pi_memberid   INT,
    IN pi_portfolioid    INT*/
)
BEGIN

  DECLARE var_memberid, var_portfolioid, var_curr_year, var_min_year, var_returns_year, var_min_month, var_month INT;
  DECLARE var_calculation_cashflow, var_calculation_market_value_last, var_calculation_market_value Decimal(20,4) DEFAULT 0.0000;
  DECLARE var_monthly_return_last, var_monthly_return, var_returns_twrr_since_inception, var_since_inception_holding_period decimal(20,4) DEFAULT 1.0000;
  DECLARE var_finished, var_count INT DEFAULT 0;
  DECLARE var_date_today, var_date_start_current_month, var_date_start_current_quarter,
  var_date_start_current_fin_year, var_min_date DATE;
  DECLARE var_out_irr  decimal(20,4) DEFAULT 1.0000;

  DECLARE portfolio_cursor CURSOR FOR
  SELECT memberid, portfolioid
  FROM  portfolio
  WHERE status = 'Active'
  -- WHERE memberid = pi_memberid AND portfolioid = pi_portfolioid
  ORDER BY memberid, portfolioid;

  DECLARE portfolio_returns_calculation_support_cursor CURSOR FOR
  SELECT calculation_cashflow, calculation_market_value
  FROM  portfolio_returns_calculation_support
  WHERE memberid = var_memberid AND
  portfolioid = var_portfolioid AND
  year(calculation_date) = var_returns_year AND
  month(calculation_date) = var_month AND
  calculation_date != (SELECT min(calculation_date)
                        FROM portfolio_returns_calculation_support
                        WHERE memberid = var_memberid AND
                        portfolioid = var_portfolioid)
  ORDER BY memberid, portfolioid, calculation_date;

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

  DELETE FROM portfolio_returns_calculation_support;

  /*WHERE     memberid = pi_memberid
        AND portfolioid = pi_portfolioid;*/

  DELETE FROM portfolio_twrr_monthly;

  /*WHERE     memberid = pi_memberid
        AND portfolioid = pi_portfolioid;*/

  DELETE FROM portfolio_irr_summary;
  /*WHERE     memberid = pi_memberid
        AND portfolioid = pi_portfolioid;*/

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
      SELECT   b.memberid, b.portfolioid, b.date date, b.amount cashflow, c.value
      FROM     portfolio_cashflow b
               LEFT JOIN portfolio_value_history c
                 ON b.memberid = c.memberid AND b.portfolioid = c.portfolioid AND b.date = c.date
      WHERE    b.memberid = var_memberid AND b.portfolioid = var_portfolioid
      ORDER BY b.memberid, b.portfolioid, date;

    -- insert end of month data exclude if cash flow is also on end of month
    INSERT INTO portfolio_returns_calculation_support
      SELECT memberid, portfolioid, portfolio_value_date date, 0 cashflow, portfolio_market_value
      FROM   portfolio_value_history a
      WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.portfolio_value_date NOT IN
               (                                                                   SELECT cashflow_date
                                                                                   FROM   portfolio_cashflow g
                                                                                   WHERE  g.memberid = var_memberid AND g.
             portfolioid = var_portfolioid) -- AND a.date NOT IN (SELECT date_today FROM setup_dates)
                                             AND portfolio_value_date IN (SELECT   MAX(portfolio_value_date)
                                                                          FROM     portfolio_value_history h
                                                                          WHERE    h.memberid = var_memberid AND h.portfolioid
             = var_portfolioid
                                                                          GROUP BY YEAR(portfolio_value_date),
             MONTH                                                                 (portfolio_value_date));

    DELETE FROM portfolio_twrr_summary WHERE benchmark_id = 0;
    DELETE FROM portfolio_irr_summary WHERE benchmark_id = 0;

    INSERT INTO portfolio_twrr_summary
      SELECT memberid, portfolioid, 0, var_date_today, 0, 0, 0, 0, 0, 0
      FROM   portfolio b;
     /* WHERE  memberid NOT IN (SELECT memberid FROM portfolio_twrr_summary)
      AND portfolioid NOT IN (SELECT portfolioid FROM portfolio_twrr_summary);
      AND portfolio_benchmark_id NOT IN (SELECT benchmark_id FROM portfolio_twrr_summary);*/

    INSERT INTO portfolio_irr_summary
      SELECT memberid, portfolioid, 0, var_date_today, 0, 0, 0, 0, 0, 0
      FROM   portfolio b;
	/*  WHERE  memberid NOT IN (SELECT memberid FROM portfolio_irr_summary)
      AND portfolioid NOT IN (SELECT portfolioid FROM portfolio_irr_summary);
     -- AND portfolio_benchmark_id NOT IN (SELECT benchmark_id FROM portfolio_irr_summary); */

    SELECT MIN(YEAR(calculation_date))
    INTO   var_min_year
    FROM   portfolio_returns_calculation_support
    WHERE  memberid = var_memberid AND portfolioid = var_portfolioid;

    SELECT MIN(MONTH(calculation_date))
    INTO   var_min_month
    FROM   portfolio_returns_calculation_support
    WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND YEAR(calculation_date) = var_min_year;

    SELECT calculation_cashflow
    INTO   var_calculation_cashflow
    FROM   portfolio_returns_calculation_support
    WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND calculation_date =
             (                                                               SELECT min(calculation_date)
                                                                             FROM   portfolio_returns_calculation_support
                                                                             WHERE  memberid = var_memberid AND portfolioid =
           var_portfolioid);

    SET var_calculation_market_value_last = abs(var_calculation_cashflow);
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
                   0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
                   0);

      -- INSERT INTO log_table values (now(), concat('In ap_process_historical_returns, 2: ',var_memberid, '-',var_portfolioid,'-',var_returns_year,'-',var_month));

      WHILE var_month <= 12
      DO
        OPEN portfolio_returns_calculation_support_cursor;

        SET var_finished            = 0;

       FETCH_SUPPORT_DATA:
        LOOP
          FETCH portfolio_returns_calculation_support_cursor INTO var_calculation_cashflow, var_calculation_market_value;

          IF var_finished = 1
          THEN
            LEAVE FETCH_SUPPORT_DATA;
          END IF;

          SET var_monthly_return                = ROUND((((var_calculation_market_value + var_calculation_cashflow) / var_calculation_market_value_last) * var_monthly_return_last), 5)
;

          -- INSERT INTO log_table values (now(), concat('In ap_process_historical_returns, 3: ',var_monthly_return));

          SET var_monthly_return_last           = var_monthly_return;
          SET var_calculation_market_value_last = var_calculation_market_value;
        END LOOP FETCH_SUPPORT_DATA;

        CLOSE portfolio_returns_calculation_support_cursor;

        CASE
          WHEN var_month = 1
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_jan = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
            END IF;
          WHEN var_month = 2
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_feb = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
            END IF;
          WHEN var_month = 3
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_mar = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
            END IF;
          WHEN var_month = 4
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_apr = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
            END IF;
          WHEN var_month = 5
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_may = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
            END IF;
          WHEN var_month = 6
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_jun = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
            END IF;
          WHEN var_month = 7
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_jul = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
            END IF;
          WHEN var_month = 8
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_aug = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
            END IF;
          WHEN var_month = 9
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_sep = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
            END IF;
          WHEN var_month = 10
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_oct = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
            END IF;
          WHEN var_month = 11
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_nov = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
            END IF;
          WHEN var_month = 12
          THEN
            UPDATE portfolio_twrr_monthly
            SET    returns_dec = var_monthly_return_last
            WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND returns_year = var_returns_year;

            IF (var_returns_year = var_curr_year AND var_month = MONTH(var_date_today))
            THEN
              UPDATE portfolio_twrr_summary
              SET    returns_twrr_since_current_month = var_monthly_return_last
              WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND benchmark_id = 0;
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
  SET    returns_mar_ending_quarter = (CASE returns_jan WHEN 0 THEN 1 ELSE returns_jan END) * (CASE returns_feb WHEN 0 THEN 1 ELSE returns_feb END) * (CASE returns_mar WHEN 0 THEN 1 ELSE returns_mar END);

  UPDATE portfolio_twrr_monthly
  SET    returns_jun_ending_quarter = (CASE returns_apr WHEN 0 THEN 1 ELSE returns_apr END) * (CASE returns_may WHEN 0 THEN 1 ELSE returns_may END) * (CASE returns_jun WHEN 0 THEN 1 ELSE returns_jun END);

  UPDATE portfolio_twrr_monthly
  SET    returns_sep_ending_quarter = (CASE returns_jul WHEN 0 THEN 1 ELSE returns_jul END) * (CASE returns_aug WHEN 0 THEN 1 ELSE returns_aug END) * (CASE returns_sep WHEN 0 THEN 1 ELSE returns_sep END);

  UPDATE portfolio_twrr_monthly
  SET    returns_dec_ending_quarter = (CASE returns_oct WHEN 0 THEN 1 ELSE returns_oct END) * (CASE returns_nov WHEN 0 THEN 1 ELSE returns_nov END) * (CASE returns_dec WHEN 0 THEN 1 ELSE returns_dec END);

  UPDATE portfolio_twrr_monthly a
  SET    a.returns_calendar_year = (CASE returns_mar_ending_quarter WHEN 0 THEN 1 ELSE returns_mar_ending_quarter END) * (CASE returns_jun_ending_quarter WHEN 0 THEN 1 ELSE returns_jun_ending_quarter END) * (CASE returns_sep_ending_quarter WHEN 0 THEN 1 ELSE returns_sep_ending_quarter END) * (CASE returns_dec_ending_quarter WHEN 0 THEN 1 ELSE returns_dec_ending_quarter END);

  UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
  SET    returns_twrr_ytd = b.returns_calendar_year
  WHERE  a.memberid = b.memberid
  AND a.portfolioid = b.portfolioid
  AND a.benchmark_id = 0
  AND b.returns_year = var_curr_year;

  UPDATE portfolio_twrr_summary
  SET    returns_twrr_one_year = returns_twrr_ytd
  WHERE  benchmark_id = 0;

  CASE
    WHEN (MONTH(var_date_today) <= 3)
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_mar_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year =
             var_curr_year;

      CASE
        WHEN (MONTH(var_date_today) = 1 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_calendar_year
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 1 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_feb * returns_mar * returns_jun_ending_quarter * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 2 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_feb * returns_mar * returns_jun_ending_quarter * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 2 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_mar * returns_jun_ending_quarter * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 3 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_mar * returns_jun_ending_quarter * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 3 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_jun_ending_quarter * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
      END CASE;
    WHEN (MONTH(var_date_today) > 3 AND MONTH(var_date_today) <= 6)
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_jun_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year =
             var_curr_year;

      CASE
        WHEN (MONTH(var_date_today) = 4 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_jun_ending_quarter * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 4 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_may * returns_jun * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 5 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_may * returns_jun * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 5 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_jun * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 6 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_jun * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 6 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
      END CASE;
    WHEN (MONTH(var_date_today) > 6 AND MONTH(var_date_today) <= 9)
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_sep_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year =
             var_curr_year;

      CASE
        WHEN (MONTH(var_date_today) = 7 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_sep_ending_quarter * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 7 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_aug * returns_sep * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 8 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_aug * returns_sep * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 8 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_sep * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 9 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_sep * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 9 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
      END CASE;
    WHEN (MONTH(var_date_today) > 9)
    THEN
      UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_dec_ending_quarter
      WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year =
             var_curr_year;

      CASE
        WHEN (MONTH(var_date_today) = 10 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_dec_ending_quarter
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 10 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_nov * returns_dec
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 11 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_nov * returns_dec
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 11 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_dec
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 12 AND DAY(var_date_today) <= 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year * returns_dec
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
        WHEN (MONTH(var_date_today) = 12 AND DAY(var_date_today) > 15)
        THEN
          UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
          SET    returns_twrr_one_year = returns_twrr_one_year
          WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
                 var_curr_year - 1);
      END CASE;
  END CASE;

  IF (MONTH(var_date_today) > 3)
  THEN
    UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
    SET    returns_twrr_since_fin_year = returns_jun_ending_quarter * returns_sep_ending_quarter * returns_dec_ending_quarter
    WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = var_curr_year;

    UPDATE portfolio_twrr_monthly
    SET    returns_fin_year = returns_jun_ending_quarter * returns_sep_ending_quarter * returns_dec_ending_quarter
    WHERE  returns_year = var_curr_year;
  ELSE
    -- first update with current March ending quarter
    UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
    SET    returns_twrr_since_fin_year = b.returns_mar_ending_quarter
    WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = var_curr_year;

    -- then multimply wiht last 3 quarters of previous year
    UPDATE portfolio_twrr_summary a, portfolio_twrr_monthly b
    SET    returns_twrr_since_fin_year = returns_twrr_since_fin_year * returns_jun_ending_quarter * returns_sep_ending_quarter * returns_dec_ending_quarter
    WHERE  a.memberid = b.memberid AND a.portfolioid = b.portfolioid AND a.benchmark_id = 0 AND b.returns_year = (
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

    SELECT round(EXP(SUM(LOG(coalesce(returns_calendar_year, 1)))), 4)
    INTO   var_returns_twrr_since_inception
    FROM   portfolio_twrr_monthly a
    WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid;

    SELECT ROUND((DATEDIFF(var_date_today, portfolio_start_date) / 365.25), 2)
    INTO   var_since_inception_holding_period
    FROM   portfolio a
    WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid;

    UPDATE portfolio_twrr_summary a
    SET    returns_twrr_since_inception = round(pow(var_returns_twrr_since_inception, (1 / var_since_inception_holding_period)), 4)
           , returns_date = var_date_today
    WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;

    -- Since Inception IRR compuation
    INSERT INTO temp_irr_calculation
    SELECT memberid, portfolioid, calculation_date, calculation_cashflow, calculation_market_value
      FROM   portfolio_returns_calculation_support
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_cashflow <> 0;

    INSERT INTO temp_irr_calculation
    SELECT memberid, portfolioid, calculation_date, calculation_market_value, 0
      FROM portfolio_returns_calculation_support
      WHERE memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date = var_date_today
      AND calculation_cashflow = 0;

    SELECT min(calculation_date)
    INTO   var_min_date
    FROM   temp_irr_calculation
    WHERE  memberid = var_memberid AND portfolioid = var_portfolioid AND calculation_cashflow <> 0;

    SET var_out_irr  = 0.0;
    CALL ap_calculate_irr(var_out_irr, var_memberid, var_portfolioid, var_min_date);

    UPDATE portfolio_irr_summary a
    SET    returns_irr_since_inception = var_out_irr, returns_date = var_date_today
    WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;

    -- One Year IRR calculation
    SELECT count(1)
    INTO   var_count
    FROM   portfolio_returns_calculation_support
    WHERE  memberid = var_memberid
    AND portfolioid = var_portfolioid
    AND calculation_date <  date_sub(var_date_today, INTERVAL 12 MONTH);

    IF var_count > 0
    THEN
      SELECT max(calculation_date)
      INTO   var_min_date
      FROM   portfolio_returns_calculation_support
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date <  date_sub(var_date_today, INTERVAL 12 MONTH);

      DELETE FROM temp_irr_calculation
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date <= var_min_date;

      INSERT INTO temp_irr_calculation
      SELECT memberid, portfolioid, var_min_date, -(calculation_market_value), calculation_market_value
      FROM portfolio_returns_calculation_support
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date = var_min_date;

      SET var_out_irr = 0.0;
      CALL ap_calculate_irr(var_out_irr, var_memberid, var_portfolioid, var_min_date);

      UPDATE portfolio_irr_summary a
      SET    returns_irr_one_year = var_out_irr
      WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
    ELSE
      UPDATE portfolio_irr_summary a
      SET    returns_irr_one_year = 0
      WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
    END IF;

    -- IRR YTD
    SELECT count(1)
    INTO   var_count
    FROM   portfolio_returns_calculation_support
    WHERE  memberid = var_memberid
    AND portfolioid = var_portfolioid
    AND calculation_date <  DATE_FORMAT(var_date_today,'%Y-01-01');

    IF (var_count>0) THEN
      DELETE FROM temp_irr_calculation
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date < var_date_today;

      SELECT max(calculation_date)
      INTO   var_min_date
      FROM   portfolio_returns_calculation_support
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date <  DATE_FORMAT(var_date_today,'%Y-01-01');

      INSERT INTO temp_irr_calculation
      SELECT memberid, portfolioid, var_min_date, -(calculation_market_value), calculation_market_value
      FROM portfolio_returns_calculation_support
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date = var_min_date;

      INSERT INTO temp_irr_calculation
      SELECT memberid, portfolioid, calculation_date, calculation_cashflow, calculation_market_value
      FROM   portfolio_returns_calculation_support
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_cashflow <> 0
      AND calculation_date > var_min_date
      AND calculation_date != var_date_today;

      SET var_out_irr = 0.0;
      CALL ap_calculate_irr(var_out_irr, var_memberid, var_portfolioid, var_min_date);

      UPDATE portfolio_irr_summary a
      SET    returns_irr_ytd = var_out_irr
      WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
    ELSE
      UPDATE portfolio_irr_summary a
      SET    returns_irr_ytd = 0
      WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
    END IF;

    -- IRR Since FIN Year
    SELECT count(1)
    INTO   var_count
    FROM   portfolio_returns_calculation_support
    WHERE  memberid = var_memberid
    AND portfolioid = var_portfolioid
    AND calculation_date <  var_date_start_current_fin_year;

    IF (var_count>0) THEN
      DELETE FROM temp_irr_calculation
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date < var_date_today;

      SELECT max(calculation_date)
      INTO   var_min_date
      FROM   portfolio_returns_calculation_support
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date <  var_date_start_current_fin_year;

      INSERT INTO temp_irr_calculation
      SELECT memberid, portfolioid, var_min_date, -(calculation_market_value), calculation_market_value
      FROM portfolio_returns_calculation_support
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date = var_min_date;

      INSERT INTO temp_irr_calculation
      SELECT memberid, portfolioid, calculation_date, calculation_cashflow, calculation_market_value
      FROM   portfolio_returns_calculation_support
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_cashflow <> 0
      AND calculation_date > var_min_date
      AND calculation_date != var_date_today;

      SET var_out_irr = 0.0;
      CALL ap_calculate_irr(var_out_irr, var_memberid, var_portfolioid, var_min_date);

      UPDATE portfolio_irr_summary a
      SET    returns_irr_since_fin_year = var_out_irr
      WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
    ELSE
      UPDATE portfolio_irr_summary a
      SET    returns_irr_since_fin_year = 0
      WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
    END IF;

    -- IRR since current quarter and month
    IF (MONTH(var_date_today) = 1) THEN
      UPDATE portfolio_irr_summary a
      SET    returns_irr_since_current_quarter = returns_irr_ytd,
             returns_irr_since_current_month = returns_irr_ytd
      WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
    ELSEIF (MONTH(var_date_today) IN (2,3)) THEN
      UPDATE portfolio_irr_summary a
      SET    returns_irr_since_current_quarter = returns_irr_ytd
      WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
    ELSEIF (MONTH(var_date_today) IN (4)) THEN
      UPDATE portfolio_irr_summary a
      SET    returns_irr_since_current_quarter = returns_irr_since_fin_year,
             returns_irr_since_current_month = returns_irr_since_fin_year
      WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
    ELSEIF (MONTH(var_date_today) IN (5,6)) THEN
      UPDATE portfolio_irr_summary a
      SET    returns_irr_since_current_quarter = returns_irr_since_fin_year
      WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
    END IF;

    IF (MONTH(var_date_today) > 6) THEN
      SELECT count(1)
      INTO   var_count
      FROM   portfolio_returns_calculation_support
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date <  var_date_start_current_quarter;

      IF var_count > 0
      THEN
        SELECT max(calculation_date)
        INTO   var_min_date
        FROM   portfolio_returns_calculation_support
        WHERE  memberid = var_memberid
        AND portfolioid = var_portfolioid
        AND calculation_date <  var_date_start_current_quarter;

        DELETE FROM temp_irr_calculation
        WHERE  memberid = var_memberid
        AND portfolioid = var_portfolioid
        AND calculation_date <= var_min_date;

        INSERT INTO temp_irr_calculation
        SELECT memberid, portfolioid, var_min_date, -(calculation_market_value), calculation_market_value
        FROM portfolio_returns_calculation_support
        WHERE  memberid = var_memberid
        AND portfolioid = var_portfolioid
        AND calculation_date = var_min_date;

        SET var_out_irr = 0.0;
        CALL ap_calculate_irr(var_out_irr, var_memberid, var_portfolioid, var_min_date);

        UPDATE portfolio_irr_summary a
        SET    returns_irr_since_current_quarter = var_out_irr
        WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
      ELSE
        UPDATE portfolio_irr_summary a
        SET    returns_irr_since_current_quarter = 0
        WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
      END IF;
    END IF;

    IF (MONTH(var_date_today) NOT IN (1,4)) THEN
      SELECT count(1)
      INTO   var_count
      FROM   portfolio_returns_calculation_support
      WHERE  memberid = var_memberid
      AND portfolioid = var_portfolioid
      AND calculation_date <  var_date_start_current_month;

      IF var_count > 0
      THEN
        SELECT max(calculation_date)
        INTO   var_min_date
        FROM   portfolio_returns_calculation_support
        WHERE  memberid = var_memberid
        AND portfolioid = var_portfolioid
        AND calculation_date <  var_date_start_current_month;

        DELETE FROM temp_irr_calculation
        WHERE  memberid = var_memberid
        AND portfolioid = var_portfolioid
        AND calculation_date <= var_min_date;

        INSERT INTO temp_irr_calculation
        SELECT memberid, portfolioid, var_min_date, -(calculation_market_value), calculation_market_value
        FROM portfolio_returns_calculation_support
        WHERE  memberid = var_memberid
        AND portfolioid = var_portfolioid
        AND calculation_date = var_min_date;

        SET var_out_irr = 0.0;
        CALL ap_calculate_irr(var_out_irr, var_memberid, var_portfolioid, var_min_date);

        UPDATE portfolio_irr_summary a
        SET    returns_irr_since_current_month = var_out_irr
        WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
      ELSE
        UPDATE portfolio_irr_summary a
        SET    returns_irr_since_current_month = 0
        WHERE  a.memberid = var_memberid AND a.portfolioid = var_portfolioid AND a.benchmark_id = 0;
      END IF;
    END IF;

    DELETE FROM temp_irr_calculation
    WHERE       memberid = var_memberid AND portfolioid = var_portfolioid;

    SET var_finished = 0;
  END LOOP fetch_data;

  CLOSE portfolio_cursor;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_portfolio_returns: End');

  commit;

END