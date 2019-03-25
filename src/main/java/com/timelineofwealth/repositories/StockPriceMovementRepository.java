package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.StockPriceMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockPriceMovementRepository extends JpaRepository<StockPriceMovement, String> {
    public StockPriceMovement findOneByTicker(String ticker);
    public List<StockPriceMovement> findAll();
}
