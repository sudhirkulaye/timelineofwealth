package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "portfolio_historical_holdings")
public class PortfolioHistoricalHoldings implements Serializable {
    @Embeddable
    public static class PortfolioHistoricalHoldingsKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "portfolioid")
        private int portfolioid;
        @Column(name = "buy_date")
        private Date buyDate;
        @Column(name = "ticker")
        private String ticker;
        @Column(name = "sell_date")
        private Date sellDate;

        public long getMemberid() {
            return memberid;
        }
        public void setMemberid(long memberid) {
            this.memberid = memberid;
        }

        public int getPortfolioid() {
            return portfolioid;
        }
        public void setPortfolioid(int portfolioid) {
            this.portfolioid = portfolioid;
        }

        public Date getBuyDate() {
            return buyDate;
        }
        public void setBuyDate(Date buyDate) {
            this.buyDate = buyDate;
        }

        public String getTicker() {
            return ticker;
        }
        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public Date getSellDate() {
            return sellDate;
        }
        public void setSellDate(Date sellDate) {
            this.sellDate = sellDate;
        }
    }

    @EmbeddedId
    private PortfolioHistoricalHoldingsKey key;
    @Column(name = "name")
    private String name;
    @Column(name = "short_name")
    private String shortName;
    @Column(name = "asset_classid")
    private int assetClassid;
    @Column(name = "subindustryid")
    private int subindustryid;
    @Column(name = "quantity")
    private BigDecimal quantity;
    @Column(name = "rate")
    private BigDecimal rate;
    @Column(name = "brokerage")
    private BigDecimal brokerage;
    @Column(name = "tax")
    private BigDecimal tax;
    @Column(name = "total_cost")
    private BigDecimal totalCost;
    @Column(name = "net_rate")
    private BigDecimal netRate;
    @Column(name = "sell_rate")
    private BigDecimal sellRate;
    @Column(name = "brokerage_sell")
    private BigDecimal brokerageSell;
    @Column(name = "tax_sell")
    private BigDecimal taxSell;
    @Column(name = "net_sell")
    private BigDecimal netSell;
    @Column(name = "net_sell_rate")
    private BigDecimal netSellRate;
    @Column(name = "holding_period")
    private BigDecimal holdingPeriod;
    @Column(name = "net_profit")
    private BigDecimal netProfit;
    @Column(name = "absolute_return")
    private BigDecimal absoluteReturn;
    @Column(name = "annualized_return")
    private BigDecimal annualizedReturn;
    @Column(name = "fin_year")
    private String finYear;

    public PortfolioHistoricalHoldingsKey getKey() {
        return key;
    }
    public void setKey(PortfolioHistoricalHoldingsKey key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    public String getShortName() {
        return shortName;
    }
    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public int getAssetClassid() {
        return assetClassid;
    }
    public void setAssetClassid(int assetClassid) {
        this.assetClassid = assetClassid;
    }

    public int getSubindustryid() {
        return subindustryid;
    }
    public void setSubindustryid(int subindustryid) {
        this.subindustryid = subindustryid;
    }

    public BigDecimal getQuantity() {
        return quantity;
    }
    public void setQuantity(BigDecimal quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getRate() {
        return rate;
    }
    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public BigDecimal getBrokerage() {
        return brokerage;
    }
    public void setBrokerage(BigDecimal brokerage) {
        this.brokerage = brokerage;
    }

    public BigDecimal getTax() {
        return tax;
    }
    public void setTax(BigDecimal tax) {
        this.tax = tax;
    }

    public BigDecimal getTotalCost() {
        return totalCost;
    }
    public void setTotalCost(BigDecimal totalCost) {
        this.totalCost = totalCost;
    }

    public BigDecimal getNetRate() {
        return netRate;
    }
    public void setNetRate(BigDecimal netRate) {
        this.netRate = netRate;
    }

    public BigDecimal getSellRate() {
        return sellRate;
    }
    public void setSellRate(BigDecimal sellRate) {
        this.sellRate = sellRate;
    }

    public BigDecimal getBrokerageSell() {
        return brokerageSell;
    }
    public void setBrokerageSell(BigDecimal brokerageSell) {
        this.brokerageSell = brokerageSell;
    }

    public BigDecimal getTaxSell() {
        return taxSell;
    }
    public void setTaxSell(BigDecimal taxSell) {
        this.taxSell = taxSell;
    }

    public BigDecimal getNetSell() {
        return netSell;
    }
    public void setNetSell(BigDecimal netSell) {
        this.netSell = netSell;
    }

    public BigDecimal getNetSellRate() {
        return netSellRate;
    }
    public void setNetSellRate(BigDecimal netSellRate) {
        this.netSellRate = netSellRate;
    }

    public BigDecimal getHoldingPeriod() {
        return holdingPeriod;
    }
    public void setHoldingPeriod(BigDecimal holdingPeriod) {
        this.holdingPeriod = holdingPeriod;
    }

    public BigDecimal getNetProfit() {
        return netProfit;
    }
    public void setNetProfit(BigDecimal netProfit) {
        this.netProfit = netProfit;
    }

    public BigDecimal getAbsoluteReturn() {
        return absoluteReturn;
    }
    public void setAbsoluteReturn(BigDecimal absoluteReturn) {
        this.absoluteReturn = absoluteReturn;
    }

    public BigDecimal getAnnualizedReturn() {
        return annualizedReturn;
    }
    public void setAnnualizedReturn(BigDecimal annualizedReturn) {
        this.annualizedReturn = annualizedReturn;
    }

    public String getFinYear() {
        return finYear;
    }
    public void setFinYear(String finYear) {
        this.finYear = finYear;
    }
}
