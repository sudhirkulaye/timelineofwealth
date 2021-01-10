
DELIMITER $$
CREATE  PROCEDURE `ap_calculate_irr`(INOUT out_irr DECIMAL(20, 4),
                                                   IN  in_memberid INT,
                                                   IN  in_portfolioid INT,
                                                   IN  in_min_date DATE)
proc_label:
BEGIN
  DECLARE LastRate     DECIMAL(20, 4);
  DECLARE RateStep     DECIMAL(20, 4);
  DECLARE Residual     DECIMAL(20, 4);
  DECLARE LastResidual DECIMAL(20, 4);
  DECLARE var_cashflow     DECIMAL(20,3);
  DECLARE var_date         DATE;
  DECLARE i            TINYINT;
  DECLARE rate, var_absolute_return, var_holding_period         DECIMAL(20,4) DEFAULT 0.1000;
  DECLARE var_finished, var_count INT DEFAULT 0;
  DECLARE var_min_date_value, var_today_value DECIMAL(20,2);

  DECLARE cashflow_cursor CURSOR FOR
  SELECT cashflow, date
  FROM   temp_irr_calculation
  WHERE  memberid = in_memberid
  AND portfolioid = in_portfolioid
  ORDER BY date;

  DECLARE CONTINUE HANDLER
  FOR NOT FOUND
    SET var_finished = 1;

  -- SET GLOBAL log_bin_trust_function_creators = 1;

  SET RateStep     = 0.1;
  SET Residual     = 10;
  SET LastResidual = 1;
  SET i            = 0;

  SET LastRate     = Rate;

  SELECT count(1)
  INTO var_count
  FROM temp_irr_calculation
  WHERE  memberid = in_memberid
  AND portfolioid = in_portfolioid;

  IF var_count = 2 THEN
      SELECT cashflow
      INTO var_min_date_value
      FROM temp_irr_calculation
      WHERE  memberid = in_memberid
      AND portfolioid = in_portfolioid
      AND date = in_min_date;

      SELECT cashflow, date
      INTO var_today_value, var_date
      FROM temp_irr_calculation
      WHERE  memberid = in_memberid
      AND portfolioid = in_portfolioid
      AND date != in_min_date;

      SET var_absolute_return = abs(var_today_value/var_min_date_value);
      SET var_holding_period = ROUND((DATEDIFF(var_date, in_min_date) / 365.25), 4);
      SET Rate = round(((var_absolute_return-1)/var_holding_period),4);

      SET LastRate     = Rate;
      SET out_irr      = 1 + LastRate;

      leave proc_label;
  END IF;

  WHILE i < 100 AND ABS((LastResidual - Residual) / LastResidual) > 0.00000001
  DO
    -- BEGIN
    SET LastResidual = Residual;
    SET Residual     = 0;

    OPEN cashflow_cursor;

    SET var_finished = 0;

   fetch_data:
    LOOP
      FETCH cashflow_cursor INTO var_cashflow, var_date;

      IF var_finished = 1
      THEN
        LEAVE fetch_data;
      END IF;

      SET Residual     = Residual + var_cashflow / POWER(1 + Rate, DATEDIFF(var_date, in_min_date) / 365);

      SET var_finished = 0;
    END LOOP fetch_data;

    CLOSE cashflow_cursor;

    SET LastRate     = Rate;

    IF Residual >= 0
    THEN
      SET Rate = Rate + RateStep;
    ELSE
      SET RateStep = RateStep / 2;
      SET Rate     = Rate - RateStep;
    END IF;

    SET i            = i + 1;
  END WHILE;

  SET out_irr      = 1 + lastrate;
