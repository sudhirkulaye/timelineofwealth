package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.StockQuarter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface StockQuarterRepository  extends JpaRepository<StockQuarter, StockQuarter.StockQuarterKey> {
    public StockQuarter findAllByKeyTickerAndKeyConsStandaloneAndKeyDate(String ticker, String consStandalone, Date date);

    @Query("select distinct stockQuarter.key.date from StockQuarter stockQuarter WHERE stockQuarter.key.ticker= :ticker Order By stockQuarter.key.date")
    List<Date> findDistinctDatesForTicker(@Param("ticker")String ticker);

    @Query("select distinct stockQuarter.resultDate from StockQuarter stockQuarter WHERE stockQuarter.key.ticker= :ticker and stockQuarter.resultDate >= :resultDate Order By stockQuarter.resultDate")
    List<Date> findDistinctResultDateForTicker(@Param("ticker")String ticker, @Param("resultDate")Date date);

}
