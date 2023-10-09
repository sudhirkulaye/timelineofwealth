package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.IndexMonthlyReturns;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndexMonthlyReturnsRepository extends JpaRepository<IndexMonthlyReturns, IndexMonthlyReturns.IndexMonthlyReturnsKey> {
    public List<IndexMonthlyReturns> findAllByKeyTickerOrderByKeyYearDesc(String ticker);
}