END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_process_benchmark_returns`( /*
    IN pi_memberid   INT,
    IN pi_portfolioid    INT*/
)
BEGIN

  DECLARE var_benchmarkid, var_year, var_month INT;
  DECLARE var_curr_year, var_value_last, var_value, var_return_temp Decimal(20,4) DEFAULT 0.0000;
  DECLARE var_monthly_return, var_returns_twrr_since_inception, var_since_inception_holding_period decimal(20,4) DEFAULT 1.0000;
  DECLARE var_finished, var_count INT DEFAULT 0;
  DECLARE var_date_last_returns_process, var_date_today, var_date_start_current_month, var_date_start_current_quarter,
  var_date_start_current_fin_year, var_min_date, var_date_1_yr_before, var_date_2_yr_before,
  var_date_3_yr_before, var_date_5_yr_before, var_date_10_yr_before, var_date_3_mt_before, var_date_6_mt_before, var_max_upload_date DATE;
  DECLARE var_is_mutual_fund VARCHAR(3);
  DECLARE var_benchmark_name VARCHAR(100);

  DECLARE benchmark_cursor CURSOR FOR
  SELECT benchmarkid, benchmark_name, date_last_returns_process, is_mutual_fund
  FROM  benchmark
  ORDER BY benchmarkid;

  DECLARE CONTINUE HANDLER
  FOR NOT FOUND
    SET var_finished = 1;

  SET SQL_SAFE_UPDATES = 0;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_benchmark_returns: Begin');

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

  INSERT INTO benchmark_twrr_summary
    SELECT benchmarkid, var_date_today, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0
	FROM   benchmark b
    WHERE  benchmarkid NOT IN (SELECT benchmarkid FROM benchmark_twrr_summary);

  OPEN benchmark_cursor;

  SET var_finished     = 0;

 FETCH_DATA:
  LOOP
    FETCH benchmark_cursor INTO var_benchmarkid, var_benchmark_name, var_date_last_returns_process, var_is_mutual_fund;

    IF var_finished = 1
    THEN
      LEAVE FETCH_DATA;
    END IF;

    IF (var_is_mutual_fund = 'No') THEN
		SELECT max(date) INTO var_max_upload_date
        FROM index_valuation
        WHERE ticker = var_benchmark_name;

        IF(var_max_upload_date > var_date_last_returns_process) THEN
			SET var_year = YEAR(var_date_last_returns_process);
            SET var_month = MONTH(var_date_last_returns_process);

			SELECT count(1) INTO var_count
            FROM benchmark_twrr_monthly
            WHERE benchmarkid = var_benchmarkid
            AND year = var_year;

            IF (var_count = 0) THEN -- No data not found for Year of date_last_returns_process, so, start from first
				SELECT MIN(date)
                INTO var_date_last_returns_process
                FROM index_valuation
                WHERE ticker = var_benchmark_name;

                SELECT value INTO var_value_last
                FROM index_valuation
                WHERE ticker = var_benchmark_name
                AND date = var_date_last_returns_process;

                SET var_year = YEAR(var_date_last_returns_process);
                SET var_month = MONTH(var_date_last_returns_process);

                UPDATE benchmark SET date_last_returns_process = var_date_last_returns_process
                WHERE benchmarkid = var_benchmarkid;

                DELETE FROM benchmark_twrr_monthly WHERE benchmarkid = var_benchmarkid;
                INSERT INTO benchmark_twrr_monthly VALUES (var_benchmarkid, var_year, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            END IF;

            IF (var_value_last = 0) THEN -- In normal case when to find last end of month date
                SELECT count(1) INTO var_count
                FROM index_valuation
                WHERE ticker = var_benchmark_name
                AND date = (SELECT MAX(date)
                            FROM index_valuation
                            WHERE ticker = var_benchmark_name
                            AND MONTH(date) = MONTH(LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH))
                            AND YEAR(date) = YEAR(LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH))
                            AND date <= LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH));

                IF (var_count != 0) THEN
					SELECT value INTO var_value_last
					FROM index_valuation
					WHERE ticker = var_benchmark_name
					AND date = (SELECT MAX(date)
								FROM index_valuation
								WHERE ticker = var_benchmark_name
								AND MONTH(date) = MONTH(LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH))
								AND YEAR(date) = YEAR(LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH))
								AND date <= LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH));
				ELSE
					SELECT value INTO var_value_last
					FROM index_valuation
					WHERE ticker = var_benchmark_name
					AND date = (SELECT MIN(date)
								FROM index_valuation
								WHERE ticker = var_benchmark_name
								AND MONTH(date) = MONTH(var_date_last_returns_process)
								AND YEAR(date) = YEAR(var_date_last_returns_process)
								AND date <= var_date_last_returns_process);
                END IF;

            END IF;

			WHILE (var_year <= YEAR(var_max_upload_date))
			DO
				IF (var_year <> YEAR(var_date_last_returns_process)) THEN
					SET var_month = 1;
				END IF;

                SELECT count(1) INTO var_count
                FROM benchmark_twrr_monthly
                WHERE benchmarkid = var_benchmarkid
                AND year = var_year;

                IF (var_count = 0) THEN
					INSERT INTO benchmark_twrr_monthly VALUES (var_benchmarkid, var_year, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                END IF;

                WHILE var_month <= 12
                DO
					SELECT value INTO var_value
					FROM index_valuation
					WHERE ticker = var_benchmark_name
					AND date = (SELECT MAX(date)
								FROM index_valuation
								WHERE ticker = var_benchmark_name
								AND MONTH(date) = var_month
								AND YEAR(date) = var_year);

                    SET var_monthly_return = ROUND((var_value / var_value_last), 5) - 1;
                    SET var_value_last = var_value;

                    CASE
						WHEN var_month = 1 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_jan = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 2 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_feb = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 3 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_mar = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 4 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_apr = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 5 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_may = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 6 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_jun = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 7 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_jul = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 8 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_aug = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 9 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_sep = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 10 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_oct = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 11 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_nov = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 12 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_dec = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
                    END CASE;
                    SET var_monthly_return = 0.0000;
					SET var_month = var_month + 1;
                END WHILE;

				UPDATE benchmark_twrr_monthly
				SET    returns_mar_ending_quarter = ((returns_jan + 1) * (returns_feb + 1) * (returns_mar + 1)) - 1;

				UPDATE benchmark_twrr_monthly
				SET    returns_jun_ending_quarter = ((returns_apr + 1) * (returns_may + 1) * (returns_jun + 1)) - 1;

				UPDATE benchmark_twrr_monthly
				SET    returns_sep_ending_quarter = ((returns_jul + 1) * (returns_aug + 1) * (returns_sep + 1)) - 1;

				UPDATE benchmark_twrr_monthly
				SET    returns_dec_ending_quarter = ((returns_oct + 1) * (returns_nov + 1) * (returns_dec + 1)) - 1;

				UPDATE benchmark_twrr_monthly a
				SET    a.returns_calendar_year = ((returns_mar_ending_quarter + 1) * (returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1;

				SELECT count(1) INTO var_count
                FROM benchmark_twrr_monthly
                WHERE benchmarkid = var_benchmarkid
                AND year = (var_year - 1);
                IF (var_count = 1) THEN
					SET var_return_temp = 0.0000;
                    SELECT ((returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1
                    INTO var_return_temp
                    FROM benchmark_twrr_monthly
                    WHERE benchmarkid = var_benchmarkid
                    AND year = (var_year - 1);
					UPDATE benchmark_twrr_monthly
					SET    returns_fin_year = var_return_temp
					WHERE  benchmarkid = var_benchmarkid
                    AND year = var_year;
                END IF;
                UPDATE benchmark_twrr_monthly
                SET    returns_fin_year = ((returns_fin_year + 1)*(returns_mar_ending_quarter + 1)) - 1
                WHERE benchmarkid = var_benchmarkid
                AND year = var_year;

                SET var_year = var_year + 1;

			END WHILE;
        END IF;
	ELSE -- Mutual Funds
		SELECT max(date) INTO var_max_upload_date
        FROM mutual_fund_nav_history
        WHERE scheme_code = var_benchmarkid;

        IF(var_max_upload_date > var_date_last_returns_process) THEN
			SET var_year = YEAR(var_date_last_returns_process);
            SET var_month = MONTH(var_date_last_returns_process);

			SELECT count(1) INTO var_count
            FROM benchmark_twrr_monthly
            WHERE benchmarkid = var_benchmarkid
            AND year = var_year;

            IF (var_count = 0) THEN -- No data not found for Year of date_last_returns_process, so, start from first
				SELECT MIN(date)
                INTO var_date_last_returns_process
                FROM mutual_fund_nav_history
                WHERE scheme_code = var_benchmarkid;

                SELECT nav INTO var_value_last
                FROM mutual_fund_nav_history
                WHERE scheme_code = var_benchmarkid
                AND date = var_date_last_returns_process;

                SET var_year = YEAR(var_date_last_returns_process);
                SET var_month = MONTH(var_date_last_returns_process);

                UPDATE benchmark SET date_last_returns_process = var_date_last_returns_process
                WHERE benchmarkid = var_benchmarkid;

                DELETE FROM benchmark_twrr_monthly WHERE benchmarkid = var_benchmarkid;
                INSERT INTO benchmark_twrr_monthly VALUES (var_benchmarkid, var_year, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
            END IF;

            IF (var_value_last = 0) THEN
                SELECT count(1) INTO var_count
                FROM mutual_fund_nav_history
                WHERE scheme_code = var_benchmarkid
                AND date = (SELECT MAX(date)
                            FROM mutual_fund_nav_history
                            WHERE scheme_code = var_benchmarkid
                            AND MONTH(date) = MONTH(LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH))
                            AND YEAR(date) = YEAR(LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH))
                            AND date <= LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH));

                IF (var_count != 0) THEN
					SELECT nav INTO var_value_last
					FROM mutual_fund_nav_history
					WHERE scheme_code = var_benchmarkid
					AND date = (SELECT MAX(date)
								FROM mutual_fund_nav_history
								WHERE scheme_code = var_benchmarkid
								AND MONTH(date) = MONTH(LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH))
								AND YEAR(date) = YEAR(LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH))
								AND date <= LAST_DAY(var_date_last_returns_process - INTERVAL 1 MONTH));
				ELSE
					SELECT nav INTO var_value_last
					FROM mutual_fund_nav_history
					WHERE scheme_code = var_benchmarkid
					AND date = (SELECT MIN(date)
								FROM mutual_fund_nav_history
								WHERE scheme_code = var_benchmarkid
								AND MONTH(date) = MONTH(var_date_last_returns_process)
								AND YEAR(date) = YEAR(var_date_last_returns_process)
								AND date <= var_date_last_returns_process);
                END IF;

            END IF;

			WHILE (var_year <= YEAR(var_max_upload_date))
			DO
				IF (var_year <> YEAR(var_date_last_returns_process)) THEN
					SET var_month = 1;
				END IF;

                SELECT count(1) INTO var_count
                FROM benchmark_twrr_monthly
                WHERE benchmarkid = var_benchmarkid
                AND year = var_year;

                IF (var_count = 0) THEN
					INSERT INTO benchmark_twrr_monthly VALUES (var_benchmarkid, var_year, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0);
                END IF;

                WHILE var_month <= 12
                DO
					SELECT nav INTO var_value
					FROM mutual_fund_nav_history
					WHERE scheme_code = var_benchmarkid
					AND date = (SELECT MAX(date)
								FROM mutual_fund_nav_history
								WHERE scheme_code = var_benchmarkid
								AND MONTH(date) = var_month
								AND YEAR(date) = var_year);

                    SET var_monthly_return = ROUND((var_value / var_value_last), 5) - 1;
                    SET var_value_last = var_value;

                    CASE
						WHEN var_month = 1 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_jan = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 2 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_feb = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 3 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_mar = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 4 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_apr = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 5 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_may = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 6 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_jun = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 7 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_jul = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 8 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_aug = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 9 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_sep = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 10 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_oct = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 11 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_nov = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
						WHEN var_month = 12 THEN
							UPDATE benchmark_twrr_monthly
                            SET    returns_dec = var_monthly_return
                            WHERE  benchmarkid = var_benchmarkid AND year = var_year;
							IF (var_year = var_curr_year AND var_month = MONTH(var_date_today))
							THEN
							  UPDATE benchmark_twrr_summary
							  SET    returns_twrr_since_current_month = var_monthly_return
							  WHERE  benchmarkid = var_benchmarkid;
							END IF;
                    END CASE;
                    SET var_monthly_return = 0.0000;
					SET var_month = var_month + 1;
                END WHILE;

				UPDATE benchmark_twrr_monthly
				SET    returns_mar_ending_quarter = ((returns_jan + 1) * (returns_feb + 1) * (returns_mar + 1)) - 1;

				UPDATE benchmark_twrr_monthly
				SET    returns_jun_ending_quarter = ((returns_apr + 1) * (returns_may + 1) * (returns_jun + 1)) - 1;

				UPDATE benchmark_twrr_monthly
				SET    returns_sep_ending_quarter = ((returns_jul + 1) * (returns_aug + 1) * (returns_sep + 1)) - 1;

				UPDATE benchmark_twrr_monthly
				SET    returns_dec_ending_quarter = ((returns_oct + 1) * (returns_nov + 1) * (returns_dec + 1)) - 1;

				UPDATE benchmark_twrr_monthly a
				SET    a.returns_calendar_year = ((returns_mar_ending_quarter + 1) * (returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1;

				SELECT count(1) INTO var_count
                FROM benchmark_twrr_monthly
                WHERE benchmarkid = var_benchmarkid
                AND year = (var_year - 1);
                IF (var_count = 1) THEN
					SET var_return_temp = 0.0000;
                    SELECT ((returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1
                    INTO var_return_temp
                    FROM benchmark_twrr_monthly
                    WHERE benchmarkid = var_benchmarkid
                    AND year = (var_year - 1);
					UPDATE benchmark_twrr_monthly
					SET    returns_fin_year = var_return_temp
					WHERE  benchmarkid = var_benchmarkid
                    AND year = var_year;
                END IF;
                UPDATE benchmark_twrr_monthly
                SET    returns_fin_year = ((returns_fin_year + 1)*(returns_mar_ending_quarter + 1)) - 1
                WHERE benchmarkid = var_benchmarkid
                AND year = var_year;

                SET var_year = var_year + 1;

			END WHILE;
        END IF;
    END IF;

    SET var_finished = 0;
  END LOOP FETCH_DATA;

  CLOSE benchmark_cursor;

  UPDATE benchmark_twrr_monthly
  SET    returns_mar_ending_quarter = ((returns_jan + 1) * (returns_feb + 1) * (returns_mar + 1)) - 1;

  UPDATE benchmark_twrr_monthly
  SET    returns_jun_ending_quarter = ((returns_apr + 1) * (returns_may + 1) * (returns_jun + 1)) - 1;

  UPDATE benchmark_twrr_monthly
  SET    returns_sep_ending_quarter = ((returns_jul + 1) * (returns_aug + 1) * (returns_sep + 1)) - 1;

  UPDATE benchmark_twrr_monthly
  SET    returns_dec_ending_quarter = ((returns_oct + 1) * (returns_nov + 1) * (returns_dec + 1)) - 1;

  UPDATE benchmark_twrr_monthly a
  SET    a.returns_calendar_year = ((returns_mar_ending_quarter + 1) * (returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1;

  UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
  SET    returns_twrr_ytd = b.returns_calendar_year
  WHERE  a.benchmarkid = b.benchmarkid
  AND b.year = var_curr_year;

  -- new script
  CASE
    WHEN (MONTH(var_date_today) = 1) -- Jan month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_mar_ending_quarter
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_months = ((returns_twrr_since_current_quarter + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1,
             returns_twrr_half_year = ((returns_twrr_since_current_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1,
             returns_twrr_one_year = ((returns_twrr_since_current_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1,
             returns_twrr_two_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
			 returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 9);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 2) -- Feb month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_mar_ending_quarter
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_months = ((returns_twrr_since_current_quarter + 1)*(returns_dec + 1)) - 1,
             returns_twrr_half_year = ((returns_twrr_since_current_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1,
             returns_twrr_one_year = ((returns_twrr_since_current_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1,
             returns_twrr_two_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1,
			 returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1,
			 returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 9);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 3) -- Mar month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_mar_ending_quarter,
             returns_twrr_three_months = b.returns_mar_ending_quarter
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_half_year = ((returns_twrr_since_current_quarter + 1)*(returns_dec_ending_quarter + 1)) - 1,
             returns_twrr_one_year = ((returns_twrr_since_current_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)) - 1,
             returns_twrr_two_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_since_current_quarter + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)) - 1,
		     returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 9);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 4) -- Apr month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_jun_ending_quarter,
             returns_twrr_three_months = ((1 + b.returns_jun_ending_quarter)*(returns_mar + 1)*(returns_feb + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_half_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1,
             returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)*(returns_may + 1)) - 1,
             returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_may + 1)*(returns_jun + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_may + 1)*(returns_jun + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
			 returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_may + 1)*(returns_jun + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 9);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_may + 1)*(returns_jun + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 5) -- May month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_jun_ending_quarter,
             returns_twrr_three_months = ((1 + b.returns_jun_ending_quarter)*(returns_mar + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_half_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_dec + 1)) - 1,
             returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)) - 1,
             returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year <= (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year <= (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year <= (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year <= (var_curr_year - 9);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_jun + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 6) -- Jun month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_jun_ending_quarter,
             returns_twrr_three_months = b.returns_jun_ending_quarter,
             returns_twrr_half_year = returns_twrr_ytd
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1,
             returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);


      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 9);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 7) -- Jul month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_sep_ending_quarter,
             returns_twrr_three_months = ((1 + b.returns_sep_ending_quarter)*(returns_may + 1)*(returns_jun + 1)) - 1,
             returns_twrr_half_year = ((returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_feb + 1)*(returns_mar + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1,
             returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 9);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_aug + 1)*(returns_sep + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 8) -- Aug month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_sep_ending_quarter,
			 returns_twrr_three_months = ((1 + b.returns_sep_ending_quarter)*(returns_jun + 1)) - 1,
             returns_twrr_half_year = ((returns_sep_ending_quarter + 1)*(returns_jun_ending_quarter + 1)*(returns_mar + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1,
             returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 9);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)*(returns_sep + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 9) -- Sep month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_sep_ending_quarter,
             returns_twrr_three_months = b.returns_sep_ending_quarter,
             returns_twrr_half_year = ((returns_jun_ending_quarter + 1)*(returns_sep_ending_quarter + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)) - 1,
             returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec_ending_quarter + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_three_year + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec_ending_quarter + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec_ending_quarter + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 9);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec_ending_quarter + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 10) -- Oct month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_dec_ending_quarter,
             returns_twrr_three_months = ((1 + b.returns_dec_ending_quarter)*(returns_aug + 1)*(returns_sep + 1)) - 1,
             returns_twrr_half_year = ((returns_dec_ending_quarter + 1)*(returns_sep_ending_quarter + 1)*(returns_may + 1)*(returns_jun + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1,
             returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_three_year + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 9);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 11) -- Nov month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_dec_ending_quarter,
             returns_twrr_three_months = ((1 + b.returns_dec_ending_quarter)*(returns_sep + 1)) - 1,
             returns_twrr_half_year = ((returns_sep_ending_quarter + 1)*(returns_dec_ending_quarter + 1)*(returns_jun + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_one_year = ((returns_twrr_ytd + 1)*(returns_dec + 1)) - 1,
			 returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ytd + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_two_year + 1)*(returns_dec + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_three_year + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)* (returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)* (returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_dec + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
			 returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_dec + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 9);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_dec + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 10);

    WHEN (MONTH(var_date_today) = 12) -- Dec month
    THEN
      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    a.returns_twrr_since_current_quarter = b.returns_dec_ending_quarter,
             returns_twrr_three_months = b.returns_dec_ending_quarter,
             returns_twrr_half_year = ((returns_sep_ending_quarter + 1)*(returns_dec_ending_quarter + 1)) - 1,
             returns_twrr_one_year = returns_twrr_ytd
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = var_curr_year;

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_twrr_one_year + 1)) - 1,
             returns_twrr_three_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ytd + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 1);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_three_year = ((returns_twrr_three_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 2);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1,
             returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 3);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_five_year = ((returns_twrr_five_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 4);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 5);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 6);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 7);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 8);

      UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
      SET    returns_twrr_ten_year = ((returns_twrr_ten_year + 1)*(returns_calendar_year + 1)) - 1
      WHERE  a.benchmarkid = b.benchmarkid
      AND b.year = (var_curr_year - 9);

  END CASE;

  UPDATE benchmark_twrr_summary a SET returns_twrr_two_year = POWER((1 +  returns_twrr_two_year), 0.5) - 1 WHERE returns_twrr_two_year <> 0;
  UPDATE benchmark_twrr_summary a SET returns_twrr_three_year = POWER((1 +  returns_twrr_three_year), (1/3)) - 1 WHERE returns_twrr_three_year <> 0;
  UPDATE benchmark_twrr_summary a SET returns_twrr_five_year = POWER((1 +  returns_twrr_five_year), 0.2) - 1 WHERE returns_twrr_five_year <> 0;
  UPDATE benchmark_twrr_summary a SET returns_twrr_ten_year = POWER((1 +  returns_twrr_ten_year), 0.1) - 1 WHERE returns_twrr_ten_year <> 0;

  IF (MONTH(var_date_today) > 3)
  THEN
    UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
    SET    returns_twrr_since_fin_year = ((returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1
    WHERE  a.benchmarkid = b.benchmarkid AND b.year = var_curr_year;

    -- UPDATE benchmark_twrr_monthly
    -- SET    returns_fin_year = ((returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1
    -- WHERE  year = var_curr_year;
  ELSE
    -- first update with current March ending quarter
    UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
    SET    returns_twrr_since_fin_year = b.returns_mar_ending_quarter
    WHERE  a.benchmarkid = b.benchmarkid AND b.year = var_curr_year;

    -- then multimply wiht last 3 quarters of previous year
    UPDATE benchmark_twrr_summary a, benchmark_twrr_monthly b
    SET    returns_twrr_since_fin_year = ((returns_twrr_since_fin_year + 1) * (returns_jun_ending_quarter + 1) * (returns_sep_ending_quarter + 1) * (returns_dec_ending_quarter + 1)) - 1
    WHERE  a.benchmarkid = b.benchmarkid AND b.year = (var_curr_year - 1);
  END IF;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_benchmark_returns: End');

  commit;

END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_process_eod`()
BEGIN
	INSERT INTO log_table
	VALUES      (now(), 'ap_process_eod: Begin');

	call ap_update_wealth_data();

	call ap_process_stat_calculation();

	update mutual_fund_universe set fund_house =
	case when scheme_name_full like 'Aditya Birla%' then 'Aditya Birla'
	when scheme_name_full like 'Aditya Birla%' then 'Aditya Birla'
	when scheme_name_full like 'Baroda %' then 'Baroda Pioneer'
	when scheme_name_full like 'DSP %' then 'DSP'
	when scheme_name_full like 'HDFC %' then 'HDFC'
	when scheme_name_full like 'Principal %' then 'Principal'
	when scheme_name_full like 'Quant %' then 'Quant'
	when scheme_name_full like 'JM %' then 'JM'
	when scheme_name_full like 'Kotak %' then 'Kotak'
	when scheme_name_full like 'LIC %' then 'LIC'
	when scheme_name_full like 'Sahara %' then 'Sahara'
	when scheme_name_full like 'ICICI Prudential%' then 'ICICI Prudential'
	when scheme_name_full like 'Reliance %' then 'Reliance'
	when scheme_name_full like 'Tata %' then 'Tata'
	when scheme_name_full like 'Franklin %' then 'Franklin'
	when scheme_name_full like 'Taurus %' then 'Taurus'
	when scheme_name_full like 'Templeton %' then 'Templeton'
	when scheme_name_full like 'Canara Robeco%' then 'Canara Robeco'
	when scheme_name_full like 'Sundaram %' then 'Sundaram'
	when scheme_name_full like 'SBI %' then 'SBI Magnum'
	when scheme_name_full like 'UTI %' then 'UTI'
	when scheme_name_full like 'HSBC %' then 'HSBC'
	when scheme_name_full like 'Quantum %' then 'Quantum'
	when scheme_name_full like 'Invesco %' then 'Invesco'
	when scheme_name_full like 'Mirae %' then 'Mirae'
	when scheme_name_full like 'IDFC %' then 'IDFC'
	when scheme_name_full like 'BOI AXA%' then 'BOI AXA'
	when scheme_name_full like 'Edelweiss %' then 'Edelweiss'
	when scheme_name_full like 'Axis %' then 'Axis'
	when scheme_name_full like 'Essel %' then 'Essel'
	when scheme_name_full like 'L&T %' then 'L&T'
	when scheme_name_full like 'IDBI %' then 'IDBI'
	when scheme_name_full like 'Motilal Oswal%' then 'Motilal Oswal'
	when scheme_name_full like 'BNP Paribas%' then 'BNP Paribas'
	when scheme_name_full like 'Union %' then 'Union'
	when scheme_name_full like 'Indiabulls %' then 'Indiabulls'
	when scheme_name_full like 'DHFL %' then 'DHFL'
	when scheme_name_full like 'IIFL %' then 'IIFL'
	when scheme_name_full like 'Parag Parikh%' then 'Parag Parikh'
	when scheme_name_full like 'Shriram %' then 'Shriram'
	when scheme_name_full like 'IIFCL %' then 'IIFCL'
	when scheme_name_full like 'IL&FS %' then 'IL&FS'
	when scheme_name_full like 'Mahindra %' then 'Mahindra' end where fund_house = 'XXX';

	update mutual_fund_universe a set a.direct_regular = 'Regular' where direct_regular is null or direct_regular = '' or direct_regular not like '%Direct%';
	update mutual_fund_universe a set a.dividend_growth = 'Growth' where dividend_growth is null or dividend_growth = '' and a.isin_div_payout_or_isin_growth = 'XXX';
	update mutual_fund_universe a set a.dividend_growth = 'Dividend' where scheme_name_full like '%Div Option';

	update mutual_fund_universe a
	set a.scheme_name_part = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(a.scheme_name_full, 'Direct Plan', ' '), 'Regular Plan', ' '), 'Dividend Option', ' '), 'Div ',' '), 'Growth Option', ' '), 'Growth', ' ')
	where a.scheme_name_part = 'XXX';

	-- update short name with -Dir-G/D or -Reg-G/D
	update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Dir-G')
	where a.scheme_name_part not like '%-Dir-G' and dividend_growth = 'Growth' and direct_regular = 'Direct';
	update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Dir-D')
	where a.scheme_name_part not like '%-Dir-D' and dividend_growth = 'Dividend' and direct_regular = 'Direct';
	update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Reg-D')
	where a.scheme_name_part not like '%-Reg-D' and dividend_growth = 'Dividend' and direct_regular = 'Regular';
	update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Reg-G')
	where a.scheme_name_part not like '%-Reg-G' and dividend_growth = 'Growth' and direct_regular = 'Regular';
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, ' -   - ', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '-  -', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '- -', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, ' -  - - ', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, ' -  - -', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '-  - -', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '- - -', '-');

	-- update wealth details short name if there is any change in name
	update wealth_details a, mutual_fund_universe b set a.short_name = b.scheme_name_part, a.asset_classid = b.asset_classid where a.ticker = b.scheme_code;
	update wealth_details a, stock_universe b set a.short_name = b.short_name, a.subindustryid = b.subindustryid, a.asset_classid = b.asset_classid where a.ticker = b.ticker;
	update sip a, mutual_fund_universe b set a.scheme_name = b.scheme_name_part where a.scheme_code = b.scheme_code;

    call ap_process_benchmark_returns();

    COMMIT;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_eod: End');
