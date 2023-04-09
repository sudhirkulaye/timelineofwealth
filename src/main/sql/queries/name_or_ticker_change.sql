-- Queries to change in Short Name of Screener or Ticker
select count(1), name from daily_data_s a group by name order by count(1); 

-- Queries to update Screener Short Name (In case of Name Change or ticker Change)
select * from stock_universe a where (is_bse500 = 1 or is_nse500 = 1) and a.ticker IN ('360ONE', '') or a.ticker5 = ' '; -- Bharat Forge, 25101010
select * from daily_data_s a where date >= '2022-01-10' and name like '%IIFL%' order by date desc;
select * from stock_price_movement_history where ticker = 'NIITTECH';
select * from stock_universe a where nse_code <> ticker and nse_code <>"";

-- update stock_universe set subindustryid = 15101020 where subindustryid = 15101050 and (is_bse500 = 1 or is_nse500 = 1);

-- update stock_universe SET ticker = '360ONE', nse_code = '360ONE', ticker5 = '360ONE', short_name = '360 ONE', name = '360 ONE WAM Ltd', ticker_old = 'IIFLWAM' where ticker = 'IIFLWAM';
-- update stock_universe SET ticker = 'LTIM', nse_code = 'LTIM', ticker5 = 'LTIM', short_name = 'LTI Mindtree', name = 'LTI Mindtree Ltd', ticker_old = 'LTI' where ticker = 'LTIM';
-- update stock_universe set ticker5 = 'Shriram Finance', short_name = 'Shriram Finance', name = 'Shriram Finance Ltd' where ticker = 'SRTRANSFIN';
-- update stock_universe set ticker5 = '360 ONE', short_name = '360 ONE WAM', name = '360 ONE WAM Ltd' where ticker = 'IIFLWAM';
-- update stock_universe set ticker5 = 'LTI Mindtree', short_name = 'LTI Mindtree', name = 'LTI Mindtree Ltd' where ticker = 'LTI';
-- update stock_universe set ticker5 = 'Westlife Food', short_name = 'Westlife Foodworld', name = 'Westlife Foodworld Ltd' where ticker = 'WESTLIFE';
-- update stock_universe set ticker5 = 'Patanjali Foods', short_name = 'Patanjali Foods', name = 'Patanjali Foods Ltd' where ticker = 'RUCHISOYA';
-- update stock_universe set ticker5 = 'Zydus Lifesci.', short_name = 'Zydus Lifescience', name = 'Zydus Lifescience' where ticker = 'CADILAHC';
-- update stock_universe set ticker5 = 'Restaurant Brand', short_name = 'Restaurant Brand', name = 'Restaurant Brands Asia Ltd' where ticker = 'BURGERKING';
-- update stock_universe set ticker5 = 'PCBL Ltd', short_name = 'PCBL', name = 'PCBL Ltd' where ticker = 'PHILIPCARB';
-- update stock_universe set ticker5 = 'Hitachi Energy', short_name = 'Hitachi Energy', name = 'Hitachi Energy India Ltd' where ticker = 'POWERINDIA';
-- update stock_universe set ticker5 = 'Borosil Renew.' where ticker = 'BORORENEW';
-- update stock_universe set ticker5 = 'Fiem Industries' where ticker = 'FIEMIND';
-- update stock_universe set ticker5 = 'RHI Magnestia' where ticker = 'ORIENTREF';
-- update stock_universe set ticker5 = 'Hatsun Agro' where ticker = 'HATSUN';
-- update stock_universe set ticker5 = 'Astral', short_name = 'Astral', name = 'Astral Ltd' where ticker = 'ASTRAL';
-- update stock_universe set ticker5 = 'Jubilant Pharmo', short_name = 'Jubilant Pharmova', name = 'Jubilant Pharmova Ltd' where ticker = 'JUBILANT';
-- update stock_universe set ticker5 = 'SIS' where ticker = 'SIS' or ticker5 = 'Security & Intel' ;

