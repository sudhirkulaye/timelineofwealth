package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.WealthAssetAllocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WealthAssetAllocationHistoryRepository extends JpaRepository<WealthAssetAllocationHistory, WealthAssetAllocationHistory.WealthAssetAllocationHistoryKey> {
    public List<WealthAssetAllocationHistory> findAllByKeyMemberidOrderByKeyDate(long memberid);
}
