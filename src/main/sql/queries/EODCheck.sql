SET SQL_SAFE_UPDATES = 0;
Commit;

select count(1), date from nse_price_history
where date = (select max(date) from nse_price_history)
group by date order by date desc;

select count(1), date from bse_price_history
where date = (select max(date) from bse_price_history)
group by date order by date desc;

select count(1), date from mutual_fund_nav_history
where date > (select date_last_trading_day from setup_dates)
group by date order by date desc;

select count(1), date from daily_data_s
where date = (select max(date) from daily_data_s)
group by date order by date desc;

select * from daily_data_s where date = (select date_today from setup_dates) order by market_cap desc;

-- proceed EOD
call ap_update_wealth_data();
-- *** Unlock Users ****
update user set active = 1 where email in ('admin', 'sudhirkulaye', 'sudhir.kulaye@gmail.com');
-- *** Report Download patch ***
UPDATE portfolio_holdings SET rate = '1', total_cost = '1', net_rate = '1', cmp = '1', market_value = '1' WHERE ticker = 'MOSL_CASH'  and total_cost = 0 and rate = 0 and net_rate = 0 and market_value = 0;
-- *** Download data patch *******
update benchmark_twrr_monthly set returns_fin_year = 0.0000 where returns_fin_year > 1000;
call ap_process_eod();
select * from log_table;
truncate table log_table;

CALL populate_table_row_count();
select * from dummy_user_table_row_count; 

select * from setup_dates;
select count(1), date from wealth_history
where date >= (select date_start_current_month from setup_dates)
group by date order by date desc;
SELECT count(1), date from wealth_asset_allocation_history
where date >= (select date_start_current_month from setup_dates)
group by date order by date desc;

-- call ap_set_market_cap_rank('2016-10-21', (select date_today from setup_dates));
call ap_process_stat_calculation();
select * from log_table;
truncate table log_table;

/*
Historical Price Movements
Call ap_process_stock_returns_history();
select * from log_table;
truncate table log_table;
*/
-- find new stocks or modified stocks -- last count 64 on 26th Nov 2018, 2 new stocks in Jan-2019
SELECT count(1), listing_date from stock_universe a group by a.listing_date order by a.listing_date desc; -- WHERE listing_date; > '2018-11-26';
select * from stock_universe a where listing_date >= '2019-01-01'; 

-- stock split probable candidate
select a.* from stock_split_probability a where a.is_processed = 'NO' order by date desc;
select * from stock_universe a where a.ticker IN ('MCL','NANDANI','ONEPOINT','AIRAN','AKASH') ;
-- new Mutual Fund Entry
select count(1), date_latest_nav from mutual_fund_universe a where a.isin_div_payout_or_isin_growth = 'XXX' group by date_latest_nav order by date_latest_nav desc;
-- select distinct fund_house from mutual_fund_universe;

-- update mutual_fund_universe set isin_div_payout_or_isin_growth = '' where isin_div_payout_or_isin_growth = 'XXX';

update mutual_fund_universe set fund_house =
case when scheme_name_full like 'Aditya Birla%' then 'Aditya Birla'
when scheme_name_full like 'Aditya Birla%' then 'Aditya Birla'
when scheme_name_full like 'Baroda %' then 'Baroda Pioneer'
when scheme_name_full like 'DSP %' then 'DSP'
when scheme_name_full like 'HDFC %' then 'HDFC'
when scheme_name_full like 'Principal %' then 'Principal'
when scheme_name_full like 'Quant %' then 'Quant'
when scheme_name_full like 'JM %' then 'JM'
when scheme_name_full like 'Kotak %' then 'Kotak'
when scheme_name_full like 'LIC %' then 'LIC'
when scheme_name_full like 'Sahara %' then 'Sahara'
when scheme_name_full like 'ICICI Prudential%' then 'ICICI Prudential'
when scheme_name_full like 'Reliance %' then 'Reliance'
when scheme_name_full like 'Tata %' then 'Tata'
when scheme_name_full like 'Franklin %' then 'Franklin'
when scheme_name_full like 'Taurus %' then 'Taurus'
when scheme_name_full like 'Templeton %' then 'Templeton'
when scheme_name_full like 'Canara Robeco%' then 'Canara Robeco'
when scheme_name_full like 'Sundaram %' then 'Sundaram'
when scheme_name_full like 'SBI %' then 'SBI Magnum'
when scheme_name_full like 'UTI %' then 'UTI'
when scheme_name_full like 'HSBC %' then 'HSBC'
when scheme_name_full like 'Quantum %' then 'Quantum'
when scheme_name_full like 'Invesco %' then 'Invesco'
when scheme_name_full like 'Mirae %' then 'Mirae'
when scheme_name_full like 'IDFC %' then 'IDFC'
when scheme_name_full like 'BOI AXA%' then 'BOI AXA'
when scheme_name_full like 'Edelweiss %' then 'Edelweiss'
when scheme_name_full like 'Axis %' then 'Axis'
when scheme_name_full like 'Essel %' then 'Essel'
when scheme_name_full like 'L&T %' then 'L&T'
when scheme_name_full like 'IDBI %' then 'IDBI'
when scheme_name_full like 'Motilal Oswal%' then 'Motilal Oswal'
when scheme_name_full like 'BNP Paribas%' then 'BNP Paribas'
when scheme_name_full like 'Union %' then 'Union'
when scheme_name_full like 'Indiabulls %' then 'Indiabulls'
when scheme_name_full like 'DHFL %' then 'DHFL'
when scheme_name_full like 'IIFL %' then 'IIFL'
when scheme_name_full like 'Parag Parikh%' then 'Parag Parikh'
when scheme_name_full like 'Shriram %' then 'Shriram'
when scheme_name_full like 'IIFCL %' then 'IIFCL'
when scheme_name_full like 'IL&FS %' then 'IL&FS'
when scheme_name_full like 'Mahindra %' then 'Mahindra' end where fund_house = 'XXX';

