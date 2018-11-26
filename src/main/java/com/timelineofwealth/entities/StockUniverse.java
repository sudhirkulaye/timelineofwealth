package com.timelineofwealth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "stock_universe")
public class StockUniverse  implements Serializable {

    @Id
    @Column(name = "ticker")
    private String ticker;

    @Column(name = "ticker1")
    private String ticker1;

    @Column(name = "ticker2")
    private String ticker2;

    @Column(name = "ticker3")
    private String ticker3;

    @Column(name = "ticker4")
    private String ticker4;

    @Column(name = "ticker5")
    private String ticker5;

    @Column(name = "nseCode")
    private String nseCode;

    @Column(name = "bse_code")
    private String bseCode;

    @Column(name = "isin_code")
    private String isinCode;

    @Column(name = "short_name")
    private String shortName;

    @Column(name = "name")
    private String name;

    @Column(name = "asset_classid")
    private int assetClassid;

    @Column(name = "bse_industry")
    private String bseIndustry;

    @Column(name = "subindustryid")
    private int subindustryid;

    @Column(name = "latest_price")
    private BigDecimal latestPrice;

    @Column(name = "date_latest_price")
    private Date dateLatestPrice;

    @Column(name = "is_sensex")
    private int isSensex;

    @Column(name = "is_nifty50")
    private int isNifty50;

    @Column(name = "is_niftyjr")
    private int isNiftyjr;

    @Column(name = "is_bse100")
    private int isBse100;

    @Column(name = "is_nse100")
    private int isNse100;

    @Column(name = "is_bse200")
    private int isBse200;

    @Column(name = "is_nse200")
    private int isNse200;

    @Column(name = "is_bse500")
    private int isBse500;

    @Column(name = "is_nse500")
    private int isNse500;

    @Column(name = "marketcap")
    private BigDecimal marketcap;

    @Column(name = "marketcap_rank")
    private int marketcapRank;

    @Column(name = "pe_ttm")
    private BigDecimal peTtm;

    @Column(name = "ticker_old")
    private String tickerOld;

    @Column(name = "listing_date")
    private Date listingDate;

    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public String getTicker1() {
        return ticker1;
    }
    public void setTicker1(String ticker1) {
        this.ticker1 = ticker1;
    }

    public String getTicker2() {
        return ticker2;
    }
    public void setTicker2(String ticker2) {
        this.ticker2 = ticker2;
    }

    public String getTicker3() {
        return ticker3;
    }
    public void setTicker3(String ticker3) {
        this.ticker3 = ticker3;
    }

    public String getTicker4() {
        return ticker4;
    }
    public void setTicker4(String ticker4) {
        this.ticker4 = ticker4;
    }

    public String getTicker5() {
        return ticker5;
    }
    public void setTicker5(String ticker5) {
        this.ticker5 = ticker5;
    }

    public String getNseCode() {
        return nseCode;
    }
    public void setNseCode(String nseCode) {
        this.nseCode = nseCode;
    }

    public String getBseCode() {
        return bseCode;
    }
    public void setBseCode(String bseCode) {
        this.bseCode = bseCode;
    }

    public String getIsinCode() {
        return isinCode;
    }
    public void setIsinCode(String isinCode) {
        this.isinCode = isinCode;
    }

    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public int getAssetClassid() {
        return assetClassid;
    }
    public void setAssetClassid(int assetClassid) {
        this.assetClassid = assetClassid;
    }

    public String getBseIndustry() {
        return bseIndustry;
    }
    public void setBseIndustry(String bseIndustry) {
        this.bseIndustry = bseIndustry;
    }

    public int getSubindustryid() {
        return subindustryid;
    }
    public void setSubindustryid(int subindustryid) {
        this.subindustryid = subindustryid;
    }

    public BigDecimal getLatestPrice() {
        return latestPrice;
    }
    public void setLatestPrice(BigDecimal latestPrice) {
        this.latestPrice = latestPrice;
    }

    public Date getDateLatestPrice() {
        return dateLatestPrice;
    }
    public void setDateLatestPrice(Date dateLatestPrice) {
        this.dateLatestPrice = dateLatestPrice;
    }

    public int getIsSensex() {
        return isSensex;
    }
    public void setIsSensex(int isSensex) {
        this.isSensex = isSensex;
    }

    public int getIsNifty50() {
        return isNifty50;
    }
    public void setIsNifty50(int isNifty50) {
        this.isNifty50 = isNifty50;
    }

    public int getIsNiftyjr() {
        return isNiftyjr;
    }
    public void setIsNiftyjr(int isNiftyjr) {
        this.isNiftyjr = isNiftyjr;
    }

    public int getIsBse100() {
        return isBse100;
    }
    public void setIsBse100(int isBse100) {
        this.isBse100 = isBse100;
    }

    public int getIsNse100() {
        return isNse100;
    }
    public void setIsNse100(int isNse100) {
        this.isNse100 = isNse100;
    }

    public int getIsBse200() {
        return isBse200;
    }
    public void setIsBse200(int isBse200) {
        this.isBse200 = isBse200;
    }

    public int getIsNse200() {
        return isNse200;
    }
    public void setIsNse200(int isNse200) {
        this.isNse200 = isNse200;
    }

    public int getIsBse500() {
        return isBse500;
    }
    public void setIsBse500(int isBse500) {
        this.isBse500 = isBse500;
    }

    public int getIsNse500() {
        return isNse500;
    }
    public void setIsNse500(int isNse500) {
        this.isNse500 = isNse500;
    }

    public BigDecimal getMarketcap() {
        return marketcap;
    }
    public void setMarketcap(BigDecimal marketcap) {
        this.marketcap = marketcap;
    }

    public int getMarketcapRank() {
        return marketcapRank;
    }
    public void setMarketcapRank(int marketcapRank) {
        this.marketcapRank = marketcapRank;
    }

    public BigDecimal getPeTtm() {
        return peTtm;
    }
    public void setPeTtm(BigDecimal peTtm) {
        this.peTtm = peTtm;
    }

    public String getTickerOld() {
        return tickerOld;
    }
    public void setTickerOld(String tickerOld) {
        this.tickerOld = tickerOld;
    }

    public Date getListingDate() {
        return listingDate;
    }
    public void setListingDate(Date listingDate) {
        this.listingDate = listingDate;
    }
}
