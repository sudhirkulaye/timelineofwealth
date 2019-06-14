package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.BsePriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BsePriceHistoryRepository extends JpaRepository<BsePriceHistory, BsePriceHistory.BsePriceHistoryKey> {
    @Query(value="select count(1), date from bse_price_history " +
            "where date = (select max(date) from bse_price_history) " +
            "group by date order by date desc ", nativeQuery = true)
    public List<Object[]> findMaxDateAndCount();
}