select * from mutual_fund_universe a where a.fund_house = 'XXX' or a.fund_house = '';

select * from mutual_fund_universe a where dividend_growth is null or dividend_growth = '' or direct_regular is null or direct_regular = '';
update mutual_fund_universe a set a.direct_regular = 'Regular' where direct_regular is null or direct_regular = '' or direct_regular not like '%Direct%';
update mutual_fund_universe a set a.dividend_growth = 'Growth' where dividend_growth is null or dividend_growth = '' and a.isin_div_payout_or_isin_growth = 'XXX';
update mutual_fund_universe a set a.dividend_growth = 'Dividend' where scheme_name_full like '%Div Option';

update mutual_fund_universe a
set a.scheme_name_part = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(a.scheme_name_full, 'Direct Plan', ' '), 'Regular Plan', ' '), 'Dividend Option', ' '), 'Div ',' '), 'Growth Option', ' '), 'Growth', ' ')
where a.scheme_name_part = 'XXX';

-- update short name with -Dir-G/D or -Reg-G/D
update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Dir-G')
where a.scheme_name_part not like '%-Dir-G' and dividend_growth = 'Growth' and direct_regular = 'Direct';
update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Dir-D')
where a.scheme_name_part not like '%-Dir-D' and dividend_growth = 'Dividend' and direct_regular = 'Direct';
update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Reg-D')
where a.scheme_name_part not like '%-Reg-D' and dividend_growth = 'Dividend' and direct_regular = 'Regular';
update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Reg-G')
where a.scheme_name_part not like '%-Reg-G' and dividend_growth = 'Growth' and direct_regular = 'Regular';
update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, ' -   - ', '-');
update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '-  -', '-');
update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '- -', '-');
update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, ' -  - - ', '-');
update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, ' -  - -', '-');
update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '-  - -', '-');
update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '- - -', '-');

-- update wealth details short name if there is any change in name
update wealth_details a, mutual_fund_universe b set a.short_name = b.scheme_name_part, a.asset_classid = b.asset_classid where a.ticker = b.scheme_code;
update wealth_details a, stock_universe b set a.short_name = b.short_name, a.subindustryid = b.subindustryid, a.asset_classid = b.asset_classid where a.ticker = b.ticker;
update sip a, mutual_fund_universe b set a.scheme_name = b.scheme_name_part where a.scheme_code = b.scheme_code;

select * from daily_data_s where date = (select date_today from setup_dates) and 1 = 2;
select count(1), date from daily_data_s where date >= (select date_last_trading_day from setup_dates)  group by date order by date desc;
select max(date) from daily_data_s;

