SET SQL_SAFE_UPDATES = 0;
Commit;

-- Newly added IPOs
select * from stock_universe a where short_name like 'Bharat El%' and is_bse500 = 1 and is_nse500 = 1;
select * from stock_universe a where ticker in ('JIOFIN', 'CYIENTDLM');
select * from subindustry;
-- Recent IPOs which daily trading value is at least 10Cr 
select distinct nse_ticker from nse_price_history 
where date > '2020-01-01' and nse_ticker not in (select ticker from stock_universe) 
and nse_ticker not in (select distinct nse_ticker from nse_price_history where date < '2020-01-01')
and nse_ticker not in (select distinct nse_ticker from nse_price_history where total_traded_value < 100000000);
/*
List of IPOs last updated - 2022-04-20
-- Probable include
-- Vedant Fashions

*/
-- Copy below columns and select results Excel and update and then insert (IPOs.xlsx)
-- ticker	ticker1	ticker2	ticker3	ticker4	ticker5	nse_code	bse_code	isin_code	short_name	name	asset_classid	bse_industry	subindustryid	latest_price	date_latest_price	is_sensex	is_nifty50	is_niftyjr	is_bse100	is_nse100	is_bse200	is_nse200	is_bse500	is_nse500	marketcap	marketcap_rank	pe_ttm	ticker_old	listing_date
-- INSERT INTO stock_universe (ticker, ticker1, ticker2, ticker3, ticker4, ticker5, nse_code, bse_code, isin_code, short_name, name, bse_industry, subindustryid, is_sensex, is_nifty50, is_niftyjr, is_bse100, is_nse100, is_bse200, is_nse200, is_bse500, is_nse500, marketcap, marketcap_rank, pe_ttm, ticker_old, listing_date)
select distinct nse_ticker, '0', '0', '0', '0', '0', nse_ticker, '0', isin_code, nse_ticker, nse_ticker, '406030', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '0', '1', '0', '0', '0', '0', '0', '2022-01-03' 
from nse_price_history b where date = (select date_today from setup_dates) and 
nse_ticker in -- below is the copy of above query
(select distinct nse_ticker from nse_price_history c where c.date = (select date_today from setup_dates) and c.nse_ticker not in (select ticker from stock_universe) 
-- and c.nse_ticker not in (select distinct nse_ticker from nse_price_history where date < '2020-01-01')
and c.nse_ticker not in (select distinct nse_ticker from nse_price_history where total_traded_value < 100000000));

update stock_universe set ticker2 = '' where ticker2 = '0';	
update stock_universe set ticker3 = '' where ticker3 = '0';	
update stock_universe set ticker4 = '' where ticker4 = '0';	
update stock_universe set ticker_old = '' where ticker_old = '0';
