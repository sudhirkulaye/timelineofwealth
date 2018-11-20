package com.timelineofwealth.entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.sql.Date;

@Entity
@Table(name = "setup_dates")
public class SetupDates implements Serializable {

    @Id
    @Column(name = "date_today")
    private Date dateToday;
    @Column(name = "date_last_trading_day")
    private Date dateLastTradingDay;
    @Column(name = "date_start_current_month")
    private Date dateStartCurrentMonth;
    @Column(name = "date_start_current_quarter")
    private Date dateStartCurrentQuarter;
    @Column(name = "date_start_current_fin_year")
    private Date dateStartCurrentFinYear;
    @Column(name = "date_start_1_quarter")
    private Date dateStart1Quarter;
    @Column(name = "date_start_2_quarter")
    private Date dateStart2Quarter;
    @Column(name = "date_start_3_quarter")
    private Date dateStart3Quarter;
    @Column(name = "date_start_4_quarter")
    private Date dateStart4Quarter;
    @Column(name = "date_start_next_fin_year")
    private Date dateStartNextFinYear;
    @Column(name = "current_fin_year")
    private int currentFinYear;
    @Column(name = "current_quarter")
    private int currentQuarter;

    public SetupDates(){}

    public Date getDateToday() {
        return dateToday;
    }
    public void setDateToday(Date dateToday) {
        this.dateToday = dateToday;
    }

    public Date getDateLastTradingDay() {
        return dateLastTradingDay;
    }
    public void setDateLastTradingDay(Date dateLastTradingDay) {
        this.dateLastTradingDay = dateLastTradingDay;
    }

    public Date getDateStartCurrentMonth() {
        return dateStartCurrentMonth;
    }
    public void setDateStartCurrentMonth(Date dateStartCurrentMonth) {
        this.dateStartCurrentMonth = dateStartCurrentMonth;
    }

    public Date getDateStartCurrentQuarter() {
        return dateStartCurrentQuarter;
    }
    public void setDateStartCurrentQuarter(Date dateStartCurrentQuarter) {
        this.dateStartCurrentQuarter = dateStartCurrentQuarter;
    }

    public Date getDateStartCurrentFinYear() {
        return dateStartCurrentFinYear;
    }
    public void setDateStartCurrentFinYear(Date dateStartCurrentFinYear) {
        this.dateStartCurrentFinYear = dateStartCurrentFinYear;
    }

    public Date getDateStart1Quarter() {
        return dateStart1Quarter;
    }
    public void setDateStart1Quarter(Date dateStart1Quarter) {
        this.dateStart1Quarter = dateStart1Quarter;
    }

    public Date getDateStart2Quarter() {
        return dateStart2Quarter;
    }
    public void setDateStart2Quarter(Date dateStart2Quarter) {
        this.dateStart2Quarter = dateStart2Quarter;
    }

    public Date getDateStart3Quarter() {
        return dateStart3Quarter;
    }
    public void setDateStart3Quarter(Date dateStart3Quarter) {
        this.dateStart3Quarter = dateStart3Quarter;
    }

    public Date getDateStart4Quarter() {
        return dateStart4Quarter;
    }
    public void setDateStart4Quarter(Date dateStart4Quarter) {
        this.dateStart4Quarter = dateStart4Quarter;
    }

    public Date getDateStartNextFinYear() {
        return dateStartNextFinYear;
    }
    public void setDateStartNextFinYear(Date dateStartNextFinYear) {
        this.dateStartNextFinYear = dateStartNextFinYear;
    }

    public int getCurrentFinYear() {
        return currentFinYear;
    }
    public void setCurrentFinYear(int currentFinYear) {
        this.currentFinYear = currentFinYear;
    }

    public int getCurrentQuarter() {
        return currentQuarter;
    }
    public void setCurrentQuarter(int currentQuarter) {
        this.currentQuarter = currentQuarter;
    }

}
