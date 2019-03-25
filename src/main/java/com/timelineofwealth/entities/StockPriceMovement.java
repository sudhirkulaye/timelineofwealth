package com.timelineofwealth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "stock_price_movement")
public class StockPriceMovement implements Serializable {

    @Id
    @Column(name = "ticker")
    private String ticker;
    @Column(name = "return_1D")
    private BigDecimal return1D;
    @Column(name = "return_1W")
    private BigDecimal return1W;
    @Column(name = "return_2W")
    private BigDecimal return2W;
    @Column(name = "return_1M")
    private BigDecimal return1M;
    @Column(name = "return_2M")
    private BigDecimal return2M;
    @Column(name = "return_3M")
    private BigDecimal return3M;
    @Column(name = "return_6M")
    private BigDecimal return6M;
    @Column(name = "return_9M")
    private BigDecimal return9M;
    @Column(name = "return_1Y")
    private BigDecimal return1Y;
    @Column(name = "return_2Y")
    private BigDecimal return2Y;
    @Column(name = "return_3Y")
    private BigDecimal return3Y;
    @Column(name = "return_5Y")
    private BigDecimal return5Y;
    @Column(name = "return_10Y")
    private BigDecimal return10Y;
    @Column(name = "return_YTD")
    private BigDecimal returnYTD;

    public String getTicker() {
        return ticker;
    }
    public void setTicker(String ticker) {
        this.ticker = ticker;
    }

    public BigDecimal getReturn1D() {
        return return1D;
    }
    public void setReturn1D(BigDecimal return1D) {
        this.return1D = return1D;
    }

    public BigDecimal getReturn1W() {
        return return1W;
    }
    public void setReturn1W(BigDecimal return1W) {
        this.return1W = return1W;
    }

    public BigDecimal getReturn2W() {
        return return2W;
    }
    public void setReturn2W(BigDecimal return2W) {
        this.return2W = return2W;
    }

    public BigDecimal getReturn1M() {
        return return1M;
    }
    public void setReturn1M(BigDecimal return1M) {
        this.return1M = return1M;
    }

    public BigDecimal getReturn2M() {
        return return2M;
    }
    public void setReturn2M(BigDecimal return2M) {
        this.return2M = return2M;
    }

    public BigDecimal getReturn3M() {
        return return3M;
    }
    public void setReturn3M(BigDecimal return3M) {
        this.return3M = return3M;
    }

    public BigDecimal getReturn6M() {
        return return6M;
    }
    public void setReturn6M(BigDecimal return6M) {
        this.return6M = return6M;
    }

    public BigDecimal getReturn9M() {
        return return9M;
    }
    public void setReturn9M(BigDecimal return9M) {
        this.return9M = return9M;
    }

    public BigDecimal getReturn1Y() {
        return return1Y;
    }
    public void setReturn1Y(BigDecimal return1Y) {
        this.return1Y = return1Y;
    }

    public BigDecimal getReturn2Y() {
        return return2Y;
    }
    public void setReturn2Y(BigDecimal return2Y) {
        this.return2Y = return2Y;
    }

    public BigDecimal getReturn3Y() {
        return return3Y;
    }
    public void setReturn3Y(BigDecimal return3Y) {
        this.return3Y = return3Y;
    }

    public BigDecimal getReturn5Y() {
        return return5Y;
    }
    public void setReturn5Y(BigDecimal return5Y) {
        this.return5Y = return5Y;
    }

    public BigDecimal getReturn10Y() {
        return return10Y;
    }
    public void setReturn10Y(BigDecimal return10Y) {
        this.return10Y = return10Y;
    }

    public BigDecimal getReturnYTD() {
        return returnYTD;
    }
    public void setReturnYTD(BigDecimal returnYTD) {
        this.returnYTD = returnYTD;
    }
}
