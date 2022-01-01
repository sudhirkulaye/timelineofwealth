DROP PROCEDURE IF EXISTS ap_process_mosl_transactions;
CREATE PROCEDURE ap_process_mosl_transactions ()
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
    AND a.quantity > 0
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
END