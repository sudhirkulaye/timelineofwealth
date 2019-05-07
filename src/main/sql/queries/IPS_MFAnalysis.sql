/* Get members id*/
SELECT * from user_members;
SELECT * from member;
/* Get Wealth Distribution of client */
SELECT memberid, asset_class_group, round(value) FROM wealth_asset_allocation_history a 
WHERE 
-- a.memberid in (1007, 1015, 1058) AND -- RVV
-- a.memberid in (1016, 1059, 1060) AND -- AKR
a.memberid in (1003, 1019) AND -- YBG, SYG
date = (select date_today from setup_dates)
ORDER BY memberid, asset_class_group;

/*Get Regular Investmetn Details*/
select a.memberid, a.instrument_type, a.scheme_name, round(a.amount*sip_freq) 
from sip a WHERE a.memberid in (1003, 1019) order by a.memberid, a.instrument_type;
select a.memberid, c.asset_class_group, round(sum(a.amount*sip_freq)) 
from sip a, mutual_fund_universe b, asset_classification c
where 
-- a.memberid in (1007, 1015, 1058) AND -- RVV
-- a.memberid in (1016, 1059, 1060) AND -- AKR
a.memberid in (1003, 1019) AND -- YBG, SYG
a.scheme_code = b.scheme_code AND
b.asset_classid = c.classid AND
instrument_type = 'Mutual Fund' AND
is_active = 'Yes'
group by a.memberid, c.asset_class_group
order by a.memberid, c.asset_class_group;

-- Query to count Direct & Regular MF Exposures
select round(sum(market_value)), count(market_value) from wealth_details a  
where a.memberid in (1003, 1019) and short_name like '%-%Dir%-%' order by memberid, name;
select round(sum(market_value)), count(market_value) from wealth_details a  
where a.memberid in (1003, 1019) and short_name like '%-%Reg%-%' order by memberid, name;
SELECT count(1) from sip a where a.scheme_name like '%-%Reg%-%' and a.memberid in (1003, 1019);
-- Query for MF Analysis
select a.memberid Member, b.asset_class_group AssetClass, if(locate('-Reg', short_name) > 0, 'Regular', 'Direct') RegDir, if(locate('-G', short_name) > 0, 'Growth', 'Dividend') GrowthDiv, short_name ShortName, portfoliono FolioNO, a.quantity units, buy_date, round(total_cost), round(market_value, 0) MktValue, net_profit, absolute_return, annualized_return, if(sipid > 0, 'Yes', '') Sip, if(datediff(now(),buy_date)/365 > 1, 'All', '-') Units, a.ticker schemeCode 
from wealth_details a, asset_classification b 
where a.memberid in (1003, 1019) and 
a.asset_classid = b.classid and
(a.asset_classid in (201020, 202020, 203040, 301010, 301020) or (a.asset_classid > 401010 and a.asset_classid < 405040))
order by memberid, asset_class_group, name;

select scheme_code, scheme_name_part, trailing_return_1yr, trailing_return_3yr, trailing_return_5yr from mutual_fund_stats a 
where a.scheme_code in ('101232','128628','113070','101481','102205','101002','101002','101909','100119','100119','102147','119062','101144','139870','100470','102883','141429','101980','102000','103312','130827','118102','122387','119598','100520','101762','101762','108594','107578','107578','129048','101161','100473','100473','107524','118989','105758','100377','129223','102205','100119','100471','102000');


select distinct(portfoliono) from wealth_details a where a.short_name like '%-Reg-%' and a.memberid in (1007, 1015, 1058) order by memberid, name;
select * from mutual_fund_house;

select * from mutual_fund_house a order by fund_house;

-- Stock analysis
SELECT /*a.memberid,*/ b.asset_class_group, c.sub_industry_name_display, short_name, sum(a.quantity), max(buy_date), round(sum(total_cost)), round(sum(market_value)), round(sum(net_profit)), round(avg(absolute_return),4), round(avg(annualized_return),4) 
FROM wealth_details a, asset_classification b, subindustry c
where a.memberid in (1000, 1011) and 
a.asset_classid = b.classid and
a.subindustryid = c.subindustryid and
(a.asset_classid > 406000 and a.asset_classid < 409000)
group by ticker-- , memberid
order by /*memberid,*/ sum(market_value) desc, asset_class_group, short_name;

select * from wealth_details where memberid in (1000, 1011);