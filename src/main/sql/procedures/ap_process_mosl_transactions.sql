DROP PROCEDURE IF EXISTS ap_process_mosl_transactions;
CREATE PROCEDURE ap_process_mosl_transactions ()
BEGIN

    DECLARE var_finished, var_count INT DEFAULT 0;
    DECLARE var_mosl_code VARCHAR(20);
    DECLARE var_script_name VARCHAR(100);
    DECLARE var_sell_buy VARCHAR(6);
    DECLARE var_date DATE;
    DECLARE var_quantity, var_rate, var_brokerage, var_txn_charges, var_stamp_duty, var_stt_ctt decimal(20,4) DEFAULT 0.0000;

    DECLARE mosl_transaction_cursor CURSOR FOR
    SELECT mosl_code, date, script_name, sell_buy, quantity, rate, brokerage, txn_charges, stamp_duty, stt_ctt
    FROM  mosl_transaction
    WHERE (is_processed = NULL OR is_processed = 'N')
    ORDER BY date, mosl_code, sell_buy, order_no, trade_no, script_name;

    DECLARE CONTINUE HANDLER
    FOR NOT FOUND
    SET var_finished = 1;

    SET SQL_SAFE_UPDATES = 0;

    OPEN mosl_transaction_cursor;

    SET var_finished     = 0;

	INSERT INTO log_table
	VALUES      (now(), 'ap_process_mosl_transactions: Begin');


FETCH_DATA:
LOOP
    FETCH mosl_transaction_cursor INTO var_mosl_code, var_date, var_script_name, var_sell_buy, var_quantity, var_rate, var_brokerage, var_txn_charges, var_stamp_duty, var_stt_ctt;

    IF var_finished = 1
    THEN
      LEAVE fetch_data;
    END IF;


    SET var_finished = 0;
END LOOP FETCH_DATA;

    CLOSE mosl_transaction_cursor;

    INSERT INTO log_table
    VALUES      (now(), 'ap_process_mosl_transactions: End');
END
