-- Generate Password
select * from user; 

INSERT INTO user (email, password, active, joining_date, prefix, name, last_name, cellno, add_line1, add_line2, add_line3, city, state, pin, last_login_time, role_name) 
VALUES ('paragmohite809@gmail.com', '$2a$10$l6WDUtf1A.pSSvrPDgQHdOvJdC/Vz0Itl.XoPu2m8hzMg4DxuJO/i', '1', '2020-02-17', 'Mr', 'Parag', 'Mohite', '9999999999', 'XXX', 'XXX', 'XXX', 'Mumbai', 'Maharashtra', '400001', '2018-01-01 00:00:00', 'ROLE_END_USER');

select * from sequence_next_high_value;

select * from member a order by a.memberid desc; 

INSERT INTO member (memberid, first_name, last_name, relationship, birth_date, gender, marital_status, email, cellno, earning_status, is_secured_by_pension, education, is_finance_professional, is_alive, date_last_update) VALUES 
('1068', 'Parag', 'Mohite', 'Self', '1999-04-01', 'M', 'Single', 'paragmohite809@gmail.com', '9999999999', 'Earning', 'N', 'Under Graduate', 'N', 'Y', '2020-02-17');

SET SQL_SAFE_UPDATES = 0;
Commit;
update sequence_next_high_value set next_val = (select max(memberid)+1 from member);

select * from user_members a order by a.memberid desc;
insert into user_members values ('paragmohite809@gmail.com', 1068, 'Self');

insert into adviser_user_mapping values ('sudhirkulaye', 'paragmohite809@gmail.com', 'Yes');
select * from adviser_user_mapping;

-- scrept to create PMS acct
select * from portfolio a order by a.memberid, a.portfolioid; -- total 21 portfolios 
INSERT INTO portfolio (memberid, portfolioid, status, description, start_date, end_date, compositeid, net_investment, market_value, holding_period, net_profit, absolute_return, annualized_return) 
VALUES ('1068', '1', 'Active', 'INTRO', '2020-02-17', '2030-02-17', '1', '5000', '5000', '1', '0', '0', '0');

select * from portfolio_cashflow where memberid in (1068) order by portfolioid, date desc;
INSERT INTO portfolio_cashflow (memberid, portfolioid, date, cashflow, description) 
VALUES ('1068', '1', '2020-02-14', '-5000', 'Initial Cash');

select * from portfolio_value_history a where memberid in (1068) and  date >= '2019-12-01' order by date desc;
INSERT INTO portfolio_value_history (memberid, portfolioid, date, value) 
VALUES ('1068', '1', '2020-02-14', '5000');

SELECT * FROM portfolio_holdings a  WHERE memberid = 1068 order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
INSERT INTO portfolio_holdings (memberid, portfolioid, buy_date, ticker, name, short_name, asset_classid, subindustryid, quantity, rate, brokerage, tax, total_cost, net_rate, cmp, market_value, holding_period, net_profit, absolute_return, annualized_return, maturity_value, maturity_date, last_valuation_date) 
VALUES ('1068', '1', '2020-02-14', 'MOSL_CASH', 'MOSL Cash', 'MOSL Cash', '101010', '0', '1.0000', '5000', '0', '0', '5000', '5000', '5000', '5000', '0', '0', '0', '0', '0.000', '2000-01-01', '2019-06-21');

select * from moslcode_memberid; 
INSERT INTO moslcode_memberid (memberid, moslcode) VALUES ('1068', 'H23578');


