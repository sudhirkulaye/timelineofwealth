package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.WealthAssetAllocationHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface WealthAssetAllocationHistoryRepository extends JpaRepository<WealthAssetAllocationHistory, WealthAssetAllocationHistory.WealthAssetAllocationHistoryKey> {
    public List<WealthAssetAllocationHistory> findAllByKeyMemberidInAndKeyDateOrderByKeyMemberidAscKeyAssetClassGroupAsc(List<Long> memberids, Date date);
    public List<WealthAssetAllocationHistory> findAllByKeyMemberidOrderByKeyMemberidAscKeyDateAscKeyAssetClassGroupAsc(List<Long> memberids);
}
