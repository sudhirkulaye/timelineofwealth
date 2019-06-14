DROP PROCEDURE IF EXISTS ap_process_eod;
CREATE PROCEDURE ap_process_eod()
BEGIN
	INSERT INTO log_table
	VALUES      (now(), 'ap_process_eod: Begin');

	call ap_update_wealth_data();

	call ap_process_stat_calculation();

	update mutual_fund_universe set fund_house =
	case when scheme_name_full like 'Aditya Birla%' then 'Aditya Birla'
	when scheme_name_full like 'Aditya Birla%' then 'Aditya Birla'
	when scheme_name_full like 'Baroda %' then 'Baroda Pioneer'
	when scheme_name_full like 'DSP %' then 'DSP'
	when scheme_name_full like 'HDFC %' then 'HDFC'
	when scheme_name_full like 'Principal %' then 'Principal'
	when scheme_name_full like 'Quant %' then 'Quant'
	when scheme_name_full like 'JM %' then 'JM'
	when scheme_name_full like 'Kotak %' then 'Kotak'
	when scheme_name_full like 'LIC %' then 'LIC'
	when scheme_name_full like 'Sahara %' then 'Sahara'
	when scheme_name_full like 'ICICI Prudential%' then 'ICICI Prudential'
	when scheme_name_full like 'Reliance %' then 'Reliance'
	when scheme_name_full like 'Tata %' then 'Tata'
	when scheme_name_full like 'Franklin %' then 'Franklin'
	when scheme_name_full like 'Taurus %' then 'Taurus'
	when scheme_name_full like 'Templeton %' then 'Templeton'
	when scheme_name_full like 'Canara Robeco%' then 'Canara Robeco'
	when scheme_name_full like 'Sundaram %' then 'Sundaram'
	when scheme_name_full like 'SBI %' then 'SBI Magnum'
	when scheme_name_full like 'UTI %' then 'UTI'
	when scheme_name_full like 'HSBC %' then 'HSBC'
	when scheme_name_full like 'Quantum %' then 'Quantum'
	when scheme_name_full like 'Invesco %' then 'Invesco'
	when scheme_name_full like 'Mirae %' then 'Mirae'
	when scheme_name_full like 'IDFC %' then 'IDFC'
	when scheme_name_full like 'BOI AXA%' then 'BOI AXA'
	when scheme_name_full like 'Edelweiss %' then 'Edelweiss'
	when scheme_name_full like 'Axis %' then 'Axis'
	when scheme_name_full like 'Essel %' then 'Essel'
	when scheme_name_full like 'L&T %' then 'L&T'
	when scheme_name_full like 'IDBI %' then 'IDBI'
	when scheme_name_full like 'Motilal Oswal%' then 'Motilal Oswal'
	when scheme_name_full like 'BNP Paribas%' then 'BNP Paribas'
	when scheme_name_full like 'Union %' then 'Union'
	when scheme_name_full like 'Indiabulls %' then 'Indiabulls'
	when scheme_name_full like 'DHFL %' then 'DHFL'
	when scheme_name_full like 'IIFL %' then 'IIFL'
	when scheme_name_full like 'Parag Parikh%' then 'Parag Parikh'
	when scheme_name_full like 'Shriram %' then 'Shriram'
	when scheme_name_full like 'IIFCL %' then 'IIFCL'
	when scheme_name_full like 'IL&FS %' then 'IL&FS'
	when scheme_name_full like 'Mahindra %' then 'Mahindra' end where fund_house = 'XXX';

	update mutual_fund_universe a set a.direct_regular = 'Regular' where direct_regular is null or direct_regular = '' or direct_regular not like '%Direct%';
	update mutual_fund_universe a set a.dividend_growth = 'Growth' where dividend_growth is null or dividend_growth = '' and a.isin_div_payout_or_isin_growth = 'XXX';
	update mutual_fund_universe a set a.dividend_growth = 'Dividend' where scheme_name_full like '%Div Option';

	update mutual_fund_universe a
	set a.scheme_name_part = REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(REPLACE(a.scheme_name_full, 'Direct Plan', ' '), 'Regular Plan', ' '), 'Dividend Option', ' '), 'Div ',' '), 'Growth Option', ' '), 'Growth', ' ')
	where a.scheme_name_part = 'XXX';

	-- update short name with -Dir-G/D or -Reg-G/D
	update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Dir-G')
	where a.scheme_name_part not like '%-Dir-G' and dividend_growth = 'Growth' and direct_regular = 'Direct';
	update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Dir-D')
	where a.scheme_name_part not like '%-Dir-D' and dividend_growth = 'Dividend' and direct_regular = 'Direct';
	update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Reg-D')
	where a.scheme_name_part not like '%-Reg-D' and dividend_growth = 'Dividend' and direct_regular = 'Regular';
	update mutual_fund_universe a  set a.scheme_name_part = concat(a.scheme_name_part, '-Reg-G')
	where a.scheme_name_part not like '%-Reg-G' and dividend_growth = 'Growth' and direct_regular = 'Regular';
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, ' -   - ', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '-  -', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '- -', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, ' -  - - ', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, ' -  - -', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '-  - -', '-');
	update mutual_fund_universe a  set a.scheme_name_part = REPLACE(a.scheme_name_part, '- - -', '-');

	-- update wealth details short name if there is any change in name
	update wealth_details a, mutual_fund_universe b set a.short_name = b.scheme_name_part, a.asset_classid = b.asset_classid where a.ticker = b.scheme_code;
	update wealth_details a, stock_universe b set a.short_name = b.short_name, a.subindustryid = b.subindustryid, a.asset_classid = b.asset_classid where a.ticker = b.ticker;
	update sip a, mutual_fund_universe b set a.scheme_name = b.scheme_name_part where a.scheme_code = b.scheme_code;

    COMMIT;

  INSERT INTO log_table
  VALUES      (now(), 'ap_process_eod: End');
END