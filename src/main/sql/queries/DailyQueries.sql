SET SQL_SAFE_UPDATES = 0;
Commit;
-- delete from company_daily_data_g where company_daily_date = '2018-11-10';
select company_daily_date, count(1) from company_daily_data_g a where a.company_daily_date = (select max(company_daily_date) from company_daily_data_g);
select company_daily_date, count(1)  from company_daily_data_b a where a.company_daily_date = (select max(company_daily_date) from company_daily_data_b);
select * from company_daily_data_g a where a.company_daily_date = (select max(company_daily_date) from company_daily_data_g);
select * from company_daily_data_b a where a.company_daily_date = (select max(company_daily_date) from company_daily_data_b);
select company_daily_date, count(1) from company_daily_data_screener a where a.company_daily_date >= '2018-01-01' group by company_daily_date order by company_daily_date desc;
select * from company_daily_data_screener a where 1=2 and a.company_daily_date = (select max(company_daily_date) from company_daily_data_b) order by company_rank;
update company_daily_data_screener a set a.company_daily_mcap_to_netprofit = a.company_daily_market_cap/a.company_daily_net_profit, a.company_daily_mcap_to_sales = a.company_daily_market_cap/a.company_daily_sales;
update company_daily_data_screener a, screener_sector_industry_mapping b set a.company_daily_sector = b.screener_sector_name, a.company_daily_industry = b.screener_industry_name, a.company_daily_sub_industry = b.screener_subindustry_name where a.company_name_screener = b.screener_name;

select * from screener_sector_industry_mapping; 
select company_daily_date, company_daily_current_pe from company_daily_data_b where company_ticker_b = 'CIPLA:IN' and company_daily_date >= '2017-01-01' order by company_daily_date;
select * from company_daily_data_g where company_daily_date = '2017-12-04';
select * from mutual_fund a where a.mf_date = (select max(company_daily_date) from company_daily_data_g);
select mf_date, count(1) from mutual_fund a where a.mf_date = (select max(company_daily_date) from company_daily_data_g);

CALL ap_process_daily_data();
CALL ap_update_portfolios();
CALL ap_process_portfolio_returns();
CALL ap_process_portfolio_benchmark_returns();

select * from log_table order by TIMESTAMP desc;
truncate table log_table;

select a.company_daily_date, company_daily_current_pe, company_daily_52week_high, a.company_name_b, a.company_sector_name_b, a.company_industry_name_b, a.company_sub_industry_name_b from company_daily_data_b a, a.* where a.company_ticker_b = 'RIL:IN' and a.company_daily_date >= '2015-04-18' order by a.company_daily_date desc, a.company_daily_market_cap desc;
select a.company_daily_date, max(a.company_daily_estimated_eps_nxt_qtr), max(a.company_daily_estimated_eps_yr), min(company_daily_current_pe), max(a.company_daily_current_pe), avg(a.company_daily_current_pe), max(a.company_daily_52week_high)
from company_daily_data_b a where a.company_ticker_b = 'CIPLA:IN' and a.company_daily_date >= '2015-04-18';
-- SOTL:IN

-- finding median 
SET @rowindex := -1;
SELECT
   AVG(b.current_pe)
FROM
   (SELECT @rowindex:=@rowindex + 1 AS rowindex,
           company_daily_data_b.company_daily_current_pe AS current_pe
    FROM company_daily_data_b
    WHERE company_ticker_b = 'CIPLA:IN' and company_daily_date >= '2016-10-21'
    ORDER BY company_daily_data_b.company_daily_current_pe) AS b
WHERE
b.rowindex IN (FLOOR(@rowindex / 2) , CEIL(@rowindex / 2));
-- finding median

