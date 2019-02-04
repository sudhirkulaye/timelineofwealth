package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Date;

@Entity
@Table(name = "stock_balancesheet")
public class StockBalancesheet implements Serializable {
    @Embeddable
    public static class StockBalancesheetKey implements Serializable {
        @Column(name = "ticker")
        private String ticker;
        @Column(name = "cons_standalone")
        private String consStandalone;
        @Column(name = "date")
        private Date date;

        public String getTicker() {
            return ticker;
        }
        public void setTicker(String ticker) {
            this.ticker = ticker;
        }

        public String getConsStandalone() {
            return consStandalone;
        }
        public void setConsStandalone(String consStandalone) {
            this.consStandalone = consStandalone;
        }

        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }
    }

    @EmbeddedId
    private StockBalancesheetKey key;
    @Column(name = "equity_share_capital")
    private BigDecimal equityShareCapital;
    @Column(name = "reserves")
    private BigDecimal reserves;
    @Column(name = "borrowings")
    private BigDecimal borrowings;
    @Column(name = "other_liabilities")
    private BigDecimal otherLiabilities;
    @Column(name = "total_equity_and_debt")
    private BigDecimal totalEquityAndDebt;
    @Column(name = "dummy1")
    private BigDecimal dummy1;
    @Column(name = "net_block")
    private BigDecimal netBlock;
    @Column(name = "capital_work_in_progress")
    private BigDecimal capitalWorkInProgress;
    @Column(name = "investments")
    private BigDecimal investments;
    @Column(name = "other_assets")
    private BigDecimal otherAssets;
    @Column(name = "total_assets")
    private BigDecimal totalAssets;
    @Column(name = "capex")
    private BigDecimal capex;
    @Column(name = "working_capital")
    private BigDecimal workingCapital;
    @Column(name = "debtors")
    private BigDecimal debtors;
    @Column(name = "inventory")
    private BigDecimal inventory;
    @Column(name = "dummy2")
    private BigDecimal dummy2;
    @Column(name = "debtor_days")
    private BigDecimal debtorDays;
    @Column(name = "inventory_turnover")
    private BigDecimal inventoryTurnover;
    @Column(name = "dummy3")
    private BigDecimal dummy3;
    @Column(name = "return_on_equity")
    private BigDecimal returnOnEquity;
    @Column(name = "return_on_capital_emp")
    private BigDecimal returnOnCapitalEmp;

    public StockBalancesheetKey getKey() {
        return key;
    }
    public void setKey(StockBalancesheetKey key) {
        this.key = key;
    }

    public BigDecimal getEquityShareCapital() {
        return equityShareCapital;
    }
    public void setEquityShareCapital(BigDecimal equityShareCapital) {
        this.equityShareCapital = equityShareCapital;
    }

    public BigDecimal getReserves() {
        return reserves;
    }
    public void setReserves(BigDecimal reserves) {
        this.reserves = reserves;
    }

    public BigDecimal getBorrowings() {
        return borrowings;
    }
    public void setBorrowings(BigDecimal borrowings) {
        this.borrowings = borrowings;
    }

    public BigDecimal getOtherLiabilities() {
        return otherLiabilities;
    }
    public void setOtherLiabilities(BigDecimal otherLiabilities) {
        this.otherLiabilities = otherLiabilities;
    }

    public BigDecimal getTotalEquityAndDebt() {
        return totalEquityAndDebt;
    }
    public void setTotalEquityAndDebt(BigDecimal totalEquityAndDebt) {
        this.totalEquityAndDebt = totalEquityAndDebt;
    }

    public BigDecimal getDummy1() {
        return dummy1;
    }
    public void setDummy1(BigDecimal dummy1) {
        this.dummy1 = dummy1;
    }

    public BigDecimal getNetBlock() {
        return netBlock;
    }
    public void setNetBlock(BigDecimal netBlock) {
        this.netBlock = netBlock;
    }

    public BigDecimal getCapitalWorkInProgress() {
        return capitalWorkInProgress;
    }
    public void setCapitalWorkInProgress(BigDecimal capitalWorkInProgress) {
        this.capitalWorkInProgress = capitalWorkInProgress;
    }

    public BigDecimal getInvestments() {
        return investments;
    }
    public void setInvestments(BigDecimal investments) {
        this.investments = investments;
    }

    public BigDecimal getOtherAssets() {
        return otherAssets;
    }
    public void setOtherAssets(BigDecimal otherAssets) {
        this.otherAssets = otherAssets;
    }

    public BigDecimal getTotalAssets() {
        return totalAssets;
    }
    public void setTotalAssets(BigDecimal totalAssets) {
        this.totalAssets = totalAssets;
    }

    public BigDecimal getCapex() {
        return capex;
    }
    public void setCapex(BigDecimal capex) {
        this.capex = capex;
    }

    public BigDecimal getWorkingCapital() {
        return workingCapital;
    }
    public void setWorkingCapital(BigDecimal workingCapital) {
        this.workingCapital = workingCapital;
    }

    public BigDecimal getDebtors() {
        return debtors;
    }
    public void setDebtors(BigDecimal debtors) {
        this.debtors = debtors;
    }

    public BigDecimal getInventory() {
        return inventory;
    }
    public void setInventory(BigDecimal inventory) {
        this.inventory = inventory;
    }

    public BigDecimal getDummy2() {
        return dummy2;
    }
    public void setDummy2(BigDecimal dummy2) {
        this.dummy2 = dummy2;
    }

    public BigDecimal getDebtorDays() {
        return debtorDays;
    }
    public void setDebtorDays(BigDecimal debtorDays) {
        this.debtorDays = debtorDays;
    }

    public BigDecimal getInventoryTurnover() {
        return inventoryTurnover;
    }
    public void setInventoryTurnover(BigDecimal inventoryTurnover) {
        this.inventoryTurnover = inventoryTurnover;
    }

    public BigDecimal getDummy3() {
        return dummy3;
    }
    public void setDummy3(BigDecimal dummy3) {
        this.dummy3 = dummy3;
    }

    public BigDecimal getReturnOnEquity() {
        return returnOnEquity;
    }
    public void setReturnOnEquity(BigDecimal returnOnEquity) {
        this.returnOnEquity = returnOnEquity;
    }

    public BigDecimal getReturnOnCapitalEmp() {
        return returnOnCapitalEmp;
    }
    public void setReturnOnCapitalEmp(BigDecimal returnOnCapitalEmp) {
        this.returnOnCapitalEmp = returnOnCapitalEmp;
    }

}

