SET SQL_SAFE_UPDATES = 0;
Commit;

-- Stock Split, Bonus
select * from stock_split_probability a order by date desc;
select a.* from stock_split_probability a, stock_universe b where a.ticker = b.ticker and (b.is_nse500 = 1 or b.is_bse500 = 1) and a.is_processed != 'YES' order by date desc;
-- For 1:1 Bonus update price as 1/2
-- FOr 1:2 Bonus update price as 2/3 and so on (i.e. Newly Issued Bonus Stocks / (sum of newly issued stocks + original stocks)
-- For 2:10 Stock Split update price as (New FV/Old FV) i.e. (2/10) or (1/5)

-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'AARTIIND' and date = '2021-06-22';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:2' where ticker = 'VBL' and date = '2021-06-10';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'REDINGTON' and date = '2021-08-18';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'IRCON' and date = '2021-05-20';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 2:5' where ticker = 'ALKYLAMINE' and date = '2021-05-11';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 2:10' where ticker = 'VAIBHAVGBL' and date = '2021-05-07';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 2:10' where ticker = 'FINPIPE' and date = '2021-04-15';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:3' where ticker = 'ASTRAL' and date = '2021-03-18';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 2:10' where ticker = 'DIXON' and date = '2021-03-18';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'INFIBEAM' and date = '2021-03-18';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'KNRCON' and date = '2021-02-03';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'VALIANTORG' and date = '2020-12-24';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'ELGIEQUIP' and date = '2020-09-24';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 2:10' where ticker = 'LAURUSLABS' and date = '2020-09-29';
-- update stock_split_probability set is_processed = 'YES', note = 'No Data Found' where ticker = 'M&MFIN' and date = '2020-08-24';
-- update stock_split_probability set is_processed = 'YES', note = 'Right Issue 1:1' where is_processed = 'NO' and ticker = 'M&MFIN' and date = '2020-07-22';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Crashed' where ticker = JETAIRWAYS' and date = '2019-06-18';
-- update rest of the stocks as ignored
-- update stock_split_probability set is_processed = 'YES', note = 'Ignored' where is_processed = 'NO' and ticker not in (select ticker from stock_universe where is_nse500 = 1 or is_bse500 = 1);

SELECT date, close_price, a.* from nse_price_history a where a.nse_ticker = 'VALIANTORG' and date <= '2020-12-24' order by date desc;
SELECT date, close_price from bse_price_history a where a.bse_ticker = '000000' and date <= '2019-12-05' order by date desc;
select * from stock_price_movement_history a where a.ticker = 'VBL' and date >= '2021-06-10';
SELECT * from wealth_details a where ticker = 'DIXON';
SELECT * from portfolio_holdings a where ticker = 'DIXON';

/*
update portfolio_holdings set quantity = quantity * 5, rate = rate * (1/5), net_rate = net_rate * (1/5) where ticker = 'DIXON';

update wealth_details set quantity = quantity * 2, rate = rate * (1/2), net_rate = net_rate * (1/2) where ticker = 'RELAXO';
update portfolio_holdings set quantity = quantity * 2, rate = rate * (1/2), net_rate = net_rate * (1/2) where ticker = 'RELAXO';

-- Copy this below and then replace ticker XXX to right one and replace date and most imp. replace fraction
update nse_price_history a set close_price = close_price * ( 1 / 1	) where a.nse_ticker = 'XXX' and date < 'XXXX-XX-XX' ;

update nse_price_history a set close_price = close_price * ( 1 / 2	) where a.nse_ticker = 'INFIBEAM' and date < '2021-03-18' ;
update nse_price_history a set close_price = close_price * ( 1 / 2	) where a.nse_ticker = 'KNRCON' and date < '2021-02-03' ;
update nse_price_history a set close_price = close_price * ( 1 / 2	) where a.nse_ticker = 'VALIANTORG' and date < '2020-12-24' ;

update nse_price_history a set close_price = close_price * ( 3 / 4	) where a.nse_ticker = 'ASTRAL' and date < '2021-03-18' ;
update nse_price_history a set close_price = close_price * ( 1 / 5	) where a.nse_ticker = 'FINPIPE' and date < '2021-04-15' ;
update nse_price_history a set close_price = close_price * ( 1 / 5	) where a.nse_ticker = 'VAIBHAVGBL' and date < '2021-05-07' ;
update nse_price_history a set close_price = close_price * ( 2 / 5	) where a.nse_ticker = 'ALKYLAMINE' and date < '2021-05-11' ;
update nse_price_history a set close_price = close_price * ( 1 / 2	) where a.nse_ticker = 'IRCON' and date < '2021-05-20' ;
update nse_price_history a set close_price = close_price * ( 1 / 2	) where a.nse_ticker = 'REDINGTON' and date < '2021-08-18' ;
update nse_price_history a set close_price = close_price * ( 2 / 3	) where a.nse_ticker = 'VBL' and date < '2021-06-10';
update nse_price_history a set close_price = close_price * ( 1 / 2	) where a.nse_ticker = 'AARTIIND' and date < '2021-06-22' ;
update nse_price_history a set close_price = close_price * ( 1 / 5	) where a.nse_ticker = 'DIXON'	 and date < '2021-03-18' ;
update nse_price_history a set close_price = close_price * ( 2 / 10	) where a.nse_ticker = 'APLAPOLLO'	 and date < '2020-12-15' ;
update nse_price_history a set close_price = close_price * ( 2 / 10	) where a.nse_ticker = 'LAURUSLABS'	 and date < '2020-09-29' ;
update nse_price_history a set close_price = close_price * ( 1 / 2	) where a.nse_ticker = 'ELGIEQUIP'	 and date < '2020-09-24' ;
update nse_price_history a set close_price = close_price * ( 1 / 10 ) where a.nse_ticker = 'EICHERMOT'  and date < '2020-08-24';
update nse_price_history a set close_price = close_price * ( 2 / 10 ) where a.nse_ticker = 'IRCON'  and date < '2020-04-03';
update nse_price_history a set close_price = close_price * ( 1 / 2 ) where a.nse_ticker = 'VINATIORGA' and date < '2020-02-05' ;
update nse_price_history a set close_price = close_price * ( 2 / 3 ) where a.nse_ticker = 'BALMLAWRIE' and date < '2019-12-26';
update nse_price_history a set close_price = close_price * ( 1 / 10 ) where a.nse_ticker = 'TRIDENT'  and date < '2019-12-13' ;
update nse_price_history a set close_price = close_price * ( 1 / 2 ) where a.nse_ticker = 'HCLTECH' and date < '2019-12-05' ;
update nse_price_history a set close_price = close_price * ( 1 / 2 ) where a.nse_ticker = 'AARTIIND' and date < '2019-09-27' ;
update nse_price_history a set close_price = close_price * ( 1 / 2 ) where a.nse_ticker = 'HDFCBANK' and date < '2019-09-19' ;
update nse_price_history a set close_price = close_price * ( 4 / 5 ) where a.nse_ticker = 'ASTRAL' and date < '2019-09-16' ;
update nse_price_history a set close_price = close_price * ( 2 / 3 ) where a.nse_ticker = 'BRIGADE' and date < '2019-08-28' ;
update nse_price_history a set close_price = close_price * ( 1 / 2 ) where a.nse_ticker = 'BHAGERIA'  and date < '2019-07-17';
update nse_price_history a set close_price = close_price * ( 1 / 2 ) where a.nse_ticker = 'SUMIT'  and date < '2019-07-17';
update nse_price_history a set close_price = close_price * ( 1 / 2 ) where a.nse_ticker = 'GAIL'  and date < '2019-07-09' ;
update nse_price_history a set close_price = close_price * ( 2 / 5 ) where a.nse_ticker = 'APCOTEXIND' and date < '2019-07-04' ;
update bse_price_history a set close_price = close_price * ( 5 / 10 ) where a.bse_ticker = '509887' and date < '2019-07-03' ;
update nse_price_history a set close_price = close_price * ( 1 / 2 ) where a.nse_ticker = 'AVADHSUGAR'  and date < '2019-06-27';
update nse_price_history a set close_price = close_price * ( 3 / 5 ) where a.nse_ticker = 'MITTAL'  and date < '2019-06-20' ;
update nse_price_history a set close_price = close_price * ( 1 / 2 ) where a.nse_ticker = 'CREATIVE'  and date < '2019-06-25' ;

*/