END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_process_mf_returns`()
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

END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_process_mosl_transactions`()
BEGIN

    DECLARE var_finished, var_count INT DEFAULT 0;

    DECLARE var_moslcode VARCHAR(20);
    DECLARE var_script_name VARCHAR(100);
    DECLARE var_sell_buy VARCHAR(6);
    DECLARE var_fin_year VARCHAR(6);
    DECLARE var_date, var_date_today, var_buy_date DATE;
    DECLARE var_quantity, var_amount, var_brokerage, var_txn_charges, var_service_tax, var_stamp_duty, var_stt_ctt decimal(20,3) DEFAULT 0.000;
    DECLARE var_memberid, var_portfolioid, var_asset_classid, var_subindustryid, var_oldest_buy_quantity, var_remaining_quantity, var_total_buy INT DEFAULT 0;
    DECLARE var_ticker VARCHAR(30);
    DECLARE var_name VARCHAR(100);
    DECLARE var_short_name VARCHAR(100);
    DECLARE var_tax, var_buy_rate, var_buy_brokerage, var_buy_tax, var_buy_total_cost, var_buy_net_rate, var_annualized_return decimal(20,3);

    DECLARE mosl_transaction_cursor CURSOR FOR
    SELECT b.memberid, a.moslcode, a.portfolioid, date, script_name, sell_buy, sum(quantity), sum(amount), sum(brokerage), sum(txn_charges), sum(service_tax), sum(stamp_duty), sum(stt_ctt)
    FROM  mosl_transaction a, moslcode_memberid b
    WHERE (is_processed = NULL OR is_processed = 'N')
    AND a.moslcode = b.moslcode
    GROUP BY b.memberid, a.moslcode, date, script_name, sell_buy
    ORDER BY date, a.moslcode, sell_buy, order_no, trade_no, script_name;

    DECLARE CONTINUE HANDLER
    FOR NOT FOUND
    SET var_finished = 1;

    SET SQL_SAFE_UPDATES = 0;

    SELECT date_today INTO var_date_today from setup_dates;

    OPEN mosl_transaction_cursor;

    SET var_finished     = 0;

	INSERT INTO log_table
	VALUES      (now(), 'ap_process_mosl_transactions: Begin');