-- Price movements query
select  b.company_sector_name_display, b.company_industry_name_display, b.company_sub_industry_name_display, c.company_name_b, c.company_daily_market_cap, a.*, c.*
from company_price_movement a, company_universe b, company_daily_data_b c
where 
a.company_ticker = b.company_ticker and 
a.company_ticker_b = c.company_ticker_b and
c.company_daily_date = (select date_today from setup_dates) and
a.company_ticker in ('RELIANCE','TCS','HDFCBANK','ITC','HDFC','HINDUNILVR','MARUTI','SBIN','ONGC','INFY','ICICIBANK','BHARTIARTL','KOTAKBANK','IOC','LT','COALINDIA','AXISBANK','NTPC','WIPRO','SUNPHARMA','TATAMOTORS','HINDZINC','HCLTECH','VEDL','ULTRACEMCO','ASIANPAINT','BPCL','POWERGRID','INDUSINDBK','BAJFINANCE','M&M','BAJAJ-AUTO','HDFCLIFE','ADANIPORTS','GAIL','MOTHERSUMI','GRASIM','TITAN','BAJAJFINSV','YESBANK','EICHERMOT','TATASTEEL','NESTLEIND','DMART','HEROMOTOCO','INFRATEL','GODREJCP','SBILIFE','JSWSTEEL','GICRE','SHREECEM','HINDPETRO','DABUR','BOSCHLTD','ICICIPRULI','HINDALCO','ZEEL','BRITANNIA','AMBUJACEM','TECHM','MCDOWELL-N','NIACL','IBULHSGFIN','PEL','CIPLA','VAKRANGEE','NMDC','DLF','INDIGO','SIEMENS','PIDILITIND','CADILAHC','BEL','PNB','SUNTV','LUPIN','DRREDDY','MARICO','SAIL','UPL','ICICIGI','AUROPHARMA','IDEA','BHEL','ASHOKLEY','BANKBARODA','PETRONET','OFSS','HAVELLS','CONCOR','ACC','SRTRANSFIN','TVSMOTOR','BHARATFORG','ABB','BIOCON','PFC','NHPC','L&TFH','COLPAL','RECLTD','BAJAJHLDNG','PGHH','MRF','UBL','M&MFIN','DIVISLAB','OIL','KANSAINER','LICHSGFIN','EMAMILTD','FRETAIL','DALMIABHA','GSKCONS','ALKEM','CUMMINSIND','TATAPOWER','GRUH','ADANITRANS','EDELWEISS','PAGEIND','BERGEPAINT','RAJESHEXPO','JINDALSTEL','TORNTPHARM','PCJEWELLER','MRPL','RBLBANK','IGL','BALKRISIND','PNBHOUSING','NBCC','GILLETTE','ADANIENT','GLAXO','CANBK','CHOLAFIN','MOTILALOFS','GODREJIND','3MINDIA','VOLTAS','LTI','IDFCBANK','AUBANK','TATAGLOBAL','GODREJPROP','BANKINDIA','WHIRLPOOL','EXIDEIND','NATCOPHARM','SUPREMEIND','DHFL','RAMCOCEM','TATACHEM','CASTROLIND','RNAM','INDIANB','TATACOMM','ENDURANCE','GLENMARK','MUTHOOTFIN','OBEROIRLTY','NAUKRI','INDHOTEL','HUDCO','CROMPTON','STRTECH','NLCINDIA','COROMANDEL','QUESS','IDBI','BAYERCROP','GRAPHITE','APOLLOHOSP','CENTURYTEX','THERMAX','MPHASIS','JSWENERGY','APOLLOTYRE','KRBL','AIAENG','NATIONALUM','WABCOINDIA','MFSL','CESC','TORNTPOWER','CENTRALBK','SJVN','ADANIPOWER','AMARAJABAT','CRISIL','JUBILANT','BHARATFIN','RAIN','JUBLFOOD','PIIND','GMRINFRA','RPOWER','RELINFRA','SHRIRAMCIT','AJANTPHARM','SYMPHONY','FCONSUMER','VBL','JMFINANCIL','ABFRL','DBL','SPARC','HATSUN','SYNGENE','IBVENTURES','UNIONBANK','GUJGASLTD','GSPL','SUNDRMFAST','CUB','ABBOTINDIA','PRESTIGE','ARVIND','EIHOTEL','MINDTREE','MINDAIND','IBREALEST','HEG','AVANTIFEED','KAJARIACER','FINCABLES','ENGINERSIN','BLUEDART','GODREJAGRO','BBTC','SANOFI','SRF','TRENT','GET&D','ERIS','HEXAWARE','APLLTD','LTTS','MGL','TV18BRDCST','SOLARINDS','PFIZER','MANAPPURAM','ASTRAL','SUNCLAYLTD','WOCKPHARMA','PHOENIXLTD','VGUARD','AEGISCHEM','GUJFLUORO','DCMSHRIRAM','SCHAEFFLER','BASF','TTKPRESTIG','KEC','SKFINDIA','ESCORTS','BATAINDIA','IDFC','AARTIIND','MAHINDCIE','JETAIRWAYS','THOMASCOOK','BIRLACORPN','ASAHIINDIA','DELTACORP','AKZOINDIA','ATUL','KARURVYSYA','HINDCOPPER','IRB','ITI','SYNDIBANK','FINPIPE','RCOM','JKCEMENT','SUZLON','INFIBEAM','VTL','GNFC','KALPATPOWR','HSCL','DISHTV','SFL','CAPF','COFFEEDAY','CEATLTD','CENTURYPLY','LALPATHLAB','RELAXO','FORTIS','COCHINSHIP','CARBORUNIV','BLUESTARCO','REDINGTON','VIJAYABANK','GPPL','WELSPUNIND','FLFL','IPCALAB','500285','JCHAC','STAR','BAJAJCORP','CYIENT','SADBHAV','NCC','PVR','JISLJALEQS','PRISMCEM','RAYMOND','GESHIP','LAXMIMACH','JYOTHYLAB','IOB','GEPIL','GUJALKALI','MOIL','CHAMBLFERT','CANFINHOME','TATAELXSI','DBCORP','CHENNPETRO','BEML','PERSISTENT','MMTC','TIMKEN','ECLERX','GSFC','EIDPARRY','UCOBANK','GRINDWELL','APLAPOLLO','SUNTECK','533033','CGPOWER','KPRMILL','LAURUSLABS','KIRLOSENG','NH','SOUTHBANK','DCBBANK','NETWORK18','IFBIND','INDIACEM','ALBK','JINDALSAW','JAGRAN','JSL','SOBHA','STARCEMENT','JPASSOCIAT','SWANENERGY','BOMDYEING','BAJAJELEC','NIITTECH','RCF','EQUITAS','GODFRYPHLP','IFCI','VSTIND','RADICO','VIPIND','JKLAKSHMI','SREINFRA','RATNAMANI','PNCINFRA','MHRIL','ANDHRABANK','VINATIORGA','BSE','ELGIEQUIP','TATAINVEST','RALLIS','JSLHISAR','SHILPAMED','ALLCARGO','GMDCLTD','UJJIVAN','CERA','MANPASAND','TIMETECHNO','CAPLIPOINT','WELCORP','LINDEINDIA','COX&KINGS','ESSELPACK','GREENPLY','SHOPERSTOP','TECHNO','KTKBANK','ORIENTBANK','CORPBANK','GULFOILLUB','JPPOWER','ITDC','TRITURBINE','MONSANTO','PHILIPCARB','MINDACORP','POLARIS','TRIDENT','SHK','500033','ASHOKA','MCX','SUPRAJIT','FDC','J&KBANK','REPCOHOME','OMAXE','ZENSARTECH','CARERATING','NAVINFLUOR','VRLLOG','SHARDACROP','JUSTDIAL','JKTYRE','VENKEYS','KPIT','MAGMA','BRIGADE','HCC','HFCL','ICRA','CCL','DHANUKA','GAYAPROJ','SCI','SUPPETRO','SHANKARA','HIMATSEIDE','DEEPAKNTR','HATHWAY','HEIDELBERG','HERITGFOOD','THYROCARE','RNAVAL','ENIL','TEJASNET','LAOPALA','DEEPAKFERT','RUPA','GVKPIL','SOMANYCERA','HSIL','GRANULES','LAKSHVILAS','NAVNETEDUL','KSCL','NFL','ITDCEM','ORIENTCEM','NOCIL','GREAVESCOT','JAICORPLTD','PTC','WABAG','EVEREADY','UNICHEMLAB','SONATSOFTW','UFLEX','ADVENZYMES','MAXINDIA','APARINDS','GHCL','SUDARSCHEM','KEI','TNPL','501150','INOXWIND','FSL','VMART','NBVENTURES','TATACOFFEE','NAVKARCORP','MEGH','DENABANK','BALRAMCHIN','MAHABANK','NILKAMAL','ASTRAZEN','SCHNEIDER','BALMLAWRIE','KOLTEPATIL','VESUVIUS','RKFORGE','UNITEDBNK','JBCHEPHARM','TVSSRICHAK','TVTODAY','INDOCO','BRFL','MAHLIFE','INOXLEISUR','IL&FSTRANS','SUVEN','HDIL','IGARASHI','GDL','INGERRAND','CAPACITE','JKIL','8KMILES','HTMEDIA','AHLUCONT','PARAGMILK','MERCK','GICHSGFIN','KWALITY','TEXRAIL','DEN','PFS','ICIL','TATAMETALI','INTELLECT','TAKE','UNITECH','EROSMEDIA','SEQUENT','SITINET','RTNPOWER','DREDGECORP','BALLARPUR','BLISSGVS','PRAJIND','MCLEODRUSS','KITEX','BFUTILITIE','HCL-INSYS','TWL','KESORAMIND','MARKSANS','NIITLTD','TATASPONGE','DBREALTY','JINDALPOLY','JBFIND','BAJAJHIND','RAMCOSYS','MTNL','GATI','RENUKA','BHUSANSTL','ZEELEARN','SINTEX','ABAN','SMLISUZU','ASTRAMICRO','BGRENERGY','SNOWMAN','RELIGARE','VIDEOIND','FEDERALBNK')
order by b.is_sensex desc, b.is_nifty50 desc, b.is_niftyjr desc, b.is_bse100 desc, b.is_nse200 desc, b.is_bse200 desc, b.is_nse500 desc, b.is_bse500 desc, b.company_market_cap_b desc;

