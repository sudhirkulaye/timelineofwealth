-- Queries to change in Short Name of Screener or Ticker
select count(1), name from daily_data_s a group by name order by count(1); 

-- Queries to update Screener Short Name (In case of Name Change or ticker Change)
select * from stock_universe a where a.ticker = 'JUBILANT' or a.ticker5 = 'Indiamart Inter.'; -- Bharat Forge, 25101010
select * from daily_data_s a where date = '2021-07-01' and name like 'Orient Re%' order by date desc;
select * from stock_price_movement_history where ticker = 'NIITTECH';

-- update stock_universe set ticker5 = 'RHI Magnestia' where ticker = 'ORIENTREF';
-- update stock_universe set ticker5 = 'Hatsun Agro' where ticker = 'HATSUN';
-- update stock_universe set ticker5 = 'Astral', short_name = 'Astral', name = 'Astral Ltd' where ticker = 'ASTRAL';
-- update stock_universe set ticker5 = 'Jubilant Pharmo', short_name = 'Jubilant Pharmova', name = 'Jubilant Pharmova Ltd' where ticker = 'JUBILANT';
-- update stock_universe set ticker5 = 'SIS' where ticker = 'SIS' or ticker5 = 'Security & Intel' ;

-- update daily_data_s set name = 'RHI Magnestia' where name = 'Orient Refrac.';
-- update daily_data_s set name = 'Hatsun Agro' where name = 'Hatsun AgroProd.';
-- update daily_data_s set name = 'Astral' where name = 'Astral Poly Tech';
-- update daily_data_s set name = 'Jubilant Pharmo' where name = 'Jubilant Life';
-- update daily_data_s set name = 'SIS' where name = 'Security & Intel';
-- update daily_data_s set name = 'Adani Total Gas' where name = 'Adani Gas';
-- update daily_data_s set name = 'Indus Towers' where name = 'Bharti Infratel';
-- update daily_data_s set name = 'Coforge' where name = 'NIIT Tech.';
-- update daily_data_s set name = 'Dhani Services' where name = 'Indiabulls Vent.';

-- UPDATE stock_universe SET ticker = 'JUBLPHARMA', ticker_old = 'JUBILANT' WHERE (`ticker` = 'JUBILANT');

-- update nse_price_history a set nse_ticker = 'JUBLPHARMA' where nse_ticker = 'JUBILANT';
-- update nse_price_history a set nse_ticker = 'INDUSTOWER' where nse_ticker = 'INFRATEL';
-- update nse_price_history a set nse_ticker = 'COFORGE' where nse_ticker = 'NIITTECH';
-- update nse_price_history a set nse_ticker = 'DHANI' where nse_ticker = 'IBVENTURES';
-- update nse_price_history a set nse_ticker = 'TATACONSUM' where nse_ticker = 'TATAGLOBAL';

-- update stock_price_movement set ticker = 'JUBLPHARMA' where ticker = 'JUBILANT';
-- update stock_price_movement set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_price_movement set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_price_movement set ticker = 'DHANI' where ticker = 'IBVENTURES' and date < '2020-11-06';

-- update stock_price_movement_history set ticker = 'JUBLPHARMA' where ticker = 'JUBILANT';
-- update stock_price_movement_history set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_price_movement_history set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_price_movement_history set ticker = 'DHANI' where ticker = 'IBVENTURES' and date < '2020-11-06';

-- update stock_quarter set ticker = 'JUBLPHARMA' where ticker = 'JUBILANT';
-- update stock_quarter set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_quarter set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_quarter set ticker = 'DHANI' where ticker = 'IBVENTURES';

-- update stock_pnl set ticker = 'JUBLPHARMA' where ticker = 'JUBILANT';
-- update stock_pnl set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_pnl set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_pnl set ticker = 'DHANI' where ticker = 'IBVENTURES';

-- update stock_cashflow set ticker = 'JUBLPHARMA' where ticker = 'JUBILANT';
-- update stock_cashflow set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_cashflow set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_cashflow set ticker = 'DHANI' where ticker = 'IBVENTURES';

-- update stock_balancesheet set ticker = 'JUBLPHARMA' where ticker = 'JUBILANT';
-- update stock_balancesheet set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_balancesheet set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_balancesheet set ticker = 'DHANI' where ticker = 'IBVENTURES';

/*--------------------------------------- old queries-------------------------------------------------------*/
-- Change of ticker
select distinct(name) from daily_data_s a where a.date = (select max(date) from daily_data_s) and a.name not in (select ticker5 from stock_universe) order by rank; 
select * from stock_universe a where a.name like 'Techno%'; 
select * from daily_data_s a where a.name like 'TCS%' and date > '2020-10-01' order by date desc;
select * from daily_data_b a where ticker_b = 'IDFCBK:IN';
-- update stock_universe a set ticker = 'IDFCFIRSTB' where ticker = 'IDFCBANK';
-- update stock_universe a set ticker5 = 'IDFC First' where ticker = 'IDFCBANK';
-- update stock_universe a set ticker2 = 'IDFCFB:IN' where ticker = 'IDFCFIRSTB';
-- update daily_data_b a set ticker_b = 'IDFCFB:IN' where ticker_b = 'IDFCBK:IN';
-- update daily_data_s a set a.name = 'Strides Pharma' where a.name = 'Strides Shasun'; 
-- update nse_price_history a set nse_ticker = 'IDFCFIRSTB' where nse_ticker = 'IDFCBANK';
-- update stock_pnl a set ticker = 'IDFCFIRSTB' where ticker = 'IDFCBANK';
-- update stock_quarter a set ticker = 'IDFCFIRSTB' where ticker = 'IDFCBANK';
-- update stock_balancesheet a set ticker = 'IDFCFIRSTB' where ticker = 'IDFCBANK';
-- update stock_cashflow a set ticker = 'IDFCFIRSTB' where ticker = 'IDFCBANK';
select * from wealth_details a where a.ticker = 'KPITTECH';
-- update wealth_details a set a.ticker = 'IDFCFIRSTB' where ticker = 'IDFCBANK';
select * from stock_universe a where ticker in ('530643');
-- BAJAJCORP to BAJAJCON and 'Bajaj Corp' to 'Bajaj Consumer'
-- IDFCBANK to IDFCFIRSTB and 'IDFC Bank' to 'IDFC First Bank'
select * from stock_cashflow a where ticker = 'KPITTECH';


/* Ticker Change/Name Change */
-- Change ticker and ticker_old
select * from stock_universe a where ticker like 'CDSL';
select * from nse_price_history where nse_ticker = 'TATAGLOBAL';
-- update nse_price_history set nse_ticker = 'TATACONSUM' where nse_ticker = 'TATAGLOBAL';
select * from nse_price_history where nse_ticker = 'TATACONSUM';