-- update daily_data_s set name = '360ONE' where name = 'IIFLWAM';
-- update daily_data_s set name = 'Shriram Finance' where name = 'Shriram Trans.';
-- update daily_data_s set name = '360 ONE' where name = 'IIFL Wealth Mgt';
-- update daily_data_s set name = 'LTI Mindtree' where name = 'L & T Infotech';
-- update daily_data_s set name = 'Westlife Food' where name = 'Westlife Develop';
-- update daily_data_s set name = 'Samvardh. Mothe.' where name = 'Motherson Sumi';
-- update daily_data_s set name = 'Patanjali Foods' where name = 'Ruchi Soya Inds.';
-- update daily_data_s set name = 'Patanjali Foods' where name = 'Ruchi Soya Inds.';
-- update daily_data_s set name = 'Zydus Lifesci.' where name = 'Cadila Health.';
-- update daily_data_s set name = 'Restaurant Brand' where name = 'Burger King';
-- update daily_data_s set name = 'PCBL Ltd' where name = 'Phillips Carbon';
-- update daily_data_s set name = 'Hitachi Energy' where name = 'ABB Power Produc';
-- update daily_data_s set name = 'RHI Magnestia' where name = 'Orient Refrac.';
-- update daily_data_s set name = 'Hatsun Agro' where name = 'Hatsun AgroProd.';
-- update daily_data_s set name = 'Astral' where name = 'Astral Poly Tech';
-- update daily_data_s set name = 'Jubilant Pharmo' where name = 'Jubilant Life';
-- update daily_data_s set name = 'SIS' where name = 'Security & Intel';
-- update daily_data_s set name = 'Adani Total Gas' where name = 'Adani Gas';
-- update daily_data_s set name = 'Indus Towers' where name = 'Bharti Infratel';
-- update daily_data_s set name = 'Coforge' where name = 'NIIT Tech.';
-- update daily_data_s set name = 'Dhani Services' where name = 'Indiabulls Vent.';

-- update stock_universe SET ticker = '360ONE', nse_code = '360ONE', ticker5 = '360ONE', short_name = '360 ONE', name = '360 ONE WAM Ltd', ticker_old = 'IIFLWAM' where ticker = 'IIFLWAM';
-- UPDATE stock_universe SET ticker = 'SHRIRAMFIN', ticker5 = 'Shriram Finance', short_name = 'Shriram Finance', name = 'Shriram Finance Ltd', ticker_old = 'SRTRANSFIN' WHERE ticker = 'SRTRANSFIN';
-- UPDATE stock_universe SET ticker = 'LTIM', ticker5 = 'LTI Mindtree', short_name = 'LTI Mindtree', name = 'LTI Mindtree Ltd', ticker_old = 'LTI' WHERE ticker = 'LTI';
-- UPDATE stock_universe SET ticker = 'MOTHERSON', ticker5 = 'Samvardh. Mothe.', short_name = 'Samvardhana Motherson', name = 'Samvardhana Motherson International Ltd', ticker_old = 'MOTHERSUMI' WHERE ticker = 'MOTHERSUMI';
-- UPDATE stock_universe SET ticker = 'PATANJALI', nse_code = 'PATANJALI', ticker_old = 'RUCHISOYA' WHERE ticker = 'RUCHISOYA';
-- UPDATE stock_universe SET ticker = 'ZYDUSLIFE', nse_code = 'ZYDUSLIFE', ticker_old = 'CADILAHC' WHERE ticker = 'CADILAHC';
-- update stock_universe set ticker = 'RBA', nse_code = 'RBA', ticker_old = 'BURGERKING'  where ticker = 'BURGERKING';
-- update stock_universe set ticker = 'PCBL', nse_code = 'PCBL', ticker_old = 'PHILIPCARB'  where ticker = 'PHILIPCARB';
-- update stock_universe set ticker = 'ANGELONE', nse_code = 'ANGELONE', ticker_old = 'ANGELBRKG'  where ticker = 'ANGELBRKG';
-- update stock_universe set ticker = 'BORORENEW',  nse_code = 'BORORENEW', ticker_old = 'BOROSIL'  where ticker = 'BOROSIL';
-- UPDATE stock_universe SET ticker = 'STLTECH',   nse_code = 'STLTECH', ticker_old = 'STRTECH' WHERE (`ticker` = 'STRTECH');
-- UPDATE stock_universe SET ticker = 'RHIM', ticker_old = 'ORIENTREF' WHERE (`ticker` = 'ORIENTREF');
-- UPDATE stock_universe SET ticker = 'HUHTAMAKI', ticker_old = 'PAPERPROD' WHERE (`ticker` = 'PAPERPROD');
-- UPDATE stock_universe SET ticker = 'ALOKINDS', ticker_old = 'ALOKTEXT' WHERE (`ticker` = 'ALOKTEXT');
-- UPDATE stock_universe SET ticker = 'ATGL', ticker_old = 'ADANIGAS' WHERE (`ticker` = 'ADANIGAS');
-- UPDATE stock_universe SET ticker = 'JUBLPHARMA', ticker_old = 'JUBILANT' WHERE (`ticker` = 'JUBILANT');