/* EOD Verification */
select * from setup_dates;
select * from portfolio_value_history a where a.portfolio_value_date = (select max(portfolio_value_date) from portfolio_value_history);
select * from portfolio_asset_allocation a where a.allocation_date = (select max(allocation_date) from portfolio_asset_allocation);
select * from portfolio_twrr_monthly a where a.client_id in (1007, 1015); 
select * from portfolio_twrr_summary a where a.client_id in (1007, 1015);
select * from portfolio_irr_summary a where a.client_id in (1007, 1015);
select * from portfolio_asset_allocation where client_id in (1007, 1015);
delete from portfolio_asset_allocation where client_id in (1007, 1015);
delete from portfolio_irr_summary where client_id in (1007, 1015);
DELETE from portfolio_benchmark_returns_calculation_support where client_id in (1007, 1015);
delete from portfolio_returns_calculation_support where client_id in (1007, 1015);
/* Portfolio verificaiton with DMAT */
select * from client;
select * from portfolio a where a.client_id in (1007,1015) order by client_id, portfolio_id;
select * from portfolio_holdings a  where a.client_id in (1007,1015) order by client_id, portfolio_id, security_asset_class_id, security_asset_subclass_id, security_id;
select distinct security_name from portfolio_holdings a where a.security_asset_class_id = 40 and a.security_asset_subclass_id > '406000' and a.client_id in (1014) and portfolio_id = 1 order by client_id, portfolio_id, security_asset_class_id, security_asset_subclass_id, security_id;
-- Remove MAJESCO, KITEX, MAJISCO
-- Add ABCAPITAL, TRIDENT (149 - 92)
update portfolio_holdings a, mutual_fund b set a.security_cmp = b.mf_nav where a.security_id = b.mf_scheme_code and a.client_id in (1007,1015);
update portfolio_holdings a set a.security_market_value = security_cmp * security_quantity where a.client_id in (1007,1015);

