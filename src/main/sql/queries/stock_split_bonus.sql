SET SQL_SAFE_UPDATES = 0;
Commit;

-- Stock Split, Bonus
select * from stock_split_probability a where a.is_processed = 'YES' order by date desc;
select a.* from stock_split_probability a, stock_universe b where a.ticker = b.ticker and (b.is_nse500 = 1 or b.is_bse500 = 1) and a.is_processed != 'YES' order by date desc;
-- For 1:1 Bonus update price as 1/2
-- FOr 1:2 Bonus update price as 2/3 and so on (i.e. Newly Issued Bonus Stocks / (sum of newly issued stocks + original stocks)
-- For 2:10 Stock Split update price as (New FV/Old FV) i.e. (2/10) or (1/5)

-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:2' where ticker = 'IOC' and date = '2022-06-30';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:2' where ticker = 'RATNAMANI' and date = '2022-06-30';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'NAZARA' and date = '2022-06-24';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:2' where ticker = 'AJANTPHARM' and date = '2022-06-22';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'AUBANK' and date = '2022-06-09';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:2' where ticker = 'VBL' and date = '2022-06-06';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 1:10' where ticker = 'SAREGAMA' and date = '2022-04-26';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 2:10' where ticker = 'JUBLFOOD' and date = '2022-04-19';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 2:3' where ticker = 'BCG' and date = '2022-03-15';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'HGS' and date = '2022-02-22';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 1:2' where ticker = 'PCBL' and date = '2022-04-11';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 2:10' where ticker = 'VTL' and date = '2022-03-24';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 2:1' where ticker = 'BSE' and date = '2022-03-21';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'INFIBEAM' and date = '2022-03-14';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 2:10' where ticker = 'SCHAEFFLER' and date = '2022-02-08';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 1:2' where ticker = 'IPCALAB' and date = '2022-01-10';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'REDINGTON' and date = '2021-08-18';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 2:1' where ticker = 'MAHLIFE' and date = '2021-09-14';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 1:1' where ticker = 'APLAPOLLO' and date = '2021-09-16';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 1:10' where ticker = 'CESC' and date = '2021-09-17';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 1:5' where ticker = 'KPRMILL' and date = '2021-09-24';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 2:10' where ticker = 'AFFLE' and date = '2021-10-07';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 4:1' where ticker = 'SRF' and date = '2021-10-13';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 2:10' where ticker = 'IRCTC' and date = '2021-10-28';
-- update stock_split_probability set is_processed = 'YES', note = 'Stock Split Ratio 1:10' where ticker = 'TTKPRESTIG' and date = '2021-12-14';
-- update stock_split_probability set is_processed = 'YES', note = 'Bonus 2:1' where ticker = 'IEX' and date = '2021-12-03';
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

SELECT date, close_price, a.* from nse_price_history a where a.nse_ticker = 'IOC' and date <= '2022-06-30' order by date desc;
SELECT date, close_price from bse_price_history a where a.bse_ticker = '000000' and date <= '2019-12-05' order by date desc;
select * from stock_price_movement_history a where a.ticker = 'TTKPRESTIG' and date >= '2021-12-14';
SELECT * from wealth_details a where ticker = 'DIXON';
SELECT * from portfolio_holdings a where ticker = 'DIXON';

/*
update portfolio_holdings set quantity = quantity * 5, rate = rate * (1/5), net_rate = net_rate * (1/5) where ticker = 'DIXON';

update wealth_details set quantity = quantity * 2, rate = rate * (1/2), net_rate = net_rate * (1/2) where ticker = 'RELAXO';
update portfolio_holdings set quantity = quantity * 2, rate = rate * (1/2), net_rate = net_rate * (1/2) where ticker = 'RELAXO';

-- Copy this below and then replace ticker XXX to right one and replace date and most imp. replace fraction
update nse_price_history a set close_price = close_price * ( 1 / 1	) where a.nse_ticker = 'XXX' and date < 'XXXX-XX-XX' ;

update nse_price_history a set close_price = close_price * ( 2 / 3	) where a.nse_ticker = 'IOC' and date < '2022-06-30' ;
update nse_price_history a set close_price = close_price * ( 2 / 3	) where a.nse_ticker = 'RATNAMANI' and date < '2022-06-30' ;
update nse_price_history a set close_price = close_price * ( 1 / 2	) where a.nse_ticker = 'NAZARA' and date < '2022-06-24' ;
update nse_price_history a set close_price = close_price * ( 2 / 3	) where a.nse_ticker = 'AJANTPHARM' and date < '2022-06-22' ;
update nse_price_history a set close_price = close_price * ( 1 / 2	) where a.nse_ticker = 'AUBANK' and date < '2022-06-09' ;
update nse_price_history a set close_price = close_price * ( 2 / 3	) where a.nse_ticker = 'VBL' and date < '2022-06-06' ;
update nse_price_history a set close_price = close_price * ( 1 / 10	) where a.nse_ticker = 'SAREGAMA' and date < '2022-04-26' ;
update nse_price_history a set close_price = close_price * ( 2 / 10	) where a.nse_ticker = 'JUBLFOOD' and date < '2022-04-19' ;
update nse_price_history a set close_price = close_price * ( 3 / 5	) where a.nse_ticker = 'BCG' and date < '2022-03-15' ;
update nse_price_history a set close_price = close_price * ( 1 / 2) where a.nse_ticker = 'HGS' and date < '2022-02-22' ;
update nse_price_history a set close_price = close_price * ( 1 / 2) where a.nse_ticker = 'PCBL' and date < '2022-04-11' ;
update nse_price_history a set close_price = close_price * ( 2 / 10) where a.nse_ticker = 'VTL' and date < '2022-03-24' ;
update nse_price_history a set close_price = close_price * ( 1 / 3) where a.nse_ticker = 'BSE' and date < '2022-03-21' ;
update nse_price_history a set close_price = close_price * ( 1 / 2) where a.nse_ticker = 'INFIBEAM' and date < '2022-03-14' ;
update nse_price_history a set close_price = close_price * ( 2 / 10) where a.nse_ticker = 'SCHAEFFLER' and date < '2022-02-08' ;
update nse_price_history a set close_price = close_price * ( 1 / 2) where a.nse_ticker = 'IPCALAB' and date < '2022-01-10' ;
update nse_price_history a set close_price = close_price * ( 1 / 2	) where a.nse_ticker = 'REDINGTON' and date < '2021-08-18' ;
update nse_price_history a set close_price = close_price * ( 1 / 3	) where a.nse_ticker = 'MAHLIFE' and date < '2021-09-14' ;
update nse_price_history a set close_price = close_price * ( 1 / 2	) where a.nse_ticker = 'APLAPOLLO' and date < '2021-09-16' ;
update nse_price_history a set close_price = close_price * ( 1 / 10	) where a.nse_ticker = 'CESC' and date < '2021-09-17' ;
update nse_price_history a set close_price = close_price * ( 1 / 5	) where a.nse_ticker = 'KPRMILL' and date < '2021-09-24' ;
update nse_price_history a set close_price = close_price * ( 2 / 10	) where a.nse_ticker = 'AFFLE' and date < '2021-10-07' ;
update nse_price_history a set close_price = close_price * ( 1 / 5	) where a.nse_ticker = 'SRF' and date < '2021-10-13' ;
update nse_price_history a set close_price = close_price * ( 2 / 10	) where a.nse_ticker = 'IRCTC' and date < '2021-10-28' ;
update nse_price_history a set close_price = close_price * ( 1 / 10	) where a.nse_ticker = 'TTKPRESTIG' and date < '2021-12-14' ;
update nse_price_history a set close_price = close_price * ( 1 / 3	) where a.nse_ticker = 'IEX' and date < '2021-12-03' ;
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


-- Copy this below and then replace ticker XXX to right one and replace date and most imp. replace fraction
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 1 ) where b.ticker = 'XXX' and b.ticker5 = a.name  and date < 'XXXX-XX-XX' ;

update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 3 ) where b.ticker = 'IOC' and b.ticker5 = a.name  and date < '2022-06-30' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 3 ) where b.ticker = 'RATNAMANI' and b.ticker5 = a.name  and date < '2022-06-30' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 3 ) where b.ticker = 'NAZARA' and b.ticker5 = a.name  and date < '2022-06-24' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 1 ) where b.ticker = 'AJANTPHARM' and b.ticker5 = a.name  and date < '2022-06-22' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 1 ) where b.ticker = 'AUBANK' and b.ticker5 = a.name  and date < '2022-06-09' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 3 ) where b.ticker = 'VBL' and b.ticker5 = a.name  and date < '2022-06-06' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 10 ) where b.ticker = 'SAREGAMA' and b.ticker5 = a.name  and date < '2022-04-26' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 10 ) where b.ticker = 'JUBLFOOD' and b.ticker5 = a.name  and date < '2022-04-19' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 3 / 5 ) where b.ticker = 'BCG' and b.ticker5 = a.name  and date < '2022-03-15' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2 ) where b.ticker = 'HGS' and b.ticker5 = a.name  and date < '2022-02-22' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2 ) where b.ticker = 'PCBL' and b.ticker5 = a.name  and date < '2022-04-11' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 10) where b.ticker = 'VTL' and b.ticker5 = a.name  and date < '2022-03-24' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 3) where b.ticker = 'BSE' and b.ticker5 = a.name  and date < '2022-03-21' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2) where b.ticker = 'INFIBEAM' and b.ticker5 = a.name  and date < '2022-03-14' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 10) where b.ticker = 'SCHAEFFLER' and b.ticker5 = a.name  and date < '2022-02-08' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2) where b.ticker = 'IPCALAB' and b.ticker5 = a.name  and date < '2022-01-10' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2	) where b.ticker = 'REDINGTON' and b.ticker5 = a.name  and date < '2021-08-18' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 3	) where b.ticker = 'MAHLIFE' and b.ticker5 = a.name  and date < '2021-09-14' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2	) where b.ticker = 'APLAPOLLO' and b.ticker5 = a.name  and date < '2021-09-16' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 10	) where b.ticker = 'CESC' and b.ticker5 = a.name  and date < '2021-09-17' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 5	) where b.ticker = 'KPRMILL' and b.ticker5 = a.name  and date < '2021-09-24' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 10	) where b.ticker = 'AFFLE' and b.ticker5 = a.name  and date < '2021-10-07' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 5	) where b.ticker = 'SRF' and b.ticker5 = a.name  and date < '2021-10-13' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 10	) where b.ticker = 'IRCTC' and b.ticker5 = a.name  and date < '2021-10-28' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 10	) where b.ticker = 'TTKPRESTIG' and b.ticker5 = a.name  and date < '2021-12-14' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 3	) where b.ticker = 'IEX' and b.ticker5 = a.name  and date < '2021-12-03' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2	) where b.ticker = 'INFIBEAM' and b.ticker5 = a.name  and date < '2021-03-18' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2	) where b.ticker = 'KNRCON' and b.ticker5 = a.name  and date < '2021-02-03' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2	) where b.ticker = 'VALIANTORG' and b.ticker5 = a.name  and date < '2020-12-24' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 3 / 4	) where b.ticker = 'ASTRAL' and b.ticker5 = a.name  and date < '2021-03-18' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 5	) where b.ticker = 'FINPIPE' and b.ticker5 = a.name  and date < '2021-04-15' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 5	) where b.ticker = 'VAIBHAVGBL' and b.ticker5 = a.name  and date < '2021-05-07' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 5	) where b.ticker = 'ALKYLAMINE' and b.ticker5 = a.name  and date < '2021-05-11' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2	) where b.ticker = 'IRCON' and b.ticker5 = a.name  and date < '2021-05-20' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2	) where b.ticker = 'REDINGTON' and b.ticker5 = a.name  and date < '2021-08-18' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 3	) where b.ticker = 'VBL' and b.ticker5 = a.name  and date < '2021-06-10';
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2	) where b.ticker = 'AARTIIND' and b.ticker5 = a.name  and date < '2021-06-22' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 5	) where b.ticker = 'DIXON'	 and b.ticker5 = a.name  and date < '2021-03-18' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 10	) where b.ticker = 'APLAPOLLO'	 and b.ticker5 = a.name  and date < '2020-12-15' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 10	) where b.ticker = 'LAURUSLABS'	 and b.ticker5 = a.name  and date < '2020-09-29' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2	) where b.ticker = 'ELGIEQUIP'	 and b.ticker5 = a.name  and date < '2020-09-24' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 10 ) where b.ticker = 'EICHERMOT'  and b.ticker5 = a.name  and date < '2020-08-24';
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 10 ) where b.ticker = 'IRCON'  and b.ticker5 = a.name  and date < '2020-04-03';
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2 ) where b.ticker = 'VINATIORGA' and b.ticker5 = a.name  and date < '2020-02-05' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 3 ) where b.ticker = 'BALMLAWRIE' and b.ticker5 = a.name  and date < '2019-12-26';
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 10 ) where b.ticker = 'TRIDENT'  and b.ticker5 = a.name  and date < '2019-12-13' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2 ) where b.ticker = 'HCLTECH' and b.ticker5 = a.name  and date < '2019-12-05' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2 ) where b.ticker = 'AARTIIND' and b.ticker5 = a.name  and date < '2019-09-27' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2 ) where b.ticker = 'HDFCBANK' and b.ticker5 = a.name  and date < '2019-09-19' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 4 / 5 ) where b.ticker = 'ASTRAL' and b.ticker5 = a.name  and date < '2019-09-16' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 3 ) where b.ticker = 'BRIGADE' and b.ticker5 = a.name  and date < '2019-08-28' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2 ) where b.ticker = 'BHAGERIA'  and b.ticker5 = a.name  and date < '2019-07-17';
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2 ) where b.ticker = 'SUMIT'  and b.ticker5 = a.name  and date < '2019-07-17';
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2 ) where b.ticker = 'GAIL'  and b.ticker5 = a.name  and date < '2019-07-09' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 2 / 5 ) where b.ticker = 'APCOTEXIND' and b.ticker5 = a.name  and date < '2019-07-04' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 5 / 10 ) where b.ticker = '509887' and b.ticker5 = a.name and date < '2019-07-03' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2 ) where b.ticker = 'AVADHSUGAR'  and b.ticker5 = a.name  and date < '2019-06-27';
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 3 / 5 ) where b.ticker = 'MITTAL'  and b.ticker5 = a.name  and date < '2019-06-20' ;
update daily_data_s a, stock_universe b set a.cmp = a.cmp * ( 1 / 2 ) where b.ticker = 'CREATIVE'  and b.ticker5 = a.name  and date < '2019-06-25' ;


*/