FETCH_DATA:
LOOP
    FETCH mosl_transaction_cursor
    INTO var_memberid, var_moslcode, var_portfolioid, var_date, var_script_name, var_sell_buy, var_quantity, var_amount, var_brokerage, var_txn_charges, var_service_tax, var_stamp_duty, var_stt_ctt;

    IF var_finished = 1
    THEN
      LEAVE fetch_data;
    END IF;

    IF (var_brokerage = NULL) THEN
		SET var_brokerage = 0;
    END IF;

    IF (var_txn_charges = NULL) THEN
		SET var_txn_charges = 0;
    END IF;

    IF (var_service_tax = NULL) THEN
		SET var_service_tax = 0;
    END IF;

    IF (var_stamp_duty = NULL) THEN
		SET var_stamp_duty = 0;
    END IF;

    IF (var_stt_ctt = NULL) THEN
		SET var_stt_ctt = 0;
    END IF;

    IF (month(var_date) < 4) THEN
		SET var_fin_year = concat('FY', year(var_date));
    ELSE
		SET var_count = year(var_date) + 1;
        SET var_fin_year = concat('FY', var_count);
        SET var_count = 0;
    END IF;

    SET var_tax = var_txn_charges + var_service_tax + var_stamp_duty + var_stt_ctt;

    SELECT count(1) INTO var_count
    FROM stock_universe a
    WHERE ticker = var_script_name;

	IF (var_count = 0 AND var_script_name != 'MOSL_CASH') THEN
		INSERT INTO log_table
		VALUES      (now(), concat('ap_process_mosl_transactions script_name NOT Found :', var_script_name));
    END IF;

    IF (var_count = 1) THEN
		SELECT ticker, short_name, name, asset_classid, subindustryid
        INTO var_ticker, var_short_name, var_name, var_asset_classid, var_subindustryid
        FROM stock_universe a WHERE ticker = var_script_name;

		-- INSERT INTO log_table
		-- VALUES      (now(), concat('ap_process_mosl_transactions: -', var_memberid, '-', var_portfolioid, '-', var_moslcode, '-', var_date, '-', var_script_name, '-', var_sell_buy, '-', var_quantity, '-', var_amount, '-', var_brokerage, '-', var_txn_charges, '-', var_service_tax, '-', var_stamp_duty, '-', var_stt_ctt));

		IF (trim((var_sell_buy)) = 'BUY') THEN
			-- Find if entry already exists in portfolio_holdings
            SELECT count(1) INTO var_count
            FROM portfolio_holdings a
            WHERE memberid = var_memberid
            AND portfolioid = var_portfolioid
            AND buy_date = var_date
            AND ticker = var_ticker;
			-- If no record found then insert into portfolio_holdings
            IF (var_count = 0) THEN
				INSERT INTO  portfolio_holdings  ( memberid ,  portfolioid ,  buy_date ,  ticker ,  name ,  short_name ,  asset_classid ,  subindustryid ,  quantity ,  rate ,  brokerage ,  tax ,  total_cost ,  net_rate ,  cmp ,  market_value ,  holding_period ,  net_profit ,  absolute_return ,  annualized_return ,  maturity_value ,  maturity_date ,  last_valuation_date )
				VALUES (var_memberid, var_portfolioid, var_date, var_ticker, var_name, var_short_name, var_asset_classid, var_subindustryid, var_quantity, (var_amount/var_quantity), var_brokerage, var_tax, (var_amount+var_brokerage+var_tax), ((var_amount+var_brokerage+var_tax)/var_quantity), ((var_amount+var_brokerage+var_tax)/var_quantity), (var_amount+var_brokerage+var_tax), 0, 0, 0, 0, 0, '2000-01-01', var_date_today);
			END IF;
            -- update process flag
            UPDATE mosl_transaction SET is_processed = 'Y'
            WHERE moslcode = var_moslcode AND
            date = var_date AND
            script_name = var_script_name AND
            sell_buy = var_sell_buy;

        ELSEIF (trim((var_sell_buy)) = 'SELL') THEN
			-- Find if entry already exists in portfolio_historical_holdings
			SELECT count(1) INTO var_count FROM portfolio_historical_holdings a
            WHERE memberid = var_memberid
            AND portfolioid = var_portfolioid
            AND sell_date = var_date
            AND ticker = var_ticker;
			-- If no record found then insert into portfolio_historical_holdings and edit/delete record portfolio_holdings
            IF (var_count = 0) THEN
				-- first find if sell quatity is not more than existing purchased quantities (assumption is no short sell applicable)
                SELECT sum(quantity)
                INTO var_total_buy
                FROM portfolio_holdings a
                WHERE memberid = var_memberid
                AND portfolioid = var_portfolioid
                AND ticker = var_ticker;

				IF (var_total_buy < var_quantity) THEN
					INSERT INTO log_table
					VALUES      (now(), concat('ap_process_mosl_transactions var_total_buy > var_quantity :', var_script_name, '-', var_total_buy, '-', var_quantity));
				END IF;
                IF (var_total_buy >= var_quantity) THEN
					-- first find quanity for min(buy_date)
					SELECT quantity, buy_date, rate, brokerage, tax, total_cost, net_rate
                    INTO var_oldest_buy_quantity, var_buy_date, var_buy_rate, var_buy_brokerage, var_buy_tax, var_buy_total_cost, var_buy_net_rate
					FROM portfolio_holdings a
					WHERE memberid = var_memberid
					AND portfolioid = var_portfolioid
					AND ticker = var_ticker
					AND buy_date = (SELECT min(buy_date) FROM portfolio_holdings b
									WHERE memberid = var_memberid
                                    AND portfolioid = var_portfolioid
									AND ticker = var_ticker);
					-- CASE 1: if var_oldest_buy_quantity = var_quanity ==> delete entry from portfolio_holdings AND INSERT 1 entry INTO portfolio_historical_holdings
					IF (var_oldest_buy_quantity = var_quantity) THEN

						INSERT INTO portfolio_historical_holdings (memberid, portfolioid, buy_date, ticker, name, short_name, asset_classid, subindustryid, quantity, rate, brokerage, tax, total_cost, net_rate, sell_date, sell_rate, brokerage_sell, tax_sell, net_sell, net_sell_rate, holding_period, net_profit, absolute_return, annualized_return, fin_year)
						VALUES (var_memberid, var_portfolioid, var_buy_date, var_ticker, var_name, var_short_name, var_asset_classid, var_subindustryid,
                        var_oldest_buy_quantity,
                        var_buy_rate,
                        var_buy_brokerage,
                        var_buy_tax,
                        var_buy_total_cost,
                        var_buy_net_rate,
                        var_date,
                        (var_amount/var_quantity),
                        var_brokerage,
                        var_tax,
                        (var_amount-var_brokerage-var_tax),
                        ((var_amount-var_brokerage-var_tax)/var_quantity),
                        0, 0, 0, 0, var_fin_year);

                        DELETE FROM portfolio_holdings
                        WHERE memberid = var_memberid
                        AND portfolioid = var_portfolioid
                        AND buy_date = var_buy_date
                        AND ticker = var_ticker;

					END IF;
					-- CASE 2: if var_oldest_buy_quantity > var_quanity ==> update portfolio_holdings entry AND INSERT 1 entry INTO portfolio_historical_holdings
					IF (var_oldest_buy_quantity > var_quantity) THEN

						INSERT INTO portfolio_historical_holdings (memberid, portfolioid, buy_date, ticker, name, short_name, asset_classid, subindustryid, quantity, rate, brokerage, tax, total_cost, net_rate, sell_date, sell_rate, brokerage_sell, tax_sell, net_sell, net_sell_rate, holding_period, net_profit, absolute_return, annualized_return, fin_year)
						VALUES (var_memberid, var_portfolioid, var_buy_date, var_ticker, var_name, var_short_name, var_asset_classid, var_subindustryid,
                        var_quantity,
                        var_buy_rate,
                        var_buy_brokerage * (var_quantity/var_oldest_buy_quantity),
                        var_buy_tax * (var_quantity/var_oldest_buy_quantity),
                        var_buy_total_cost * (var_quantity/var_oldest_buy_quantity),
                        var_buy_net_rate,
                        var_date,
                        (var_amount/var_quantity),
                        var_brokerage,
                        var_tax,
                        (var_amount-var_brokerage-var_tax),
                        ((var_amount-var_brokerage-var_tax)/var_quantity),
                        0, 0, 0, 0, var_fin_year);

                        UPDATE portfolio_holdings
                        SET quantity = (var_oldest_buy_quantity - var_quantity),
                        brokerage = brokerage * ((var_oldest_buy_quantity - var_quantity)/var_oldest_buy_quantity),
                        tax = tax * ((var_oldest_buy_quantity - var_quantity)/var_oldest_buy_quantity),
                        total_cost = total_cost * ((var_oldest_buy_quantity - var_quantity)/var_oldest_buy_quantity),
                        market_value = market_value * ((var_oldest_buy_quantity - var_quantity)/var_oldest_buy_quantity),
                        net_profit = market_value - total_cost
                        WHERE memberid = var_memberid
                        AND portfolioid = var_portfolioid
                        AND buy_date = var_buy_date
                        AND ticker = var_ticker;

                    END IF;
					-- CASE 3: if var_oldest_buy_quantity < var_quanity ==> check all sell or partial sell
                    IF (var_oldest_buy_quantity < var_quantity) THEN
						SET var_remaining_quantity = var_quantity;

						WHILE (var_remaining_quantity > 0) DO

							SELECT quantity, buy_date, rate, brokerage, tax, total_cost, net_rate
							INTO var_oldest_buy_quantity, var_buy_date, var_buy_rate, var_buy_brokerage, var_buy_tax, var_buy_total_cost, var_buy_net_rate
							FROM portfolio_holdings a
							WHERE memberid = var_memberid
							AND portfolioid = var_portfolioid
							AND ticker = var_ticker
							AND buy_date = (SELECT min(buy_date) FROM portfolio_holdings b
											WHERE memberid = var_memberid
											AND ticker = var_ticker
											AND portfolioid = var_portfolioid);

							IF (var_oldest_buy_quantity <= var_remaining_quantity AND var_remaining_quantity > 0) THEN

								INSERT INTO portfolio_historical_holdings (memberid, portfolioid, buy_date, ticker, name, short_name, asset_classid, subindustryid, quantity, rate, brokerage, tax, total_cost, net_rate, sell_date, sell_rate, brokerage_sell, tax_sell, net_sell, net_sell_rate, holding_period, net_profit, absolute_return, annualized_return, fin_year)
								VALUES (var_memberid, var_portfolioid, var_buy_date, var_ticker, var_name, var_short_name, var_asset_classid, var_subindustryid,
                                var_oldest_buy_quantity,
                                var_buy_rate,
                                var_buy_brokerage,
                                var_buy_tax,
                                var_buy_total_cost,
                                var_buy_net_rate,
                                var_date,
                                (var_amount/var_quantity),
                                (var_brokerage * (var_oldest_buy_quantity/var_quantity)),
                                (var_tax * (var_oldest_buy_quantity/var_quantity)),
                                (var_amount-var_brokerage-var_tax)*(var_oldest_buy_quantity/var_quantity),
                                ((var_amount-var_brokerage-var_tax)/var_quantity),
                                0, 0, 0, 0, var_fin_year);

								DELETE FROM portfolio_holdings
								WHERE memberid = var_memberid
								AND portfolioid = var_portfolioid
								AND buy_date = var_buy_date
								AND ticker = var_ticker;

							ELSEIF (var_oldest_buy_quantity > var_remaining_quantity AND var_remaining_quantity > 0) THEN

								INSERT INTO portfolio_historical_holdings (memberid, portfolioid, buy_date, ticker, name, short_name, asset_classid, subindustryid, quantity, rate, brokerage, tax, total_cost, net_rate, sell_date, sell_rate, brokerage_sell, tax_sell, net_sell, net_sell_rate, holding_period, net_profit, absolute_return, annualized_return, fin_year)
								VALUES (var_memberid, var_portfolioid, var_buy_date, var_ticker, var_name, var_short_name, var_asset_classid, var_subindustryid,
                                var_remaining_quantity,
                                var_buy_rate,
                                var_buy_brokerage * (var_remaining_quantity/var_oldest_buy_quantity),
                                var_buy_tax * (var_remaining_quantity/var_oldest_buy_quantity),
                                var_buy_total_cost * (var_remaining_quantity/var_oldest_buy_quantity),
                                var_buy_net_rate,
                                var_date,
                                (var_amount / var_quantity),
                                var_brokerage * (var_remaining_quantity/var_quantity),
                                var_tax * (var_remaining_quantity/var_quantity),
                                (var_amount-var_brokerage-var_tax) * (var_remaining_quantity/var_quantity),
                                ((var_amount-var_brokerage-var_tax)/var_quantity),
                                0, 0, 0, 0, var_fin_year);

								UPDATE portfolio_holdings
								SET quantity = (var_oldest_buy_quantity - var_remaining_quantity),
								brokerage = brokerage * ((var_oldest_buy_quantity - var_remaining_quantity)/var_oldest_buy_quantity),
								tax = tax * ((var_oldest_buy_quantity - var_remaining_quantity)/var_oldest_buy_quantity),
								total_cost = total_cost * ((var_oldest_buy_quantity - var_remaining_quantity)/var_oldest_buy_quantity),
								market_value = market_value * ((var_oldest_buy_quantity - var_remaining_quantity)/var_oldest_buy_quantity),
								net_profit = market_value - total_cost
								WHERE memberid = var_memberid
								AND portfolioid = var_portfolioid
								AND buy_date = var_buy_date
								AND ticker = var_ticker;
							END IF;
                            SET var_remaining_quantity = var_remaining_quantity - var_oldest_buy_quantity;
						END WHILE;
                    END IF;

					-- update process flag
					UPDATE mosl_transaction SET is_processed = 'Y'
					WHERE moslcode = var_moslcode AND
					date = var_date AND
					script_name = var_script_name AND
					sell_buy = var_sell_buy;
                END IF;

			END IF;

        END IF;
    END IF;

	UPDATE portfolio_holdings a, nse_price_history b
	SET    a.cmp = b.close_price
	WHERE  a.ticker = b.nse_ticker AND b.date = var_date_today;

	UPDATE portfolio_holdings a, bse_price_history b
	SET    a.cmp = b.close_price
	WHERE  a.ticker = b.bse_ticker AND b.date = var_date_today;

	UPDATE portfolio_holdings
	SET    market_value = cmp * quantity,
		 net_profit = market_value - total_cost,
		 holding_period = ROUND((DATEDIFF(var_date_today, buy_date) / 365.25), 2),
		 absolute_return = round((market_value / total_cost) - 1, 2),
         annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 2)
    WHERE holding_period > 1;

	UPDATE portfolio_holdings
	SET  annualized_return = absolute_return
    WHERE holding_period <= 1;
		 -- annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 2);
	-- WHERE asset_classid not in ('101010', '101020', '201010', '202010', '203010', '203020', '203050');

	UPDATE portfolio_historical_holdings
	SET  net_profit = net_sell - total_cost,
		 holding_period = ROUND((DATEDIFF(sell_date, buy_date) / 365.25), 2),
		 absolute_return = round((net_sell / total_cost) - 1, 2),
         annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 2)
	WHERE holding_period > 1;

    UPDATE portfolio_historical_holdings
	SET	 annualized_return = absolute_return
    WHERE holding_period <= 1;

    SET var_finished = 0;