select * from portfolio_historical_holdings a where a.client_id in (1007,1015) order by client_id, portfolio_id, security_sell_date desc;
select * from portfolio_value_history where client_id in (1007,1015) order by client_id, portfolio_id, portfolio_value_date desc;

/* Enter Cashflow Amount */
select * from portfolio_cashflow a where client_id in (1007,1015) and portfolio_id = 1;
update portfolio_value_history a set a.portfolio_market_value = portfolio_market_value + 50000 where portfolio_value_date >= '2018-01-15' and client_id = 1003 and portfolio_id = 1;
select security_id, sum(a.security_quantity), sum(security_market_value) from portfolio_holdings a where a.client_id in (1005) and portfolio_id = 1 group by security_id order by client_id, portfolio_id, security_asset_class_id, security_asset_subclass_id, security_id;




/* Index Valuation */
select * from index_valuation a where index_valuation_date = (select max(index_valuation_date) from index_valuation);  
select * from fixed_income_index a where index_date = (select max(index_date) from fixed_income_index);

/* Corporate Actions related */
select * from company_price_movement a where price_move_weekly < - 0.5;
select * from company_price_movement a where a.price_move_monthly < -0.5;
select * from company_price_movement a where a.price_move_quarterly < -0.5 order by price_move_quarterly;
select * from company_price_movement a where a.price_move_semianual < -0.5 order by price_move_semianual;
select * from company_price_movement a where a.price_move_annual < -0.5 order by price_move_annual;

