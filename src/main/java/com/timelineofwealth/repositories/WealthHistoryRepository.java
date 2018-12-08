package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.WealthHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WealthHistoryRepository extends JpaRepository<WealthHistory,WealthHistory.WealthHistoryKey> {
    public List<WealthHistory> findByKeyMemberidInOrderByKeyDateAscKeyMemberid(List<Long> memberids);
}
