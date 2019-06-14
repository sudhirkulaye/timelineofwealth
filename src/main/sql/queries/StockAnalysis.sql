-- stock PE & PB ratio
SELECT date, last_result_date, name, rank, market_cap,  cmp, pe_ttm, historical_pe_3years, pb_ttm, profit_growth_3years, sales_growth_3years, 
net_profit, sales, opm_latest_quarter, opm_last_year, npm_latest_quarter, npm_last_year, roe, avg_roe_3years, debt, 
debt_to_equity, debt_3years_back  from daily_data_s a 
where date BETWEEN '2018-01-01' and (select date_today from setup_dates) and a.name like 'TCS%' order by date desc, market_cap desc; 
-- IN ('TVS Motor Co.', 'Bajaj Auto', 'Hero Motocorp', 'Eicher Motors');
-- IN ('HDFC Bank', 'ICICI Bank', 'Kotak Mah. Bank', 'Axis Bank, 'IndusInd Bank', 'Yes Bank', 'Bandhan Bank', 'RBL Bank', 'Federal Bank', 'IDFC First Bank', 'City Union Bank', 'AU Small Finance' )
-- IN ('St Bk of India', 'IDBI Bank', 'Punjab Natl.Bank', 'Bank of Baroda', 'Bank of India', 'Canara Bank', 'Indian Bank', 'Central Bank', 'Union Bank (I)'
-- IN ('H D F C', 'Indiabulls Hous.', 'LIC Housing Fin.', 'GRUH Finance', 'PNB Housing', 'H U D C O', 'Dewan Hsg. Fin.', 
-- IN ('Bajaj Fin.', 'Shriram Trans.', 'M & M Fin. Serv.','Muthoot Finance','Cholaman.Inv.&Fn','Bharat Financial','Shri.City Union.','Manappuram Fin.')
                             -- 'Power Fin.Corpn.', 'REC'
-- IN ('Bajaj Holdings', 'L&T Fin.Holdings', 'Aditya Birla Cap', 'Reliance Capital', 'Indiabulls Vent.')
-- IN ('Reliance Nip.Lif', 'ICICI Sec' , 'Edelweiss.Fin.', 'Motil.Oswal.Fin.')
-- IN ('HDFC Life Insur.', 'SBI Life Insuran', 'ICICI Pru Life', 'ICICI Lombard', 'Max Financial', 'Bajaj Finserv','General Insuranc', 'New India Assura')
-- IN ('DLF', 'Oberoi Realty', 'Godrej Propert.', 'Prestige Estates', 
-- IN ('CRISIL', 'CARE Ratings')
-- 
SELECT * from stock_universe a where a.is_nse500 = 1 or a.is_bse500 = 1;
-- stock price movements
SELECT a.* from stock_price_movement a order by ticker;
-- 
SELECT name, min(pe_ttm), max(pe_ttm), min(pb_ttm), max(pb_ttm) FROM daily_data_s 
where date > (select date_sub(date_today, INTERVAL 12 MONTH) from setup_dates)
group by name order by name;

-- query for quarter result tracking
select a.next_earning_date, b.ticker, c.name, c.cmp, c.pe_ttm, c.pb_ttm, c.historical_pe_3years, c.sales, c.net_profit, 
c.yoy_quarterly_sales_growth, c.yoy_quarterly_profit_growth, c.qoq_sales_growth, c.qoq_profit_growth, 
c.opm_latest_quarter, c.opm_last_year, c.npm_latest_quarter, c.npm_last_year, 
c.sales_growth_3years, c.profit_growth_3years, c.roe, 
d.return_1D, d.return_1W, d.return_2W, d.return_1M, d.return_2M, d.return_3M, d.return_6M, d.return_9M, d.return_1Y
from daily_data_b a, stock_universe b, daily_data_s c, stock_price_movement d
where a.ticker_b = b.ticker2 and c.name = b.ticker5 and d.ticker = b.ticker and (b.is_nse500 = 1 or b.is_bse500 = 1) and 
a.date = (select date_today from setup_dates) and c.date = (select date_today from setup_dates)  and 
next_earning_date >= '2019-04-01' order by next_earning_date;

select date, name, pe_ttm, pb_ttm, historical_pe_3years from daily_data_s a where a.name like 'Cadila %' and date > '2019-01-01' order by date desc; 
select date, last_result_date, name, rank, market_cap,  cmp, pe_ttm, historical_pe_3years, pb_ttm, profit_growth_3years, sales_growth_3years, 
net_profit, sales, opm_latest_quarter, opm_last_year, npm_latest_quarter, npm_last_year, roe, avg_roe_3years, debt, 
debt_to_equity, debt_3years_back  from daily_data_s a where date =  (select date_today from setup_dates) order by name;

-- trading data
select date, close_price from nse_price_history a where a.nse_ticker = 'BAJAJ-AUTO' order by date desc; 

select  ticker_b, date, current_pe, price_book from daily_data_b a where ticker_b = 'IIB:IN' order by date desc;