/*ALTER TABLE sip 
ADD COLUMN last_processing_date DATE NOT NULL DEFAULT '1900-01-01' COMMENT 'SIP last processing date' AFTER is_active,
ADD COLUMN next_processing_date DATE NOT NULL DEFAULT '1900-01-01' COMMENT 'SIP next processing date' AFTER last_processing_date,
ADD COLUMN absolute_return decimal(20,3) DEFAULT 0 COMMENT 'Unrealized absolute return' AFTER next_processing_date,
ADD COLUMN irr_return decimal(20,3) DEFAULT 0 COMMENT 'Unrealized IRR return' AFTER next_processing_date,
ADD COLUMN twr_return decimal(20,3) DEFAULT 0 COMMENT 'Unrealized TWRR return' AFTER next_processing_date; */

select distinct category from mutual_fund_universe; 
select * from wealth_details where memberid = 1011 order by name; -- 1019;
SELECT * from sip  where memberid = 1022;-- 25 records
select ticker, buy_date, sum(quantity), name, rate, total_cost, net_Rate from wealth_details where memberid = 1003 group by memberid, ticker order by name; 
select * from wealth_details where memberid = 1046; -- 1019;
UPDATE `timelineofwealth`.`wealth_details` SET `quantity`='1918.825', `rate`='33.620', `total_cost`='64502.54', `net_rate`='33.620' 
WHERE memberid = 1046 and ticker = 103196 and buy_date = '2012-01-17';
-- 1230.354	Axis Long Term Equity Fund - Growth

-- mutual fund history 
select count(1), year(date) from mutual_fund_nav_history a group by year(date) having count(1) > 1000 order by year(date);
select count(1), year(a.date), b.fund_house from mutual_fund_nav_history a, mutual_fund_universe b 
where a.scheme_code = b.scheme_code group by year(a.date), b.fund_house order by b.fund_house, year(a.date);

select * from mutual_fund_universe a where a.fund_house like 'Motilal%';
select a.*, day(a.date) from mutual_fund_nav_history a where a.scheme_code = '129046' and date > '2018-05-01' ;

call ap_process_sips;
select * from log_table;
truncate table log_table;
-- truncate table sip_process_log;
-- truncate table sip_process_msg_log
select * from sip_process_log a where memberid in (1007,1015,1058) order by sipid;
select * from sip a where memberid in (1007,1015,1058) order by sipid;
select * from wealth_details a where memberid in (1007,1015,1058) order by memberid, sipid; 
select * from sip_process_msg_log a where memberid in (1007,1015,1058) order by date desc, sipid;
select max(buy_date), ticker, short_name, sum(quantity), sum(market_value), sum(net_profit) from wealth_details a where memberid in (1007,1015,1058) group by ticker order by memberid, ticker; 
UPDATE wealth_details set maturity_date = '2000-01-01' where maturity_date is null;
select * from member;

select * from wealth_details a where memberid = 1046;
select * from mutual_fund_nav_history where scheme_code = 119347 and date > '2018-12-01';


select date_add('2019-01-01', INTERVAL 5 DAY);

select * from wealth_history a where a.memberid = 1022;
select * from wealth_asset_allocation_history a where a.memberid = 1022;

-- query to update CMP, market_vaue, net_profit & absolute_return
/*
UPDATE wealth_details
SET    market_value = cmp * quantity,
	 net_profit = market_value - total_cost,
	 holding_period = ROUND((DATEDIFF((select date_today from setup_dates), buy_date) / 365.25), 2),
	 absolute_return = round((market_value / total_cost) - 1, 2),
	 annualized_return = round(pow((absolute_return + 1), (1 / holding_period)) - 1, 2)
WHERE asset_classid not in ('101010', '101020', '201010', '202010', '203010', '203020', '203050')
AND memberid = 1005;
*/

commit;

select * from sip_process_log a where memberid in (1031,1030) order by sipid;
select * from sip a where memberid in (1031,1030) order by sipid;
select * from wealth_details a where memberid in (1031,1030) order by memberid, sipid; 
select * from sip_process_msg_log a where memberid in (1031,1030) order by date desc, sipid;
select max(buy_date), ticker, short_name, sum(quantity), sum(market_value), sum(net_profit) from wealth_details a where memberid in (1007,1015,1058) group by ticker order by memberid, ticker; 
UPDATE wealth_details set maturity_date = '2000-01-01' where maturity_date is null;
select * from member;

select * from member a where last_name like 'Rane%';
select * from sip a where a.memberid in (1016, 1059, 1060) order by memberid, scheme_name;
select * from wealth_details a where a.memberid in (1007, 1015, 1058) order by memberid, name;
select distinct(portfoliono) from wealth_details a where a.short_name like '%-Reg-%' and a.memberid in (1007, 1015, 1058) order by memberid, name;
select * from mutual_fund_house;