END LOOP FETCH_DATA;

    CLOSE mosl_transaction_cursor;

	-- Update Cash
    UPDATE portfolio_holdings a, moslcode_memberid c, mosl_transaction b
	SET a.buy_date = b.date, a.rate = b.net_amount, a.total_cost = b.net_amount,
        a.net_rate = b.net_amount, a.cmp = b.net_amount, a.market_value = net_amount,
        a.holding_period = 0, a.net_profit = 0, a.absolute_return = 0, a.annualized_return = 0,
        b.is_processed = 'Y'
	WHERE a.memberid = c.memberid and a.portfolioid = b.portfolioid and b.moslcode = c.moslcode
	and b.script_name = 'MOSL_CASH' and a.ticker = 'MOSL_CASH' and b.is_processed = 'N';

    INSERT INTO log_table
    VALUES      (now(), 'ap_process_mosl_transactions: End');

    commit;
END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_process_portfolio_returns`( /*
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
      SET    returns_twrr_half_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_nov + 1)*(returns_dec + 1)) - 1
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
      SET    returns_twrr_half_year = ((returns_twrr_ytd + 1)*(returns_dec_ending_quarter + 1)*(returns_dec + 1)) - 1
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
      SET    returns_twrr_two_year = ((returns_twrr_ytd + 1)*(returns_twrr_one_year + 1)) - 1
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

END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_process_sip_history`(IN in_memberid INT, IN in_sipid INT)
BEGIN

  DECLARE var_finished, var_count, var_scheme_code, var_sip_freq, var_deduction_day INT DEFAULT 0;
  DECLARE var_sip_start_date, var_sip_end_date, var_date_today, var_next_process_date DATE;
  DECLARE var_sip_amount, var_new_units, var_nav DECIMAL(20, 3);

  INSERT INTO log_table
  VALUES      (now(), concat('ap_process_sip_history: Begin :: For memberid|sipid: ', in_memberid, '|', in_sipid));

  SELECT count(1)
  INTO var_count
  FROM sip
  WHERE  memberid = in_memberid
  AND sipid = in_sipid
  AND instrument_type = 'Mutual Fund'
  AND is_active = 'Yes';

  IF (var_count = 1) THEN
	  SELECT scheme_code, start_date,  end_date, deduction_day, amount, sip_freq
	  INTO var_scheme_code, var_sip_start_date,  var_sip_end_date, var_deduction_day, var_sip_amount, var_sip_freq
	  FROM sip
	  WHERE  memberid = in_memberid
	  AND sipid = in_sipid
	  AND instrument_type = 'Mutual Fund'
	  AND is_active = 'Yes';

	  SELECT count(1)
	  INTO var_count
	  FROM wealth_details
	  WHERE  memberid = in_memberid
      AND ticker = var_scheme_code
	  AND sipid = in_sipid
	  AND buy_date = var_sip_start_date;

      IF (var_count >= 1) THEN
		  INSERT INTO log_table
		  VALUES      (now(), concat('ap_process_sip_history: Sip already exist memberid|sipid|count: ', in_memberid, '|', in_sipid, '|', var_count));
      ELSE
          SELECT date_today INTO var_date_today FROM setup_dates;

          IF (var_deduction_day >= day(var_sip_start_date)) THEN
			SET var_next_process_date = Date_sub(var_sip_start_date, INTERVAL Dayofmonth(var_sip_start_date) - 1 DAY);
            SET var_next_process_date = date_add(var_next_process_date, INTERVAL (var_deduction_day - 1) DAY);
		  ELSE
			SET var_next_process_date = Date_sub(var_sip_start_date, INTERVAL Dayofmonth(var_sip_start_date) - 1 DAY);
            SET var_next_process_date = date_add(var_next_process_date, INTERVAL 1 MONTH); -- One month Freq is assumed: TODO for other freq.
            SET var_next_process_date = date_add(var_next_process_date, INTERVAL (var_deduction_day - 1) DAY);
          END IF;
          DELETE FROM sip_process_log WHERE memberid = in_memberid AND sipid = in_sipid;
          DELETE FROM wealth_details WHERE memberid = in_memberid AND ticker = var_scheme_code AND buy_date = var_sip_start_date;
          -- INSERT record into sip_process_log
		  INSERT INTO sip_process_log
			SELECT   b.memberid, b.sipid, b.scheme_code, var_next_process_date, '1900-01-01', var_next_process_date, 0, 0, 0, 0
			FROM     sip b
			WHERE    b.memberid = in_memberid
            AND      b.sipid = in_sipid;

          -- INSERT record into wealth_details
          INSERT INTO wealth_details VALUES (in_memberid, var_sip_start_date, var_scheme_code, '', '', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '1900-01-01', var_date_today, in_sipid, '');

		  WHILE (var_next_process_date <= var_date_today AND var_next_process_date <= var_sip_end_date) DO
			SELECT count(1) INTO var_count
			FROM mutual_fund_nav_history
			WHERE scheme_code = var_scheme_code
			AND date = var_next_process_date;

			IF(var_count = 1) THEN
				SELECT nav INTO var_nav
				FROM mutual_fund_nav_history
				WHERE scheme_code = var_scheme_code
				AND date = var_next_process_date;
                -- INSERT INTO log_table values (now(), concat('var_count:',var_count, '|',var_scheme_code,'|',var_next_process_date));
			ELSE
				SELECT nav INTO var_nav
				FROM mutual_fund_nav_history
				WHERE scheme_code = var_scheme_code
				AND date = (SELECT MIN(date) from mutual_fund_nav_history WHERE scheme_code = var_scheme_code AND date >= var_next_process_date);
                -- INSERT INTO log_table values (now(), concat('var_count:',var_count, '|',var_scheme_code,'|',var_next_process_date));
			END IF;

            IF(var_nav <> 0) THEN
				 SET var_new_units = ROUND(var_sip_amount/var_nav, 3);

                 -- INSERT INTO log_table
                -- VALUES      (now(), concat('ap_process_sip_history: var_next_process_date|var_nav|var_new_units: ', var_next_process_date, '|', var_nav, '|', var_new_units));

                 UPDATE sip_process_log a
                 SET last_process_date = next_process_date,
                 nav = var_nav,
                 units_added_last_sip = var_new_units,
                 next_process_date =  CASE  var_sip_freq
                                         WHEN 250 THEN date_add(var_next_process_date, INTERVAL 1 DAY)
                                         WHEN 52 THEN date_add(var_next_process_date, INTERVAL 8 DAY)
                                         WHEN 24 THEN date_add(var_next_process_date,  INTERVAL 15 DAY)
                                         WHEN 12 THEN date_add(var_next_process_date,  INTERVAL 1 MONTH)
                                         WHEN 4 THEN date_add(var_next_process_date,  INTERVAL 3 MONTH)
                                         WHEN 2 THEN date_add(var_next_process_date,  INTERVAL 6 MONTH)
                                         WHEN 1 THEN date_add(var_next_process_date,  INTERVAL 12 MONTH)
                                         ELSE var_next_process_date
									  END
                 WHERE a.memberid = in_memberid
                 AND a.sipid = in_sipid;

				 UPDATE wealth_details a
                 SET a.quantity = a.quantity + var_new_units, a.total_cost = a.total_cost + var_sip_amount,
                 a.rate = (a.total_cost/a.quantity), a.net_rate = a.rate, a.sipid = in_sipid
                 WHERE a.memberid = in_memberid
                 AND a.ticker = var_scheme_code
                 AND a.buy_date = var_sip_start_date;

            END IF;
            SET var_next_process_date = CASE  var_sip_freq
                                         WHEN 250 THEN date_add(var_next_process_date, INTERVAL 1 DAY)
                                         WHEN 52 THEN date_add(var_next_process_date, INTERVAL 8 DAY)
                                         WHEN 24 THEN date_add(var_next_process_date,  INTERVAL 15 DAY)
                                         WHEN 12 THEN date_add(var_next_process_date,  INTERVAL 1 MONTH)
                                         WHEN 4 THEN date_add(var_next_process_date,  INTERVAL 3 MONTH)
                                         WHEN 2 THEN date_add(var_next_process_date,  INTERVAL 6 MONTH)
                                         WHEN 1 THEN date_add(var_next_process_date,  INTERVAL 12 MONTH)
                                         ELSE var_next_process_date
									  END;
          END WHILE;

		  UPDATE wealth_details a, mutual_fund_universe b
          SET a.short_name = b.scheme_name_part, a.name = b.scheme_name_full,
          a.asset_classid = b.asset_classid, a.cmp = b.latest_nav,
          market_value = cmp * quantity,
          net_profit = market_value - total_cost,
          holding_period = ROUND((DATEDIFF(var_date_today, buy_date) / 365.25), 2),
          absolute_return = round((market_value / total_cost) - 1, 2),
          annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 2)
          WHERE a.ticker = b.scheme_code
          AND a.memberid = in_memberid
          AND a.buy_date = var_sip_start_date
          AND a.ticker = var_scheme_code;
      END IF;
  END IF;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_sip_history: End ');

