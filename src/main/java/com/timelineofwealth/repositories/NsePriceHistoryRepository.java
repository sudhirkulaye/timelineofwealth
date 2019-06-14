package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.NsePriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.sql.Date;
import java.util.List;

public interface NsePriceHistoryRepository extends JpaRepository<NsePriceHistory, NsePriceHistory.NsePriceHistoryKey> {
    @Query(value="select count(1), date from nse_price_history " +
            "where date = (select max(date) from nse_price_history) " +
            "group by date order by date desc ", nativeQuery = true)
    public List<Object[]> findMaxDateAndCount();
}
