package com.timelineofwealth.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "income_expense_savings")
public class IncomeExpenseSavings implements Serializable {

    @Embeddable
    public static class IncomeExpenseSavingsKey implements Serializable {
        @Column(name = "memberid")
        private long memberid;
        @Column(name = "finyear")
        private int finyear;

        public long getMemberid() {
            return memberid;
        }
        public void setMemberid(long memberid) {
            this.memberid = memberid;
        }

        public int getFinyear() {
            return finyear;
        }
        public void setFinyear(int finyear) {
            this.finyear = finyear;
        }
    }

    @EmbeddedId
    private IncomeExpenseSavingsKey key;

    @Column(name = "regular_income")
    private BigDecimal regularIncome;
    @Column(name = "interest_dividend_income")
    private BigDecimal interestDividendIncome;
    @Column(name = "rent_income")
    private BigDecimal rentIncome;
    @Column(name = "other_income")
    private BigDecimal otherIncome;
    @Column(name = "gross_total_income")
    private BigDecimal grossTotalIncome;
    @Column(name = "income_tax")
    private BigDecimal incomeTax;
    @Column(name = "net_income")
    private BigDecimal netIncome;
    @Column(name = "tax_rate")
    private BigDecimal taxRate;
    @Column(name = "investment_total")
    private BigDecimal investmentTotal;
    @Column(name = "investment_increase_in_bankbalance")
    private BigDecimal investmentIncreaseInBankbalance;
    @Column(name = "investment_tax_savings")
    private BigDecimal investmentTaxSavings;
    @Column(name = "investment_in_equity")
    private BigDecimal investmentInEquity;
    @Column(name = "investment_in_fixed_income")
    private BigDecimal investmentInFixedIncome;
    @Column(name = "investment_in_other")
    private BigDecimal investmentInOther;
    @Column(name = "gross_total_expenses")
    private BigDecimal grossTotalExpenses;
    @Column(name = "infrequent_total_expenses")
    private BigDecimal infrequentTotalExpenses;
    @Column(name = "infrequent_medical_expenses")
    private BigDecimal infrequentMedicalExpenses;
    @Column(name = "infrequent_renovation_expenses")
    private BigDecimal infrequentRenovationExpenses;
    @Column(name = "infrequent_other_expenses")
    private BigDecimal infrequentOtherExpenses;
    @Column(name = "annual_liability")
    private BigDecimal annualLiability;
    @Column(name = "normalized_regular_expenses")
    private BigDecimal normalizedRegularExpenses;
    @Column(name = "adjustment")
    private BigDecimal adjustment;
    @Column(name = "note")
    private String note;

    public IncomeExpenseSavingsKey getKey() {
        return key;
    }
    public void setKey(IncomeExpenseSavingsKey key) {
        this.key = key;
    }

    public BigDecimal getRegularIncome() {
        return regularIncome;
    }
    public void setRegularIncome(BigDecimal regularIncome) {
        this.regularIncome = regularIncome;
    }

    public BigDecimal getInterestDividendIncome() {
        return interestDividendIncome;
    }
    public void setInterestDividendIncome(BigDecimal interestDividendIncome) {
        this.interestDividendIncome = interestDividendIncome;
    }

    public BigDecimal getRentIncome() {
        return rentIncome;
    }
    public void setRentIncome(BigDecimal rentIncome) {
        this.rentIncome = rentIncome;
    }

    public BigDecimal getOtherIncome() {
        return otherIncome;
    }
    public void setOtherIncome(BigDecimal otherIncome) {
        this.otherIncome = otherIncome;
    }

    public BigDecimal getGrossTotalIncome() {
        return grossTotalIncome;
    }
    public void setGrossTotalIncome(BigDecimal grossTotalIncome) {
        this.grossTotalIncome = grossTotalIncome;
    }

    public BigDecimal getIncomeTax() {
        return incomeTax;
    }
    public void setIncomeTax(BigDecimal incomeTax) {
        this.incomeTax = incomeTax;
    }