END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_process_sips`()
BEGIN
  DECLARE var_finished, var_count, var_memberid, var_sipid, var_scheme_code INT DEFAULT 0;

  DECLARE var_date_today, var_date_last_trading_day, var_date_start_current_month,
          var_date_start_next_month, var_next_process_date, var_sip_start_date, var_sip_end_date DATE;

  DECLARE var_nav, var_sip_amount, var_new_units DECIMAL(20,3) DEFAULT 0;

  -- only process records for which next_process_date is between
  DECLARE sip_process_log_cursor CURSOR FOR
    SELECT memberid, sipid, scheme_code, next_process_date
    FROM   sip_process_log
    WHERE next_process_date <= var_date_today
    AND next_process_date >= var_date_last_trading_day -- both inclusive
    ORDER BY memberid, sipid;

  DECLARE CONTINUE HANDLER
  FOR NOT FOUND
    SET var_finished = 1;

  SET SQL_SAFE_UPDATES          = 0;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_sips: Begin');

  SELECT date_today, date_last_trading_day, date_start_current_month
  INTO   var_date_today, var_date_last_trading_day, var_date_start_current_month
  FROM   setup_dates;

  SET var_date_start_next_month = date_add(var_date_start_current_month, INTERVAL 1 MONTH);
  -- INSERT INTO log_table values (now(), concat('var_date_start_current_month: ',var_date_start_next_month));

  -- insert new Mutual Fund SIPs into sip_process_log tables with default values
  INSERT INTO sip_process_log
    SELECT   b.memberid, b.sipid, b.scheme_code, var_date_today, '1900-01-01', '1900-01-01', 0, 0, 0, 0
    FROM     sip b
    WHERE    (b.memberid, b.sipid) NOT in (SELECT memberid, sipid FROM sip_process_log)
    AND      b.instrument_type = 'Mutual Fund'
    AND      b.is_active = 'Yes'
    AND      b.sip_freq <> 0
    ORDER BY b.memberid, b.sipid;

  -- For all update original units and total cost into process log just in case if we need to ROLLBACK
  UPDATE sip_process_log a, wealth_details b
  SET a.units_before_addition = b.quantity, a.amount_cummulative = b.total_cost
  WHERE a.memberid = b.memberid
  AND a.sipid = b.sipid
  AND a.scheme_code = b.ticker;

  -- For newly added entries & monthly freq update next_process_date as this month's date if deduction date is grear than or equal to today's process date
  UPDATE sip_process_log a, sip b
  SET a.next_process_date = date_add(var_date_start_current_month, INTERVAL (b.deduction_day - 1) DAY)
  WHERE a.memberid = b.memberid
  AND a.sipid = b.sipid
  AND a.scheme_code = b.scheme_code
  AND a.next_process_date = '1900-01-01'
  AND b.sip_freq = 12 -- i.e. monthly
  AND b.deduction_day >= day(var_date_today)
  AND b.is_active = 'Yes';

  -- For newly added entries & monthly freq update next_process_date as next month's date if deduction date is less than today's process date
  UPDATE sip_process_log a, sip b
  SET a.next_process_date = date_add(var_date_start_next_month, INTERVAL (b.deduction_day - 1) DAY)
  WHERE a.memberid = b.memberid
  AND a.sipid = b.sipid
  AND a.scheme_code = b.scheme_code
  AND a.next_process_date = '1900-01-01'
  AND b.sip_freq = 12 -- i.e. monthly
  AND b.deduction_day < day(var_date_today)
  AND b.is_active = 'Yes';

  -- For newly added entries & daily freq update next_process_date as next month's date if deduction date is less than today's process date
  UPDATE sip_process_log a, sip b
  SET a.next_process_date = date_add(var_date_start_next_month, INTERVAL (b.deduction_day - 1) DAY)
  WHERE a.memberid = b.memberid
  AND a.sipid = b.sipid
  AND a.scheme_code = b.scheme_code
  AND a.next_process_date = '1900-01-01'
  AND b.sip_freq = 12 -- i.e. monthly
  AND b.deduction_day < day(var_date_today)
  AND b.is_active = 'Yes';

  DELETE FROM sip_process_msg_log where date = var_date_today;

  OPEN sip_process_log_cursor;

  SET var_finished              = 0;

 FETCH_DATA:
  LOOP
    FETCH sip_process_log_cursor INTO var_memberid, var_sipid, var_scheme_code, var_next_process_date;

    IF var_finished = 1
    THEN
      LEAVE fetch_data;
    END IF;

    SELECT end_date INTO var_sip_end_date
    FROM sip
    WHERE memberid = var_memberid
    AND sipid = var_sipid;

    IF (var_sip_end_date > var_next_process_date) THEN

		SELECT count(1) INTO var_count
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_next_process_date;

		IF(var_count = 1) THEN
			SELECT nav INTO var_nav
			FROM mutual_fund_nav_history
			WHERE scheme_code = var_scheme_code
			AND date = var_next_process_date;
		ELSE
			SELECT count(1) INTO var_count
			FROM mutual_fund_nav_history
			WHERE scheme_code = var_scheme_code
			AND date = var_date_today;

			IF (var_count = 1) THEN
				SELECT nav INTO var_nav
				FROM mutual_fund_nav_history
				WHERE scheme_code = var_scheme_code
				AND date = var_date_today;
			END IF;
		END IF;

		-- INSERT INTO log_table values (now(), concat('In sip_process_log_cursor: ',var_memberid, '|',var_sipid, '|',var_scheme_code));

		IF(var_nav <> 0) THEN
			/*SELECT nav INTO var_nav
			FROM mutual_fund_nav_history
			WHERE scheme_code = var_scheme_code
			AND date = var_next_process_date;*/

			SELECT amount, start_date INTO var_sip_amount, var_sip_start_date
			FROM sip
			WHERE memberid = var_memberid
			AND sipid = var_sipid;

			SET var_new_units = ROUND(var_sip_amount/var_nav, 3);

			SELECT count(1) INTO var_count FROM wealth_details a
			WHERE a.memberid = var_memberid
			AND a.ticker = var_scheme_code
			AND a.sipid = var_sipid;

			IF var_count > 1 THEN
				INSERT INTO sip_process_msg_log VALUES
				(var_date_today, var_memberid, var_sipid, var_scheme_code, 'ERR', 'More than 1 entry in Wealth Details for same sipid');
			ELSEIF var_count = 0 THEN
				-- This can be done: Check if linking of SIP is missing, then 1) link the scheme OR 2) INSERT fresh record
				SELECT count(1) INTO var_count
				FROM wealth_details a
				WHERE a.memberid = var_memberid
				AND a.ticker = var_scheme_code
				AND a.buy_date = var_sip_start_date;

				IF var_count = 0 THEN
					-- INSERT a FRESH ENTRY
					INSERT INTO wealth_details VALUES
					(var_memberid, var_sip_start_date, var_scheme_code, '', '', 0, 0, var_new_units, var_nav, 0, 0,
					 var_sip_amount, var_nav, var_nav, var_sip_amount, 0, 0, 0, 0, 0, '1900-01-01', var_date_today, var_sipid, '');

					 UPDATE sip_process_log a, sip b
					 SET last_process_date = next_process_date,
					 nav = var_nav,
					 units_added_last_sip = var_new_units,
					 next_process_date =  CASE  sip_freq
											 WHEN 250 THEN date_add(next_process_date, INTERVAL 1 DAY)
											 WHEN 52 THEN date_add(next_process_date, INTERVAL 8 DAY)
											 WHEN 24 THEN date_add(next_process_date,  INTERVAL 15 DAY)
											 WHEN 12 THEN date_add(next_process_date,  INTERVAL 1 MONTH)
											 WHEN 4 THEN date_add(next_process_date,  INTERVAL 3 MONTH)
											 WHEN 2 THEN date_add(next_process_date,  INTERVAL 6 MONTH)
											 WHEN 1 THEN date_add(next_process_date,  INTERVAL 12 MONTH)
											 ELSE next_process_date
										  END
					 WHERE a.memberid = var_memberid
					 AND a.sipid = var_sipid
					 AND a.memberid = b.memberid
					 AND a.sipid = b.sipid;

					 INSERT INTO sip_process_msg_log VALUES
					 (var_date_today, var_memberid, var_sipid, var_scheme_code, 'MSG', 'Fresh Entry Added in wealth_details');
				ELSEIF var_count = 1 THEN
					 UPDATE wealth_details a
					 SET a.quantity = a.quantity + var_new_units, a.total_cost = a.total_cost + var_sip_amount,
					 a.rate = (a.total_cost/a.quantity), a.net_rate = a.rate, a.sipid = var_sipid
					 WHERE a.memberid = var_memberid
					 AND a.ticker = var_scheme_code
					 AND a.buy_date = var_sip_start_date;

					 UPDATE sip_process_log a, sip b
					 SET last_process_date = next_process_date,
					 nav = var_nav,
					 units_added_last_sip = var_new_units,
					 next_process_date =  CASE sip_freq
											 WHEN 250 THEN date_add(next_process_date,  INTERVAL 1 DAY)
											 WHEN 52 THEN date_add(next_process_date,  INTERVAL 8 DAY)
											 WHEN 24 THEN date_add(next_process_date,  INTERVAL 15 DAY)
											 WHEN 12 THEN date_add(next_process_date,  INTERVAL 1 MONTH)
											 WHEN 4 THEN date_add(next_process_date,  INTERVAL 3 MONTH)
											 WHEN 2 THEN date_add(next_process_date,  INTERVAL 6 MONTH)
											 WHEN 1 THEN date_add(next_process_date,  INTERVAL 12 MONTH)
											 ELSE next_process_date
										  END
					 WHERE a.memberid = var_memberid
					 AND a.sipid = var_sipid
					 AND a.memberid = b.memberid
					 AND a.sipid = b.sipid;

					INSERT INTO sip_process_msg_log VALUES
					 (var_date_today, var_memberid, var_sipid, var_scheme_code, 'MSG', 'Linked sipid to existing Entry in wealth_details');
				END IF;
			ELSE
				-- update wealth details
				UPDATE wealth_details a
				SET a.quantity = a.quantity + var_new_units, a.total_cost = a.total_cost + var_sip_amount,
				a.rate = (a.total_cost/a.quantity), a.net_rate = a.rate
				WHERE a.memberid = var_memberid
				AND a.ticker = var_scheme_code
				AND a.sipid = var_sipid;

				 UPDATE sip_process_log a, sip b
				 SET last_process_date = next_process_date,
				 nav = var_nav,
				 units_added_last_sip = var_new_units,
				 next_process_date =  CASE sip_freq
										 WHEN 250 THEN date_add(next_process_date,  INTERVAL 1 DAY)
										 WHEN 52 THEN date_add(next_process_date,  INTERVAL 8 DAY)
										 WHEN 24 THEN date_add(next_process_date,  INTERVAL 15 DAY)
										 WHEN 12 THEN date_add(next_process_date,  INTERVAL 1 MONTH)
										 WHEN 4 THEN date_add(next_process_date,  INTERVAL 3 MONTH)
										 WHEN 2 THEN date_add(next_process_date,  INTERVAL 6 MONTH)
										 WHEN 1 THEN date_add(next_process_date,  INTERVAL 12 MONTH)
										 ELSE next_process_date
									  END
				 WHERE a.memberid = var_memberid
				 AND a.sipid = var_sipid
				 AND a.memberid = b.memberid
				 AND a.sipid = b.sipid;

				INSERT INTO sip_process_msg_log VALUES
				 (var_date_today, var_memberid, var_sipid, var_scheme_code, 'MSG', 'Updated existing Entry in wealth_details');
			END IF;

		ELSE
			INSERT INTO sip_process_msg_log VALUES
			(var_date_today, var_memberid, var_sipid, var_scheme_code, 'ERR', 'No NAV record found in mutual_fund_nav_history');
		END IF;
    END IF;
  END LOOP fetch_data;

  CLOSE sip_process_log_cursor;

  commit;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_sips: End');


END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_process_stat_calculation`()
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
      '', 0, close_price, date, 0,0,0,0,0,0,0,0,0,0,0,0, '', date
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

  -- Compute Benchmark Returns
  -- call ap_process_benchmark_returns();

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_stat_calculation: End');

  commit;
