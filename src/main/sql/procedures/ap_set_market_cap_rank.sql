DROP PROCEDURE IF EXISTS ap_set_market_cap_rank;
CREATE PROCEDURE ap_set_market_cap_rank(
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

      SET date1 = date1 - INTERVAL 1 DAY;

      -- TODO update screener stock rank too
   END WHILE;
   commit;
END;