    public BigDecimal getNetIncome() {
        return netIncome;
    }
    public void setNetIncome(BigDecimal netIncome) {
        this.netIncome = netIncome;
    }

    public BigDecimal getTaxRate() {
        return taxRate;
    }
    public void setTaxRate(BigDecimal taxRate) {
        this.taxRate = taxRate;
    }

    public BigDecimal getInvestmentTotal() {
        return investmentTotal;
    }
    public void setInvestmentTotal(BigDecimal investmentTotal) {
        this.investmentTotal = investmentTotal;
    }

    public BigDecimal getInvestmentIncreaseInBankbalance() {
        return investmentIncreaseInBankbalance;
    }
    public void setInvestmentIncreaseInBankbalance(BigDecimal investmentIncreaseInBankbalance) {
        this.investmentIncreaseInBankbalance = investmentIncreaseInBankbalance;
    }

    public BigDecimal getInvestmentTaxSavings() {
        return investmentTaxSavings;
    }
    public void setInvestmentTaxSavings(BigDecimal investmentTaxSavings) {
        this.investmentTaxSavings = investmentTaxSavings;
    }

    public BigDecimal getInvestmentInEquity() {
        return investmentInEquity;
    }
    public void setInvestmentInEquity(BigDecimal investmentInEquity) {
        this.investmentInEquity = investmentInEquity;
    }

    public BigDecimal getInvestmentInFixedIncome() {
        return investmentInFixedIncome;
    }
    public void setInvestmentInFixedIncome(BigDecimal investmentInFixedIncome) {
        this.investmentInFixedIncome = investmentInFixedIncome;
    }

    public BigDecimal getInvestmentInOther() {
        return investmentInOther;
    }
    public void setInvestmentInOther(BigDecimal investmentInOther) {
        this.investmentInOther = investmentInOther;
    }

    public BigDecimal getGrossTotalExpenses() {
        return grossTotalExpenses;
    }
    public void setGrossTotalExpenses(BigDecimal grossTotalExpenses) {
        this.grossTotalExpenses = grossTotalExpenses;
    }

    public BigDecimal getInfrequentTotalExpenses() {
        return infrequentTotalExpenses;
    }
    public void setInfrequentTotalExpenses(BigDecimal infrequentTotalExpenses) {
        this.infrequentTotalExpenses = infrequentTotalExpenses;
    }

    public BigDecimal getInfrequentMedicalExpenses() {
        return infrequentMedicalExpenses;
    }
    public void setInfrequentMedicalExpenses(BigDecimal infrequentMedicalExpenses) {
        this.infrequentMedicalExpenses = infrequentMedicalExpenses;
    }

    public BigDecimal getInfrequentRenovationExpenses() {
        return infrequentRenovationExpenses;
    }
    public void setInfrequentRenovationExpenses(BigDecimal infrequentRenovationExpenses) {
        this.infrequentRenovationExpenses = infrequentRenovationExpenses;
    }

    public BigDecimal getInfrequentOtherExpenses() {
        return infrequentOtherExpenses;
    }
    public void setInfrequentOtherExpenses(BigDecimal infrequentOtherExpenses) {
        this.infrequentOtherExpenses = infrequentOtherExpenses;
    }

    public BigDecimal getAnnualLiability() {
        return annualLiability;
    }
    public void setAnnualLiability(BigDecimal annualLiability) {
        this.annualLiability = annualLiability;
    }

    public BigDecimal getNormalizedRegularExpenses() {
        return normalizedRegularExpenses;
    }
    public void setNormalizedRegularExpenses(BigDecimal normalizedRegularExpenses) {
        this.normalizedRegularExpenses = normalizedRegularExpenses;
    }

    public BigDecimal getAdjustment() {
        return adjustment;
    }
    public void setAdjustment(BigDecimal adjustment) {
        this.adjustment = adjustment;
    }

    public String getNote() {
        return note;
    }
    public void setNote(String note) {
        this.note = note;
    }
}
