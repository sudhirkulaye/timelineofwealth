package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "mosl_transaction")
public class MOSLTransaction implements Serializable {

    @Embeddable
    public static class MOSLTransactionKey implements Serializable {
        @Column(name = "moslcode")
        private String moslCode;
        @Column(name = "date")
        private Date date;
        @Column(name = "script_name")
        private String scriptName;
        @Column(name = "sell_buy")
        private String sellBuy;
        @Column(name = "order_no")
        private String orderNo;
        @Column(name = "trade_no")
        private String tradeNo;
        @Column(name = "portfolioid")
        private int portfolioid;

        public MOSLTransactionKey() {}

        public String getMoslCode() {
            return moslCode;
        }
        public void setMoslCode(String moslCode) {
            this.moslCode = moslCode;
        }

        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }

        public String getScriptName() {
            return scriptName;
        }
        public void setScriptName(String scriptName) {
            this.scriptName = scriptName;
        }

        public String getSellBuy() {
            return sellBuy;
        }
        public void setSellBuy(String sellBuy) {
            this.sellBuy = sellBuy;
        }

        public String getOrderNo() {
            return orderNo;
        }
        public void setOrderNo(String orderNo) {
            this.orderNo = orderNo;
        }

        public String getTradeNo() {
            return tradeNo;
        }
        public void setTradeNo(String tradeNo) {
            this.tradeNo = tradeNo;
        }

        public int getPortfolioid() {
            return portfolioid;
        }
        public void setPortfolioid(int portfolioid) {
            this.portfolioid = portfolioid;
        }
    }
    @EmbeddedId
    private MOSLTransactionKey key;

    @Column(name = "exchange")
    private String exchange;
    @Column(name = "quantity")
    private BigDecimal quantity;
    @Column(name = "rate")
    private BigDecimal rate;
    @Column(name = "amount")
    private BigDecimal amount;
    @Column(name = "brokerage")
    private BigDecimal brokerage;
    @Column(name = "txn_charges")
    private BigDecimal txnCharges;
    @Column(name = "service_tax")
    private BigDecimal serviceTax;
    @Column(name = "stamp_duty")
    private BigDecimal stampDuty;
    @Column(name = "stt_ctt")
    private BigDecimal sttCtt;
    @Column(name = "net_rate")
    private BigDecimal netRate;
    @Column(name = "net_amount")
    private BigDecimal netAmount;
    @Column(name = "is_processed")
    private String isProcessed;


    public MOSLTransactionKey getKey() {
        return key;
    }
    public void setKey(MOSLTransactionKey key) {
        this.key = key;
    }

    public String getExchange() {
        return exchange;
    }
    public void setExchange(String exchange) {
        this.exchange = exchange;
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

    public BigDecimal getAmount() {
        return amount;
    }
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getBrokerage() {
        return brokerage;
    }
    public void setBrokerage(BigDecimal brokerage) {
        this.brokerage = brokerage;
    }

    public BigDecimal getTxnCharges() {
        return txnCharges;
    }
    public void setTxnCharges(BigDecimal txnCharges) {
        this.txnCharges = txnCharges;
    }

    public BigDecimal getServiceTax() {
        return serviceTax;
    }
    public void setServiceTax(BigDecimal serviceTax) {
        this.serviceTax = serviceTax;
    }

    public BigDecimal getStampDuty() {
        return stampDuty;
    }
    public void setStampDuty(BigDecimal stampDuty) {
        this.stampDuty = stampDuty;
    }

    public BigDecimal getSttCtt() {
        return sttCtt;
    }
    public void setSttCtt(BigDecimal sttCtt) {
        this.sttCtt = sttCtt;
    }

    public BigDecimal getNetRate() {
        return netRate;
    }
    public void setNetRate(BigDecimal netRate) {
        this.netRate = netRate;
    }

    public BigDecimal getNetAmount() {
        return netAmount;
    }
    public void setNetAmount(BigDecimal netAmount) {
        this.netAmount = netAmount;
    }

    public String getIsProcessed() {
        return isProcessed;
    }
    public void setIsProcessed(String isProcessed) {
        this.isProcessed = isProcessed;
    }
}
