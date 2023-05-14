package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.StockValuation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockValuationRepository extends JpaRepository<StockValuation, StockValuation.StockValuationKey> {

    @Query("SELECT MAX(s.key.quarter) FROM StockValuation s")
    public String findMaxKeyQuarter();

    public List<StockValuation> findAllByKeyTickerAndKeyQuarterIn(String ticker, List<String> quarters);
}
