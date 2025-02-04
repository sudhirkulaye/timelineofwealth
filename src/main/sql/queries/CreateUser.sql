-- Generate Password
select * from user; 

INSERT INTO user (email, password, active, joining_date, prefix, name, last_name, cellno, add_line1, add_line2, add_line3, city, state, pin, last_login_time, role_name) 
VALUES ('shankarparab80@gmail.com', '$2a$10$CsyXLxFUXUvL0NUmv31UR.qpiWv6arQJJlti7L1Om5C6aJSpMVTam', '1', '2024-09-01', 'Mrs', 'Rashmi', 'Parab', '9999999999', 'XXX', 'XXX', 'XXX', 'Mumbai', 'Maharashtra', '400001', '2018-01-01 00:00:00', 'ROLE_END_USER');

select * from sequence_next_high_value;

select * from member a order by a.memberid desc; 

INSERT INTO member (memberid, first_name, last_name, relationship, birth_date, gender, marital_status, email, cellno, earning_status, is_secured_by_pension, education, is_finance_professional, is_alive, date_last_update) VALUES 
('1077', 'Rashmi', 'Parab', 'Self', '1960-01-01', 'F', 'Window', 'shankarparab80@gmail.com', '9999999999', 'Dependent', 'N', 'Under Graduate', 'N', 'Y', '2024-09-01');

SET SQL_SAFE_UPDATES = 0;
Commit;
update sequence_next_high_value set next_val = (select max(memberid)+1 from member);

select * from user_members a order by a.memberid desc;
insert into user_members values ('shankarparab80@gmail.com', 1077, 'Self');

select * from adviser_user_mapping;
insert into adviser_user_mapping values ('sudhirkulaye', 'shankarparab80@gmail.com', 'Yes');

-- scrept to create PMS acct
select * from portfolio a order by a.memberid desc, a.portfolioid; -- total 35 portfolios 
INSERT INTO portfolio (memberid, portfolioid, status, description, start_date, end_date, compositeid, net_investment, market_value, holding_period, net_profit, absolute_return, annualized_return) 
VALUES ('1077', '1', 'Active', 'FOCUS-FIVE', '2024-09-11', '2031-09-11', '2', '200000', '200000', '0', '0', '0', '0');

select * from portfolio_cashflow where memberid in (1076, 1077) order by portfolioid, date desc;
INSERT INTO portfolio_cashflow (memberid, portfolioid, date, cashflow, description) 
VALUES ('1077', '1', '2024-09-11', '-200000', 'Initial Cash');

select * from portfolio_value_history a where memberid in (1076, 1077) and  date >= '2021-01-01' order by date desc;
INSERT INTO portfolio_value_history (memberid, portfolioid, date, value) 
VALUES ('1077', '1', '2024-09-11', '200000');

SELECT * FROM portfolio_holdings a  WHERE memberid in (1076, 1077) order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
INSERT INTO portfolio_holdings (memberid, portfolioid, buy_date, ticker, name, short_name, asset_classid, subindustryid, quantity, rate, brokerage, tax, total_cost, net_rate, cmp, market_value, holding_period, net_profit, absolute_return, annualized_return, maturity_value, maturity_date, last_valuation_date) 
VALUES ('1077', '1', '2024-09-11', 'MOSL_CASH', 'MOSL Cash', 'MOSL Cash', '101010', '0', '1.0000', '1000', '0', '0', '1000', '1000', '1000', '1000', '0', '0', '0', '0', '0.000', '2000-01-01', '2024-09-11');

select * from moslcode_memberid where memberid in (1074, 1075); 
INSERT INTO moslcode_memberid (memberid, moslcode) VALUES ('1077', 'H231511');

select count(1) from portfolio a where memberid in (1077) order by a.memberid, a.portfolioid;
select count(1) from portfolio_cashflow where memberid in (1077) order by date desc;
select min(date), memberid, portfolioid from portfolio_value_history a where memberid in(1077) group by memberid, portfolioid order by date desc;
select count(1) from portfolio_value_history a where memberid in (1077) order by date;
select count(1) from moslcode_memberid a where memberid in (1077); 
SELECT count(1) FROM portfolio_holdings a  WHERE memberid in (1077) order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
