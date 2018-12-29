package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.IndexValuation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface IndexValuationRepository extends JpaRepository<IndexValuation, IndexValuation.IndexValuationKey> {
    public List<IndexValuation> findAllByKeyTickerOrderByKeyDate(String ticker);
    public List<IndexValuation> findAllByKeyTickerAndKeyDateBetweenOrderByKeyDate(String ticker, Date startDate, Date endDate);
}