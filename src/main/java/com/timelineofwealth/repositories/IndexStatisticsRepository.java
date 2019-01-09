package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.IndexStatistics;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface IndexStatisticsRepository extends JpaRepository<IndexStatistics, String> {
//    @Cacheable("IndexStatistics")
    public List<IndexStatistics> findAll();
}
