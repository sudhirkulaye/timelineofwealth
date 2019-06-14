package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.MutualFundNavHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface MutualFundNavHistoryRepository extends JpaRepository<MutualFundNavHistory, MutualFundNavHistory.MutualFundNavHistoryKey> {
    @Query(value="select count(1), date from mutual_fund_nav_history " +
            "where date = (select max(date) from mutual_fund_nav_history) " +
            "group by date order by date desc ", nativeQuery = true)
    public List<Object[]> findMaxDateAndCount();
}