-- update nse_price_history a set nse_ticker = 'XXX' where nse_ticker = 'XXX';
-- update nse_price_history a set nse_ticker = '360ONE' where nse_ticker = 'IIFLWAM';
-- update nse_price_history a set nse_ticker = 'SHRIRAMFIN' where nse_ticker = 'SRTRANSFIN';
-- update nse_price_history a set nse_ticker = 'LTIM' where nse_ticker = 'LTI';
-- update nse_price_history a set nse_ticker = 'MOTHERSON' where nse_ticker = 'MOTHERSUMI';
-- update nse_price_history a set nse_ticker = 'PATANJALI' where nse_ticker = 'RUCHISOYA';
-- update nse_price_history a set nse_ticker = 'ZYDUSLIFE' where nse_ticker = 'CADILAHC';
-- update nse_price_history a set nse_ticker = 'RBA' where nse_ticker = 'BURGERKING';
-- update nse_price_history a set nse_ticker = 'PCBL' where nse_ticker = 'PHILIPCARB';
-- update nse_price_history a set nse_ticker = 'ANGELONE' where nse_ticker = 'ANGELBRKG';
-- update nse_price_history a set nse_ticker = 'STLTECH' where nse_ticker = 'STRTECH';
-- update nse_price_history a set nse_ticker = 'RHIM' where nse_ticker = 'ORIENTREF';
-- update nse_price_history a set nse_ticker = 'ATGL' where nse_ticker = 'ADANIGAS';
-- update nse_price_history a set nse_ticker = 'JUBLPHARMA' where nse_ticker = 'JUBILANT';
-- update nse_price_history a set nse_ticker = 'INDUSTOWER' where nse_ticker = 'INFRATEL';
-- update nse_price_history a set nse_ticker = 'COFORGE' where nse_ticker = 'NIITTECH';
-- update nse_price_history a set nse_ticker = 'DHANI' where nse_ticker = 'IBVENTURES';
-- update nse_price_history a set nse_ticker = 'TATACONSUM' where nse_ticker = 'TATAGLOBAL';

-- update stock_price_movement set ticker = 'XXX' where ticker = 'XXX';
-- update stock_price_movement set ticker = '360ONE' where ticker = 'IIFLWAM';
-- update stock_price_movement set ticker = 'SHRIRAMFIN' where ticker = 'SRTRANSFIN';
-- update stock_price_movement set ticker = 'LTIM' where ticker = 'LTI';
-- update stock_price_movement set ticker = 'MOTHERSON' where ticker = 'MOTHERSUMI';
-- update stock_price_movement set ticker = 'PATANJALI' where ticker = 'RUCHISOYA';
-- update stock_price_movement set ticker = 'ZYDUSLIFE' where ticker = 'CADILAHC';
-- update stock_price_movement set ticker = 'RBA' where ticker = 'BURGERKING';
-- update stock_price_movement set ticker = 'PCBL' where ticker = 'PHILIPCARB';
-- update stock_price_movement set ticker = 'ANGELONE' where ticker = 'ANGELBRKG';
-- update stock_price_movement set ticker = 'STLTECH' where ticker = 'STRTECH';
-- update stock_price_movement set ticker = 'RHIM' where ticker = 'ORIENTREF';
-- update stock_price_movement set ticker = 'ATGL' where ticker = 'ADANIGAS';
-- update stock_price_movement set ticker = 'JUBLPHARMA' where ticker = 'JUBILANT';
-- update stock_price_movement set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_price_movement set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_price_movement set ticker = 'DHANI' where ticker = 'IBVENTURES' and date < '2020-11-06';

