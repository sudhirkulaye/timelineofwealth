-- Generate Password
select * from user; 

INSERT INTO user (email, password, active, joining_date, prefix, name, last_name, cellno, add_line1, add_line2, add_line3, city, state, pin, last_login_time, role_name) 
VALUES ('vijaykarandekar53@gmail.com', '$2a$10$P8/KWt87ye8HVbOxm/2hNOZFwIKXl.xu/OPEaqHSX8U0YNSB.Wu8C', '1', '2023-12-21', 'Mrs', 'Vijay', 'Karandekar', '9999999999', 'XXX', 'XXX', 'XXX', 'Mumbai', 'Maharashtra', '400001', '2018-01-01 00:00:00', 'ROLE_END_USER');

select * from sequence_next_high_value;

select * from member a order by a.memberid desc; 

INSERT INTO member (memberid, first_name, last_name, relationship, birth_date, gender, marital_status, email, cellno, earning_status, is_secured_by_pension, education, is_finance_professional, is_alive, date_last_update) VALUES 
('1073', 'Vijay', 'Karandekar', 'Self', '1976-06-30', 'F', 'Widow', 'vijaykarandekar53@gmail.com', '9999999999', 'Earning', 'N', 'Doctor', 'N', 'Y', '2022-02-01');

SET SQL_SAFE_UPDATES = 0;
Commit;
update sequence_next_high_value set next_val = (select max(memberid)+1 from member);

select * from user_members a order by a.memberid desc;
insert into user_members values ('vijaykarandekar53@gmail.com', 1073, 'Self');

insert into adviser_user_mapping values ('sudhirkulaye', 'vijaykarandekar53@gmail.com', 'Yes');
select * from adviser_user_mapping;

-- scrept to create PMS acct
select * from portfolio a order by a.memberid, a.portfolioid; -- total 29 portfolios 
INSERT INTO portfolio (memberid, portfolioid, status, description, start_date, end_date, compositeid, net_investment, market_value, holding_period, net_profit, absolute_return, annualized_return) 
VALUES ('1073', '1', 'Active', 'NIFTY+', '2023-12-21', '2031-12-21', '4', '10000', '10000', '0', '0', '0', '0');

select * from portfolio_cashflow where memberid in (1069, 1071) order by portfolioid, date desc;
INSERT INTO portfolio_cashflow (memberid, portfolioid, date, cashflow, description) 
VALUES ('1073', '1', '2023-12-21', '-10000', 'Initial Cash');

select * from portfolio_value_history a where memberid in (1071) and  date >= '2021-01-01' order by date desc;
INSERT INTO portfolio_value_history (memberid, portfolioid, date, value) 
VALUES ('1073', '1', '2023-12-20', '10000');

SELECT * FROM portfolio_holdings a  WHERE memberid in (1069, 1071) order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
INSERT INTO portfolio_holdings (memberid, portfolioid, buy_date, ticker, name, short_name, asset_classid, subindustryid, quantity, rate, brokerage, tax, total_cost, net_rate, cmp, market_value, holding_period, net_profit, absolute_return, annualized_return, maturity_value, maturity_date, last_valuation_date) 
VALUES ('1073', '1', '2023-12-20', 'MOSL_CASH', 'MOSL Cash', 'MOSL Cash', '101010', '0', '1.0000', '10000', '0', '0', '10000', '10000', '10000', '10000', '0', '0', '0', '0', '0.000', '2000-01-01', '2023-12-21');

select * from moslcode_memberid where memberid in (1069, 1073); 
INSERT INTO moslcode_memberid (memberid, moslcode) VALUES ('1073', 'H3202');


select * from portfolio a where memberid in (1069,1071) order by a.memberid, a.portfolioid;
select * from portfolio_cashflow where memberid in (1069,1071) order by date desc;
select min(date), memberid, portfolioid from portfolio_value_history a where memberid in (1069,1071) group by memberid, portfolioid order by date desc;
select * from portfolio_value_history a where memberid in (1069,1071) order by date;
select * from moslcode_memberid a where memberid in (1069,1071); 
SELECT * FROM portfolio_holdings a  WHERE memberid in (1069,1071) order by a.memberid, a.portfolioid, a.asset_classid, a.ticker, a.buy_date;
select * from nse_price_history where nse_ticker = 'TCS' and date between '2021-01-20' and '2021-01-31' order by date desc;