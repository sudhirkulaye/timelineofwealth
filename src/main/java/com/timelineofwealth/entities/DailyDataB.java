package com.timelineofwealth.entities;

import com.timelineofwealth.dto.Ticker;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "daily_data_b")
public class DailyDataB implements Serializable {

    @Embeddable
    public static class DailyDataBKey implements Serializable {
        @Column(name = "ticker_b")
        private String tickerB;
        @Column(name = "date")
        private Date date;

        public DailyDataBKey(){}

        public DailyDataBKey(String tickerB, Date date){
            this.tickerB = tickerB;
            this.date = date;
        }

        public String getTickerB() {
            return tickerB;
        }
        public void setTickerB(String tickerB) {
            this.tickerB = tickerB;
        }

        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }
    }
    @EmbeddedId
    private DailyDataBKey key;
    @Column(name = "name_b")
    private String nameB;
    @Column(name = "closing_price")
    private BigDecimal closingPrice;
    @Column(name = "previous_day_closing_price")
    private BigDecimal previousDayClosingPrice;
    @Column(name = "volume")
    private BigDecimal volume;
    @Column(name = "volume_30d")
    private BigDecimal volume30d;
    @Column(name = "market_cap")
    private BigDecimal marketCap;
    @Column(name = "shares_outstanding")
    private BigDecimal sharesOutstanding;
    @Column(name = "eps")
    private BigDecimal eps;
    @Column(name = "best_eps_lst_qtr")
    private BigDecimal bestEpsLstQtr;
    @Column(name = "est_eps_last_qtr")
    private BigDecimal estEpsLastQtr;
    @Column(name = "eps_surprise_last_qtr")
    private BigDecimal epsSurpriseLastQtr;
    @Column(name = "estimated_eps_yr")
    private BigDecimal estimatedEpsYr;
    @Column(name = "estimated_eps_nxt_qtr")
    private BigDecimal estimatedEpsNxtQtr;
    @Column(name = "current_pe")
    private BigDecimal currentPe;
    @Column(name = "estimated_pe_cur_yr")
    private BigDecimal estimatedPeCurYr;
    @Column(name = "price_book")
    private BigDecimal priceBook;
    @Column(name = "price_to_sales")
    private BigDecimal priceToSales;
    @Column(name = "dividend_yield")
    private BigDecimal dividendYield;
    @Column(name = "sector_name_b")
    private String sectorNameB;
    @Column(name = "industry_name_b")
    private String industryNameB;
    @Column(name = "sub_industry_name_b")
    private String subIndustryNameB;
    @Column(name = "DS199")
    private String DS199;
    @Column(name = "DS201")
    private String DS201;
    @Column(name = "fiftytwo_week_low")
    private BigDecimal fiftyTwoWeekLow;
    @Column(name = "fiftytwo_week_high")
    private BigDecimal fiftyTwoWeekHigh;
    @Column(name = "price_chge_1D")
    private BigDecimal priceChge1D;
    @Column(name = "pct_chge_1D")
    private BigDecimal pctChge1D;
    @Column(name = "total_return_1year")
    private BigDecimal totalReturn1year;
    @Column(name = "total_return_YTD")
    private BigDecimal totalReturnYTD;
    @Column(name = "market_cap_rank")
    private BigDecimal marketCapRank;
    @Column(name = "last_earning_date")
    private Date lastEarningDate;
    @Column(name = "next_earning_date")
    private Date nextEarningDate;
    @Column(name = "latest_anncmt_period")
    private String latestAnncmtPeriod;
    @Column(name = "shares")
    private int shares;

    public DailyDataB(){
        this.key = new DailyDataB.DailyDataBKey();
    }

    public DailyDataB(Ticker ticker){
        java.util.Date utimeToUtilDate = new java.util.Date((long)ticker.getUTIME()*1000);
        this.key = new DailyDataB.DailyDataBKey(ticker.getTicker(),new java.sql.Date(utimeToUtilDate.getTime()));
        this.nameB = ticker.getDisp_name();
        this.closingPrice = ticker.getLast_price()!= null ? new BigDecimal(ticker.getLast_price()):new BigDecimal(0);
        this.previousDayClosingPrice = new BigDecimal(0);
        this.volume = ticker.getVolume()!= null ? new BigDecimal(ticker.getVolume()) : new BigDecimal(0);
        this.volume30d = ticker.getVolume_30d() != null ? new BigDecimal(ticker.getVolume_30d()) : new BigDecimal(0);
        this.marketCap = ticker.getMarket_cap() != null ? new BigDecimal(ticker.getMarket_cap()) : new BigDecimal(0);
        if (this.getMarketCap() != null && this.getClosingPrice() != null && ((float) this.getClosingPrice().floatValue()) > 0.0)
            this.sharesOutstanding = this.getMarketCap().divide(this.getClosingPrice(), 2, RoundingMode.HALF_UP);
        this.eps = ticker.getEps() != null ? new BigDecimal(ticker.getEps()) : new BigDecimal(0);
        this.bestEpsLstQtr = ticker.getBest_eps_lst_qtr() != null ? new BigDecimal(ticker.getBest_eps_lst_qtr()) : new BigDecimal(0);
        this.estEpsLastQtr = ticker.getEst_eps_last_qtr() != null ? new BigDecimal(ticker.getEst_eps_last_qtr()) : new BigDecimal(0);
        this.epsSurpriseLastQtr = ticker.getEps_surprise_last_qtr() != null ? new BigDecimal(ticker.getEps_surprise_last_qtr()) : new BigDecimal(0);
        this.estimatedEpsYr = ticker.getEstimated_eps_yr() != null ? new BigDecimal(ticker.getEstimated_eps_yr()) : new BigDecimal(0);
        this.estimatedEpsNxtQtr = ticker.getEstimated_eps_nxt_qtr() != null ? new BigDecimal(ticker.getEstimated_eps_nxt_qtr()) : new BigDecimal(0);
        this.currentPe = ticker.getCurrent_pe() != null ? new BigDecimal(ticker.getCurrent_pe()) : new BigDecimal(0);
        this.estimatedPeCurYr = ticker.getEstimated_pe_cur_yr() != null ? new BigDecimal(ticker.getEstimated_pe_cur_yr()) : new BigDecimal(0);
        this.priceBook = ticker.getPrice_book() != null ? new BigDecimal(ticker.getPrice_book()) : new BigDecimal(0);
        this.priceToSales = new BigDecimal(0);
        this.dividendYield = ticker.getDividend_indicated_gross_yield() != null ? new BigDecimal(ticker.getDividend_indicated_gross_yield()) : new BigDecimal(0);
        this.sectorNameB = ticker.getCompany_sector();
        this.industryNameB = ticker.getCompany_industry();
        this.subIndustryNameB = "";
        this.DS199 = ticker.getDS199();
        this.DS201 = ticker.getDS201();
        this.fiftyTwoWeekLow = ticker.getRange_52wk_low() != null ? new BigDecimal(ticker.getRange_52wk_low()) : new BigDecimal(0);
        this.fiftyTwoWeekHigh = ticker.getRange_52wk_high() != null ? new BigDecimal(ticker.getRange_52wk_high()) : new BigDecimal(0);
        this.priceChge1D = ticker.getPrice_chge_1D() != null ? new BigDecimal(ticker.getPrice_chge_1D()) : new BigDecimal(0);
        this.pctChge1D = ticker.getPct_chge_1D() != null ? new BigDecimal(ticker.getPct_chge_1D()) : new BigDecimal(0);
        this.totalReturn1year = ticker.getPct_return_52wk() != null ? new BigDecimal(ticker.getPct_return_52wk()): new BigDecimal(0);
        this.totalReturnYTD = new BigDecimal(0);
        this.marketCapRank = new BigDecimal(0);
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        java.sql.Date lastEarningDate = null;
        java.sql.Date nextEarningDate = null;
        try {
            lastEarningDate = ticker.getLast_earning_date() != null && !ticker.getLast_earning_date().isEmpty()? new java.sql.Date(format.parse(ticker.getLast_earning_date()).getTime()):new java.sql.Date(format.parse("2000-01-01").getTime());
            nextEarningDate = ticker.getNext_earning_date() != null && !ticker.getNext_earning_date().isEmpty()? new java.sql.Date(format.parse(ticker.getNext_earning_date()).getTime()):new java.sql.Date(format.parse("2000-01-01").getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        this.lastEarningDate = lastEarningDate;
        this.nextEarningDate = nextEarningDate;
        this.latestAnncmtPeriod = ticker.getLatest_anncmt_period();
        this.shares = ticker.getShares();
    }

    public DailyDataBKey getKey() {
        return key;
    }

    public void setKey(DailyDataBKey key) {
        this.key = key;
    }

    public String getNameB() {
        return nameB;
    }

    public void setNameB(String nameB) {
        this.nameB = nameB;
    }

    public BigDecimal getClosingPrice() {
        return closingPrice;
    }

    public void setClosingPrice(BigDecimal closingPrice) {
        this.closingPrice = closingPrice;
    }

    public BigDecimal getPreviousDayClosingPrice() {
        return previousDayClosingPrice;
    }

    public void setPreviousDayClosingPrice(BigDecimal previousDayClosingPrice) {
        this.previousDayClosingPrice = previousDayClosingPrice;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public BigDecimal getVolume30d() {
        return volume30d;
    }

    public void setVolume30d(BigDecimal volume30d) {
        this.volume30d = volume30d;
    }

    public BigDecimal getMarketCap() {
        return marketCap;
    }

    public void setMarketCap(BigDecimal marketCap) {
        this.marketCap = marketCap;
    }

    public BigDecimal getSharesOutstanding() {
        return sharesOutstanding;
    }

    public void setSharesOutstanding(BigDecimal sharesOutstanding) {
        this.sharesOutstanding = sharesOutstanding;
    }

    public BigDecimal getEps() {
        return eps;
    }

    public void setEps(BigDecimal eps) {
        this.eps = eps;
    }

    public BigDecimal getBestEpsLstQtr() {
        return bestEpsLstQtr;
    }

    public void setBestEpsLstQtr(BigDecimal bestEpsLstQtr) {
        this.bestEpsLstQtr = bestEpsLstQtr;
    }

    public BigDecimal getEstEpsLastQtr() {
        return estEpsLastQtr;
    }

    public void setEstEpsLastQtr(BigDecimal estEpsLastQtr) {
        this.estEpsLastQtr = estEpsLastQtr;
    }

    public BigDecimal getEpsSurpriseLastQtr() {
        return epsSurpriseLastQtr;
    }

    public void setEpsSurpriseLastQtr(BigDecimal epsSurpriseLastQtr) {
        this.epsSurpriseLastQtr = epsSurpriseLastQtr;
    }

    public BigDecimal getEstimatedEpsYr() {
        return estimatedEpsYr;
    }

    public void setEstimatedEpsYr(BigDecimal estimatedEpsYr) {
        this.estimatedEpsYr = estimatedEpsYr;
    }

    public BigDecimal getEstimatedEpsNxtQtr() {
        return estimatedEpsNxtQtr;
    }

    public void setEstimatedEpsNxtQtr(BigDecimal estimatedEpsNxtQtr) {
        this.estimatedEpsNxtQtr = estimatedEpsNxtQtr;
    }

    public BigDecimal getCurrentPe() {
        return currentPe;
    }

    public void setCurrentPe(BigDecimal currentPe) {
        this.currentPe = currentPe;
    }

    public BigDecimal getEstimatedPeCurYr() {
        return estimatedPeCurYr;
    }

    public void setEstimatedPeCurYr(BigDecimal estimatedPeCurYr) {
        this.estimatedPeCurYr = estimatedPeCurYr;
    }

    public BigDecimal getPriceBook() {
        return priceBook;
    }

    public void setPriceBook(BigDecimal priceBook) {
        this.priceBook = priceBook;
    }

    public BigDecimal getPriceToSales() {
        return priceToSales;
    }

    public void setPriceToSales(BigDecimal priceToSales) {
        this.priceToSales = priceToSales;
    }

    public BigDecimal getDividendYield() {
        return dividendYield;
    }

    public void setDividendYield(BigDecimal dividendYield) {
        this.dividendYield = dividendYield;
    }

    public String getSectorNameB() {
        return sectorNameB;
    }

    public void setSectorNameB(String sectorNameB) {
        this.sectorNameB = sectorNameB;
    }

    public String getIndustryNameB() {
        return industryNameB;
    }

    public void setIndustryNameB(String industryNameB) {
        this.industryNameB = industryNameB;
    }

    public String getSubIndustryNameB() {
        return subIndustryNameB;
    }

    public void setSubIndustryNameB(String subIndustryNameB) {
        this.subIndustryNameB = subIndustryNameB;
    }

    public String getDS199() {
        return DS199;
    }

    public void setDS199(String DS199) {
        this.DS199 = DS199;
    }

    public String getDS201() {
        return DS201;
    }

    public void setDS201(String DS201) {
        this.DS201 = DS201;
    }

    public BigDecimal getFiftyTwoWeekLow() {
        return fiftyTwoWeekLow;
    }

    public void setFiftyTwoWeekLow(BigDecimal fiftyTwoWeekLow) {
        this.fiftyTwoWeekLow = fiftyTwoWeekLow;
    }

    public BigDecimal getFiftyTwoWeekHigh() {
        return fiftyTwoWeekHigh;
    }

    public void setFiftyTwoWeekHigh(BigDecimal fiftyTwoWeekHigh) {
        this.fiftyTwoWeekHigh = fiftyTwoWeekHigh;
    }

    public BigDecimal getPriceChge1D() {
        return priceChge1D;
    }

    public void setPriceChge1D(BigDecimal priceChge1D) {
        this.priceChge1D = priceChge1D;
    }

    public BigDecimal getPctChge1D() {
        return pctChge1D;
    }

    public void setPctChge1D(BigDecimal pctChge1D) {
        this.pctChge1D = pctChge1D;
    }

    public BigDecimal getTotalReturn1year() {
        return totalReturn1year;
    }

    public void setTotalReturn1year(BigDecimal totalReturn1year) {
        this.totalReturn1year = totalReturn1year;
    }

    public BigDecimal getTotalReturnYTD() {
        return totalReturnYTD;
    }

    public void setTotalReturnYTD(BigDecimal totalReturnYTD) {
        this.totalReturnYTD = totalReturnYTD;
    }

    public BigDecimal getMarketCapRank() {
        return marketCapRank;
    }

    public void setMarketCapRank(BigDecimal marketCapRank) {
        this.marketCapRank = marketCapRank;
    }

    public Date getLastEarningDate() {
        return lastEarningDate;
    }

    public void setLastEarningDate(Date lastEarningDate) {
        this.lastEarningDate = lastEarningDate;
    }

    public Date getNextEarningDate() {
        return nextEarningDate;
    }

    public void setNextEarningDate(Date nextEarningDate) {
        this.nextEarningDate = nextEarningDate;
    }

    public String getLatestAnncmtPeriod() {
        return latestAnncmtPeriod;
    }

    public void setLatestAnncmtPeriod(String latestAnncmtPeriod) {
        this.latestAnncmtPeriod = latestAnncmtPeriod;
    }

    public int getShares() {
        return shares;
    }

    public void setShares(int shares) {
        this.shares = shares;
    }
}
