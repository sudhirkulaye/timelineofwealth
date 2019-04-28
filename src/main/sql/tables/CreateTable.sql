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

select * from member a order by a.memberid desc;

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
  interest_rate decimal(4,2) NOT NULL COMMENT 'Interest Rate',
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
  quantity decimal(20,3) DEFAULT NULL COMMENT 'Security Quantity',
  rate decimal(20,3) DEFAULT NULL COMMENT 'Security Buy Rate per Quantity',
  brokerage decimal(20,2) DEFAULT NULL COMMENT 'Security Total Brokerage',
  tax decimal(20,2) DEFAULT NULL COMMENT 'Security Total Tax',
  total_cost decimal(20,3) DEFAULT NULL COMMENT 'Security Total Cost (Buy Rate*Quantity) + Brokerage + Tax',
  net_rate decimal(20,3) DEFAULT NULL COMMENT 'Security effective cost per quantity i.e. Total Cost/Quantity',
  cmp decimal(20,3) DEFAULT NULL COMMENT 'Security Current Market Price',
  market_value decimal(20,3) DEFAULT NULL COMMENT 'Investment market value (CMP*Quanity)',
  holding_period decimal(7,3) DEFAULT NULL COMMENT 'Security holding period in years i.e. Buy date to till date ',
  net_profit decimal(20,3) DEFAULT NULL COMMENT 'Unrealized Net Profit = Market Value - Total Cost',
  absolute_return decimal(20,3) DEFAULT NULL COMMENT 'Unrealized absolute return',
  annualized_return decimal(20,3) DEFAULT NULL COMMENT 'Unrealized annualized return',
  maturity_value decimal(20,3) DEFAULT NULL COMMENT 'Security Maturity Value especially for FDs',
  maturity_date date DEFAULT '1900-01-01' COMMENT 'Security Maturity Value especially for FDs',
  last_valuation_date date DEFAULT '1900-01-01' COMMENT 'Last Valution Date',
  sipid INT(2) NULL DEFAULT 0 COMMENT 'SIP ID for automatic transaction',
  portfoliono VARCHAR(45) NULL DEFAULT '0' COMMENT 'Portfolio Number in case of Mutual Fund',
  PRIMARY KEY (memberid,ticker,buy_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Members current wealth Wealth at transaction level';

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

SELECT * from bse_price_history a where a.date = (select max(date) from bse_price_history);

SET SQL_SAFE_UPDATES = 0;
Commit;

create table mutual_fund_nav_history (
  scheme_code int(15) NOT NULL COMMENT 'PK Mutual Fund Scheme Code Unique',
  date date NOT NULL COMMENT 'PK Mutual Fund NAV Date',
  nav decimal(20,3) DEFAULT '0.000' COMMENT 'Mutual Fund NAV',
  PRIMARY KEY (scheme_code,date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='BSE Daily Price History';


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
  div_yield decimal(10,2) DEFAULT NULL COMMENT 'Index Div Yield Ratio',
  value decimal(10,2) DEFAULT NULL COMMENT 'Index value',
  turnover decimal(20,2) DEFAULT NULL COMMENT 'Index turnover in Rs',
  implied_earnings decimal(10,3) DEFAULT NULL COMMENT 'Index implied earnings Index Value/index PE',
  PRIMARY KEY (ticker,date)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Valutation - Index Valuation Data';

select * from index_valuation;

CREATE TABLE wealth_history (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  date date NOT NULL COMMENT 'Date ',
  value decimal(20,3) DEFAULT NULL COMMENT 'Portfolio market value related to date',
  PRIMARY KEY (memberid,date)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Historical Wealth Values';

select * from wealth_history a where a.memberid in (1000,1011) order by date;

-- Drop table wealth_asset_allocation_history;
CREATE TABLE wealth_asset_allocation_history (
  memberid int(11) NOT NULL COMMENT 'PK member ID unique Auto Generated',
  date date NOT NULL COMMENT 'PK Date of asset allocation',
  asset_class_group varchar(50) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Group for Asset classes',
  value decimal(20,3) DEFAULT NULL COMMENT 'Market Value by Asset class group',
  value_percent decimal(7,3) DEFAULT NULL COMMENT '%(Market Value) of Asset class',
  PRIMARY KEY (memberid,date,asset_class_group)
) ENGINE=MyISAM DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Wealth Asset Allocation History ';

select * from wealth_asset_allocation_history a where a.date = (select date_today from setup_dates) order by memberid, asset_class_group;

-- drop table stock_split_probability;
CREATE TABLE stock_split_probability (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  date date NOT NULL COMMENT 'PK Index valuation date',
  close_price decimal(20,3) DEFAULT NULL COMMENT 'Close price today',
  previous_close_price decimal(20,3) DEFAULT NULL COMMENT 'Close price last trading session',
  day_percent_change decimal(7,3) DEFAULT NULL COMMENT '%(Market Value) of Asset class',
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

-- Drop table adviser_user_mapping
CREATE TABLE adviser_user_mapping (
  adviserid varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Email ID or login ID of adviser',
  userid varchar(100) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Email ID or login ID of adviser',
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
  net_profit decimal(20,2) DEFAULT '0.00',
  sales decimal(20,2) DEFAULT '0.00',
  yoy_quarterly_sales_growth decimal(10,2) DEFAULT '0.00',
  yoy_quarterly_profit_growth decimal(10,2) DEFAULT '0.00',
  qoq_sales_growth decimal(10,2) DEFAULT '0.00',
  qoq_profit_growth decimal(10,2) DEFAULT '0.00',
  opm_latest_quarter decimal(10,2) DEFAULT '0.00',
  opm_last_year decimal(10,2) DEFAULT '0.00',
  npm_latest_quarter decimal(10,2) DEFAULT '0.00',
  npm_last_year decimal(10,2) DEFAULT '0.00',
  profit_growth_3years decimal(10,2) DEFAULT '0.00',
  sales_growth_3years decimal(10,2) DEFAULT '0.00',
  pe_ttm decimal(10,2) DEFAULT '0.00',
  historical_pe_3years decimal(10,2) DEFAULT '0.00',
  peg_ratio decimal(10,2) DEFAULT '0.00',
  pb_ttm decimal(10,2) DEFAULT '0.00',
  ev_to_ebit decimal(10,2) DEFAULT '0.00',
  dividend_payout decimal(10,2) DEFAULT '0.00',
  roe decimal(10,2) DEFAULT '0.00',
  avg_roe_3years decimal(10,2) DEFAULT '0.00',
  debt decimal(20,2) DEFAULT '0.00',
  debt_to_equity decimal(10,2) DEFAULT '0.00',
  debt_3years_back decimal(20,2) DEFAULT '0.00',
  mcap_to_netprofit decimal(10,2) DEFAULT '0.00',
  mcap_to_sales decimal(10,2) DEFAULT '0.00',
  sector varchar(50) CHARACTER SET latin1 DEFAULT 'NA',
  industry varchar(100) CHARACTER SET latin1 DEFAULT 'NA',
  sub_industry varchar(100) CHARACTER SET latin1 DEFAULT 'NA',
  PRIMARY KEY (date,name),
  KEY name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Securities - Daily Data obtained from Screener Watchlist';

select count(1) from daily_data_s a where date = (select date_today from setup_dates);

-- drop table index_statistics; 
CREATE TABLE index_statistics (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  mean_returns_1yr decimal(10,3) DEFAULT NULL COMMENT 'Avg returns 1 yerar',
  median_returns_1yr decimal(10,3) DEFAULT NULL COMMENT 'Median returns 1 yerar',
  mean_returns_3yr decimal(10,3) DEFAULT NULL COMMENT 'Avg returns 3 yerars',
  median_returns_3yr decimal(10,3) DEFAULT NULL COMMENT 'Median returns 3 yerars',
  mean_returns_5yr decimal(10,3) DEFAULT NULL COMMENT 'Avg returns 5 yerars',
  median_returns_5yr decimal(10,3) DEFAULT NULL COMMENT 'Median returns 5 yerars',
  mean_returns_10yr decimal(10,3) DEFAULT NULL COMMENT 'Avg returns 10 yerars',
  median_returns_10yr decimal(10,3) DEFAULT NULL COMMENT 'Median returns 10 yerars',
  minimum_returns_1yr decimal(10,3) DEFAULT NULL COMMENT 'Minimum returns 1 yerar',
  minimum_returns_1yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Minimum returns 1 yerar',
  maximum_returns_1yr decimal(10,3) DEFAULT NULL COMMENT 'Maximum returns 1 yerar',
  maximum_returns_1yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Maximum returns 1 yerar',
  minimum_returns_3yr decimal(10,3) DEFAULT NULL COMMENT 'Minimum returns 3 yerars',
  minimum_returns_3yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Minimum returns 3 yerars',
  maximum_returns_3yr decimal(10,3) DEFAULT NULL COMMENT 'Maximum returns 3 yerars',
  maximum_returns_3yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Maximum returns 3 yerars',
  minimum_returns_5yr decimal(10,3) DEFAULT NULL COMMENT 'Minimum returns 5 yerars',
  minimum_returns_5yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Minimum returns 5 yerars',
  maximum_returns_5yr decimal(10,3) DEFAULT NULL COMMENT 'Maximum returns 5 yerars',
  maximum_returns_5yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Maximum returns 5 yerars',
  minimum_returns_10yr decimal(10,3) DEFAULT NULL COMMENT 'Minimum returns 10 yerars',
  minimum_returns_10yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Minimum returns 10 yerars',
  maximum_returns_10yr decimal(10,3) DEFAULT NULL COMMENT 'Maximum returns 10 yerars', 
  maximum_returns_10yr_duration varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'Duration of Maximum returns 10 yerars',
  standard_deviation_1yr decimal(10,3) DEFAULT NULL COMMENT 'Standard Deviation 1 year',
  standard_deviation_3yr decimal(10,3) DEFAULT NULL COMMENT 'Standard Deviation 3 years',
  standard_deviation_5yr decimal(10,3) DEFAULT NULL COMMENT 'Standard Deviation 5 years',
  standard_deviation_10yr decimal(10,3) DEFAULT NULL COMMENT 'Standard Deviation 10 years',
  mean_pe_1yr decimal(10,3) DEFAULT NULL COMMENT 'Avg PE 1 yerar',
  median_pe_1yr decimal(10,3) DEFAULT NULL COMMENT 'Median PE 1 yerar',
  minimum_pe_1yr decimal(10,3) DEFAULT NULL COMMENT 'Minimum PE 1 yerar',
  maximum_pe_1yr decimal(10,3) DEFAULT NULL COMMENT 'Maximum PE 1 yerar',
  mean_pe_3yr decimal(10,3) DEFAULT NULL COMMENT 'Avg PE 3 yerars',
  median_pe_3yr decimal(10,3) DEFAULT NULL COMMENT 'Median PE 3 yerars',
  minimum_pe_3yr decimal(10,3) DEFAULT NULL COMMENT 'Minimum PE 3 yerars',
  maximum_pe_3yr decimal(10,3) DEFAULT NULL COMMENT 'Maximum PE 3 yerars',
  mean_pe_5yr decimal(10,3) DEFAULT NULL COMMENT 'Avg PE 5 yerars',
  median_pe_5yr decimal(10,3) DEFAULT NULL COMMENT 'Median PE 5 yerars',
  minimum_pe_5yr decimal(10,3) DEFAULT NULL COMMENT 'Minimum PE 5 yerars',
  maximum_pe_5yr decimal(10,3) DEFAULT NULL COMMENT 'Maximum PE 5 yerars',
  mean_pe_10yr decimal(10,3) DEFAULT NULL COMMENT 'Avg PE 10 yerars',
  median_pe_10yr decimal(10,3) DEFAULT NULL COMMENT 'Median PE 10 yerars',
  minimum_pe_10yr decimal(10,3) DEFAULT NULL COMMENT 'Minimum PE 10 yerars',
  maximum_pe_10yr decimal(10,3) DEFAULT NULL COMMENT 'Maximum PE 10 yerars',
  mean_pe decimal(10,3) DEFAULT NULL COMMENT 'Avg PE all yerars',
  median_pe decimal(10,3) DEFAULT NULL COMMENT 'Median PE all yerars',
  minimum_pe decimal(10,3) DEFAULT NULL COMMENT 'Minimum PE all yerars',
  maximum_pe decimal(10,3) DEFAULT NULL COMMENT 'Maximum PE all yerars',
  current_pe decimal(10,3) DEFAULT NULL COMMENT 'Current PE',
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
  total_returns_y0 DECIMAL(10,3) NOT NULL COMMENT 'Current Year Returns (YTD)',
  total_returns_y1 DECIMAL(10,3) NOT NULL COMMENT '1 Year before Returns',
  total_returns_y2 DECIMAL(10,3) NOT NULL COMMENT '2 Year before Returns',
  total_returns_y3 DECIMAL(10,3) NOT NULL COMMENT '3 Year before Returns',
  total_returns_y4 DECIMAL(10,3) NOT NULL COMMENT '4 Year before Returns',
  total_returns_y5 DECIMAL(10,3) NOT NULL COMMENT '5 Year before Returns',
  total_returns_y6 DECIMAL(10,3) NOT NULL COMMENT '6 Year before Returns',
  total_returns_y7 DECIMAL(10,3) NOT NULL COMMENT '7 Year before Returns',
  total_returns_y8 DECIMAL(10,3) NOT NULL COMMENT '8 Year before Returns',
  total_returns_y9 DECIMAL(10,3) NOT NULL COMMENT '9 Year before Returns',
  total_returns_y10 DECIMAL(10,3) NOT NULL COMMENT '10 Year before Returns',
  trailing_return_1yr DECIMAL(10,3) NOT NULL COMMENT '1 Year Trailing Returns',
  trailing_return_3yr DECIMAL(10,3) NOT NULL COMMENT '3 Years Trailing Returns',
  trailing_return_5yr DECIMAL(10,3) NOT NULL COMMENT '5 Years Trailing Returns',
  trailing_return_10yr DECIMAL(10,3) NOT NULL COMMENT '10 Years Trailing Returns',
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
  sector_basic_materials DECIMAL(10,3) NOT NULL COMMENT 'Exposure to sector basic materials',
  sector_consumer_cyclical DECIMAL(10,3) NOT NULL COMMENT 'Exposure to sector consumer cyclical',
  sector_finacial_services DECIMAL(10,3) NOT NULL COMMENT 'Exposure to sector finacial services',
  sector_industrial DECIMAL(10,3) NOT NULL COMMENT 'Exposure to sector industrial',
  sector_technology DECIMAL(10,3) NOT NULL COMMENT 'Exposure to sector technology',
  sector_consumer_defensive DECIMAL(10,3) NOT NULL COMMENT 'Exposure to sector consumer defensive',
  sector_healthcare DECIMAL(10,3) NOT NULL COMMENT 'Exposure to sector healthcare',
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
  dividend_payout DECIMAL(10,3) NOT NULL COMMENT 'Dividend Payout',
  opm DECIMAL(10,3) NOT NULL COMMENT 'Operating Profit Margine',
  npm DECIMAL(10,3) NOT NULL COMMENT 'Net Profit Margine',
  re DECIMAL(10,3) NOT NULL COMMENT 'Return on Equity',
  PRIMARY KEY (ticker, cons_standalone, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stock Annual P&L Results';

select count(1), year(date) from stock_pnl a group by year(date) order by date desc; 
select * from stock_pnl a where ticker in ('INFY', 'TCS') and date > '2009-03-31' and sales = 0 and expenses = 0 and operating_profit = 0 and other_income = 0;
select distinct ticker from stock_pnl a where date = '2017-12-31';

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
  opm DECIMAL(10,3) NOT NULL COMMENT 'Operating Profit Margine',
  PRIMARY KEY (ticker, cons_standalone, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stock Quarter P&L Results';

select count(1), year(date) from stock_quarter a group by year(date) order by date desc; 
select * from stock_quarter a where sales = 0 and expenses = 0 and operating_profit = 0 and other_income = 0;
select * from stock_quarter a where a.ticker in ('ACC','COROMANDEL', 'IBULISL', 'ISEC', 'LUXIND', 'STRTECH', 'TATAGLOBAL', 'HEXAWARE', 'IBREALEST','IBULHSGFIN', 'M&MFIN', 'ULTRACEMCO', 'TATAELXSI') and date = '2019-03-31';
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
  inventory_turnover DECIMAL(20,3) NOT NULL COMMENT 'Inventory turnover',
  dummy3 DECIMAL(20,3) NOT NULL COMMENT 'Dummy3',
  return_on_equity DECIMAL(20,3) NOT NULL COMMENT 'Return on equity',
  return_on_capital_emp DECIMAL(20,3) NOT NULL COMMENT 'Return on capital employed',
  PRIMARY KEY (ticker, cons_standalone, date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stock Balancesheet';

select count(1), year(date) from stock_balancesheet a group by year(date) order by date desc; 
select count(1) from stock_balancesheet a where a.ticker = 'PGHH' and 1=2;

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

select count(1), year(date) from stock_cashflow a group by year(date) order by date desc; 
select count(1) from stock_cashflow a where a.ticker = 'PGHH' and 1=2;

-- drop table stock_price_movement;
CREATE TABLE stock_price_movement (
  ticker varchar(30) COLLATE utf8_unicode_ci NOT NULL COMMENT 'PK Index ticker',
  CMP decimal(20,3) DEFAULT NULL COMMENT 'CMP',
  52w_min decimal(20,3) DEFAULT NULL COMMENT '52-Week Min Price',
  52w_max decimal(20,3) DEFAULT NULL COMMENT '52-Week Max Price',
  up_52w_min decimal(10,3) DEFAULT NULL COMMENT 'Up from 52-Week Min Price',
  down_52w_max decimal(10,3) DEFAULT NULL COMMENT 'Down from 52-Week Max Price',
  return_1D decimal(10,3) DEFAULT NULL COMMENT '1 Day Returns',
  return_1W decimal(10,3) DEFAULT NULL COMMENT '1 Week Returns',
  return_2W decimal(10,3) DEFAULT NULL COMMENT '2 Weeks Returns',
  return_1M decimal(10,3) DEFAULT NULL COMMENT '1 Month Returns',
  return_2M decimal(10,3) DEFAULT NULL COMMENT '2 Months Returns',
  return_3M decimal(10,3) DEFAULT NULL COMMENT '3 Months Returns',
  return_6M decimal(10,3) DEFAULT NULL COMMENT '6 Months Returns',
  return_9M decimal(10,3) DEFAULT NULL COMMENT '9 Months Returns',
  return_1Y decimal(10,3) DEFAULT NULL COMMENT '1 Year Returns',
  return_2Y decimal(10,3) DEFAULT NULL COMMENT '2 Years Returns',
  return_3Y decimal(10,3) DEFAULT NULL COMMENT '3 Years Returns',
  return_5Y decimal(10,3) DEFAULT NULL COMMENT '5 Years Returns',
  return_10Y decimal(10,3) DEFAULT NULL COMMENT '10 Years Returns',
  return_YTD decimal(10,3) DEFAULT NULL COMMENT 'YTD Returns',
  PRIMARY KEY (ticker)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci COMMENT='Stock Price Movement';

