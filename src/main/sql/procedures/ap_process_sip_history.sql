DROP PROCEDURE IF EXISTS ap_process_sip_history;
CREATE PROCEDURE ap_process_sip_history(IN in_memberid INT, IN in_sipid INT)
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
          INSERT INTO wealth_details VALUES (in_memberid, var_sip_start_date, var_scheme_code, '', '', 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, '1900-01-01', var_date_today, in_sipid);

		  WHILE (var_next_process_date <= var_date_today) DO
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

END;
