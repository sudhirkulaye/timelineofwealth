package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.MutualFundNavHistory;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MutualFundNavHistoryRepository extends JpaRepository<MutualFundNavHistory, MutualFundNavHistory.MutualFundNavHistoryKey> {

}
