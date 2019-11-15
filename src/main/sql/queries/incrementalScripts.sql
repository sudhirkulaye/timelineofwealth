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
select * from wealth_details a where memberid in (1001,1011) order by memberid, sipid; 
select * from sip_process_msg_log a where memberid in (1007,1015,1058) order by date desc, sipid;
select max(buy_date), ticker, short_name, sum(quantity), sum(market_value), sum(net_profit) from wealth_details a where memberid in (1007,1015,1058) group by ticker order by memberid, ticker; 
UPDATE wealth_details set maturity_date = '2000-01-01' where maturity_date is null;
select * from member;

select * from wealth_details a where memberid = 1018;
select * from mutual_fund_nav_history where scheme_code = 119347 and date > '2018-12-01';


select date_add('2019-01-01', INTERVAL 5 DAY);

select * from wealth_history a where a.memberid = 1022;
select * from wealth_asset_allocation_history a where a.memberid in (1022) and date = (select date_today from setup_dates);

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

select * from member a where last_name like 'V%';
select * from sip a where a.memberid in (1007, 1015, 1058) order by memberid, scheme_name;
select * from stock_universe; 

select * from wealth_details a where a.memberid in (1022) order by asset_classid, memberid, name;
SELECT * FROM wealth_asset_allocation_history a WHERE a.memberid in (1016, 1059, 1060) and date = '2019-02-21' order by date desc, memberid, asset_class_group;
update wealth_details a, stock_universe b Set a.asset_classid = b.asset_classid, a.subindustryid = b.subindustryid where a.ticker = b.ticker; 
-- Query to count Direct & Regular MF Exposures
select sum(market_value), count(market_value) from wealth_details a  where a.memberid in (1007, 1015, 1058) and short_name like '%-%Dir%-%' order by memberid, name;
select sum(market_value), count(market_value) from wealth_details a  where a.memberid in (1007, 1015, 1058) and short_name like '%-%Reg%-%' order by memberid, name;
-- Query for MF Analysis
select a.memberid Member, b.asset_class_group AssetClass, if(locate('-Reg', short_name) > 0, 'Regular', 'Direct') RegDir, if(locate('-G', short_name) > 0, 'Growth', 'Dividend') GrowthDiv, short_name ShortName, portfoliono FolioNO, a.quantity units, round(market_value, 0) MktValue, if(sipid > 0, 'Yes', '') Sip, if(datediff(now(),buy_date)/365 > 1, 'All', '-') Units, buy_date, a.ticker schemeCode 
from wealth_details a, asset_classification b 
where a.memberid in (1007, 1015, 1058) and 
a.asset_classid = b.classid and
(a.asset_classid in (201020, 202020, 203040, 301010, 301020) or (a.asset_classid > 401010 and a.asset_classid < 405040))
order by memberid, asset_class_group, name;

select * from mutual_fund_stats a where a.scheme_code in (101002,103155,102920,103215,105989,103360,103155,103174,103174,112323,100471,102000,109445,108909,103504,103504,103504,101922,100520,100520,103151,107578,111381,105989,105989,103360,103155,102920,105989);

select distinct(portfoliono) from wealth_details a where a.short_name like '%-Reg-%' and a.memberid in (1007, 1015, 1058) order by memberid, name;
select * from mutual_fund_house;

SELECT * from wealth_details a where a.memberid in (1002, 1018) and asset_classid in ('201010','202010');

select memberid, sum(market_value) from wealth_details w group by memberid order by memberid, buy_date, ticker;
select * from wealth_details w /*where memberid = 1011 and ticker = 'AUROPHARMA'*/ order by memberid, buy_date, ticker;

