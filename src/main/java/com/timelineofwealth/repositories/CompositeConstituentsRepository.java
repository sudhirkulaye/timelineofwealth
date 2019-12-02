package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.CompositeConstituents;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompositeConstituentsRepository extends JpaRepository<CompositeConstituents, Long> {
    public List<CompositeConstituents> findAll();
    public List<CompositeConstituents> findAllByKeyCompositeidInOrderByTargetWeightDesc(List<Long> compositeids);
    public int countByKeyCompositeidAndKeyTicker(long compositeid, String ticker);
}
