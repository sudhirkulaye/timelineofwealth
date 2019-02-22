package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.StockPnl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface StockPnlRepository  extends JpaRepository<StockPnl, StockPnl.StockPnlKey> {
    public StockPnl findAllByKeyTickerAndKeyConsStandaloneAndKeyDate(String ticker, String consStandalone, Date date);

    @Query("select distinct stockPnl.key.date from StockPnl stockPnl WHERE stockPnl.key.ticker= :ticker Order By stockPnl.key.date")
    List<Date> findDistinctDatesForTicker(@Param("ticker")String ticker);
}
