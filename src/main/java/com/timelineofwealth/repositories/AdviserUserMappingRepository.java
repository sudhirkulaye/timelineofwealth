package com.timelineofwealth.repositories;


import com.timelineofwealth.entities.AdviserUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdviserUserMappingRepository extends JpaRepository<AdviserUserMapping, AdviserUserMapping.AdviserUserMappingKey> {
    public List<AdviserUserMapping> findByKeyAdviseridOrderByKeyUseridAsc(String adviserid);
}