END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_process_stock_returns`()
BEGIN

  DECLARE var_finished, var_count INT DEFAULT 0;
  DECLARE var_price_today, var_price_history, var_52w_min, var_52w_max DECIMAL(20,3);
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
  SET CMP = 0, 52w_min = 0, 52w_max = 0, up_52w_min = 0, down_52w_max = 0,
      return_1D = 0, return_1W = 0, return_2W = 0, return_1M = 0, return_2M = 0,
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
	FROM stock_universe
	WHERE nse_code = var_ticker;

    IF (var_count > 0) THEN
		SET var_is_nse_ticker = 'Y';
	ELSE
		SET var_is_nse_ticker = 'N';
    END IF;

    SET var_count = 0;
    SET var_price_today = 0;

	IF (var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
        date = var_date_today;

        IF (var_count > 0) THEN
			SELECT close_price
			INTO var_price_today
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = var_date_today;
        END IF;
    ELSE
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
        date = var_date_today;

        IF (var_count > 0) THEN
			SELECT close_price
			INTO var_price_today
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = var_date_today;
        END IF;
    END IF;

    UPDATE stock_price_movement
    SET CMP = var_price_today
    WHERE ticker = var_ticker;

    SET var_price_history = 0;

	IF (var_price_today > 0 AND var_is_nse_ticker = 'Y') THEN
		SELECT count(1)
		INTO var_count
		FROM nse_price_history
		WHERE nse_ticker = var_ticker AND
		date = var_date_last_trading_day;

		IF (var_count > 0) THEN
			SELECT close_price
			INTO var_price_history
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM nse_price_history
					WHERE nse_ticker = var_ticker AND
					date = var_date_last_trading_day);
		END IF;
	END IF;
    IF (var_price_today > 0 AND var_is_nse_ticker = 'N') THEN
		SELECT count(1)
		INTO var_count
		FROM bse_price_history
		WHERE bse_ticker = var_ticker AND
		date = var_date_last_trading_day;

		IF (var_count > 0) THEN
			SELECT close_price
			INTO var_price_history
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date = (SELECT MAX(date)
					FROM bse_price_history
					WHERE bse_ticker = var_ticker AND
					date = var_date_last_trading_day);
		END IF;
    END IF;

	-- INSERT INTO log_table
	-- VALUES      (now(), concat('ap_process_stock_returns: var_ticker-', var_ticker, '-IsNSE?-',var_is_nse_ticker, '-CMP-', var_price_today, '-var_price_history-',var_price_history ));

	IF (var_price_today <> 0 AND var_price_history <> 0) THEN
		UPDATE stock_price_movement
		SET return_1D = ((var_price_today/var_price_history) - 1)*100
		WHERE ticker = var_ticker;
    END IF;

    IF (var_price_today <> 0) THEN
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

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_1W = ((var_price_today/var_price_history) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

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

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_2W = ((var_price_today/var_price_history) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

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

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_1M = ((var_price_today/var_price_history) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

		END IF;

		-- 2 Month Change
		SET var_count = 0;
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT count(1)
			INTO var_count
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date <= var_date_2_mt_before AND
			date > var_date_3_mt_before;
		ELSE
			SELECT count(1)
			INTO var_count
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date <= var_date_2_mt_before AND
			date > var_date_3_mt_before;
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
						date <= var_date_2_mt_before AND
						date > var_date_3_mt_before);
			ELSE
				SELECT close_price
				INTO var_price_history
				FROM bse_price_history
				WHERE bse_ticker = var_ticker AND
				date = (SELECT MAX(date)
						FROM bse_price_history
						WHERE bse_ticker = var_ticker AND
						date <= var_date_2_mt_before AND
						date > var_date_3_mt_before);
			END IF;

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_2M = ((var_price_today/var_price_history) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

		END IF;

		-- 3 Month Change
		SET var_count = 0;
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT count(1)
			INTO var_count
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date <= var_date_3_mt_before AND
			date > var_date_6_mt_before;
		ELSE
			SELECT count(1)
			INTO var_count
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date <= var_date_3_mt_before AND
			date > var_date_6_mt_before;
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
						date <= var_date_3_mt_before AND
						date > var_date_6_mt_before);
			ELSE
				SELECT close_price
				INTO var_price_history
				FROM bse_price_history
				WHERE bse_ticker = var_ticker AND
				date = (SELECT MAX(date)
						FROM bse_price_history
						WHERE bse_ticker = var_ticker AND
						date <= var_date_3_mt_before AND
						date > var_date_6_mt_before);
			END IF;

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_3M = ((var_price_today/var_price_history) - 1)*100
				WHERE ticker = var_ticker;
			END IF;
		END IF;

		-- 6 Month Change
		SET var_count = 0;
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT count(1)
			INTO var_count
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date <= var_date_6_mt_before AND
			date > var_date_9_mt_before;
		ELSE
			SELECT count(1)
			INTO var_count
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date <= var_date_6_mt_before AND
			date > var_date_9_mt_before;
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
						date <= var_date_6_mt_before AND
						date > var_date_9_mt_before);
			ELSE
				SELECT close_price
				INTO var_price_history
				FROM bse_price_history
				WHERE bse_ticker = var_ticker AND
				date = (SELECT MAX(date)
						FROM bse_price_history
						WHERE bse_ticker = var_ticker AND
						date <= var_date_6_mt_before AND
						date > var_date_9_mt_before);
			END IF;

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_6M = ((var_price_today/var_price_history) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

		END IF;

		-- 9 Month Change
		SET var_count = 0;
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT count(1)
			INTO var_count
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date <= var_date_9_mt_before AND
			date > var_date_1_yr_before;
		ELSE
			SELECT count(1)
			INTO var_count
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date <= var_date_9_mt_before AND
			date > var_date_1_yr_before;
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
						date <= var_date_9_mt_before AND
						date > var_date_1_yr_before);
			ELSE
				SELECT close_price
				INTO var_price_history
				FROM bse_price_history
				WHERE bse_ticker = var_ticker AND
				date = (SELECT MAX(date)
						FROM bse_price_history
						WHERE bse_ticker = var_ticker AND
						date <= var_date_9_mt_before AND
						date > var_date_1_yr_before);
			END IF;

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_9M = ((var_price_today/var_price_history) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

		END IF;

		-- 1 Year Change
		SET var_count = 0;
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT count(1)
			INTO var_count
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date <= var_date_1_yr_before AND
			date > var_date_2_yr_before;
		ELSE
			SELECT count(1)
			INTO var_count
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date <= var_date_1_yr_before AND
			date > var_date_2_yr_before;
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
						date <= var_date_1_yr_before AND
						date > var_date_2_yr_before);
			ELSE
				SELECT close_price
				INTO var_price_history
				FROM bse_price_history
				WHERE bse_ticker = var_ticker AND
				date = (SELECT MAX(date)
						FROM bse_price_history
						WHERE bse_ticker = var_ticker AND
						date <= var_date_1_yr_before AND
						date > var_date_2_yr_before);
			END IF;

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_1Y = ((var_price_today/var_price_history) - 1)*100
				WHERE ticker = var_ticker;
			END IF;
		END IF;

		-- 52W High & Low
		SET var_count = 0;
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT count(1)
			INTO var_count
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date <= var_date_today AND
			date > var_date_1_yr_before;
		ELSE
			SELECT count(1)
			INTO var_count
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date <= var_date_today AND
			date > var_date_1_yr_before;
		END IF;

		IF (var_count > 0) THEN
			IF (var_is_nse_ticker = 'Y') THEN
				SELECT min(close_price), max(close_price)
				INTO var_52w_min, var_52w_max
				FROM nse_price_history
				WHERE nse_ticker = var_ticker
				AND date > var_date_1_yr_before;
			ELSE
				SELECT min(close_price), max(close_price)
				INTO var_52w_min, var_52w_max
				FROM bse_price_history
				WHERE bse_ticker = var_ticker
				AND date > var_date_1_yr_before;
			END IF;

			IF (var_price_today <> 0 AND var_52w_min <> 0 AND var_52w_max <> 0) THEN
				UPDATE stock_price_movement
				SET 52w_min = var_52w_min, 52w_max = var_52w_max,
				up_52w_min = ((var_price_today/var_52w_min) - 1)*100,
				down_52w_max = ((var_price_today/var_52w_max) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

		END IF;

		-- 2 Year Change
		SET var_count = 0;
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT count(1)
			INTO var_count
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date <= var_date_2_yr_before AND
			date > var_date_3_yr_before;
		ELSE
			SELECT count(1)
			INTO var_count
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date <= var_date_2_yr_before AND
			date > var_date_3_yr_before;
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
						date <= var_date_2_yr_before AND
						date > var_date_3_yr_before);
			ELSE
				SELECT close_price
				INTO var_price_history
				FROM bse_price_history
				WHERE bse_ticker = var_ticker AND
				date = (SELECT MAX(date)
						FROM bse_price_history
						WHERE bse_ticker = var_ticker AND
						date <= var_date_2_yr_before AND
						date > var_date_3_yr_before);
			END IF;

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_2Y = (pow((var_price_today/var_price_history), 0.5) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

		END IF;

		-- 3 Year Change
		SET var_count = 0;
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT count(1)
			INTO var_count
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date <= var_date_3_yr_before AND
			date > var_date_5_yr_before;
		ELSE
			SELECT count(1)
			INTO var_count
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date <= var_date_3_yr_before AND
			date > var_date_5_yr_before;
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
						date <= var_date_3_yr_before AND
						date > var_date_5_yr_before);
			ELSE
				SELECT close_price
				INTO var_price_history
				FROM bse_price_history
				WHERE bse_ticker = var_ticker AND
				date = (SELECT MAX(date)
						FROM bse_price_history
						WHERE bse_ticker = var_ticker AND
						date <= var_date_3_yr_before AND
						date > var_date_5_yr_before);
			END IF;

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_3Y = (pow((var_price_today/var_price_history), (1/3)) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

		END IF;

		-- 5 Year Change
		SET var_count = 0;
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT count(1)
			INTO var_count
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date <= var_date_5_yr_before AND
			date > var_date_10_yr_before;
		ELSE
			SELECT count(1)
			INTO var_count
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date <= var_date_5_yr_before AND
			date > var_date_10_yr_before;
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
						date <= var_date_5_yr_before AND
						date > var_date_10_yr_before);
			ELSE
				SELECT close_price
				INTO var_price_history
				FROM bse_price_history
				WHERE bse_ticker = var_ticker AND
				date = (SELECT MAX(date)
						FROM bse_price_history
						WHERE bse_ticker = var_ticker AND
						date <= var_date_5_yr_before AND
						date > var_date_10_yr_before);
			END IF;

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_5Y = (pow((var_price_today/var_price_history), 0.2) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

		END IF;

		-- 10 Year Change
		SET var_count = 0;
		IF (var_is_nse_ticker = 'Y') THEN
			SELECT count(1)
			INTO var_count
			FROM nse_price_history
			WHERE nse_ticker = var_ticker AND
			date <= var_date_10_yr_before;
		ELSE
			SELECT count(1)
			INTO var_count
			FROM bse_price_history
			WHERE bse_ticker = var_ticker AND
			date <= var_date_10_yr_before;
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
						date <= var_date_10_yr_before);
			ELSE
				SELECT close_price
				INTO var_price_history
				FROM bse_price_history
				WHERE bse_ticker = var_ticker AND
				date = (SELECT MAX(date)
						FROM bse_price_history
						WHERE bse_ticker = var_ticker AND
						date <= var_date_10_yr_before);
			END IF;

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_10Y = (pow((var_price_today/var_price_history), 0.1) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

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

			IF (var_price_today <> 0 AND var_price_history <> 0) THEN
				UPDATE stock_price_movement
				SET return_YTD = ((var_price_today/var_price_history) - 1)*100
				WHERE ticker = var_ticker;
			END IF;

		END IF;
    END IF;
  END LOOP fetch_data;

  CLOSE stock_price_movement_cursor;

  DELETE FROM stock_price_movement_history WHERE date = var_date_today;
  INSERT INTO stock_price_movement_history (date, ticker, return_1D, return_1W, return_2W, return_1M)
  (SELECT var_date_today, ticker,  return_1D, return_1W, return_2W, return_1M FROM stock_price_movement);

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_stock_returns: End');

  commit;

END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_process_stock_returns_history`()
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

