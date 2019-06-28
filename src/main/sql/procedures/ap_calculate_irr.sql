DROP PROCEDURE IF EXISTS ap_calculate_irr;
CREATE PROCEDURE ap_calculate_irr(INOUT out_irr DECIMAL(20, 4),
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
      SET var_holding_period = ROUND((DATEDIFF(var_date, in_min_date) / 365.25), 2);
      /*
      if (in_memberid = 1014) THEN
        insert into log_table  VALUES (now(), concat(' var_absolute_return',var_absolute_return));
        insert into log_table  VALUES (now(), concat(' 1/holding period ',1/var_holding_period));
        insert into log_table  VALUES (now(), concat(' rate ',round((var_absolute_return-1/var_holding_period),2)));
      END if;
      */
      SET Rate = round(((var_absolute_return-1)/var_holding_period),2);

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
END