select a.company_ticker, a.company_nse_code, a.company_bse_code, a.company_isin_code,  b.sub_industry_id/*,  a.company_sub_industry_name_display*/ from company_universe a, sub_industry b where a.company_sub_industry_name_display = b.sub_industry_name_display; 
select * from sub_industry a;

select * from company_universe where company_short_name like '%Oil%';
select * from company_corp_action a order by a.corp_action_date desc; 
select * from company_daily_data_g a where a.company_ticker like 'OIL' and company_daily_date <= '2017-12-01' order by company_daily_date desc;
select * from company_daily_data_b a where a.company_ticker_b = 'OINL:IN' and a.company_daily_date <= '2017-12-01' order by company_daily_date desc;
update company_daily_data_b a 
set a.company_daily_closing_price = company_daily_closing_price/1, 
a.company_daily_previous_day_closing_price = company_daily_previous_day_closing_price/1,
a.company_daily_eps = company_daily_eps/1,
a.company_daily_best_eps_lst_qtr = company_daily_best_eps_lst_qtr/1,
a.company_daily_est_eps_last_qtr = company_daily_est_eps_last_qtr/1,
a.company_daily_eps_surprise_last_qtr = company_daily_eps_surprise_last_qtr/1,
a.company_daily_estimated_eps_yr = company_daily_estimated_eps_yr/1,
a.company_daily_estimated_eps_nxt_qtr = company_daily_estimated_eps_nxt_qtr/1,
a.company_daily_52week_low = company_daily_52week_low/1,
a.company_daily_52week_high = company_daily_52week_high/1
where company_ticker_b = '' and company_daily_date < '2017-01-12';
update company_daily_data_g 
set company_daily_last_price = company_daily_last_price/1,
company_daily_open_price = company_daily_open_price/1,
company_daily_high_price = company_daily_high_price/1,
company_daily_low_price = company_daily_low_price/1
where company_ticker = '' and company_daily_date < '2017-01-12';

SELECT table_name, table_rows FROM information_schema.tables WHERE table_schema = 'twealthbookdev';
show table status;

SELECT CONCAT(
    'SELECT "', 
    table_name, 
    '" AS table_name, COUNT(*) AS exact_row_count FROM `', 
    table_schema,
    '`.`',
    table_name, 
    '` UNION ' ) 
FROM INFORMATION_SCHEMA.TABLES 
WHERE table_schema = 'twealthbookdev';

/* 
SET FOREIGN_KEY_CHECKS = 0;
SELECT concat('DROP TABLE IF EXISTS `', table_name, '`;')
FROM information_schema.tables
WHERE table_schema = 'twealthbookdev';
SET FOREIGN_KEY_CHECKS = 1;

take a backup 
mysqldump -u root -p<<Password without space>> twealthbookprod > dump_filename.sql

restore

C:\Program Files (x86)\EasyPHP-DevServer-14.1VC9\binaries\mysql\bin>mysql -u roo
t -p twealthbookdev < C:\MyDocuments\16TWealthbook\ProductionOldJars\20180813TWe
althbookprod.sql
Enter password:<<local password is blank>>
*/
