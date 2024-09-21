-- Generate Password
select * from user; 

INSERT INTO user (email, password, active, joining_date, prefix, name, last_name, cellno, add_line1, add_line2, add_line3, city, state, pin, last_login_time, role_name) 
VALUES ('ketanpatil656@gmail.com', '$2a$10$6SltS5fzyIvD10XbeT3RGuWQBcKp8G8CVAtfd6E9YwAkBJSODaP.6', '1', '2024-08-07', 'Mr', 'Ketan', 'Patil', '9999999999', 'XXX', 'XXX', 'XXX', 'Mumbai', 'Maharashtra', '400001', '2018-01-01 00:00:00', 'ROLE_END_USER');

select * from sequence_next_high_value;

select * from member a order by a.memberid desc; 

INSERT INTO member (memberid, first_name, last_name, relationship, birth_date, gender, marital_status, email, cellno, earning_status, is_secured_by_pension, education, is_finance_professional, is_alive, date_last_update) VALUES 
('1076', 'Ketan', 'Patil', 'Self', '1983-01-18', 'M', 'Married', 'ketanpatil656@gmail.com', '9999999999', 'Earning', 'N', 'Graduate', 'N', 'Y', '2024-08-07');

SET SQL_SAFE_UPDATES = 0;
Commit;
update sequence_next_high_value set next_val = (select max(memberid)+1 from member);

select * from user_members a order by a.memberid desc;
insert into user_members values ('ketanpatil656@gmail.com', 1076, 'Self');

select * from adviser_user_mapping;
insert into adviser_user_mapping values ('sudhirkulaye', 'ketanpatil656@gmail.com', 'Yes');

-- scrept to create PMS acct
select * from portfolio a order by a.memberid, a.portfolioid; -- total 33 portfolios 
INSERT INTO portfolio (memberid, portfolioid, status, description, start_date, end_date, compositeid, net_investment, market_value, holding_period, net_profit, absolute_return, annualized_return) 
VALUES ('1076', '1', 'Active', 'NIFTY+', '2024-08-08', '2031-08-07', '4', '50000', '50000', '0', '0', '0', '0');

select * from portfolio_cashflow where memberid in (1074, 1075) order by portfolioid, date desc;
INSERT INTO portfolio_cashflow (memberid, portfolioid, date, cashflow, description) 
VALUES ('1076', '1', '2024-08-08', '-50000', 'Initial Cash');

select * from portfolio_value_history a where memberid in (1074, 1075) and  date >= '2021-01-01' order by date desc;
INSERT INTO portfolio_value_history (memberid, portfolioid, date, value) 
VALUES ('1076', '1', '2024-08-08', '50000');

SELECT * FROM portfolio_holdings a  WHERE memberid in (1074, 1075) order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
INSERT INTO portfolio_holdings (memberid, portfolioid, buy_date, ticker, name, short_name, asset_classid, subindustryid, quantity, rate, brokerage, tax, total_cost, net_rate, cmp, market_value, holding_period, net_profit, absolute_return, annualized_return, maturity_value, maturity_date, last_valuation_date) 
VALUES ('1076', '1', '2024-08-08', 'MOSL_CASH', 'MOSL Cash', 'MOSL Cash', '101010', '0', '1.0000', '50000', '0', '0', '50000', '50000', '50000', '50000', '0', '0', '0', '0', '0.000', '2000-01-01', '2024-08-08');

select * from moslcode_memberid where memberid in (1074, 1075); 
INSERT INTO moslcode_memberid (memberid, moslcode) VALUES ('1076', 'H4079');

select * from portfolio a where memberid in (1076) order by a.memberid, a.portfolioid;
select * from portfolio_cashflow where memberid in (1076) order by date desc;
select min(date), memberid, portfolioid from portfolio_value_history a where memberid in(1076) group by memberid, portfolioid order by date desc;
select * from portfolio_value_history a where memberid in (1076) order by date;
select * from moslcode_memberid a where memberid in (1076); 
SELECT * FROM portfolio_holdings a  WHERE memberid in (1076) order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
select * from nse_price_history where nse_ticker = 'TCS' and date between '2021-01-20' and '2021-01-31' order by date desc;