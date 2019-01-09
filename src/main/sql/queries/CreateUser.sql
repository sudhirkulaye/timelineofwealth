-- Generate Password
select * from user; 

INSERT INTO user (email, password, active, joining_date, prefix, name, last_name, cellno, add_line1, add_line2, add_line3, city, state, pin, last_login_time, role_name) 
VALUES ('vishnupadave', '$2a$10$Rf.2.I4ecFrb/Lt.e.e8FeyExPQxoZnrrPF0ywj8F2mbExCJyU5WC', '1', '2018-01-01', 'Mr', 'Vishnu', 'Padave', '9999999999', 'XXX', 'XXX', 'XXX', 'Mumbai', 'Maharashtra', '400001', '2018-01-01 00:00:00', 'ROLE_END_USER');

select * from sequence_next_high_value;

select * from member a order by a.memberid desc; 

INSERT INTO member (memberid, first_name, last_name, relationship, birth_date, gender, marital_status, email, cellno, earning_status, is_secured_by_pension, education, is_finance_professional, is_alive, date_last_update) VALUES 
('1065', 'Rohit', 'Padave', 'Self', '1981-01-01', 'M', 'Married', 'nishaaneeketrane@gmail.com', '9999999999', 'Earning', 'N', 'Graduate', 'N', 'Y', '2018-12-03');

SET SQL_SAFE_UPDATES = 0;
Commit;
update sequence_next_high_value set next_val = (select max(memberid)+1 from member);

select * from user_members a order by a.memberid desc;
insert into user_members values ('vishnupadave', 1065, 'Self');

insert into adviser_user_mapping values ('sudhirkulaye', 'vishnupadave', 'Yes');
select * from adviser_user_mapping;
