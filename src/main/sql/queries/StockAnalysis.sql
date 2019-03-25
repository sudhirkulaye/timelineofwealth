-- stock PE & PB ratio
SELECT date, last_result_date, name, rank, market_cap,  cmp, pe_ttm, historical_pe_3years, pb_ttm, profit_growth_3years, sales_growth_3years, 
net_profit, sales, opm_latest_quarter, opm_last_year, npm_latest_quarter, npm_last_year, roe, avg_roe_3years, debt, 
debt_to_equity, debt_3years_back  from daily_data_s a 
where date BETWEEN '2018-01-01' and (select date_today from setup_dates) and a.name like 'CARE%' order by date desc, market_cap desc; 
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
