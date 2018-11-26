package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.NsePriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NsePriceHistoryRepository extends JpaRepository<NsePriceHistory, NsePriceHistory.NsePriceHistoryKey> {

}