select * from index_valuation a where a.date >= (select max(date) from index_valuation); -- '2020-11-21';  -- 
SELECT * from index_statistics a; 
select * from index_monthly_returns order by year desc, ticker;
-- code to update PE ratio
/*
SET SQL_SAFE_UPDATES = 0;
Commit;
update index_statistics set mean_pe_1yr = 
(select avg(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 12 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 
update index_statistics set minimum_pe_1yr = 
(select min(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 12 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 
update index_statistics set maximum_pe_1yr = 
(select max(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 12 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 

update index_statistics set mean_pe_3yr = 
(select avg(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 36 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 
update index_statistics set minimum_pe_3yr = 
(select min(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 36 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 
update index_statistics set maximum_pe_3yr = 
(select max(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 36 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 

update index_statistics set mean_pe_5yr = 
(select avg(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 60 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 
update index_statistics set minimum_pe_5yr = 
(select min(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 60 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 
update index_statistics set maximum_pe_5yr = 
(select max(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 60 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 

update index_statistics set mean_pe_10yr = 
(select avg(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 120 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 
update index_statistics set minimum_pe_10yr = 
(select min(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 120 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 
update index_statistics set maximum_pe_10yr = 
(select max(pe) from index_valuation where ticker = 'NIFTY' and date >= (select date_sub(max(date), INTERVAL 120 MONTH) from index_valuation where ticker = 'NIFTY')) where ticker = 'NIFTY'; 

update index_statistics set median_pe_1yr = 
(SELECT AVG(dd.pe) as median_val
FROM (
SELECT d.pe, @rownum:=@rownum+1 as `row_number`, @total_rows:=@rownum
  FROM index_valuation d, (SELECT @rownum:=0) r
  WHERE d.pe is NOT NULL
  AND ticker = 'NIFTY'
  AND date >= (select date_sub(max(date), INTERVAL 12 MONTH) from index_valuation where ticker = 'NIFTY')
  ORDER BY d.pe
) as dd
WHERE dd.row_number IN ( FLOOR((@total_rows+1)/2), FLOOR((@total_rows+2)/2) )) where ticker = 'NIFTY';

update index_statistics set median_pe_3yr = 
(SELECT AVG(dd.pe) as median_val
FROM (
SELECT d.pe, @rownum:=@rownum+1 as `row_number`, @total_rows:=@rownum
  FROM index_valuation d, (SELECT @rownum:=0) r
  WHERE d.pe is NOT NULL
  AND ticker = 'NIFTY'
  AND date >= (select date_sub(max(date), INTERVAL 36 MONTH) from index_valuation where ticker = 'NIFTY')
  ORDER BY d.pe
) as dd
WHERE dd.row_number IN ( FLOOR((@total_rows+1)/2), FLOOR((@total_rows+2)/2) )) where ticker = 'NIFTY';

update index_statistics set median_pe_5yr = 
(SELECT AVG(dd.pe) as median_val
FROM (
SELECT d.pe, @rownum:=@rownum+1 as `row_number`, @total_rows:=@rownum
  FROM index_valuation d, (SELECT @rownum:=0) r
  WHERE d.pe is NOT NULL
  AND ticker = 'NIFTY'
  AND date >= (select date_sub(max(date), INTERVAL 60 MONTH) from index_valuation where ticker = 'NIFTY')
  ORDER BY d.pe
) as dd
WHERE dd.row_number IN ( FLOOR((@total_rows+1)/2), FLOOR((@total_rows+2)/2) )) where ticker = 'NIFTY';

update index_statistics set median_pe_10yr = 
(SELECT AVG(dd.pe) as median_val
FROM (
SELECT d.pe, @rownum:=@rownum+1 as `row_number`, @total_rows:=@rownum
  FROM index_valuation d, (SELECT @rownum:=0) r
  WHERE d.pe is NOT NULL
  AND ticker = 'NIFTY'
  AND date >= (select date_sub(max(date), INTERVAL 120 MONTH) from index_valuation where ticker = 'NIFTY')
  ORDER BY d.pe
) as dd
WHERE dd.row_number IN ( FLOOR((@total_rows+1)/2), FLOOR((@total_rows+2)/2) )) where ticker = 'NIFTY';
*/

SELECT DISTINCT ticker from index_valuation a where ticker like '%200';
UPDATE index_valuation Set ticker = 'NIFTY200' where ticker = 'NIFY200';
select * from index_valuation where ticker = 'NIFTY200' order by date desc;
commit;
-- new quarter results
SELECT b.ticker from daily_data_s a, stock_universe b 
where a.name = b.ticker and 
a.last_result_date = '202303' and 
date = (select max(date) from daily_data_s a) and 
-- b.ticker not in ('ADANIGAS', 'CENTURYPLY', 'GRSE', 'RVNL', 'STRTECH', 'BSOFT') and 
b.ticker not in (select distinct ticker from stock_quarter a where date = '2023-03-31');
-- new annual p&L 
select distinct ticker, max(date) from stock_pnl a where month(date) != 3 group by ticker having max(date) not in ('2018-12-31', '2019-03-31', '2018-06-30') ORDER BY max(date) desc;
SELECT ticker, cons_standalone, max(date) from stock_pnl a group by ticker, cons_standalone 
having max(date) < '2018-01-01' order by max(date) desc, ticker;
-- update stock_quarter set opm = opm/100 where opm > 1;