END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_set_market_cap_rank`(
   IN pi_date_today       DATE,
   IN pi_date_prior   DATE)
BEGIN
   DECLARE date1   date;
   SET date1 = pi_date_today;

   WHILE (date1 > pi_date_prior)
   DO
      SET @rowcount = 0;
      UPDATE daily_data_b  a SET a.market_cap_rank = (@rowcount:=@rowcount+1)
      WHERE a.date = date1
      AND a.ticker_b NOT IN ('NIFTY:IND', 'SENSEX:IND','CFIN:IND', 'NBEES:IN','JBEES:IN')
      -- ('SENSEX:IND','NIFTY:IND','NBEES:IN','JBEES:IN','BSE500:IND','BSE200:IND','BSE100:IND','BBEES:IN')
      ORDER BY market_cap desc;

      SET @rowcount = 0;
      UPDATE daily_data_s  a SET a.rank = (@rowcount:=@rowcount+1)
      WHERE a.date = date1
      -- AND a.ticker_b NOT IN ('NIFTY:IND', 'SENSEX:IND','CFIN:IND', 'NBEES:IN','JBEES:IN')
      -- ('SENSEX:IND','NIFTY:IND','NBEES:IN','JBEES:IN','BSE500:IND','BSE200:IND','BSE100:IND','BBEES:IN')
      ORDER BY market_cap desc;

      SET date1 = date1 - INTERVAL 1 DAY;

   END WHILE;
   commit;
END$$
DELIMITER ;

DELIMITER $$
CREATE  PROCEDURE `ap_update_wealth_data`()
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

  -- update portfolio data
   -- update CMP of FD
  UPDATE portfolio_holdings
  SET    cmp = total_cost + (maturity_value - total_cost) * (DATEDIFF(var_date_today, buy_date) / DATEDIFF(maturity_date, buy_date))
  WHERE  maturity_date > var_date_today
  AND asset_classid in (201010, 202010, 203010);

  -- CMP of stocks
  UPDATE portfolio_holdings a, nse_price_history b
  SET    a.cmp = b.close_price
  WHERE  a.ticker = b.nse_ticker AND b.date = var_date_today;

  UPDATE portfolio_holdings a, bse_price_history b
  SET    a.cmp = b.close_price
  WHERE  a.ticker = b.bse_ticker AND b.date = var_date_today;

  UPDATE portfolio_holdings a, mutual_fund_nav_history b
  SET    a.cmp = b.nav
  WHERE  a.ticker = b.scheme_code AND b.date = var_date_today;

  -- query to update asset_classid, name, short_name, subindustryid
  UPDATE portfolio_holdings a, stock_universe b
  SET a.asset_classid = b.asset_classid,
      a.name = b.name,
      a.short_name = b.short_name,
      a.subindustryid = b.subindustryid
  WHERE a.ticker = b.ticker;

  -- query to update CMP, market_vaue, net_profit & absolute_return
  UPDATE portfolio_holdings a
  SET total_cost = (quantity * rate) + brokerage + tax,
      net_rate = total_cost/quantity,
      market_value = cmp * quantity,
      net_profit = market_value - total_cost,
      holding_period = ROUND((DATEDIFF(var_date_today, buy_date) / 365.25), 2),
      absolute_return = round((market_value / total_cost) - 1, 4),
      annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 4)
  WHERE asset_classid not in ('101010', '101020', '201010', '202010', '203010', '203020', '203050')
  AND holding_period > 1;

  UPDATE portfolio_holdings a
  SET total_cost = (quantity * rate) + brokerage + tax,
      net_rate = total_cost/quantity,
      market_value = cmp * quantity,
      net_profit = market_value - total_cost,
      holding_period = ROUND((DATEDIFF(var_date_today, buy_date) / 365.25), 2),
      absolute_return = round((market_value / total_cost) - 1, 4),
      annualized_return = absolute_return
  WHERE asset_classid not in ('101010', '101020', '201010', '202010', '203010', '203020', '203050')
  AND holding_period <= 1;

  UPDATE portfolio_historical_holdings a
  SET total_cost = (quantity * rate) + brokerage + tax,
      net_rate = total_cost/quantity,
      net_sell = (quantity *sell_rate) - brokerage_sell - tax_sell,
      net_sell_rate = net_sell/quantity,
      net_profit = net_sell - total_cost,
      holding_period = ROUND((DATEDIFF(sell_date, buy_date) / 365.25), 2),
      absolute_return = round((net_sell / total_cost) - 1, 4),
      annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 4)
  WHERE holding_period > 1;

  UPDATE portfolio_historical_holdings a
  SET total_cost = (quantity * rate) + brokerage + tax,
      net_rate = total_cost/quantity,
      net_sell = (quantity *sell_rate) - brokerage_sell - tax_sell,
      net_sell_rate = net_sell/quantity,
      net_profit = net_sell - total_cost,
      holding_period = ROUND((DATEDIFF(sell_date, buy_date) / 365.25), 2),
      absolute_return = round((net_sell / total_cost) - 1, 4),
      annualized_return = absolute_return
  WHERE holding_period <= 1;

  DELETE FROM portfolio_value_history
  WHERE  date = var_date_today;

  INSERT INTO portfolio_value_history
    (SELECT   memberid, portfolioid, var_date_today, TRUNCATE(SUM(market_value), 2)
      FROM     portfolio_holdings
      GROUP BY memberid, portfolioid);

  DELETE FROM portfolio_asset_allocation
  WHERE  date = var_date_today;

  INSERT INTO portfolio_asset_allocation
    SELECT   memberid, portfolioid, var_date_today, b.asset_class_group, round(sum(market_value), 2), 0.0
    FROM     portfolio_holdings a, asset_classification b
    WHERE a.asset_classid = b.classid
    GROUP BY a.memberid, a.portfolioid, b.asset_class_group;

  UPDATE portfolio_asset_allocation a, portfolio_value_history b
  SET a.value_percent = (a.value/b.value)
  WHERE a.memberid = b.memberid
  AND a.portfolioid = b.portfolioid
  AND a.date = b.date
  AND b.date = var_date_today;

  call ap_process_portfolio_returns;
  -- call ap_process_benchmark_returns;

  UPDATE portfolio a
  SET a.net_investment = (SELECT SUM(b.cashflow)*-1
                          FROM portfolio_cashflow b
                          GROUP BY b.memberid, b.portfolioid
                          HAVING b.memberid = a.memberid
						  AND b.portfolioid = a.portfolioid);

  UPDATE portfolio a, portfolio_value_history b
  SET a.market_value = b.value,
	  a.net_profit = b.value - a.net_investment,
      a.holding_period = ROUND((DATEDIFF(var_date_today, a.start_date) / 365.25), 2),
      a.absolute_return = round((b.value / a.net_investment) - 1, 4)
  WHERE a.memberid = b.memberid
  AND a.portfolioid = b.portfolioid
  AND b.date = var_date_today;

  UPDATE portfolio a, portfolio_twrr_summary b
  SET a.annualized_return = returns_twrr_since_inception
  WHERE a.memberid = b.memberid
  AND a.portfolioid = b.portfolioid
  AND b.benchmarkid = 0;

  -- update Wealth_details
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

  -- DELETE and INSERT from portfolios
  DELETE FROM wealth_details
  -- WHERE (memberid, buy_date, ticker)
  -- IN (SELECT memberid, start_date, concat(memberid,'-',portfolioid) FROM portfolio WHERE status = 'Active');
  WHERE (memberid, ticker)
  IN (SELECT memberid, concat(memberid,'-',portfolioid) FROM portfolio WHERE status = 'Active');

  INSERT INTO wealth_details
  (SELECT a.memberid, start_date, concat(a.memberid,'-',a.portfolioid), a.description, a.description, asset_classid, 0, 1, net_investment, 0, 0, net_investment, net_investment, market_value, market_value, holding_period, net_profit, absolute_return, annualized_return, 0, '2000-01-01', var_date_today, 0, 0
   FROM portfolio a, composite b
   WHERE a.compositeid = b.compositeid
   AND a.status = 'Active'
   AND (a.memberid, a.portfolioid, a.start_date) NOT IN (SELECT memberid, SUBSTRING_INDEX(ticker,'-',-1), buy_date FROM wealth_details));

  -- query to update CMP, market_vaue, net_profit & absolute_return
  UPDATE wealth_details
  SET    market_value = cmp * quantity,
         net_profit = market_value - total_cost,
         holding_period = ROUND((DATEDIFF(var_date_today, buy_date) / 365.25), 2),
         absolute_return = round((market_value / total_cost) - 1, 4),
         annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 4)
  WHERE asset_classid not in ('101010', '101020', '201010', '202010', '203010', '203020', '203050')
  AND holding_period > 1;

  UPDATE wealth_details
  SET    market_value = cmp * quantity,
         net_profit = market_value - total_cost,
         holding_period = ROUND((DATEDIFF(var_date_today, buy_date) / 365.25), 2),
         absolute_return = round((market_value / total_cost) - 1, 4),
         annualized_return = absolute_return
  WHERE asset_classid not in ('101010', '101020', '201010', '202010', '203010', '203020', '203050')
  AND holding_period <= 1;

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

  UPDATE wealth_asset_allocation_history a, wealth_history b
  SET a.value_percent = (a.value/b.value)
  WHERE a.memberid = b.memberid
  AND a.date = b.date
  AND b.date = var_date_today;

  INSERT INTO log_table
  VALUES      (now(), 'ap_update_wealth_data: End');

  commit;

END$$
DELIMITER ;