-- update stock_price_movement_history set ticker = 'XXX' where ticker = 'XXX';
-- update stock_price_movement_history set ticker = '360ONE' where ticker = 'IIFLWAM';
-- update stock_price_movement_history set ticker = 'SHRIRAMFIN' where ticker = 'SRTRANSFIN';
-- update stock_price_movement_history set ticker = 'LTIM' where ticker = 'LTI';
-- update stock_price_movement_history set ticker = 'MOTHERSON' where ticker = 'MOTHERSUMI';
-- update stock_price_movement_history set ticker = 'PATANJALI' where ticker = 'RUCHISOYA';
-- update stock_price_movement_history set ticker = 'ZYDUSLIFE' where ticker = 'CADILAHC';
-- update stock_price_movement_history set ticker = 'RBA' where ticker = 'BURGERKING';
-- update stock_price_movement_history set ticker = 'PCBL' where ticker = 'PHILIPCARB';
-- update stock_price_movement_history set ticker = 'ANGELONE' where ticker = 'ANGELBRKG';
-- update stock_price_movement_history set ticker = 'STLTECH' where ticker = 'STRTECH';
-- update stock_price_movement_history set ticker = 'RHIM' where ticker = 'ORIENTREF';
-- update stock_price_movement_history set ticker = 'ATGL' where ticker = 'ADANIGAS';
-- update stock_price_movement_history set ticker = 'JUBLPHARMA' where ticker = 'JUBILANT';
-- update stock_price_movement_history set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_price_movement_history set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_price_movement_history set ticker = 'DHANI' where ticker = 'IBVENTURES' and date < '2020-11-06';

-- update stock_quarter set ticker = 'XXX' where ticker = 'XXX';
-- update stock_quarter set ticker = '360ONE' where ticker = 'IIFLWAM';
-- update stock_quarter set ticker = 'SHRIRAMFIN' where ticker = 'SRTRANSFIN';
-- update stock_quarter set ticker = 'LTIM' where ticker = 'LTI';
-- update stock_quarter set ticker = 'MOTHERSON' where ticker = 'MOTHERSUMI';
-- update stock_quarter set ticker = 'PATANJALI' where ticker = 'RUCHISOYA';
-- update stock_quarter set ticker = 'ZYDUSLIFE' where ticker = 'CADILAHC';
-- update stock_quarter set ticker = 'RBA' where ticker = 'BURGERKING';
-- update stock_quarter set ticker = 'PCBL' where ticker = 'PHILIPCARB';
-- update stock_quarter set ticker = 'ANGELONE' where ticker = 'ANGELBRKG';
-- update stock_quarter set ticker = 'STLTECH' where ticker = 'STRTECH';
-- update stock_quarter set ticker = 'RHIM' where ticker = 'ORIENTREF';
-- update stock_quarter set ticker = 'ATGL' where ticker = 'ADANIGAS';
-- update stock_quarter set ticker = 'JUBLPHARMA' where ticker = 'JUBILANT';
-- update stock_quarter set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_quarter set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_quarter set ticker = 'DHANI' where ticker = 'IBVENTURES';

