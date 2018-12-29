package com.timelineofwealth.repositories;


import com.timelineofwealth.entities.AdviserUserMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdviserUserMappingRepository extends JpaRepository<AdviserUserMapping, AdviserUserMapping.AdviserUserMappingKey> {
    public List<AdviserUserMapping> findByKeyAdviseridOrderByKeyUseridAsc(String adviserid);
    //@Query(value = "select count(1) from adviser_user_mapping a where adviserid = ?1 and userid = ?2", nativeQuery = true)
    public int countByKeyAdviseridAndKeyUserid(String adviserid, String clientid);
    //@Query(value = "select * from adviser_user_mapping a where adviserid = ?1 and userid = ?2", nativeQuery = true)
    public AdviserUserMapping findOneByKeyAdviseridAndKeyUserid(String adviserid, String clientid);
}
