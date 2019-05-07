package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.StockPriceMovementHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface StockPriceMovementHistoryRepository extends JpaRepository<StockPriceMovementHistory, StockPriceMovementHistory.StockPriceMovementHistoryKey> {
    public List<StockPriceMovementHistory> findAllByKeyTickerAndKeyDateGreaterThanOrderByKeyDateAsc(String ticker, Date date);
}
