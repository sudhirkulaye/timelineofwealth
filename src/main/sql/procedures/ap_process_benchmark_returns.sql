DROP PROCEDURE IF EXISTS ap_process_benchmark_returns;
CREATE PROCEDURE ap_process_benchmark_returns( /*
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

END