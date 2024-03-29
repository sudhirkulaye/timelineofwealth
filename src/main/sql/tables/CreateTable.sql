SET SQL_SAFE_UPDATES = 0;
Commit;

-- Drop Table sequence_next_high_value;
CREATE TABLE sequence_next_high_value (
  id varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT 'PK - Code for which sequnce no is required to be maintained',
  next_val int(11) DEFAULT NULL COMMENT 'Next high value that is to be used to generate sequence',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='User - Setup - Next Sequence Number for Clinet ID generation';

select * from sequence_next_high_value;

-- Drop Table user ;
CREATE TABLE user (
  email varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Email ID  unique also for communication',
  password varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Encrypted Password',
  active int(1) DEFAULT '1' COMMENT '1:Active Free, 2:Active Standard, 3:Active Premium, 4:Inactive, 5:Closed',
  joining_date date DEFAULT NULL COMMENT 'Joining date',
  prefix varchar(10) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Pre fix',
  name varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'First Name',
  last_name varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Family Name',
  cellno varchar(20) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Cell Number for communication',
  add_line1 varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Address Line 1',
  add_line2 varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Address Line 2',
  add_line3 varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Address Line 3',
  city varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Address City',
  state varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Address State',
  pin varchar(10) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Address PIN Code',
  last_login_time datetime DEFAULT '2000-01-01' COMMENT 'Last login timestamp',
  role_name varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Role Name: ROLE_ADMIN, END_USER, ADVISER',
  PRIMARY KEY (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Users - Who can access the system';

select * from user order by last_login_time desc;

-- update user a set a.last_login_time = '2018-01-01';

-- Drop table member;
CREATE TABLE member (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  first_name varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Member First Name',
  middle_name varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Member Middle Name',
  last_name varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Member Last Name',
  relationship varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Relationship Self,Wife,Husband,Father,Mother,Son,Daughter,Brother,Sister,In-laws,Other',
  birth_date date DEFAULT NULL COMMENT 'Member Birth Date',
  gender varchar(10) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Gender M: Male F: Female O: Other',
  marital_status varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Single, Married, Divorcee, Widow',
  email varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Member email id',
  cellno varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Member Cell Phone NO',
  earning_status varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Earning Status: Dependent, Earning, Not-Earning',
  profession varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Profession Category - Secured Govt. Job,Stable Pvt. Sector Job,Unstable Pvt. Sector Job,Large Size Business,Medium Size Business,Small Size Business,Professional/Self Employed,Athlete/Actor/Model,Pensioner,Retired No Pension,Housewife,HNI,Other',
  industry varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Industry from MSCI Industry Classficiation',
  is_secured_by_pension varchar(3) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Y: Yes, N: No',
  education varchar(30) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Professional, Master, Graduate, Under Graduate',
  is_finance_professional varchar(3) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Y: Yes, N: No',
  expected_retirement_date date DEFAULT '2000-01-01' COMMENT 'Member Expected Retirement Date',
  is_alive varchar(3) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Y: Yes, N: No',
  date_last_update date DEFAULT '2000-01-01' COMMENT 'Date Last Update',
  PRIMARY KEY (memberid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Members - Account Members - Institutional Investor and Retail Investors including earning members and dependents';

SELECT * FROM member a ORDER BY a.memberid;

-- Drop table user_members;
CREATE table user_members (
  email varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Email ID  unique also for communication',
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  relationship varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Relationship Self,Wife,Husband,Father,Mother,Son,Daughter,Brother,Sister,In-laws,Other',
  PRIMARY KEY (memberid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='User - Member Mappings';

select * from user_members a order by a.memberid desc;
/* Deleting member*/
-- delete from user_members where memberid = 1043;
-- delete from member where memberid = 1043;
-- update sequence_next_high_value set next_val = next_val - 1 where id = 'memberid';

-- drop table income_expense_savings;
CREATE table income_expense_savings (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  finyear int(4) NOT NULL COMMENT 'PK financial year',
  regular_income decimal(20,3) NOT NULL COMMENT 'Regular Income',
  interest_dividend_income decimal(20,3) NOT NULL COMMENT 'Interest or Dividend Income',
  rent_income decimal(20,3) NOT NULL COMMENT 'Rent Income',
  other_income decimal(20,3) NOT NULL COMMENT 'Other Income like Royalty',
  gross_total_income decimal(20,3) NOT NULL COMMENT 'Gross Income',
  income_tax decimal(20,3) NOT NULL COMMENT 'Income Tax Paid',
  net_income decimal(20,3) NOT NULL COMMENT 'Net Income',
  tax_rate decimal(5,2) NOT NULL COMMENT 'Tax Rate ',
  investment_total decimal(20,3) NOT NULL COMMENT 'Total investments made during the year',
  investment_increase_in_bankbalance decimal(20,3) NOT NULL COMMENT 'Increase in Bank Balance',
  investment_tax_savings decimal(20,3) NOT NULL COMMENT 'Investment in Tax Saving Instruments',
  investment_in_equity decimal(20,3) NOT NULL COMMENT 'Instruments in Equity',
  investment_in_fixed_income decimal(20,3) NOT NULL COMMENT 'Instruments in Fixed Income, FDs etc',
  investment_in_other decimal(20,3) NOT NULL COMMENT 'Instruments in others',
  gross_total_expenses decimal(20,3) NOT NULL COMMENT 'Gross total expenses',
  infrequent_total_expenses decimal(20,3) NOT NULL COMMENT 'Infrequent total expenses',
  infrequent_medical_expenses decimal(20,3) NOT NULL COMMENT 'Infrequent medical expenses',
  infrequent_renovation_expenses decimal(20,3) NOT NULL COMMENT 'Infrequent renovation expenses',
  infrequent_other_expenses decimal(20,3) NOT NULL COMMENT 'Infrequent Other expenses',
  annual_liability decimal(20,3) NOT NULL COMMENT 'Sum of EMIs and other annual liability',
  normalized_regular_expenses decimal(20,3) NOT NULL COMMENT 'Normalized regular expenses',
  adjustment decimal(20,3) default 0 COMMENT 'Mismatch',
  note varchar(1000) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'Related comment',
  PRIMARY KEY (memberid,finyear)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Income, Expense & Savings - individual member';

select * from income_expense_savings;

-- drop table liability;
CREATE table liability (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  loanid int(2) NOT NULL COMMENT 'PK loan ID unique Auto Generated',
  loan_desc varchar(100) NOT NULL COMMENT 'Loan Description',
  loan_type varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Type of Loan - Home Loan, Car Loan, Education Loan, Personal Loan, Other Loan',
  disbursement_amount decimal(20,3) NOT NULL COMMENT 'Original disbursement amount',
  disbursement_date date NOT NULL COMMENT 'Original disbursement date',
  initial_total_emis int(3) NOT NULL COMMENT 'Initial Total EMIs',
  first_emi_date date NOT NULL COMMENT 'First disbursment date',
  initial_emi_amount decimal(20,3) NOT NULL COMMENT 'Initial EMI Amount',
  current_emi_amount decimal(20,3) NOT NULL COMMENT 'Current EMI Amount',
  current_emi_day Varchar(2) NOT NULL COMMENT 'Current EMI day of the month',
  last_emi_month Varchar(2) NOT NULL COMMENT 'Last EMI month',
  last_emi_year varchar(4) NOT NULL COMMENT 'Last EMI year',
  remaining_emis int(3) NOT NULL COMMENT 'Remaining months',
  interest_rate decimal(7,4) NOT NULL COMMENT 'Interest Rate',
  pv_outstanding_emis decimal(20,3) NOT NULL COMMENT 'PV of outstanding emis',
  active_status VARCHAR(10) NOT NULL DEFAULT 'Active' COMMENT 'Current Active Status: Active or Fully Paid',
  date_last_update date NOT NULL COMMENT 'Date when record last updated',
  PRIMARY KEY (memberid,loanid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Loans & Liabilities';

select * from liability;

-- Drop table insurance;
create table insurance (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  insuranceid int(2) NOT NULL COMMENT 'PK insurance ID unique Auto Generated',
  product_UIN Varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Insurance UIN by IRDA',
  product_name varchar(200) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Insurance policy name',
  category varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Types of Insurance: Term Life Insurance, Whole Life Insurance, Endowment Policy, Money-back Policy, Unit-linked Insurance Plans(ULIPs), Child Plan, Pension Plans, Health Insurance, Motor Insurance, Other Insurance, Annuity Scheme',
  cover_amount decimal(20,3) NOT NULL COMMENT 'Cover Amount',
  premium_amount decimal(20,3) NOT NULL COMMENT 'Prmeium Amount',
  premium_frequency_in_months int(2) NOT NULL COMMENT 'Premium Frequency in months Frequency: 0 - One Time, 1 - Annually, 2: Semiannually, 4: Quarterly,  12: Monthly, 24: Fortnightly, 52: Weekly, 250: Daily',
  last_date_of_premium date NOT NULL COMMENT 'Premium last payment date',
  life_time_cover varchar(5) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Yes or No specially for Money Back',
  expiry_date date NOT NULL COMMENT 'Maturity or Expirty date',
  maturity_amount decimal(20,3) NOT NULL COMMENT 'Maturity Amount or sum assured without bonus in case of money back',
  maturity_frequency int(2) NOT NULL COMMENT 'Maturity Frequency: 0 - One Time, 1 - Annually, 2: Semiannually, 4: Quarterly,  12: Monthly, 24: Fortnightly, 52: Weekly, 250: Daily',
  expected_bonus_amount decimal(20,3) NOT NULL COMMENT 'Bonus amount in case of money back',
  date_last_update date NOT NULL COMMENT 'Date when record last updated',
  PRIMARY KEY (memberid,insuranceid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Insurance Details';
ALTER TABLE insurance
CHANGE COLUMN last_date_premium_paid last_date_of_premium DATE NOT NULL COMMENT 'Prmeium last payment date' ;

select * from insurance;

-- drop table liquidity;
create table liquidity (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  liquidityid int(2) NOT NULL COMMENT 'PK liquidity ID unique Auto Generated',
  liquidity_desc varchar(200) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Liqudity description',
  priority varchar(20) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Priority: Critical, High, Medium, Low',
  expected_start_date date NOT NULL COMMENT 'Expected start Date of liquidity',
  amount_required_start_date decimal(20,3) NOT NULL COMMENT 'Amount required at begining date',
  frequency  int(2) NOT NULL COMMENT 'Frequency: 0 - One Time, 1 - Annually, 2: Semiannually, 4: Quarterly,  12: Monthly, 24: Fortnightly, 52: Weekly, 250: Daily',
  expected_end_date date NOT NULL COMMENT 'Expected Date of liquidity',
  date_last_update date NOT NULL COMMENT 'Date when record last updated',
  PRIMARY KEY (memberid,liquidityid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Liquidity Need/ Financial Goals';

select * from liquidity;

-- drop table asset_classification;
CREATE TABLE asset_classification (
  classid int(6) NOT NULL COMMENT 'Asset Class ID',
  class_name varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Asset Class Display Name',
  subclass_name varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Asset Sub Class name Display',
  asset_class_group varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Group for Asset classes',
  subclass_description varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Description of Asset - Asset Subclass',
  PRIMARY KEY (classid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Asset Classification';

select * from asset_classification order by asset_Class_group;

-- create table mutual_fund_universe_old as select * from mutual_fund_universe;
-- drop table mutual_fund_universe;
create table mutual_fund_universe (
  scheme_code int(15) NOT NULL COMMENT 'PK Mutual Fund Scheme Code Unique',
  isin_div_payout_or_isin_growth varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '-' COMMENT 'ISIN Dividend Payout or Growth Code',
  isin_div_reinvestment varchar(50) COLLATE utf8_unicode_ci DEFAULT '-' COMMENT 'ISIN Dividend Reinvestment Code',
  scheme_code_direct_growth int(15) COMMENT 'Scheme Code for Direct Growth',
  scheme_code_regular_growth int(15) COMMENT 'Scheme Code for Regular Growth',
  fund_house varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Mutual Fund House',
  direct_regular varchar(10) COLLATE utf8_unicode_ci DEFAULT '-' COMMENT 'Regular or Direct Plan',
  dividend_growth varchar(10) COLLATE utf8_unicode_ci DEFAULT '-' COMMENT 'Dividend or Growth Option',
  dividend_freq varchar(10) COLLATE utf8_unicode_ci DEFAULT '-' COMMENT 'Dividend freq. Monthly, Quarterly, Annual, Bonus, Flexible Etc.',
  scheme_name_part varchar(1000) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Mutual Fund Scheme Name - Core',
  scheme_name_full varchar(1000) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Mutual Fund Scheme Name as it is from AMIFI',
  asset_classid int(6) DEFAULT 0 COMMENT 'Mutual Fund asset classid',
  category varchar(100) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'Mutual Fund category',
  equity_style_box varchar(100) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'Mutual Fund Equity Style Box: Large-Mid-Small Cap Vs Growth-Blend-Value',
  debt_style_box VARCHAR(50) NULL DEFAULT '' COMMENT 'Mutual Fund Debt Style Box: High-Mid-Low Credit Quality Vs High-Medium-Low Duration',
  latest_nav decimal(20,4) NOT NULL COMMENT 'Mutual Fund NAV',
  date_latest_nav date DEFAULT '2000-01-01' COMMENT 'Date of latest nav',
  benchmark_ticker varchar(50) COLLATE utf8_unicode_ci DEFAULT '-' COMMENT 'Mutual Fund House',
  PRIMARY KEY (scheme_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Mutual Fund Universe';

select * from mutual_fund_universe a where a.scheme_code = 145112;
select distinct asset_classid, category, equity_style_box, debt_style_box from mutual_fund_universe a;
-- update mutual_fund_universe a set a.scheme_name_part = concat(fund_house, ' ',a.scheme_name_part);
select * from mutual_fund_universe a order by scheme_name_full;

-- Drop table subindustry
CREATE TABLE subindustry (
  subindustryid int(8) NOT NULL COMMENT 'PK Sub Industry ID',
  sector_name_display varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Short Name for display purpose',
  industry_name_display varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Short Name for display purpose',
  sub_industry_name_display varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Short Name for display purpose',
  PRIMARY KEY (subindustryid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Sector-Industry-Subindustry Mapping as per MSCI Sector-Industry Classification 2015';

SELECT * from subindustry;

-- drop table stock_universe;
CREATE TABLE stock_universe (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Usually NSE Code, BSE Code in case there is ''&'' in NSE Code or stock is only listed on BSE',
  ticker1 varchar(50) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'Money Control Code for the stock',
  ticker2 varchar(30) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'Bloomberg code for the stock',
  ticker3 varchar(30) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'Bloomberg Quint code for the stock',
  ticker4 varchar(30) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'Markets Mojo code for the stock',
  ticker5 varchar(30) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'Screener.in code for the stock',
  nse_code varchar(30) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'NSE Code',
  bse_code varchar(30) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'BSE Code',
  isin_code varchar(20) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'ISIN Code ',
  short_name varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Short Name for Display Purpose',
  name varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Full Name on as of NSE or BSE',
  asset_classid int(6) DEFAULT '0' COMMENT 'Stock Asset classid',
  bse_industry varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'BSE industry classification',
  subindustryid int(8) DEFAULT '0' COMMENT 'Sub Industry ID',
  latest_price decimal(20,3) DEFAULT '0.000' COMMENT 'Latest Price',
  date_latest_price date DEFAULT '2000-01-01' COMMENT 'Date of latest price',
  is_sensex int(1) DEFAULT '0' COMMENT '1 = If Stock is in SENSEX ',
  is_nifty50 int(1) DEFAULT '0' COMMENT '1 = If Stock is in NIFTY50',
  is_niftyjr int(1) DEFAULT '0' COMMENT '1 = If Stock is in NIFTY JR',
  is_bse100 int(1) DEFAULT '0' COMMENT '1 = If Stock is in BSE100',
  is_nse100 int(1) DEFAULT '0' COMMENT '1 = If Stock is in NSE100',
  is_bse200 int(1) DEFAULT '0' COMMENT '1 = If Stock is in NSE200',
  is_nse200 int(1) DEFAULT '0' COMMENT '1 = If Stock is in NSE200',
  is_bse500 int(1) DEFAULT '0' COMMENT '1 = If Stock is in BSE500',
  is_nse500 int(1) DEFAULT '0' COMMENT '1 = If Stock is in NSE500',
  is_fno int(1) DEFAULT '0' COMMENT '1 = Future and options are available',
  marketcap decimal(20,0) DEFAULT '0' COMMENT 'Market Cap in Rs',
  marketcap_rank int(5) DEFAULT '0' COMMENT 'Market Cap Rank',
  pe_ttm decimal(10,2) DEFAULT NULL COMMENT 'Company Latest TTM PE ratio',
  ticker_old varchar(30) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Old Ticker',
  listing_date date DEFAULT '2000-01-01' COMMENT 'Listing Date on NSE or BSE',
  PRIMARY KEY (ticker),
  UNIQUE KEY isin_code (isin_code),
  KEY ticker5 (ticker5),
  KEY ticker2 (ticker2)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Securities - Company universe composed of NIFTY50, NIFTY JR, NIFTY100/BSE100, NIFTY200/BSE200, NIFTY500/BSE500 and other small stocks tracked by brokerage houses';
ALTER TABLE stock_universe ADD INDEX index_stock_universe_ticker (ticker);
ALTER TABLE stock_universe ADD INDEX index_stock_universe_ticker2 (ticker2);
ALTER TABLE stock_universe ADD INDEX index_stock_universe_ticker3 (ticker3);
ALTER TABLE stock_universe ADD INDEX index_stock_universe_ticker5 (ticker5);
ALTER TABLE stock_universe ADD INDEX index_stock_universe_is_bse500 (is_bse500);
ALTER TABLE stock_universe ADD INDEX index_stock_universe_is_nse500 (is_nse500);

select * from stock_universe a where a.is_nse500 = 1;
select * from stock_universe order by asset_classid, marketcap desc, ticker;

-- drop table sip;
create table sip (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  sipid int(2) NOT NULL COMMENT 'PK SIP ID unique Auto Generated',
  instrument_type varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'SIP Instrument Type Mutual Fund, PPF, Endowment Ensurance, ULIP, Other',
  scheme_code int(15) NOT NULL COMMENT 'PK Mutual Fund Scheme Code Unique',
  scheme_name varchar(200) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Mutual Fund Or Other Scheme Name',
  start_date date NOT NULL COMMENT 'SIP Start Date',
  end_date date NOT NULL COMMENT 'SIP end Date',
  deduction_day int(2) NOT NULL COMMENT 'Day on which units are purchased',
  amount decimal(20,3)  NOT NULL COMMENT 'SIP Amount ',
  sip_freq int(2) NOT NULL COMMENT 'Frequency: 0 - One Time, 1 - Annually, 2: Semiannually, 4: Quarterly,  12: Monthly, 24: Fortnightly, 52: Weekly, 250: Daily',
  is_active varchar(3) COLLATE utf8_unicode_ci DEFAULT 'Yes' COMMENT 'Is Active: Yes/No',
  PRIMARY KEY (memberid, sipid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='SIP Details';
ALTER TABLE sip ADD INDEX index_sip_memberid (memberid);
select * from sip;

-- drop table wealth_details;
CREATE TABLE wealth_details (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  buy_date date NOT NULL COMMENT 'Security Buy Date',
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Usually NSE Code, BSE Code in case there is ''&'' in NSE Code or stock is only listed on BSE',
  name varchar(1000) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Security Name',
  short_name varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Security Name - Short',
  asset_classid int(6) NOT NULL COMMENT 'Asset Class ID',
  subindustryid int(8) DEFAULT 0 COMMENT 'Sub Industry ID',
  quantity decimal(20,4) DEFAULT NULL COMMENT 'Security Quantity',
  rate decimal(20,4) DEFAULT NULL COMMENT 'Security Buy Rate per Quantity',
  brokerage decimal(20,3) DEFAULT NULL COMMENT 'Security Total Brokerage',
  tax decimal(20,3) DEFAULT NULL COMMENT 'Security Total Tax',
  total_cost decimal(20,3) DEFAULT NULL COMMENT 'Security Total Cost (Buy Rate*Quantity) + Brokerage + Tax',
  net_rate decimal(20,4) DEFAULT NULL COMMENT 'Security effective cost per quantity i.e. Total Cost/Quantity',
  cmp decimal(20,4) DEFAULT NULL COMMENT 'Security Current Market Price',
  market_value decimal(20,3) DEFAULT NULL COMMENT 'Investment market value (CMP*Quanity)',
  holding_period decimal(7,3) DEFAULT NULL COMMENT 'Security holding period in years i.e. Buy date to till date ',
  net_profit decimal(20,3) DEFAULT NULL COMMENT 'Unrealized Net Profit = Market Value - Total Cost',
  absolute_return decimal(10,4) DEFAULT NULL COMMENT 'Unrealized absolute return',
  annualized_return decimal(10,4) DEFAULT NULL COMMENT 'Unrealized annualized return',
  maturity_value decimal(20,3) DEFAULT NULL COMMENT 'Security Maturity Value especially for FDs',
  maturity_date date DEFAULT '1900-01-01' COMMENT 'Security Maturity Value especially for FDs',
  last_valuation_date date DEFAULT '1900-01-01' COMMENT 'Last Valution Date',
  sipid INT(2) NULL DEFAULT 0 COMMENT 'SIP ID for automatic transaction',
  portfoliono VARCHAR(45) NULL DEFAULT '0' COMMENT 'Portfolio Number in case of Mutual Fund',
  PRIMARY KEY (memberid,ticker,buy_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Members current wealth Wealth at transaction level';
ALTER TABLE wealth_details ADD INDEX index_wealth_details_memberid (memberid);

select * from wealth_details a  where a.memberid in (1002) order by a.memberid, a.asset_classid, a.ticker, a.buy_date;
update wealth_details a, stock_universe b set a.asset_classid = b.asset_classid where a.ticker = b.ticker;

CREATE TABLE setup_dates (
  date_today date NOT NULL COMMENT 'Today''s trading date',
  date_last_trading_day date NOT NULL COMMENT 'Last trading date',
  date_start_current_month date NOT NULL COMMENT 'Current month begining date',
  date_start_current_quarter date NOT NULL COMMENT 'Current quarter begining date',
  date_start_current_fin_year date NOT NULL COMMENT 'Current FIN year begining date',
  date_start_1_quarter date NOT NULL COMMENT 'Same as date_start_current_quarter i.e. begining date of current quarter',
  date_start_2_quarter date NOT NULL COMMENT 'Begining date of last quarter',
  date_start_3_quarter date NOT NULL COMMENT 'Begining date of last to last quarters before',
  date_start_4_quarter date NOT NULL COMMENT 'Begining date of quarter started 3 quarters before',
  date_start_next_fin_year date NOT NULL COMMENT 'Next Year date start',
  current_fin_year int(4) NOT NULL COMMENT 'Current FIN Year',
  current_quarter int(1) NOT NULL COMMENT 'Current Quarter'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Date Setup';


insert into setup_dates values ('2018-11-15','2018-11-14','2018-11-01','2018-10-01','2018-04-01','2018-10-01','2018-07-01','2018-04-01','2018-01-01','2019-04-01','2019','3');

SELECT * from setup_dates;

CREATE TABLE log_table (
  timestamp datetime DEFAULT NULL COMMENT 'timestamp',
  log_query varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Log Query'
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Debug - Log table';

select * from log_table;

-- drop table nse_price_history;
create table nse_price_history (
  nse_ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK NSE Code',
  series varchar(5) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'PK Series for NSE Code only EQ, BE & SM ',
  open_price decimal(20,3) DEFAULT NULL COMMENT 'open price',
  high_price decimal(20,3) DEFAULT NULL COMMENT 'high price',
  low_price decimal(20,3) DEFAULT NULL COMMENT 'low price',
  close_price decimal(20,3) DEFAULT NULL COMMENT 'close price',
  last_price decimal(20,3) DEFAULT NULL COMMENT 'last price',
  previous_close_price decimal(20,3) DEFAULT NULL COMMENT 'previous close price',
  total_traded_quantity decimal(20,3) DEFAULT NULL COMMENT 'Total traded quanitity',
  total_traded_value decimal(20,3) DEFAULT NULL COMMENT 'total traded value',
  date date NOT NULL COMMENT 'trading date',
  total_trades decimal(20,3) DEFAULT NULL COMMENT 'total no of traded',
  isin_code varchar(20) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'ISIN Code ',
  PRIMARY KEY (nse_ticker,date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='NSE Daily Price History';
ALTER TABLE nse_price_history ADD INDEX index_nse_price_history_date (date);
ALTER TABLE nse_price_history ADD INDEX index_nse_price_history_nse_ticker (nse_ticker);

select * from nse_price_history a where a.date = (select max(date) from nse_price_history);

-- drop table bse_price_history;
create table bse_price_history (
  bse_ticker varchar(30) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'PK BSE Code',
  company_name varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'BSE Name',
  company_group varchar(3) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Group only A, B, T',
  company_type varchar(3) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Type',
  open_price decimal(20,3) DEFAULT NULL COMMENT 'open price',
  high_price decimal(20,3) DEFAULT NULL COMMENT 'high price',
  low_price decimal(20,3) DEFAULT NULL COMMENT 'low price',
  close_price decimal(20,3) DEFAULT NULL COMMENT 'close price',
  last_price decimal(20,3) DEFAULT NULL COMMENT 'last price',
  previous_close_price decimal(20,3) DEFAULT NULL COMMENT 'previous close price',
  total_trades decimal(20,3) DEFAULT NULL COMMENT 'total no of traded',
  total_traded_quantity decimal(20,3) DEFAULT NULL COMMENT 'Total traded quanitity',
  total_traded_value decimal(20,3) DEFAULT NULL COMMENT 'total traded value',
  isin_code varchar(20) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'ISIN Code ',
  date date NOT NULL COMMENT 'trading date',
  PRIMARY KEY (bse_ticker,date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='BSE Daily Price History';
ALTER TABLE bse_price_history ADD INDEX index_bse_price_history_date (date);
ALTER TABLE bse_price_history ADD INDEX index_bse_price_history_bse_ticker (bse_ticker);

SELECT * from bse_price_history a where a.date = (select max(date) from bse_price_history);

SET SQL_SAFE_UPDATES = 0;
Commit;

create table mutual_fund_nav_history (
  scheme_code int(15) NOT NULL COMMENT 'PK Mutual Fund Scheme Code Unique',
  date date NOT NULL COMMENT 'PK Mutual Fund NAV Date',
  nav decimal(20,4) DEFAULT '0.000' COMMENT 'Mutual Fund NAV',
  PRIMARY KEY (scheme_code,date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='BSE Daily Price History';
ALTER TABLE mutual_fund_nav_history ADD INDEX index_mutual_fund_nav_history_date (date);
ALTER TABLE mutual_fund_nav_history ADD INDEX index_mutual_fund_nav_history_scheme_code (scheme_code);

-- drop table timelineofwealth.daily_data_b;
CREATE TABLE daily_data_b (
  ticker_b varchar(30) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'Bloomberg code for the stock',
  date date NOT NULL COMMENT 'PK Date; mapped to UTIME',
  name_b varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Full Name on Bloomberg; mapped to disp_name',
  closing_price decimal(20,3) DEFAULT '0.000' COMMENT 'Last Close Price of the day; mapped to last_price',
  previous_day_closing_price decimal(20,3) DEFAULT '0.000' COMMENT 'Previous trading day closing price; mapped to ',
  volume decimal(20,2) DEFAULT '0.00' COMMENT 'Day Volume; mapped to volume',
  volume_30d decimal(20,2) DEFAULT '0.00' COMMENT '30 Day Avg Volume; mapped to volume_30d',
  market_cap decimal(20,0) DEFAULT '0' COMMENT 'Market Cap in Rs; mapped to market_cap',
  shares_outstanding decimal(20,0) DEFAULT '0' COMMENT 'Shares Outstanding; mapped to (company_daily_market_cap/company_daily_closing_price)',
  eps decimal(10,3) DEFAULT '0.000' COMMENT 'EPS; mapped to eps',
  best_eps_lst_qtr decimal(10,3) DEFAULT '0.000' COMMENT 'Best EPS last Quarter; mapped to best_eps_lst_qtr',
  est_eps_last_qtr decimal(10,3) DEFAULT '0.000' COMMENT 'Estimated EPS last Quarter; mapped to est_eps_last_qtr',
  eps_surprise_last_qtr decimal(10,3) DEFAULT '0.000' COMMENT 'EPS surprise last Quarter; mapped to eps_surprise_last_qtr',
  estimated_eps_yr decimal(10,3) DEFAULT '0.000' COMMENT 'Estimated EPS for Year; mapped to estimated_eps_yr',
  estimated_eps_nxt_qtr decimal(10,3) DEFAULT '0.000' COMMENT 'Estimated EPS next Quarter; mapped to estimated_eps_nxt_qtr',
  current_pe decimal(10,3) DEFAULT '0.000' COMMENT 'PE Ratio; mapped to current_pe',
  estimated_pe_cur_yr decimal(10,3) DEFAULT '0.000' COMMENT 'PE Ratio; mapped to estimated_pe_cur_yr',
  price_book decimal(10,3) DEFAULT '0.000' COMMENT 'PE Ratio; mapped to price_book',
  price_to_sales decimal(10,3) DEFAULT '0.000' COMMENT 'Price to Sales; mapped to ',
  dividend_yield decimal(10,3) DEFAULT '0.000' COMMENT 'Dividend Yield; mapped to dividend_indicated_gross_yield',
  sector_name_b varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Sector; mapped to company_sector',
  industry_name_b varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Industry; mapped to company_industry',
  sub_industry_name_b varchar(50) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Sub Industry Name taken from Bloomberg; mapped to ',
  DS199 varchar(32) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Sector; mapped to DS199',
  DS201 varchar(90) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Industry; mapped to DS201',
  fiftytwo_week_low decimal(10,3) DEFAULT '0.000' COMMENT '52 week low; mapped to range_52wk_low',
  fiftytwo_week_high decimal(10,3) DEFAULT '0.000' COMMENT '52 week High; mapped to range_52wk_high',
  price_chge_1D decimal(10,2) DEFAULT '0.00' COMMENT '52 week High; mapped to price_chge_1D',
  pct_chge_1D decimal(10,3) DEFAULT '0.000' COMMENT '52 week High; mapped to pct_chge_1D',
  total_return_1year decimal(10,3) DEFAULT '0.000' COMMENT 'Total Return 1 Year; mapped to pct_return_52wk',
  total_return_YTD decimal(10,3) DEFAULT '0.000' COMMENT 'Total Return YTD; mapped to ',
  market_cap_rank int(5) DEFAULT '0' COMMENT 'Rank by Market Cap; mapped to ',
  last_earning_date date DEFAULT '2000-01-01' COMMENT 'Last Earnings Date; mapped to last_earning_date',
  next_earning_date date DEFAULT '2000-01-01' COMMENT 'Next Earnings Date; mapped to next_earning_date',
  latest_anncmt_period varchar(10) COLLATE utf8_unicode_ci DEFAULT '2000:Q1' COMMENT 'Latest announcement period; mapped to latest_anncmt_period',
  shares int(3) DEFAULT '0' COMMENT 'If not 10 then it could be split or bonus event; mapped to shares',
  PRIMARY KEY (ticker_b, date),
  KEY idx_daily_data_b_ticker_b (ticker_b),
  KEY idx_daily_data_b_date (date)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Securities - Daily Data obtained from Bloomberg Portfolio';
ALTER TABLE daily_data_b ADD INDEX index_daily_data_b_date (date);

select count(1) from daily_data_b;
-- truncate table daily_data_b;
select count(1) from company_daily_data_b;

CREATE TABLE index_universe (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  name varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index Name',
  description varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index Name',
  value decimal(10,2) DEFAULT NULL COMMENT 'Index value',
  PRIMARY KEY (ticker)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Index Universe';
select * from index_universe;
-- benchmark & benchmark_constituent

CREATE TABLE index_valuation (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  date date NOT NULL COMMENT 'PK Index valuation date',
  pe decimal(10,3) DEFAULT NULL COMMENT 'Index PE Ratio',
  pb decimal(10,3) DEFAULT NULL COMMENT 'Index PB Ratio',
  div_yield decimal(10,4) DEFAULT NULL COMMENT 'Index Div Yield Ratio',
  value decimal(10,3) DEFAULT NULL COMMENT 'Index value',
  turnover decimal(20,3) DEFAULT NULL COMMENT 'Index turnover in Rs',
  implied_earnings decimal(10,3) DEFAULT NULL COMMENT 'Index implied earnings Index Value/index PE',
  PRIMARY KEY (ticker,date)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Valutation - Index Valuation Data';
ALTER TABLE index_valuation ADD INDEX index_index_valuation_ticker (ticker);
ALTER TABLE index_valuation ADD INDEX index_index_valuation_date (date);

select * from index_valuation;

CREATE TABLE wealth_history (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  date date NOT NULL COMMENT 'Date ',
  value decimal(20,3) DEFAULT NULL COMMENT 'Portfolio market value related to date',
  PRIMARY KEY (memberid,date)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Historical Wealth Values';
ALTER TABLE wealth_history ADD INDEX index_wealth_history_memberid (memberid);
ALTER TABLE wealth_history ADD INDEX index_wealth_history_date (date);

select * from wealth_history a where a.memberid in (1000,1011) order by date;

-- Drop table wealth_asset_allocation_history;
CREATE TABLE wealth_asset_allocation_history (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  date date NOT NULL COMMENT 'PK Date of asset allocation',
  asset_class_group varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Group for Asset classes',
  value decimal(20,3) DEFAULT NULL COMMENT 'Market Value by Asset class group',
  value_percent decimal(7,4) DEFAULT NULL COMMENT '%(Market Value) of Asset class',
  PRIMARY KEY (memberid,date,asset_class_group)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Wealth Asset Allocation History ';
ALTER TABLE wealth_asset_allocation_history ADD INDEX index_wealth_asset_allocation_history_memberid (memberid);
ALTER TABLE wealth_asset_allocation_history ADD INDEX index_wealth_asset_allocation_history_date (date);

select * from wealth_asset_allocation_history a where a.date = (select date_today from setup_dates) order by memberid, asset_class_group;

-- drop table stock_split_probability;
CREATE TABLE stock_split_probability (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  date date NOT NULL COMMENT 'PK Index valuation date',
  close_price decimal(20,3) DEFAULT NULL COMMENT 'Close price today',
  previous_close_price decimal(20,3) DEFAULT NULL COMMENT 'Close price last trading session',
  day_percent_change decimal(7,4) DEFAULT NULL COMMENT '%(Market Value) of Asset class',
  is_processed varchar(3) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'NO' COMMENT 'Status is processed? YES/No',
  note varchar(50) COLLATE utf8_unicode_ci NOT NULL DEFAULT '' COMMENT 'Status is processed? YES/No',
  PRIMARY KEY (ticker,date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Probable Stock Split List ';

-- Drop table sip_process_log;
CREATE TABLE sip_process_log (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  sipid int(2) NOT NULL COMMENT 'PK SIP ID unique Auto Generated',
  scheme_code int(15) NOT NULL COMMENT 'PK Mutual Fund Scheme Code Unique',
  first_process_date date NOT NULL COMMENT 'Initial Process Date when reocrd is first inserted in this table then it will not be updated',
  last_process_date date NOT NULL COMMENT 'Last Process Date when units are added to existing wealth_details record',
  next_process_date date NOT NULL COMMENT 'next Process Date will be updated when next time it suppose to be updated',
  amount_cummulative decimal(20,3)  NOT NULL COMMENT 'SIP Amount cumluated',
  nav decimal(20,3)  NOT NULL COMMENT 'SIP NAV ',
  units_added_last_sip decimal(20,3)  NOT NULL COMMENT 'SIP Units added in the last processing date',
  units_before_addition decimal(20,3)  NOT NULL COMMENT 'SIP Units before addition',
  PRIMARY KEY (memberid,sipid)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='SIP Process Log ';
ALTER TABLE sip_process_log ADD INDEX index_sip_process_log_memberid (memberid);


-- DROP table sip_process_msg_log;
CREATE TABLE sip_process_msg_log (
  date date NOT NULL COMMENT 'PK member ID unique Auto Generated',
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  sipid int(2) NOT NULL COMMENT 'PK SIP ID unique Auto Generated',
  scheme_code int(15) NOT NULL COMMENT 'PK Mutual Fund Scheme Code Unique',
  message_type varchar(5) COLLATE utf8_unicode_ci NOT NULL  COMMENT 'Message Type: ERR OR MSG',
  message varchar(50) COLLATE utf8_unicode_ci NOT NULL  COMMENT 'Message Type',
  PRIMARY KEY (date,memberid,sipid,scheme_code)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='SIP Process Erro Log ';
ALTER TABLE sip_process_msg_log ADD INDEX index_sip_process_msg_log_memberid (memberid);
ALTER TABLE sip_process_msg_log ADD INDEX index_sip_process_msg_log_date (date);

-- Drop table adviser_user_mapping
CREATE TABLE adviser_user_mapping (
  adviserid varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Email ID or login ID of adviser',
  userid varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Email ID or login ID of user',
  is_adviser_manager varchar(3) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Yes: Will have edit access to edit cliens data NO: can only give opinion',
  PRIMARY KEY (adviserid, userid)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Adviser User Mapping ';

SELECT * from adviser_user_mapping;
--  insert into adviser_user_mapping (select 'sudhirkulaye', email, 'Yes' from user where email <> 'sudhirkulaye' and email not in (select userid from adviser_user_mapping where adviserid = 'sudhirkulaye'));

CREATE TABLE daily_data_s (
  date date NOT NULL,
  rank int(11) DEFAULT '0',
  name varchar(100) CHARACTER SET latin1 NOT NULL,
  cmp decimal(10,3) DEFAULT '0.000',
  market_cap decimal(20,3) DEFAULT '0.000',
  last_result_date int(11) DEFAULT '0',
  net_profit decimal(20,3) DEFAULT '0.00',
  sales decimal(20,3) DEFAULT '0.00',
  yoy_quarterly_sales_growth decimal(10,4) DEFAULT '0.00',
  yoy_quarterly_profit_growth decimal(10,4) DEFAULT '0.00',
  qoq_sales_growth decimal(10,4) DEFAULT '0.00',
  qoq_profit_growth decimal(10,4) DEFAULT '0.00',
  opm_latest_quarter decimal(10,4) DEFAULT '0.00',
  opm_last_year decimal(10,4) DEFAULT '0.00',
  npm_latest_quarter decimal(10,4) DEFAULT '0.00',
  npm_last_year decimal(10,4) DEFAULT '0.00',
  profit_growth_3years decimal(10,4) DEFAULT '0.00',
  sales_growth_3years decimal(10,4) DEFAULT '0.00',
  pe_ttm decimal(10,3) DEFAULT '0.00',
  historical_pe_3years decimal(10,3) DEFAULT '0.00',
  peg_ratio decimal(10,3) DEFAULT '0.00',
  pb_ttm decimal(10,3) DEFAULT '0.00',
  ev_to_ebit decimal(10,3) DEFAULT '0.00',
  dividend_payout decimal(10,4) DEFAULT '0.00',
  roce decimal(10,4) DEFAULT '0.00',
  roe decimal(10,4) DEFAULT '0.00',
  avg_roce_3years decimal(10,4) DEFAULT '0.00',
  avg_roe_3years decimal(10,4) DEFAULT '0.00',
  debt decimal(20,3) DEFAULT '0.00',
  debt_to_equity decimal(10,3) DEFAULT '0.00',
  debt_3years_back decimal(20,3) DEFAULT '0.00',
  mcap_to_netprofit decimal(10,3) DEFAULT '0.00',
  mcap_to_sales decimal(10,3) DEFAULT '0.00',
  sector varchar(50) CHARACTER SET latin1 DEFAULT 'NA',
  industry varchar(100) CHARACTER SET latin1 DEFAULT 'NA',
  sub_industry varchar(100) CHARACTER SET latin1 DEFAULT 'NA',
  PRIMARY KEY (date,name),
  KEY name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Securities - Daily Data obtained from Screener Watchlist';
ALTER TABLE daily_data_s ADD INDEX index_daily_data_s_date (date);
ALTER TABLE daily_data_s ADD INDEX index_daily_data_s_name (name);

select count(1) from daily_data_s a where date = (select date_today from setup_dates);

-- drop table index_statistics; 
CREATE TABLE index_statistics (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  mean_returns_1yr decimal(10,4) DEFAULT NULL COMMENT 'Avg returns 1 yerar',
  median_returns_1yr decimal(10,4) DEFAULT NULL COMMENT 'Median returns 1 yerar',
  mean_returns_3yr decimal(10,4) DEFAULT NULL COMMENT 'Avg returns 3 yerars',
  median_returns_3yr decimal(10,4) DEFAULT NULL COMMENT 'Median returns 3 yerars',
  mean_returns_5yr decimal(10,4) DEFAULT NULL COMMENT 'Avg returns 5 yerars',
  median_returns_5yr decimal(10,4) DEFAULT NULL COMMENT 'Median returns 5 yerars',
  mean_returns_10yr decimal(10,4) DEFAULT NULL COMMENT 'Avg returns 10 yerars',
  median_returns_10yr decimal(10,4) DEFAULT NULL COMMENT 'Median returns 10 yerars',
  minimum_returns_1yr decimal(10,4) DEFAULT NULL COMMENT 'Minimum returns 1 yerar',
  minimum_returns_1yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Minimum returns 1 yerar',
  maximum_returns_1yr decimal(10,4) DEFAULT NULL COMMENT 'Maximum returns 1 yerar',
  maximum_returns_1yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Maximum returns 1 yerar',
  minimum_returns_3yr decimal(10,4) DEFAULT NULL COMMENT 'Minimum returns 3 yerars',
  minimum_returns_3yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Minimum returns 3 yerars',
  maximum_returns_3yr decimal(10,4) DEFAULT NULL COMMENT 'Maximum returns 3 yerars',
  maximum_returns_3yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Maximum returns 3 yerars',
  minimum_returns_5yr decimal(10,4) DEFAULT NULL COMMENT 'Minimum returns 5 yerars',
  minimum_returns_5yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Minimum returns 5 yerars',
  maximum_returns_5yr decimal(10,4) DEFAULT NULL COMMENT 'Maximum returns 5 yerars',
  maximum_returns_5yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Maximum returns 5 yerars',
  minimum_returns_10yr decimal(10,4) DEFAULT NULL COMMENT 'Minimum returns 10 yerars',
  minimum_returns_10yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Minimum returns 10 yerars',
  maximum_returns_10yr decimal(10,4) DEFAULT NULL COMMENT 'Maximum returns 10 yerars', 
  maximum_returns_10yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Maximum returns 10 yerars',
  standard_deviation_1yr decimal(10,4) DEFAULT NULL COMMENT 'Standard Deviation 1 year',
  standard_deviation_3yr decimal(10,4) DEFAULT NULL COMMENT 'Standard Deviation 3 years',
  standard_deviation_5yr decimal(10,4) DEFAULT NULL COMMENT 'Standard Deviation 5 years',
  standard_deviation_10yr decimal(10,4) DEFAULT NULL COMMENT 'Standard Deviation 10 years',
  mean_pe_1yr decimal(10,4) DEFAULT NULL COMMENT 'Avg PE 1 yerar',
  median_pe_1yr decimal(10,4) DEFAULT NULL COMMENT 'Median PE 1 yerar',
  minimum_pe_1yr decimal(10,4) DEFAULT NULL COMMENT 'Minimum PE 1 yerar',
  maximum_pe_1yr decimal(10,4) DEFAULT NULL COMMENT 'Maximum PE 1 yerar',
  mean_pe_3yr decimal(10,4) DEFAULT NULL COMMENT 'Avg PE 3 yerars',
  median_pe_3yr decimal(10,4) DEFAULT NULL COMMENT 'Median PE 3 yerars',
  minimum_pe_3yr decimal(10,4) DEFAULT NULL COMMENT 'Minimum PE 3 yerars',
  maximum_pe_3yr decimal(10,4) DEFAULT NULL COMMENT 'Maximum PE 3 yerars',
  mean_pe_5yr decimal(10,4) DEFAULT NULL COMMENT 'Avg PE 5 yerars',
  median_pe_5yr decimal(10,4) DEFAULT NULL COMMENT 'Median PE 5 yerars',
  minimum_pe_5yr decimal(10,4) DEFAULT NULL COMMENT 'Minimum PE 5 yerars',
  maximum_pe_5yr decimal(10,4) DEFAULT NULL COMMENT 'Maximum PE 5 yerars',
  mean_pe_10yr decimal(10,4) DEFAULT NULL COMMENT 'Avg PE 10 yerars',
  median_pe_10yr decimal(10,4) DEFAULT NULL COMMENT 'Median PE 10 yerars',
  minimum_pe_10yr decimal(10,4) DEFAULT NULL COMMENT 'Minimum PE 10 yerars',
  maximum_pe_10yr decimal(10,4) DEFAULT NULL COMMENT 'Maximum PE 10 yerars',
  mean_pe decimal(10,4) DEFAULT NULL COMMENT 'Avg PE all yerars',
  median_pe decimal(10,4) DEFAULT NULL COMMENT 'Median PE all yerars',
  minimum_pe decimal(10,4) DEFAULT NULL COMMENT 'Minimum PE all yerars',
  maximum_pe decimal(10,4) DEFAULT NULL COMMENT 'Maximum PE all yerars',
  current_pe decimal(10,4) DEFAULT NULL COMMENT 'Current PE',
  last_updated date DEFAULT NULL COMMENT 'Last Updated',
  PRIMARY KEY (ticker)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Statistics - Index Statistic Data';

select * from index_statistics; 

-- drop table mutual_fund_stats;
CREATE TABLE mutual_fund_stats (
  scheme_code int(15) NOT NULL COMMENT 'PK Mutual Fund Scheme Code Unique',
  scheme_name_part varchar(1000) NOT NULL COMMENT 'Mutual Fund Scheme Short Name',
  scheme_type varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'MF Type',
  scheme_index varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'MF Benchmark Index',
  scheme_investment_style varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'MF Investment Style',
  total_returns_y0 DECIMAL(10,4) NOT NULL COMMENT 'Current Year Returns (YTD)',
  total_returns_y1 DECIMAL(10,4) NOT NULL COMMENT '1 Year before Returns',
  total_returns_y2 DECIMAL(10,4) NOT NULL COMMENT '2 Year before Returns',
  total_returns_y3 DECIMAL(10,4) NOT NULL COMMENT '3 Year before Returns',
  total_returns_y4 DECIMAL(10,4) NOT NULL COMMENT '4 Year before Returns',
  total_returns_y5 DECIMAL(10,4) NOT NULL COMMENT '5 Year before Returns',
  total_returns_y6 DECIMAL(10,4) NOT NULL COMMENT '6 Year before Returns',
  total_returns_y7 DECIMAL(10,4) NOT NULL COMMENT '7 Year before Returns',
  total_returns_y8 DECIMAL(10,4) NOT NULL COMMENT '8 Year before Returns',
  total_returns_y9 DECIMAL(10,4) NOT NULL COMMENT '9 Year before Returns',
  total_returns_y10 DECIMAL(10,4) NOT NULL COMMENT '10 Year before Returns',
  trailing_return_1yr DECIMAL(10,4) NOT NULL COMMENT '1 Year Trailing Returns',
  trailing_return_3yr DECIMAL(10,4) NOT NULL COMMENT '3 Years Trailing Returns',
  trailing_return_5yr DECIMAL(10,4) NOT NULL COMMENT '5 Years Trailing Returns',
  trailing_return_10yr DECIMAL(10,4) NOT NULL COMMENT '10 Years Trailing Returns',
  quartile_rank_y1 INT(1) NOT NULL COMMENT 'Quartile Rank for 1 Year before Returns',
  quartile_rank_y2 INT(1) NOT NULL COMMENT 'Quartile Rank for 2 Year before Returns',
  quartile_rank_y3 INT(1) NOT NULL COMMENT 'Quartile Rank for 3 Year before Returns',
  quartile_rank_y4 INT(1) NOT NULL COMMENT 'Quartile Rank for 4 Year before Returns',
  quartile_rank_y5 INT(1) NOT NULL COMMENT 'Quartile Rank for 5 Year before Returns',
  quartile_rank_y6 INT(1) NOT NULL COMMENT 'Quartile Rank for 6 Year before Returns',
  quartile_rank_y7 INT(1) NOT NULL COMMENT 'Quartile Rank for 7 Year before Returns',
  quartile_rank_y8 INT(1) NOT NULL COMMENT 'Quartile Rank for 8 Year before Returns',
  quartile_rank_y9 INT(1) NOT NULL COMMENT 'Quartile Rank for 9 Year before Returns',
  quartile_rank_y10 INT(1) NOT NULL COMMENT 'Quartile Rank for 10 Year before Returns',
  quartile_rank_1yr INT(1) NOT NULL COMMENT 'Quartile Rank for 1 Year Trailing Returns',
  quartile_rank_3yr INT(1) NOT NULL COMMENT 'Quartile Rank for 3 Years Trailing Returns',
  quartile_rank_5yr INT(1) NOT NULL COMMENT 'Quartile Rank for 5 Years Trailing Returns',
  quartile_rank_10yr INT(1) NOT NULL COMMENT 'Quartile Rank for 10 Years Trailing Returns',
  sector_basic_materials DECIMAL(10,4) NOT NULL COMMENT 'Exposure to sector basic materials',
  sector_consumer_cyclical DECIMAL(10,4) NOT NULL COMMENT 'Exposure to sector consumer cyclical',
  sector_finacial_services DECIMAL(10,4) NOT NULL COMMENT 'Exposure to sector finacial services',
  sector_industrial DECIMAL(10,4) NOT NULL COMMENT 'Exposure to sector industrial',
  sector_technology DECIMAL(10,4) NOT NULL COMMENT 'Exposure to sector technology',
  sector_consumer_defensive DECIMAL(10,4) NOT NULL COMMENT 'Exposure to sector consumer defensive',
  sector_healthcare DECIMAL(10,4) NOT NULL COMMENT 'Exposure to sector healthcare',
  stock_1 varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT '1st Preferred Stock',
  stock_2 varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT '2nd Preferred Stock',
  stock_3 varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT '3rd Preferred Stock',
  stock_4 varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT '4th Preferred Stock',
  PRIMARY KEY (scheme_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Selected Mutual Fund Statistics';

select * from mutual_fund_stats; 

-- drop table mutual_fund_house; 
create table mutual_fund_house (
  fund_house varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Mutual Fund House Name',
  fund_house_agency varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Mutual Fund House Agency Name',
  PRIMARY KEY (fund_house)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Mutual Fund House And Agency';
SELECT * from mutual_fund_house; 

-- drop table stock_pnl;
create table stock_pnl (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  cons_standalone varchar(1) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK C: Consolidated S:Standalone ',
  date date NOT NULL COMMENT 'PK Date',
  sales DECIMAL(20,3) NOT NULL COMMENT 'Annual Sales',
  expenses DECIMAL(20,3) NOT NULL COMMENT 'Annual Expense',
  operating_profit DECIMAL(20,3) NOT NULL COMMENT 'Annual Operating Profit',
  other_income DECIMAL(20,3) NOT NULL COMMENT 'Annual Other Income',
  depreciation DECIMAL(20,3) NOT NULL COMMENT 'Annual Depreciation',
  interest DECIMAL(20,3) NOT NULL COMMENT 'Annual Interest',
  profit_before_tax DECIMAL(20,3) NOT NULL COMMENT 'Annual profit before tax',
  tax DECIMAL(20,3) NOT NULL COMMENT 'Annual tax',
  net_profit DECIMAL(20,3) NOT NULL COMMENT 'Annual net Profit',
  eps DECIMAL(10,3) NOT NULL COMMENT 'Annual earning per share',
  pe DECIMAL(10,3) NOT NULL COMMENT 'Annual price to earnings',
  price DECIMAL(10,3) NOT NULL COMMENT 'Share Price',
  dummy1 DECIMAL(10,3) NOT NULL COMMENT 'dummy1',
  ratios DECIMAL(10,3) NOT NULL COMMENT 'Ratios',
  dividend_payout DECIMAL(10,4) NOT NULL COMMENT 'Dividend Payout',
  opm DECIMAL(10,4) NOT NULL COMMENT 'Operating Profit Margin',
  npm DECIMAL(10,4) NOT NULL COMMENT 'Net Profit Margin',
  re DECIMAL(10,4) NOT NULL COMMENT 'Return on Equity',
  tax_rate decimal(10,4) DEFAULT '0.0000' COMMENT 'Tax Rate',
  sales_g decimal(10,4) DEFAULT '0.0000' COMMENT 'YoY Sales growth',
  ebitda_g decimal(10,4) DEFAULT '0.0000' COMMENT 'YoY EBITDA growth',
  pat_g decimal(10,4) DEFAULT '0.0000' COMMENT 'YoY PAT growth',
  non_op_inc_g decimal(10,4) DEFAULT '0.0000' COMMENT 'YoY Non Operating Income growth',
  debt_to_capital decimal(10,4) DEFAULT '0.0000' COMMENT 'Debt To Capital',
  ppe_to_sales decimal(10,4) DEFAULT '0.0000' COMMENT 'Net PPE to Sales',
  dep_to_ppe decimal(10,4) DEFAULT '0.0000' COMMENT 'Depreciation to Net PPE',
  non_op_inc_to_invst decimal(10,4) DEFAULT '0.0000' COMMENT 'Non Operating Income to Investment',
  noplat decimal(20,3) DEFAULT '0.000' COMMENT 'Net Operating profit less Adjusted Tax',
  capex decimal(20,3) DEFAULT '0.000' COMMENT 'Capex',
  fcff decimal(20,3) DEFAULT '0.000' COMMENT 'Free Cash Flow to the Firm',
  sales_g_3yr decimal(10,4) DEFAULT '0.0000' COMMENT '3 Years Sales growth',
  sales_g_5yr decimal(10,4) DEFAULT '0.0000' COMMENT '5 Years Sales growth',
  sales_g_10yr decimal(10,4) DEFAULT '0.0000' COMMENT '10 Years Sales growth',
  avg_opm_3yr decimal(10,4) DEFAULT '0.0000' COMMENT '3 Years Avg. OPM',
  avg_opm_5yr decimal(10,4) DEFAULT '0.0000' COMMENT '5 Years Avg. OPM',
  avg_opm_10yr decimal(10,4) DEFAULT '0.0000' COMMENT '10 Years Avg. OPM',
  avg_opm decimal(10,4) DEFAULT '0.0000' COMMENT 'Long Term Avg. OPM',
  opm_min decimal(10,4) DEFAULT '0.0000' COMMENT 'Min. OPM',
  opm_max decimal(10,4) DEFAULT '0.0000' COMMENT 'Max. OPM',
  avg_roic_3yr decimal(10,4) DEFAULT '0.0000' COMMENT '3 Years Avg. ROIC',
  avg_roic_5yr decimal(10,4) DEFAULT '0.0000' COMMENT '5 Years Avg. ROIC',
  avg_roic_10yr decimal(10,4) DEFAULT '0.0000' COMMENT '3 Years Avg. ROIC',
  avg_roic decimal(10,4) DEFAULT '0.0000' COMMENT 'Long Term Avg. ROIC',
  roic_min decimal(10,4) DEFAULT '0.0000' COMMENT 'Min. ROIC',
  roic_max decimal(10,4) DEFAULT '0.0000' COMMENT 'Max. ROIC',
  avg_ppe_to_sales decimal(10,4) DEFAULT '0.0000' COMMENT 'Avg. Net PPE to Sales',
  avg_dep_to_ppe decimal(10,4) DEFAULT '0.0000' COMMENT 'Avg. Depreciation to Net PPE',
  PRIMARY KEY (ticker, cons_standalone, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stock Annual P&L Results';
ALTER TABLE stock_pnl ADD INDEX index_stock_pnl_ticker (ticker);
ALTER TABLE stock_pnl ADD INDEX index_stock_pnl_date (date);

select count(1), year(date) from stock_pnl a group by year(date) order by date desc; 
select * from stock_pnl a where ticker in ('TCS') and date >= '2019-03-31' and sales = 0 and expenses = 0 and operating_profit = 0 and other_income = 0;
select * from stock_pnl a where ticker like 'NETWORK%' and date = '2017-12-31';
-- update stock_pnl a SET ticker = 'BAJFINANCE_1' where a.ticker = 'BAJFINANCE' and cons_standalone = 'C'; 

-- drop table stock_quarter;
create table stock_quarter (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  cons_standalone varchar(1) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK C: Consolidated S:Standalone ',
  date date NOT NULL COMMENT 'PK Date',
  sales DECIMAL(20,3) NOT NULL COMMENT 'Annual Sales',
  expenses DECIMAL(20,3) NOT NULL COMMENT 'Annual Expense',
  operating_profit DECIMAL(20,3) NOT NULL COMMENT 'Annual Operating Profit',
  other_income DECIMAL(20,3) NOT NULL COMMENT 'Annual Other Income',
  depreciation DECIMAL(20,3) NOT NULL COMMENT 'Annual Depreciation',
  interest DECIMAL(20,3) NOT NULL COMMENT 'Annual Interest',
  profit_before_tax DECIMAL(20,3) NOT NULL COMMENT 'Annual profit before tax',
  tax DECIMAL(20,3) NOT NULL COMMENT 'Annual tax',
  net_profit DECIMAL(20,3) NOT NULL COMMENT 'Annual net Profit',
  dummy1 DECIMAL(10,3) NOT NULL COMMENT 'dummy1',
  opm DECIMAL(10,4) NOT NULL COMMENT 'Operating Profit Margin',
  noplat decimal(20,3) DEFAULT '0.000' COMMENT 'Net Operating Profit Less Adjusted For Tax',
  ttm_sales decimal(20,3) DEFAULT '0.000' COMMENT 'TTM Sales',
  ttm_ebitda decimal(20,3) DEFAULT '0.000' COMMENT 'TTM EBITDA',
  ttm_noplat decimal(20,3) DEFAULT '0.000' COMMENT 'TTM NOPLAT',
  ttm_opm decimal(10,4) DEFAULT '0.0000' COMMENT 'Operating Margin i.e. EBITDA% based on TTM',
  sales_g decimal(10,4) DEFAULT '0.0000' COMMENT 'YoY Sales growth ',
  ttm_sales_g decimal(10,4) DEFAULT '0.0000' COMMENT 'YoY TTM Sales growth',
  ebitda_g decimal(10,4) DEFAULT '0.0000' COMMENT 'YoY EBITDA growth',
  ttm_ebitda_g decimal(10,4) DEFAULT '0.0000' COMMENT 'YoY ebitda growth based on TTM ebitda',
  mcap decimal(20,3) DEFAULT '0.000' COMMENT 'Market Cap on the result day',
  price decimal(20,3) DEFAULT '0.000' COMMENT 'Stock price on the result day',
  result_date DATE NULL COMMENT 'Quarter result date',
  PRIMARY KEY (ticker, cons_standalone, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stock Quarter P&L Results';
ALTER TABLE stock_quarter ADD INDEX index_stock_quarter_ticker (ticker);
ALTER TABLE stock_quarter ADD INDEX index_stock_quarter_date (date);
-- ALTER TABLE `timelineofwealth`.`stock_quarter` 
-- ADD COLUMN `result_date` DATE NULL COMMENT 'Quarter result date' AFTER `price`;


select count(1), year(date) from stock_quarter a group by year(date) order by date desc; 
select * from stock_quarter a where sales = 0 and expenses = 0 and operating_profit = 0 and other_income = 0;
select * from stock_quarter a where a.ticker in ('ICICIBANK') and date >= '2019-03-31';
-- update stock_quarter a set a.ticker = 'BAJFINANCE_1' where a.ticker = 'BAJFINANCE' and cons_standalone = 'C'; 
select * from stock_quarter a where date = '2021-09-30';
-- pending results
SELECT b.ticker from daily_data_s a, stock_universe b where a.name = b.ticker5 and a.last_result_date = '201812' and date = '2019-02-01' and b.ticker not in (select distinct ticker from stock_quarter a where date = '2018-12-31');


-- drop table stock_balancesheet;
create table stock_balancesheet (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  cons_standalone varchar(1) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK C: Consolidated S:Standalone ',
  date date NOT NULL COMMENT 'PK Date',
  equity_share_capital DECIMAL(20,3) NOT NULL COMMENT 'Equity share capital',
  reserves DECIMAL(20,3) NOT NULL COMMENT 'Reserves',
  borrowings DECIMAL(20,3) NOT NULL COMMENT 'Borrowings',
  other_liabilities DECIMAL(20,3) NOT NULL COMMENT 'Other liabilities',
  total_equity_and_debt DECIMAL(20,3) NOT NULL COMMENT 'Total equity and debt',
  dummy1 DECIMAL(20,3) NOT NULL COMMENT 'Dummy1',
  net_block DECIMAL(20,3) NOT NULL COMMENT 'Net block',
  capital_work_in_progress DECIMAL(20,3) NOT NULL COMMENT 'Capital work in progress',
  investments DECIMAL(20,3) NOT NULL COMMENT 'Investments',
  other_assets DECIMAL(20,3) NOT NULL COMMENT 'Other assets',
  total_assets DECIMAL(20,3) NOT NULL COMMENT 'Total assets',
  capex DECIMAL(20,3) NOT NULL COMMENT 'Capex',
  working_capital DECIMAL(20,3) NOT NULL COMMENT 'Working capital',
  debtors DECIMAL(20,3) NOT NULL COMMENT 'Debtors',
  inventory DECIMAL(20,3) NOT NULL COMMENT 'Inventory',
  dummy2 DECIMAL(20,3) NOT NULL COMMENT 'Dummy2',
  debtor_days DECIMAL(20,3) NOT NULL COMMENT 'Debtor days',
  inventory_turnover DECIMAL(10,4) NOT NULL COMMENT 'Inventory turnover',
  dummy3 DECIMAL(20,3) NOT NULL COMMENT 'Dummy3',
  return_on_equity DECIMAL(10,4) NOT NULL COMMENT 'Return on equity',
  return_on_capital_emp DECIMAL(10,4) NOT NULL COMMENT 'Return on capital employed',
  PRIMARY KEY (ticker, cons_standalone, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stock Balancesheet';
ALTER TABLE stock_balancesheet ADD INDEX index_stock_balancesheet_ticker (ticker);
ALTER TABLE stock_balancesheet ADD INDEX index_stock_balancesheetr_date (date);

select count(1), year(date) from stock_balancesheet a group by year(date) order by date desc; 
select * from stock_balancesheet a where a.ticker = 'HDFC' and 1=2;
select * from stock_balancesheet a where ticker = 'CENTRUM' and equity_share_capital = 0; 

-- drop table stock_cashflow;
create table stock_cashflow (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  cons_standalone varchar(1) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK C: Consolidated S:Standalone ',
  date date NOT NULL COMMENT 'PK Date',
  cash_from_operating_activity DECIMAL(20,3) NOT NULL COMMENT 'Cash from operating activity',
  cash_from_investing_activity DECIMAL(20,3) NOT NULL COMMENT 'Cash from investing activity',
  cash_from_financing_activity DECIMAL(20,3) NOT NULL COMMENT 'Cash from financing activity',
  net_cashflow DECIMAL(20,3) NOT NULL COMMENT 'Net Cashflow',
  PRIMARY KEY (ticker, cons_standalone, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stock Cashflow';
ALTER TABLE stock_cashflow ADD INDEX index_stock_cashflow_ticker (ticker);
ALTER TABLE stock_cashflow ADD INDEX index_stock_cashflow_date (date);

select count(1), year(date) from stock_cashflow a group by year(date) order by date desc; 
select count(1) from stock_cashflow a where a.ticker = 'TCS' and 1=2;
SELECT * from stock_cashflow a where a.ticker = 'CENTRUM' and 1=2; 

-- drop table stock_price_movement;
CREATE TABLE stock_price_movement (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  CMP decimal(20,3) DEFAULT NULL COMMENT 'CMP',
  52w_min decimal(20,3) DEFAULT NULL COMMENT '52-Week Min Price',
  52w_max decimal(20,3) DEFAULT NULL COMMENT '52-Week Max Price',
  up_52w_min decimal(10,3) DEFAULT NULL COMMENT 'Up from 52-Week Min Price',
  down_52w_max decimal(10,4) DEFAULT NULL COMMENT 'Down from 52-Week Max Price',
  return_1D decimal(10,4) DEFAULT NULL COMMENT '1 Day Returns',
  return_1W decimal(10,4) DEFAULT NULL COMMENT '1 Week Returns',
  return_2W decimal(10,4) DEFAULT NULL COMMENT '2 Weeks Returns',
  return_1M decimal(10,4) DEFAULT NULL COMMENT '1 Month Returns',
  return_2M decimal(10,4) DEFAULT NULL COMMENT '2 Months Returns',
  return_3M decimal(10,4) DEFAULT NULL COMMENT '3 Months Returns',
  return_6M decimal(10,4) DEFAULT NULL COMMENT '6 Months Returns',
  return_9M decimal(10,4) DEFAULT NULL COMMENT '9 Months Returns',
  return_1Y decimal(10,4) DEFAULT NULL COMMENT '1 Year Returns',
  return_2Y decimal(10,4) DEFAULT NULL COMMENT '2 Years Returns',
  return_3Y decimal(10,4) DEFAULT NULL COMMENT '3 Years Returns',
  return_5Y decimal(10,4) DEFAULT NULL COMMENT '5 Years Returns',
  return_10Y decimal(10,4) DEFAULT NULL COMMENT '10 Years Returns',
  return_YTD decimal(10,4) DEFAULT NULL COMMENT 'YTD Returns',
  1w_min decimal(20,3) NULL DEFAULT 0 COMMENT '1 Week Min Price',
  1w_max decimal(20,3) NULL DEFAULT 0 COMMENT '1 Week Max Price',
  2w_min decimal(20,3) NULL DEFAULT 0 COMMENT '2 Week Min Price',
  2w_max decimal(20,3) NULL DEFAULT 0 COMMENT '2 Week Max Price',
  1m_min decimal(20,3) NULL DEFAULT 0 COMMENT '1 Month Min Price',
  1m_max decimal(20,3) NULL DEFAULT 0 COMMENT '1 Month Max Price',
  2m_min decimal(20,3) NULL DEFAULT 0 COMMENT '2 Month Min Price',
  2m_max decimal(20,3) NULL DEFAULT 0 COMMENT '2 Month Max Price',
  3m_min decimal(20,3) NULL DEFAULT 0 COMMENT '3 Month Min Price',
  3m_max decimal(20,3) NULL DEFAULT 0 COMMENT '3 Month Max Price',
  6m_min decimal(20,3) NULL DEFAULT 0 COMMENT '6 Month Min Price',
  6m_max decimal(20,3) NULL DEFAULT 0 COMMENT '6 Month Max Price',
  PRIMARY KEY (ticker)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stock Price Movement';
ALTER TABLE stock_price_movement ADD INDEX index_stock_price_movement_ticker (ticker);

-- drop table stock_price_movement_history;
CREATE TABLE stock_price_movement_history (
  date date NOT NULL COMMENT 'PK Date',
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  return_1D decimal(10,4) DEFAULT NULL COMMENT '1 Day Returns',
  return_1W decimal(10,4) DEFAULT NULL COMMENT '1 Week Returns',
  return_2W decimal(10,4) DEFAULT NULL COMMENT '2 Weeks Returns',
  return_1M decimal(10,4) DEFAULT NULL COMMENT '1 Month Returns',
  PRIMARY KEY (date, ticker)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stock Price Movement History';
ALTER TABLE stock_price_movement_history ADD INDEX index_stock_price_movement_history_ticker (ticker);
ALTER TABLE stock_price_movement_history ADD INDEX index_stock_price_movement_history_date (date);

-- TRUNCATE stock_price_movement_history
SELECT * from stock_price_movement_history WHERE ticker like 'TCS%' order by date desc;
SELECT * from stock_price_movement_history WHERE date = '2019-05-06' order by ticker, date desc;
SELECT * from stock_price_movement order by ticker;

-- DROP table composite;
CREATE TABLE composite (
  compositeid int(3) NOT NULL COMMENT 'PK Composite id',
  name varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Composite Name',
  description varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Composite Description',
  min_size decimal(20,3) NOT NULL COMMENT 'Minimum Portfolio Size',
  benchmarkid int(3) DEFAULT 1 COMMENT 'Benchmark ticker',
  asset_classid int(6) DEFAULT '0' COMMENT 'Asset Class ID',
  amc_name varchar(50) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'Name of the ACM',
  adviserid varchar(100) COLLATE utf8_unicode_ci DEFAULT '' COMMENT 'Adviser login id mapped in adviser_user_mapping',
  adviser_memberid int(11) NOT NULL DEFAULT '0' COMMENT 'Dummy memberid linked to model portfolio',
  portfolioid int(3) NOT NULL DEFAULT '1' COMMENT 'Linked to model portfolio',
  PRIMARY KEY (compositeid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Composite - Portfolio Strategies';

select * from composite; 
   
-- DROP table portfolio;
CREATE TABLE portfolio (
  memberid int(11) NOT NULL COMMENT 'PK member i.e. client ID unique Auto Generated',
  portfolioid int(3) NOT NULL COMMENT 'PK Portfolio No unique',
  status varchar(20) COLLATE utf8_unicode_ci DEFAULT 'Active' COMMENT 'Active, Inactive, Closed',
  description varchar(200) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Description of the portfolio',
  start_date date DEFAULT NULL COMMENT 'Portfolio Start Date',
  end_date date DEFAULT NULL COMMENT 'Portfolio Expected End Date',
  compositeid int(3) NOT NULL COMMENT 'Composite id',
  net_investment DECIMAL(20,3) NULL DEFAULT 0 COMMENT 'Net Cash-inflow',
  market_value decimal(20,3) NULL DEFAULT 0  COMMENT 'Market value of the portfolio',
  holding_period decimal(7,3) DEFAULT 0 COMMENT 'Holding period in years i.e. Start date to till date ',
  net_profit decimal(20,3) DEFAULT NULL COMMENT 'Unrealized Net Profit = Market Value - Net Investment',
  absolute_return DECIMAL(20,4) NULL DEFAULT 0 COMMENT 'Absolute Returns',
  annualized_return DECIMAL(20,4) NULL DEFAULT 0 COMMENT 'Annualized Returns',
  PRIMARY KEY (memberid,portfolioid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Portfolio - Portfolio Description';
ALTER TABLE portfolio ADD INDEX index_portfolio_memberid (memberid);

SELECT * from portfolio;
select b.first_name, a.* from portfolio a, member b where a.memberid = b.memberid;
select compositeid, count(1) from portfolio a GROUP BY compositeid;

-- DROP table mosl_code;
CREATE TABLE moslcode_memberid (
  memberid int(11) NOT NULL COMMENT 'PK member i.e. client ID unique Auto Generated',
  moslcode varchar(20) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK MOSL CODE',
  PRIMARY KEY (memberid, moslcode)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='MOSL Code And MemberID Cross Reference';
select * from moslcode_memberid;

-- DROP table portfolio_cashflow;
CREATE TABLE portfolio_cashflow (
  memberid int(11) NOT NULL COMMENT 'PK member i.e. client ID unique Auto Generated',
  portfolioid int(3) NOT NULL COMMENT 'PK Portfolio No unique',
  date date NOT NULL COMMENT 'Date on which major cash inflow or outflow happens',
  cashflow decimal(20,3) NOT NULL COMMENT 'Amount of cash inflow (negative) or outflow (outflow) happens',
  description varchar(500) COLLATE utf8_unicode_ci NULL DEFAULT '' COMMENT 'Cashflow Description',
  PRIMARY KEY (memberid, portfolioid, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Portfolio - Cashflow';
ALTER TABLE portfolio_cashflow ADD INDEX index_portfolio_cashflow_memberid (memberid);
ALTER TABLE portfolio_cashflow ADD INDEX index_portfolio_cashflow_date (date);
select * from portfolio_cashflow;

-- DROP table portfolio_holdings;
CREATE TABLE portfolio_holdings (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  portfolioid int(3) NOT NULL COMMENT 'PK Portfolio No unique',
  buy_date date NOT NULL COMMENT 'Security Buy Date',
  ticker varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Usually NSE Code, BSE Code in case there is ''&'' in NSE Code or stock is only listed on BSE',
  name varchar(1000) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Security Name',
  short_name varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Security Name - Short',
  asset_classid int(6) NOT NULL COMMENT 'Asset Class ID',
  subindustryid int(8) DEFAULT '0' COMMENT 'Sub Industry ID',
  quantity decimal(20,4) DEFAULT NULL COMMENT 'Security Quantity',
  rate decimal(20,4) DEFAULT NULL COMMENT 'Security Buy Rate per Quantity',
  brokerage decimal(20,3) DEFAULT NULL COMMENT 'Security Total Brokerage',
  tax decimal(20,3) DEFAULT NULL COMMENT 'Security Total Tax',
  total_cost decimal(20,3) DEFAULT NULL COMMENT 'Security Total Cost (Buy Rate*Quantity) + Brokerage + Tax',
  net_rate decimal(20,4) DEFAULT NULL COMMENT 'Security effective cost per quantity i.e. Total Cost/Quantity',
  cmp decimal(20,4) DEFAULT NULL COMMENT 'Security Current Market Price',
  market_value decimal(20,3) DEFAULT NULL COMMENT 'Investment market value (CMP*Quanity)',
  holding_period decimal(7,3) DEFAULT NULL COMMENT 'Security holding period in years i.e. Buy date to till date ',
  net_profit decimal(20,3) DEFAULT NULL COMMENT 'Unrealized Net Profit = Market Value - Total Cost',
  absolute_return decimal(10,4) DEFAULT NULL COMMENT 'Unrealized absolute return',
  annualized_return decimal(10,4) DEFAULT NULL COMMENT 'Unrealized annualized return',
  maturity_value decimal(20,3) DEFAULT NULL COMMENT 'Security Maturity Value especially for FDs',
  maturity_date date DEFAULT '1900-01-01' COMMENT 'Security Maturity Value especially for FDs',
  last_valuation_date date DEFAULT '1900-01-01' COMMENT 'Last Valution Date',
  PRIMARY KEY (memberid, portfolioid, buy_date, ticker)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
ALTER TABLE portfolio_holdings ADD INDEX index_portfolio_holdings_memberid (memberid);

SELECT * from portfolio_holdings a where a.memberid = 1026;

-- DROP table portfolio_value_history;
CREATE TABLE portfolio_value_history (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  portfolioid int(3) NOT NULL COMMENT 'PK Portfolio No unique',
  date date NOT NULL COMMENT 'Date ',
  value decimal(20,3) DEFAULT NULL COMMENT 'Portfolio market value related to date',
  PRIMARY KEY (memberid, portfolioid, date)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Portfolio - Historical Values';
ALTER TABLE portfolio_value_history ADD INDEX index_portfolio_value_history_memberid (memberid);
ALTER TABLE portfolio_value_history ADD INDEX index_portfolio_value_history_date (date);

SELECT * from portfolio_value_history;

-- DROP table portfolio_historical_holdings;
CREATE TABLE portfolio_historical_holdings (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  portfolioid int(3) NOT NULL COMMENT 'PK Portfolio No unique',
  buy_date date NOT NULL COMMENT 'Security Buy Date',
  ticker varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Usually NSE Code, BSE Code in case there is ''&'' in NSE Code or stock is only listed on BSE',
  name varchar(1000) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Security Name',
  short_name varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Security Name - Short',
  asset_classid int(6) NOT NULL COMMENT 'Asset Class ID',
  subindustryid int(8) DEFAULT '0' COMMENT 'Sub Industry ID',
  quantity decimal(20,4) DEFAULT NULL COMMENT 'Security Quantity',
  rate decimal(20,4) DEFAULT NULL COMMENT 'Security Buy Rate per Quantity',
  brokerage decimal(20,3) DEFAULT NULL COMMENT 'Security Total Brokerage',
  tax decimal(20,3) DEFAULT NULL COMMENT 'Security Total Tax',
  total_cost decimal(20,3) DEFAULT NULL COMMENT 'Security Total Cost (Buy Rate*Quantity) + Brokerage + Tax',
  net_rate decimal(20,4) DEFAULT NULL COMMENT 'Security effective cost per quantity i.e. Total Cost/Quantity',
  sell_date date NOT NULL COMMENT 'Security Sell Date',
  sell_rate decimal(20,4) DEFAULT NULL COMMENT 'Security Sell Rate per Quantity',
  brokerage_sell decimal(20,3) DEFAULT NULL COMMENT 'Brokerage for sell',
  tax_sell decimal(20,3) DEFAULT NULL COMMENT 'Tax for sell',
  net_sell decimal(20,3) DEFAULT NULL COMMENT 'Security Total Sell (Sell Rate*Quantity) - Brokerage - Tax',
  net_sell_rate decimal(20,4) DEFAULT NULL COMMENT 'Security effective sell per quantity i.e. Net Sell/Quantity',
  holding_period decimal(7,3) DEFAULT NULL COMMENT 'Security holding period in years i.e. Buy date to till date ',
  net_profit decimal(20,3) DEFAULT NULL COMMENT 'Unrealized Net Profit = Market Value - Total Cost',
  absolute_return decimal(20,4) DEFAULT NULL COMMENT 'Unrealized absolute return',
  annualized_return decimal(20,4) DEFAULT NULL COMMENT 'Unrealized annualized return',
  fin_year varchar(9) COLLATE utf8_unicode_ci NOT NULL COMMENT 'FIN Year when security was sold',
  PRIMARY KEY (memberid,portfolioid,buy_date,ticker,sell_date)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Portfolio - Historical Holdings';
ALTER TABLE portfolio_historical_holdings ADD INDEX index_portfolio_historical_holdings_memberid (memberid);


SELECT * FROM portfolio_historical_holdings; 

-- DROP TABLE mosl_transaction;
CREATE TABLE mosl_transaction (
  moslcode varchar(20) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK MOSL Client Code',
  exchange varchar(10) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT 'Exchange',
  date date NOT NULL COMMENT 'Transaction Date',
  script_name varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Script Name',
  sell_buy varchar(6) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Sell Buy',
  quantity decimal(20,3) DEFAULT '0' COMMENT 'Quantity',
  rate decimal(20,4) DEFAULT '0' COMMENT 'Buy Rate',
  amount decimal(20,3) DEFAULT '0' COMMENT 'Amount = quantity x rate',
  brokerage decimal(20,3) DEFAULT '0' COMMENT 'Brokerage',
  txn_charges decimal(20,3) DEFAULT '0' COMMENT 'Transaction Charges',
  service_tax decimal(20,3) DEFAULT '0' COMMENT 'Service Charges',
  stamp_duty decimal(20,3) DEFAULT '0' COMMENT 'Stamp Duty Charges',
  stt_ctt decimal(20,3) DEFAULT '0' COMMENT 'STT CTT Charges',
  net_rate decimal(20,4) DEFAULT '0' COMMENT 'Net Rate',
  net_amount decimal(20,3) DEFAULT '0' COMMENT 'Net Amount',
  order_no varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT 'Order No.',
  trade_no varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci NOT NULL COMMENT 'Trade No',
  is_processed varchar(1) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT 'N' NULL COMMENT 'Y/N for is Processed',
  PRIMARY KEY (mosl_code, date, scrip_name, sell_buy, order_no, trade_no)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='MOSL Transactions';
ALTER TABLE mosl_transaction ADD INDEX index_mosl_transaction_date (date);
ALTER TABLE mosl_transaction ADD INDEX index_mosl_transaction_moslcode (moslcode); 
ALTER TABLE mosl_transaction ADD INDEX index_mosl_transaction_script_name (script_name);

SELECT * from mosl_transaction where 1=2; 
-- UPDATE mosl_transaction a set is_processed = 'N';

-- DROP table portfolio_asset_allocation;
CREATE TABLE portfolio_asset_allocation (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  portfolioid int(3) NOT NULL COMMENT 'PK Portfolio No unique',
  date date NOT NULL COMMENT 'PK Date of asset allocation',
  asset_class_group varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Group for Asset classes',
  value decimal(20,3) DEFAULT NULL COMMENT 'Market Value of Asset sub class',
  value_percent decimal(7,4) DEFAULT NULL COMMENT '%(Market Value) of Asset class',
  PRIMARY KEY (memberid,portfolioid,date,asset_class_group)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Portfolio - Daily Asset Allocation ';
ALTER TABLE portfolio_asset_allocation ADD INDEX index_portfolio_asset_allocation_memberid (memberid);
ALTER TABLE portfolio_asset_allocation ADD INDEX index_portfolio_asset_allocation_date (date);

SELECT * from portfolio_asset_allocation; 

-- DROP table portfolio_returns_calculation_support; 
CREATE TABLE portfolio_returns_calculation_support (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  portfolioid int(3) NOT NULL COMMENT 'PK Portfolio No unique',
  date date NOT NULL COMMENT 'Date either cashflow date or end month',
  cashflow decimal(20,3) DEFAULT NULL COMMENT 'Cashflow amount Cash-in is negative, Cash-out is positive',
  value decimal(20,3) DEFAULT NULL COMMENT 'Market Value of the portfolio',
  description varchar(500) COLLATE utf8_unicode_ci NULL DEFAULT '' COMMENT 'Cashflow Description',
  PRIMARY KEY (memberid,portfolioid,date)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Portfolio - Time Weighted Returns calculaiton support data';
ALTER TABLE portfolio_returns_calculation_support ADD INDEX index_portfolio_returns_calculation_support_memberid (memberid);

SELECT * FROM portfolio_returns_calculation_support;

-- DROP table portfolio_twrr_monthly;
CREATE TABLE portfolio_twrr_monthly (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  portfolioid int(3) NOT NULL COMMENT 'PK Portfolio No unique',
  returns_year int(4) NOT NULL COMMENT 'PK Year of returns',
  returns_calendar_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR for calendar year',
  returns_fin_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR for FIN year',
  returns_mar_ending_quarter decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Jan to Mar',
  returns_jun_ending_quarter decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Apr to Jun',
  returns_sep_ending_quarter decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Jul to Sep',
  returns_dec_ending_quarter decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Oct to Dec',
  returns_jan decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Jan',
  returns_feb decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Feb',
  returns_mar decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Mar',
  returns_apr decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Apr',
  returns_may decimal(20,4) DEFAULT NULL COMMENT 'TWRR for May',
  returns_jun decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Jun',
  returns_jul decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Jul',
  returns_aug decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Aug',
  returns_sep decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Sep',
  returns_oct decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Oct',
  returns_nov decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Nov',
  returns_dec decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Dec',
  PRIMARY KEY (memberid,portfolioid,returns_year)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Portfolio - TWRR (Time Weighted Rate of Returns) monthwise ';
ALTER TABLE portfolio_twrr_monthly ADD INDEX index_portfolio_twrr_monthly_memberid (memberid);

SELECT * from portfolio_twrr_monthly; 

-- DROP table portfolio_twrr_summary;
CREATE TABLE portfolio_twrr_summary (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  portfolioid int(3) NOT NULL COMMENT 'PK Portfolio No unique',
  benchmarkid int(3) NOT NULL DEFAULT '0' COMMENT 'Benchmark ID for comparision, 0 for portfolio',
  returns_date date DEFAULT NULL COMMENT 'Returns as of',
  returns_twrr_since_current_month decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns from current month',
  returns_twrr_since_current_quarter decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns from current quarter',
  returns_twrr_since_fin_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since current fin year',
  returns_twrr_ytd decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since Jan 1st',
  returns_twrr_three_months decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since three months',
  returns_twrr_half_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since six months',
  returns_twrr_one_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since one year',
  returns_twrr_two_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since two year',
  returns_twrr_three_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since three year',
  returns_twrr_five_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since five year',
  returns_twrr_ten_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since ten year',
  returns_twrr_since_inception decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since inception',
  PRIMARY KEY (memberid,portfolioid,benchmarkid)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Portfolio - TWRR (Time Weighted Rate of Returns) Summary';
ALTER TABLE portfolio_twrr_summary ADD INDEX index_portfolio_twrr_summary_memberid (memberid);

SELECT * FROM portfolio_twrr_summary; 

-- DROP Table benchmark;
CREATE TABLE benchmark (
  benchmarkid int(3) NOT NULL COMMENT 'PK Benchmark ID',
  benchmark_name varchar(100) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Benchmark Name',
  benchmark_type varchar(30) CHARACTER SET utf8 COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Benchmark Description',
  date_last_returns_process date DEFAULT '2000-01-01' COMMENT 'Benchmark Last Reurns Process Date',
  is_mutual_fund varchar(3) DEFAULT 'No' COMMENT 'Is a Benchmark Mutual Fund',
  PRIMARY KEY (benchmarkid)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Portfolio Benchmarks - Custom Benchmarks';
SELECT * from benchmark order by benchmark_type, benchmarkid;

-- DROP TABLE benchmark_twrr_monthly;
CREATE TABLE benchmark_twrr_monthly (
  benchmarkid int(3) NOT NULL COMMENT 'Benchmark ID',
  year int(4) NOT NULL COMMENT 'PK Year of returns',
  returns_calendar_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR for calendar year',
  returns_fin_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR for FIN year',
  returns_mar_ending_quarter decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Jan to Mar',
  returns_jun_ending_quarter decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Apr to Jun',
  returns_sep_ending_quarter decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Jul to Sep',
  returns_dec_ending_quarter decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Oct to Dec',
  returns_jan decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Jan',
  returns_feb decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Feb',
  returns_mar decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Mar',
  returns_apr decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Apr',
  returns_may decimal(20,4) DEFAULT NULL COMMENT 'TWRR for May',
  returns_jun decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Jun',
  returns_jul decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Jul',
  returns_aug decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Aug',
  returns_sep decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Sep',
  returns_oct decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Oct',
  returns_nov decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Nov',
  returns_dec decimal(20,4) DEFAULT NULL COMMENT 'TWRR for Dec',
  PRIMARY KEY (benchmarkid,year)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Portfolio - Benchmark TWRR (Time Weighted Rate of Returns) monthwise ';
ALTER TABLE benchmark_twrr_monthly ADD INDEX index_benchmark_twrr_monthly_memberid (benchmarkid);

-- Drop table benchmark_twrr_summary;
CREATE TABLE benchmark_twrr_summary (
  benchmarkid int(3) NOT NULL COMMENT 'Benchmark ID',
  returns_date date DEFAULT NULL COMMENT 'Returns as of',
  returns_twrr_since_current_month decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns from current month',
  returns_twrr_since_current_quarter decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns from current quarter',
  returns_twrr_since_fin_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since current fin year',
  returns_twrr_ytd decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since Jan 1st',
  returns_twrr_three_months decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since three months',
  returns_twrr_half_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since six months',
  returns_twrr_one_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since one year',
  returns_twrr_two_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since two year',
  returns_twrr_three_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since three year',
  returns_twrr_five_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since five year',
  returns_twrr_ten_year decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since ten year',
  returns_twrr_since_inception decimal(20,4) DEFAULT NULL COMMENT 'TWRR Returns since inception',
  PRIMARY KEY (benchmarkid),
  KEY index_benchmark_twrr_summary_benchmarkid (benchmarkid)
) ENGINE=InnoDB DEFAULT CHARSET=latin1 COMMENT='Portfolio - TWRR (Time Weighted Rate of Returns) Summary';
SELECT * from benchmark_twrr_summary;

-- DROP TABLE composite_constituents;
CREATE TABLE composite_constituents (
  compositeid int(3) NOT NULL COMMENT 'PK Composite id',
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Usually NSE Code, BSE Code in case there is ''&'' in NSE Code or stock is only listed on BSE',
  name varchar(1000) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Security Name',
  short_name varchar(100) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Security Name - Short',
  asset_classid int(6) NOT NULL COMMENT 'Asset Class ID',
  subindustryid int(8) DEFAULT '0' COMMENT 'Sub Industry ID',
  target_weight int(3) DEFAULT '0' COMMENT 'Target Weight',
  min_weight int(3) DEFAULT '0' COMMENT 'Min. Weight',
  max_weight int(3) DEFAULT '0' COMMENT 'Min. Weight',
  PRIMARY KEY (compositeid,ticker)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Composite Details - The Latest Constituents';

select * from composite_constituents a order by a.target_weight desc;

-- DROP TABLE stock_analyst_reco;
CREATE TABLE stock_analyst_reco (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  quarter varchar(6) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Quarter e.g. FY23Q4',
  date date NOT NULL COMMENT 'PK Report Date',
  mcap decimal(20,3) NOT NULL COMMENT 'Market Cap',
  cmp decimal(20,3) NOT NULL COMMENT 'CMP',
  price_change decimal(10,4) DEFAULT '0.0000' COMMENT 'Percentage change in price since report date',
  broker varchar(20) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Broker',
  reco varchar(20) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Recommendation Add/Buy/Hold/Neutral/Reduce/Sell/Not Rated',
  target decimal(20,3) NOT NULL COMMENT 'Target Price',
  upside_or_downside decimal(10,4) DEFAULT '0.0000' COMMENT 'Upside or Downside potential',
  sales_y0 decimal(20,3) DEFAULT NULL COMMENT 'Sales or Inc. Y0',
  sales_y1 decimal(20,3) DEFAULT NULL COMMENT 'Sales or Inc. Y1',
  sales_y2 decimal(20,3) DEFAULT NULL COMMENT 'Sales or Inc. Y2',
  sales_growth decimal(10,4) DEFAULT '0.0000' COMMENT 'Sales or Inc. growth',
  ebit_y0 decimal(20,3) DEFAULT NULL COMMENT 'EBIT or PAT Y0',
  ebit_y1 decimal(20,3) DEFAULT NULL COMMENT 'EBIT or PAT Y1',
  ebit_y2 decimal(20,3) DEFAULT NULL COMMENT 'EBIT or PAT Y2',
  ebit_growth decimal(10,4) DEFAULT '0.0000' COMMENT 'EBIT or PAT growth',
  opm_y0 decimal(10,4) DEFAULT '0.0000' COMMENT 'OPM (or EBIT or NIM) Y0',
  opm_y1 decimal(10,4) DEFAULT '0.0000' COMMENT 'OPM (or EBIT or NIM) Y1',
  opm_y2 decimal(10,4) DEFAULT '0.0000' COMMENT 'OPM (or EBIT or NIM) Y1',
  roce_y0 decimal(10,4) DEFAULT '0.0000' COMMENT 'ROCE Y0',
  roce_y1 decimal(10,4) DEFAULT '0.0000' COMMENT 'ROCE Y1',
  roce_y2 decimal(10,4) DEFAULT '0.0000' COMMENT 'ROCE Y2',
  valuation_y0 decimal(10,4) DEFAULT '0.0000' COMMENT 'Either EV by EBIT or P/B or P/Emb.Value Y0',
  valuation_y1 decimal(10,4) DEFAULT '0.0000' COMMENT 'Either EV by EBIT or P/B or P/Emb.Value Y1',
  valuation_y2 decimal(10,4) DEFAULT '0.0000' COMMENT 'Either EV by EBIT or P/B or P/Emb.Value Y2',
  aum_y0 decimal(20,3) DEFAULT NULL COMMENT 'AUM Y0',
  aum_y1 decimal(20,3) DEFAULT NULL COMMENT 'AUM Y1',
  aum_y2 decimal(20,3) DEFAULT NULL COMMENT 'AUM Y2',
  aum_growth decimal(10,4) DEFAULT '0.0000' COMMENT 'AUM growth',
  credit_cost_y0 decimal(10,4) DEFAULT '0.0000' COMMENT 'Credit Cost Y0',
  credit_cost_y1 decimal(10,4) DEFAULT '0.0000' COMMENT 'Credit Cost Y1',
  credit_cost_y2 decimal(10,4) DEFAULT '0.0000' COMMENT 'Credit Cost Y2',
  gnpa_y0 decimal(10,4) DEFAULT '0.0000' COMMENT 'GNPA Y0',
  gnpa_y1 decimal(10,4) DEFAULT '0.0000' COMMENT 'GNPA Y1',
  gnpa_y2 decimal(10,4) DEFAULT '0.0000' COMMENT 'GNPA Y2',
  nnpa_y0 decimal(10,4) DEFAULT '0.0000' COMMENT 'NNPA Y0',
  nnpa_y1 decimal(10,4) DEFAULT '0.0000' COMMENT 'NNPA Y1',
  nnpa_y2 decimal(10,4) DEFAULT '0.0000' COMMENT 'NNPA Y2',
  analyst_names varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Comma Separated Analyst Names',
  summary varchar(500) COLLATE utf8_unicode_ci DEFAULT NULL COMMENT 'Summary',
  PRIMARY KEY (ticker,quarter,date,broker),
  KEY index_stock_analyst_reco_ticker (ticker),
  KEY index_stock_analyst_reco_broker (broker),
  KEY index_stock_analyst_reco_quarter (quarter)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Analyst Recommendations';

-- DROP stock_valuation;
CREATE TABLE stock_valuation (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  quarter varchar(6) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Quarter e.g. FY23Q4',
  ttm_revenue decimal(20,3) DEFAULT NULL COMMENT 'TTM Revenue',
  ttm_noplat decimal(20,3) DEFAULT NULL COMMENT 'TTM NOPLAT',
  ttm_fcff decimal(20,3) DEFAULT NULL COMMENT 'TTM FCFF',
  ttm_after_tax_other_inc decimal(20,3) DEFAULT NULL COMMENT 'TTM After Tax Other Inc.',
  debt_outstanding decimal(20,3) DEFAULT NULL COMMENT 'Debt Outstanding',
  wacc decimal(10,4) DEFAULT '0.00' COMMENT 'Weighted Avg. Cost of Capital',
  tax_rate decimal(10,4) DEFAULT '0.00' COMMENT 'Tax Rate',
  revenue_growth_next_10yr decimal(10,4) DEFAULT '0.00' COMMENT 'Revenue growth for Next 10 Yr',
  opm_next_10yr decimal(10,4) DEFAULT '0.00' COMMENT 'OPM or EBITDA Margin for next 10 year',
  net_ppe_by_revenue_10yr decimal(10,4) DEFAULT '0.00' COMMENT 'Net PP&E by Revenue for next 10 year',
  depreciation_by_net_ppe_10yr decimal(10,4) DEFAULT '0.00' COMMENT 'Net PP&E by Revenue for next 10 year',
  other_inc_growth_next_10yr decimal(10,4) DEFAULT '0.00' COMMENT 'Other Inc. growth for Next 10 Yr',
  other_inc_growth_period decimal(10,4) DEFAULT '0.00' COMMENT 'Other Inc. growth period',
  other_inc_terminal_growth decimal(10,4) DEFAULT '0.00' COMMENT 'Other Inc. terminal growth rate',
  other_inc_by_investment decimal(10,4) DEFAULT '0.00' COMMENT 'Other Inc. by investment',
  historical_roic decimal(10,4) DEFAULT '0.00' COMMENT 'Historical Return on Capital',
  ronic decimal(10,4) DEFAULT '0.00' COMMENT 'Return on New Invested Capital',
  next_stage_growth_period decimal(10,4) DEFAULT '0.00' COMMENT 'Next stage growth period',
  revenue_growth_second_stage decimal(10,4) DEFAULT '0.00' COMMENT 'Revenue growth for second stage',
  roic_second_stage decimal(10,4) DEFAULT '0.00' COMMENT 'Return on New Invested Capital',
  terminal_growth decimal(10,4) DEFAULT '0.00' COMMENT 'Revenue terminal growth rate',
  terminal_roic decimal(10,4) DEFAULT '0.00' COMMENT 'Terminal ROIC rate',
  mcap decimal(20,3) DEFAULT '0.00' COMMENT 'Market Cap at the time of valuation',
  price decimal(20,3) DEFAULT '0.00' COMMENT 'Market price at the time of valuation ',
  min_fair_price decimal(20,3) DEFAULT '0.00' COMMENT 'Minimum Fair Price of stock',
  max_fair_price decimal(20,3) DEFAULT '0.00' COMMENT 'Maximum Fair Price of stock',
  exp_ttm_revenue decimal(20,3) DEFAULT NULL COMMENT 'Expected TTM Revenue',
  exp_ttm_after_tax_other_inc decimal(20,3) DEFAULT NULL COMMENT 'Expected TTM After Tax Other Inc.',
  min_revenue_growth_next_10yr decimal(10,4) DEFAULT '0.00' COMMENT 'Min. Revenue growth for Next 10 Yr for min. price',
  max_revenue_growth_next_10yr decimal(10,4) DEFAULT '0.00' COMMENT 'Max. Revenue growth for Next 10 Yr for min. price',
  min_mcap decimal(20,3) DEFAULT '0.00' COMMENT 'Min. Market Cap for min. price',
  max_mcap decimal(20,3) DEFAULT '0.00' COMMENT 'Max. Market Cap for min. price',
  actual_min_price decimal(20,3) DEFAULT '0.00' COMMENT 'Minimum Fair Value',
  actual_max_price decimal(20,3) DEFAULT '0.00' COMMENT 'Maximum Fair Value',
  actual_min_mcap decimal(20,3) DEFAULT '0.00' COMMENT 'Actual Min. Market Cap for the quarter',
  actual_max_mcap decimal(20,3) DEFAULT '0.00' COMMENT 'Actual Max. Market Cap for the quarter',
  PRIMARY KEY (ticker,quarter),
  KEY index_stock_valuation_ticker (ticker)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stock Valuation';

-- DROP index_monthly_returns;
CREATE TABLE index_monthly_returns (
    ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
    year int(4) NOT NULL COMMENT 'Year',
    jan_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'January Return',
    feb_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'February Return',
    mar_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'March Return',
    apr_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'April Return',
    may_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'May Return',
    jun_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'June Return',
    jul_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'July Return',
    aug_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'August Return',
    sep_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'September Return',
    oct_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'October Return',
    nov_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'November Return',
    dec_return DECIMAL(10,4) DEFAULT '0.00' COMMENT 'December Return',
    annual_return DECIMAL(10,4) COMMENT 'Annual Return',
    PRIMARY KEY (ticker, year),
    KEY index_index_monthly_returns_ticker (ticker)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Index Monthly Returns';
