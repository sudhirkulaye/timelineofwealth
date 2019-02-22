SET SQL_SAFE_UPDATES = 0;
Commit;

select count(1), date from nse_price_history
where date = (select max(date) from nse_price_history)
group by date order by date desc;

select count(1), date from bse_price_history
where date = (select max(date) from bse_price_history)
group by date order by date desc;

select count(1), date from mutual_fund_nav_history
where date >= (select date_last_trading_day from setup_dates)
group by date order by date desc;

select count(1), date from daily_data_b
where date >= (select date_last_trading_day from setup_dates)
group by date order by date desc;

select * from daily_data_b where date = (select date_today from setup_dates) order by market_cap desc;

-- proceed EOD
call ap_update_wealth_data();
select * from log_table;
truncate table log_table;

select * from setup_dates;
select count(1), date from wealth_history
where date = (select date_today from setup_dates)
group by date order by date desc;
SELECT count(1), date from wealth_asset_allocation_history
where date = (select date_today from setup_dates)
group by date order by date desc;

-- call ap_set_market_cap_rank('2016-10-21', (select date_today from setup_dates));
call ap_process_stat_calculation();
select * from log_table;
truncate table log_table;

-- find new stocks or modified stocks -- last count 64 on 26th Nov 2018
SELECT count(1), listing_date from stock_universe a group by a.listing_date order by a.listing_date desc; -- WHERE listing_date; > '2018-11-26';

-- stock split probable candidate
select * from stock_split_probability a where a.is_processed = 'NO' order by date desc;

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

select * from daily_data_s where 1 = 2;
select count(1), date from daily_data_s where date >= (select date_last_trading_day from setup_dates)  group by date order by date desc;

select * from index_valuation a where a.date = (select max(date) from index_valuation);
SELECT * from index_statistics a; 

commit;

-- Change of ticker
select distinct(name) from daily_data_s a where a.date = (select max(date) from daily_data_s) and a.name not in (select ticker5 from stock_universe) order by rank; 
select * from stock_universe a where a.name like 'Techno%'; 
select * from daily_data_s a where a.name like 'IDFC%';
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
select * from wealth_details a where a.ticker = 'IDFCFIRSTB';
-- update wealth_details a set a.ticker = 'IDFCFIRSTB' where ticker = 'IDFCBANK';
select * from stock_universe a where ticker in ('IDFCFIRSTB','BAJAJCON');
-- BAJAJCORP to BAJAJCON and 'Bajaj Corp' to 'Bajaj Consumer'
-- IDFCBANK to IDFCFIRSTB and 'IDFC Bank' to 'IDFC First Bank'
select * from stock_cashflow a where ticker = 'BAJFINANCE';
