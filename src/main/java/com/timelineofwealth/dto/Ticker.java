package com.timelineofwealth.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Ticker implements Serializable {

    private String est_eps_last_qtr;
    private String disp_name;
    private String last_price;
    private String company_sector;
    private String company_industry;
    private String best_eps_lst_qtr;
    private String buyprice_pershare;
    @JsonProperty("UTIME")
    private long UTIME;
    private String total_value;
    private String sec_type;
    private String torder;
    private String ticker;
    private String time_of_last_updt;
    private String latest_anncmt_period;
    private String current_pe;
    private String next_earning_date;
    private String market_cap;
    private String price_book;
    private String total_pct_change;
    private String eps;
    private String fund_type;
    private String wds_returnCode;
    private String price_chge_1D;
    private String pct_chge_1D;
    private String cost;
    private String percent_chge_1_year;
    private String dividend_indicated_gross_yield;
    private int shares;
    private String eps_surprise_last_qtr;
    private String estimated_pe_cur_yr;
    private String volume_30d;
    private String security_type;
    @JsonProperty("DS201")
    private String DS201;
    private String pct_return_52wk;
    private String volume;
    @JsonProperty("SECTYPE")
    private String SECTYPE;
    private String estimated_eps_yr;
    private String currency;
    private String range_52wk_high;
    private String last_earning_date;
    private String range_52wk_low;
    private String exch_code;
    private String total_gain_loss;
    private String today_gain_loss;
    private String price_precision;
    @JsonProperty("DS199")
    private String DS199;
    private String estimated_eps_nxt_qtr;

    public Ticker() {
    }

    public String getEst_eps_last_qtr() {
        return est_eps_last_qtr;
    }

    public void setEst_eps_last_qtr(String est_eps_last_qtr) {
        this.est_eps_last_qtr = est_eps_last_qtr;
    }

    public String getDisp_name() {
        return disp_name;
    }

    public void setDisp_name(String disp_name) {
        this.disp_name = disp_name;
    }

    public String getLast_price() {
        return last_price;
    }

    public void setLast_price(String last_price) {
        this.last_price = last_price;
    }

    public String getCompany_sector() {
        return company_sector;
    }

    public void setCompany_sector(String company_sector) {
        this.company_sector = company_sector;
    }

    public String getCompany_industry() {
        return company_industry;
    }

    public void setCompany_industry(String company_industry) {
        this.company_industry = company_industry;
    }

    public String getBest_eps_lst_qtr() {
        return best_eps_lst_qtr;
    }

    public void setBest_eps_lst_qtr(String best_eps_lst_qtr) {
        this.best_eps_lst_qtr = best_eps_lst_qtr;
    }

    public String getBuyprice_pershare() {
        return buyprice_pershare;
    }

    public void setBuyprice_pershare(String buyprice_pershare) {
        this.buyprice_pershare = buyprice_pershare;
    }

    public long getUTIME() {
        return UTIME;
    }

    public void setUTIME(long UTIME) {
        this.UTIME = UTIME;
    }

    public String getTotal_value() {
        return total_value;
    }

    public void setTotal_value(String total_value) {
        this.total_value = total_value;
    }

    public String getSec_type() {
        return sec_type;
    }

    public void setSec_type(String sec_type) {
        this.sec_type = sec_type;
    }

    public String getTorder() {
        return torder;
    }

    public void setTorder(String torder) {
        this.torder = torder;
    }

    public String getTicker() {
        return ticker;
    }

    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getTime_of_last_updt() {
        return time_of_last_updt;
    }

    public void setTime_of_last_updt(String time_of_last_updt) {
        this.time_of_last_updt = time_of_last_updt;
    }

    public String getLatest_anncmt_period() {
        return latest_anncmt_period;
    }

    public void setLatest_anncmt_period(String latest_anncmt_period) {
        this.latest_anncmt_period = latest_anncmt_period;
    }

    public String getCurrent_pe() {
        return current_pe;
    }

    public void setCurrent_pe(String current_pe) {
        this.current_pe = current_pe;
    }

    public String getNext_earning_date() {
        return next_earning_date;
    }

    public void setNext_earning_date(String next_earning_date) {
        this.next_earning_date = next_earning_date;
    }

    public String getMarket_cap() {
        return market_cap;
    }

    public void setMarket_cap(String market_cap) {
        this.market_cap = market_cap;
    }

    public String getPrice_book() {
        return price_book;
    }

    public void setPrice_book(String price_book) {
        this.price_book = price_book;
    }

    public String getTotal_pct_change() {
        return total_pct_change;
    }

    public void setTotal_pct_change(String total_pct_change) {
        this.total_pct_change = total_pct_change;
    }

    public String getEps() {
        return eps;
    }

    public void setEps(String eps) {
        this.eps = eps;
    }

    public String getFund_type() {
        return fund_type;
    }

    public void setFund_type(String fund_type) {
        this.fund_type = fund_type;
    }

    public String getWds_returnCode() {
        return wds_returnCode;
    }

    public void setWds_returnCode(String wds_returnCode) {
        this.wds_returnCode = wds_returnCode;
    }

    public String getPrice_chge_1D() {
        return price_chge_1D;
    }

    public void setPrice_chge_1D(String price_chge_1D) {
        this.price_chge_1D = price_chge_1D;
    }

    public String getPct_chge_1D() {
        return pct_chge_1D;
    }

    public void setPct_chge_1D(String pct_chge_1D) {
        this.pct_chge_1D = pct_chge_1D;
    }

    public String getCost() {
        return cost;
    }

    public void setCost(String cost) {
        this.cost = cost;
    }

    public String getPercent_chge_1_year() {
        return percent_chge_1_year;
    }

    public void setPercent_chge_1_year(String percent_chge_1_year) {
        this.percent_chge_1_year = percent_chge_1_year;
    }

    public String getDividend_indicated_gross_yield() {
        return dividend_indicated_gross_yield;
    }

    public void setDividend_indicated_gross_yield(String dividend_indicated_gross_yield) {
        this.dividend_indicated_gross_yield = dividend_indicated_gross_yield;
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }

    public String getEps_surprise_last_qtr() {
        return eps_surprise_last_qtr;
    }

    public void setEps_surprise_last_qtr(String eps_surprise_last_qtr) {
        this.eps_surprise_last_qtr = eps_surprise_last_qtr;
    }

    public String getEstimated_pe_cur_yr() {
        return estimated_pe_cur_yr;
    }

    public void setEstimated_pe_cur_yr(String estimated_pe_cur_yr) {
        this.estimated_pe_cur_yr = estimated_pe_cur_yr;
    }

    public String getVolume_30d() {
        return volume_30d;
    }

    public void setVolume_30d(String volume_30d) {
        this.volume_30d = volume_30d;
    }

    public String getSecurity_type() {
        return security_type;
    }

    public void setSecurity_type(String security_type) {
        this.security_type = security_type;
    }

    public String getDS201() {
        return DS201;
    }

    public void setDS201(String DS201) {
        this.DS201 = DS201;
    }

    public String getPct_return_52wk() {
        return pct_return_52wk;
    }

    public void setPct_return_52wk(String pct_return_52wk) {
        this.pct_return_52wk = pct_return_52wk;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }

    public String getSECTYPE() {
        return SECTYPE;
    }

    public void setSECTYPE(String SECTYPE) {
        this.SECTYPE = SECTYPE;
    }

    public String getEstimated_eps_yr() {
        return estimated_eps_yr;
    }

    public void setEstimated_eps_yr(String estimated_eps_yr) {
        this.estimated_eps_yr = estimated_eps_yr;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getRange_52wk_high() {
        return range_52wk_high;
    }

    public void setRange_52wk_high(String range_52wk_high) {
        this.range_52wk_high = range_52wk_high;
    }

    public String getLast_earning_date() {
        return last_earning_date;
    }

    public void setLast_earning_date(String last_earning_date) {
        this.last_earning_date = last_earning_date;
    }

    public String getRange_52wk_low() {
        return range_52wk_low;
    }

    public void setRange_52wk_low(String range_52wk_low) {
        this.range_52wk_low = range_52wk_low;
    }

    public String getExch_code() {
        return exch_code;
    }

    public void setExch_code(String exch_code) {
        this.exch_code = exch_code;
    }

    public String getTotal_gain_loss() {
        return total_gain_loss;
    }

    public void setTotal_gain_loss(String total_gain_loss) {
        this.total_gain_loss = total_gain_loss;
    }

    public String getToday_gain_loss() {
        return today_gain_loss;
    }

    public void setToday_gain_loss(String today_gain_loss) {
        this.today_gain_loss = today_gain_loss;
    }

    public String getPrice_precision() {
        return price_precision;
    }

    public void setPrice_precision(String price_precision) {
        this.price_precision = price_precision;
    }

    public String getDS199() {
        return DS199;
    }

    public void setDS199(String DS199) {
        this.DS199 = DS199;
    }

    public String getEstimated_eps_nxt_qtr() {
        return estimated_eps_nxt_qtr;
    }

    public void setEstimated_eps_nxt_qtr(String estimated_eps_nxt_qtr) {
        this.estimated_eps_nxt_qtr = estimated_eps_nxt_qtr;
    }
}
