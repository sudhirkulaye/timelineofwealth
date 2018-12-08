DROP PROCEDURE IF EXISTS ap_process_sips;
CREATE PROCEDURE ap_process_sips()
BEGIN
  DECLARE var_finished, var_count, var_memberid, var_sipid, var_scheme_code INT DEFAULT 0;

  DECLARE var_date_today, var_date_last_trading_day, var_date_start_current_month,
          var_date_start_next_month, var_next_process_date, var_sip_start_date DATE;

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

    INSERT INTO log_table values (now(), concat('In sip_process_log_cursor: ',var_memberid, '|',var_sipid, '|',var_scheme_code));

    IF(var_nav <> 0) THEN
		SELECT nav INTO var_nav
		FROM mutual_fund_nav_history
		WHERE scheme_code = var_scheme_code
		AND date = var_next_process_date;

		SELECT amount, start_date INTO var_sip_amount, var_sip_start_date
		FROM sip
		WHERE memberid = var_memberid
        AND sipid = var_sipid;

        IF var_nav <> 0 THEN
			SET var_new_units = ROUND(var_sip_amount/var_nav, 3);
		END IF;

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
                 var_sip_amount, var_nav, var_nav, var_sip_amount, 0, 0, 0, 0, 0, '1900-01-01', var_date_today, var_sipid);

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


  END LOOP fetch_data;

  CLOSE sip_process_log_cursor;

  commit;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_sips: End');


END;