-- update stock_pnl set ticker = 'XXX' where ticker = 'XXX';
-- update stock_pnl set ticker = '360ONE' where ticker = 'IIFLWAM';
-- update stock_pnl set ticker = 'SHRIRAMFIN' where ticker = 'SRTRANSFIN';
-- update stock_pnl set ticker = 'LTIM' where ticker = 'LTI';
-- update stock_pnl set ticker = 'MOTHERSON' where ticker = 'MOTHERSUMI';
-- update stock_pnl set ticker = 'PATANJALI' where ticker = 'RUCHISOYA';
-- update stock_pnl set ticker = 'ZYDUSLIFE' where ticker = 'CADILAHC';
-- update stock_pnl set ticker = 'RBA' where ticker = 'BURGERKING';
-- update stock_pnl set ticker = 'PCBL' where ticker = 'PHILIPCARB';
-- update stock_pnl set ticker = 'ANGELONE' where ticker = 'ANGELBRKG';
-- update stock_pnl set ticker = 'STLTECH' where ticker = 'STRTECH';
-- update stock_pnl set ticker = 'RHIM' where ticker = 'ORIENTREF';
-- update stock_pnl set ticker = 'ATGL' where ticker = 'ADANIGAS';
-- update stock_pnl set ticker = 'JUBLPHARMA' where ticker = 'JUBILANT';
-- update stock_pnl set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_pnl set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_pnl set ticker = 'DHANI' where ticker = 'IBVENTURES';

-- update stock_cashflow set ticker = 'XXX' where ticker = 'XXX';
-- update stock_cashflow set ticker = '360ONE' where ticker = 'IIFLWAM';
-- update stock_cashflow set ticker = 'SHRIRAMFIN' where ticker = 'SRTRANSFIN';
-- update stock_cashflow set ticker = 'LTIM' where ticker = 'LTI';
-- update stock_cashflow set ticker = 'MOTHERSON' where ticker = 'MOTHERSUMI';
-- update stock_cashflow set ticker = 'PATANJALI' where ticker = 'RUCHISOYA';
-- update stock_cashflow set ticker = 'ZYDUSLIFE' where ticker = 'CADILAHC';
-- update stock_cashflow set ticker = 'RBA' where ticker = 'BURGERKING';
-- update stock_cashflow set ticker = 'PCBL' where ticker = 'PHILIPCARB';
-- update stock_cashflow set ticker = 'ANGELONE' where ticker = 'ANGELBRKG';
-- update stock_cashflow set ticker = 'STLTECH' where ticker = 'STRTECH';
-- update stock_cashflow set ticker = 'RHIM' where ticker = 'ORIENTREF';
-- update stock_cashflow set ticker = 'ATGL' where ticker = 'ADANIGAS';
-- update stock_cashflow set ticker = 'JUBLPHARMA' where ticker = 'JUBILANT';
-- update stock_cashflow set ticker = 'INDUSTOWER' where ticker = 'INFRATEL';
-- update stock_cashflow set ticker = 'COFORGE' where ticker = 'NIITTECH';
-- update stock_cashflow set ticker = 'DHANI' where ticker = 'IBVENTURES';

-- update stock_balancesheet set ticker = 'XXX' where ticker = 'XXX';
-- update stock_balancesheet set ticker = '360ONE' where ticker = 'IIFLWAM';
-- update stock_balancesheet set ticker = 'SHRIRAMFIN' where ticker = 'SRTRANSFIN';
-- update stock_balancesheet set ticker = 'LTIM' where ticker = 'LTI';
-- update stock_balancesheet set ticker = 'MOTHERSON' where ticker = 'MOTHERSUMI';
-- update stock_balancesheet set ticker = 'PATANJALI' where ticker = 'RUCHISOYA';
-- update stock_balancesheet set ticker = 'ZYDUSLIFE' where ticker = 'CADILAHC';
-- update stock_balancesheet set ticker = 'RBA' where ticker = 'BURGERKING';
-- update stock_balancesheet set ticker = 'PCBL' where ticker = 'PHILIPCARB';
-- update stock_balancesheet set ticker = 'ANGELONE' where ticker = 'ANGELBRKG';
-- update stock_balancesheet set ticker = 'STLTECH' where ticker = 'STRTECH';
-- update stock_balancesheet set ticker = 'RHIM' where ticker = 'ORIENTREF';
-- update stock_balancesheet set ticker = 'ATGL' where ticker = 'ADANIGAS';
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
