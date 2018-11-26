package com.timelineofwealth.dto;

import java.io.Serializable;
import java.util.List;

public class DailyDataBJSON implements Serializable {
    private List<Ticker> tickers;

    public List<Ticker> getTickers() {
        return tickers;
    }
    public void setTickers(List<Ticker> tickers) {
        this.tickers = tickers;
    }

}
