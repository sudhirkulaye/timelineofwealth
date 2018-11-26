package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.BsePriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BsePriceHistoryRepository extends JpaRepository<BsePriceHistory, BsePriceHistory.BsePriceHistoryKey> {
}