rollback;
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('IOC', 'C', '2019-09-30', '111128.70', '107369.25', '3759.45', '735.94', '2359.95', '1452.84', '682.60', '312.16', '468.04', '0.00', '0.03');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('ADVENZYMES', 'C', '2019-09-30', '111.44', '64.17', '47.27', '1.54', '6.47', '0.74', '41.60', '9.75', '31.84', '0.00', '0.42');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('ADVENZYMES', 'S', '2019-09-30', '58.91', '43.34', '15.57', '1.10', '2.19', '0.22', '14.26', '3.11', '11.15', '0.00', '0.26');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('BANKINDIA', 'C', '2019-09-30', '10715.29', '4823.14', '5892.15', '1347.26', '0.00', '6817.84', '421.57', '143.16', '257.31', '0.00', '0.55');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('BANKINDIA', 'S', '2019-09-30', '10658.14', '4780.25', '5877.89', '1327.36', '0.00', '6797.68', '407.57', '141.20', '266.37', '0.00', '0.55');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('BEML', 'C', '2019-09-30', '687.11', '694.37', '-7.26', '7.59', '18.10', '9.43', '-27.20', '0.00', '-27.20', '0.00', '-0.01');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('BEML', 'S', '2019-09-30', '687.74', '694.03', '-6.29', '7.59', '17.83', '9.39', '-25.92', '0.00', '-25.92', '0.00', '-0.01');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('BEL', 'C', '2019-09-30', '2748.32', '2199.60', '548.72', '11.53', '91.22', '1.23', '467.80', '132.61', '343.85', '0.00', '0.20');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('BEL', 'S', '2019-09-30', '2742.71', '2197.97', '544.74', '15.03', '85.65', '1.22', '472.90', '133.41', '339.49', '0.00', '0.20');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('CANFINHOME', 'S', '2019-09-30', '500.49', '29.24', '471.25', '0.18', '2.07', '338.47', '130.89', '33.27', '97.62', '0.00', '0.94');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('CENTRALBK', 'C', '2019-09-30', '5918.54', '2481.35', '3437.19', '809.63', '0.00', '4018.84', '227.98', '100.90', '138.58', '0.00', '0.58');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('CENTRALBK', 'S', '2019-09-30', '5890.08', '2470.15', '3419.93', '813.63', '0.00', '3999.12', '234.44', '100.37', '134.07', '0.00', '0.58');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('DRREDDY', 'C', '2019-09-30', '4812.80', '3749.00', '1063.80', '65.70', '313.10', '30.30', '786.10', '-320.70', '1106.80', '0.00', '0.22');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('DRREDDY', 'S', '2019-09-30', '3446.00', '2285.20', '1160.80', '76.70', '204.10', '12.20', '1021.20', '-343.90', '1365.10', '0.00', '0.34');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('ESCORTS', 'C', '2019-09-30', '1333.77', '1209.58', '124.19', '12.22', '26.74', '4.05', '105.62', '4.08', '101.74', '0.00', '0.09');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('ESCORTS', 'S', '2019-09-30', '1323.86', '1197.15', '126.71', '11.83', '26.00', '3.85', '108.69', '4.09', '104.60', '0.00', '0.10');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('GICHSGFIN', 'S', '2019-09-30', '312.11', '44.66', '267.45', '1.13', '0.76', '241.14', '26.68', '36.65', '-9.97', '0.00', '0.86');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('HDFC', 'C', '2019-09-30', '32796.50', '13704.39', '19092.11', '1255.39', '68.00', '8217.33', '12062.17', '1313.48', '10388.61', '0.00', '0.58');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('HDFC', 'S', '2019-09-30', '13487.44', '1099.73', '12387.71', '6.68', '33.31', '7830.70', '4530.38', '568.85', '3961.53', '0.00', '0.92');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('HTMEDIA', 'C', '2019-09-30', '520.52', '498.89', '21.63', '59.50', '48.71', '26.86', '5.56', '27.04', '-24.47', '0.00', '0.04');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('HTMEDIA', 'S', '2019-09-30', '309.24', '291.34', '17.90', '39.18', '30.45', '28.50', '-1.87', '8.66', '-10.53', '0.00', '0.06');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('HSIL', 'C', '2019-09-30', '469.93', '396.38', '73.55', '4.12', '38.25', '18.76', '20.66', '5.68', '14.98', '0.00', '0.16');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('HSIL', 'S', '2019-09-30', '309.24', '291.34', '17.90', '39.18', '30.45', '28.50', '-1.87', '8.66', '-10.53', '0.00', '0.06');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('IOB', 'S', '2019-09-30', '4275.65', '4202.27', '73.38', '748.35', '0.00', '3071.76', '-2250.03', '3.61', '-2253.64', '0.00', '0.02');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('IFBIND', 'C', '2019-09-30', '713.20', '662.47', '50.73', '2.57', '15.51', '2.00', '35.79', '14.09', '21.70', '0.00', '0.07');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('IFBIND', 'S', '2019-09-30', '692.46', '642.89', '49.57', '2.50', '14.77', '1.59', '35.71', '14.09', '21.62', '0.00', '0.07');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('INTELLECT', 'C', '2019-09-30', '326.58', '332.55', '-5.97', '11.51', '16.21', '4.78', '-15.45', '0.69', '-16.14', '0.00', '-0.02');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('INTELLECT', 'S', '2019-09-30', '180.60', '186.09', '-5.49', '8.16', '11.89', '4.10', '-13.32', '0.00', '-13.32', '0.00', '-0.03');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('JKLAKSHMI', 'C', '2019-09-30', '1012.36', '834.28', '178.08', '5.75', '51.58', '55.60', '76.65', '26.93', '48.67', '0.00', '0.18');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('JKLAKSHMI', 'S', '2019-09-30', '935.48', '786.83', '148.65', '6.19', '43.66', '38.43', '72.75', '26.84', '45.91', '0.00', '0.16');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('JSWENERGY', 'C', '2019-09-30', '2118.55', '1183.75', '934.80', '115.16', '294.30', '272.17', '483.49', '133.79', '352.98', '0.00', '0.44');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('JSWENERGY', 'S', '2019-09-30', '1063.50', '865.96', '197.54', '86.89', '93.05', '89.14', '102.24', '37.61', '64.63', '0.00', '0.19');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('KANSAINER', 'C', '2019-09-30', '1325.55', '1106.67', '218.88', '4.33', '33.84', '4.83', '184.54', '-5.38', '190.77', '0.00', '0.17');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('KANSAINER', 'S', '2019-09-30', '1243.52', '1030.59', '212.93', '3.94', '28.64', '1.24', '186.99', '-6.40', '193.39', '0.00', '0.17');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('KARURVYSYA', 'S', '2019-09-30', '1537.51', '808.02', '729.49', '277.73', '0.00', '941.23', '65.99', '2.66', '63.33', '0.00', '0.47');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('KRBL', 'C', '2019-09-30', '889.84', '723.71', '166.13', '6.18', '19.30', '7.30', '145.71', '32.32', '113.39', '0.00', '0.19');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('KRBL', 'S', '2019-09-30', '889.78', '723.09', '166.69', '6.12', '19.28', '7.30', '146.23', '32.32', '113.91', '0.00', '0.19');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('NOCIL', 'C', '2019-09-30', '209.73', '161.26', '48.47', '2.07', '8.03', '0.33', '42.18', '-12.75', '54.93', '0.00', '0.23');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('NOCIL', 'S', '2019-09-30', '209.73', '161.84', '47.89', '2.48', '7.70', '0.33', '42.34', '-12.89', '55.23', '0.00', '0.23');
-- INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('ORIENTREF', 'C', '2019-06-30', '188.95', '154.05', '34.90', '1.83', '2.37', '0.00', '34.36', '11.90', '22.46', '0.00', '0.18');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('ORIENTREF', 'S', '2019-09-30', '178.84', '147.57', '31.27', '1.65', '2.60', '0.00', '30.32', '4.30', '26.02', '0.00', '0.17');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('PERSISTENT', 'C', '2019-09-30', '884.60', '762.96', '121.64', '38.25', '42.49', '1.84', '115.56', '29.50', '86.07', '0.00', '0.14');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('PERSISTENT', 'S', '2019-09-30', '501.71', '407.98', '93.73', '55.89', '14.11', '1.17', '134.34', '27.49', '106.85', '0.00', '0.19');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('RELAXO', 'S', '2019-09-30', '621.77', '517.05', '104.72', '1.65', '27.76', '4.31', '74.30', '3.76', '70.54', '0.00', '0.17');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('SPARC', 'S', '2019-09-30', '17.19', '80.11', '-62.92', '2.26', '2.25', '0.25', '-63.16', '0.00', '-63.16', '0.00', '-3.66');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('SRF', 'C', '2019-09-30', '1737.80', '1406.80', '331.00', '122.19', '92.92', '55.02', '305.25', '4.12', '301.13', '0.00', '0.19');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('SRF', 'S', '2019-09-30', '1508.44', '1226.02', '282.42', '198.57', '83.84', '49.11', '348.04', '10.57', '337.47', '0.00', '0.19');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('TCIEXP', 'S', '2019-09-30', '269.47', '238.76', '30.71', '1.61', '1.84', '0.25', '30.23', '4.13', '26.10', '0.00', '0.11');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('TRIDENT', 'C', '2019-09-30', '1340.72', '1082.61', '258.11', '5.83', '84.97', '27.08', '151.89', '12.08', '139.81', '0.00', '0.19');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('TRIDENT', 'S', '2019-09-30', '1320.78', '1068.80', '251.98', '7.02', '84.89', '27.07', '147.04', '9.94', '137.10', '0.00', '0.19');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('UNICHEMLAB', 'C', '2019-09-30', '295.61', '322.43', '-26.82', '28.13', '20.98', '1.86', '-21.53', '0.67', '-22.21', '0.00', '-0.09');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('UNICHEMLAB', 'S', '2019-09-30', '225.10', '260.92', '-35.82', '29.05', '17.86', '0.23', '-24.86', '0.00', '-24.86', '0.00', '-0.16');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('VMART', 'S', '2019-09-30', '314.16', '302.85', '11.31', '1.18', '22.26', '13.11', '-22.88', '-4.84', '-18.04', '0.00', '0.04');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('VBL', 'C', '2019-09-30', '1739.73', '1414.07', '325.66', '4.04', '127.34', '86.74', '115.62', '34.50', '80.73', '0.00', '0.19');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('VBL', 'S', '2019-09-30', '1338.73', '1065.34', '273.39', '11.52', '103.88', '82.27', '98.76', '33.53', '65.23', '0.00', '0.20');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('VSTIND', 'S', '2019-09-30', '299.38', '202.93', '96.45', '12.27', '9.30', '0.00', '99.42', '23.10', '76.32', '0.00', '0.32');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('WABCOINDIA', 'S', '2019-09-30', '445.99', '393.36', '52.63', '14.61', '20.89', '0.55', '45.80', '11.95', '33.86', '0.00', '0.12');
-- INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('WESTLIFE', 'C', '2019-09-30', '396.53', '338.54', '57.99', '3.82', '34.35', '20.05', '7.41', '2.73', '4.68', '0.00', '0.15');
-- INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('WESTLIFE', 'S', '2019-09-30', '0.00', '0.24', '-0.24', '0.16', '0.00', '0.00', '-0.08', '0.00', '-0.08', '0.00', '0.00');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('YESBANK', 'C', '2019-09-30', '7382.72', '3047.47', '4335.25', '964.78', '0.00', '5199.41', '100.62', '729.71', '-629.09', '0.00', '0.59');
INSERT INTO `timelineofwealth`.`stock_quarter` (`ticker`, `cons_standalone`, `date`, `sales`, `expenses`, `operating_profit`, `other_income`, `depreciation`, `interest`, `profit_before_tax`, `tax`, `net_profit`, `dummy1`, `opm`) VALUES ('YESBANK', 'S', '2019-09-30', '7386.28', '3009.65', '4376.63', '945.93', '0.00', '5200.37', '122.19', '722.27', '-600.08', '0.00', '0.59');
