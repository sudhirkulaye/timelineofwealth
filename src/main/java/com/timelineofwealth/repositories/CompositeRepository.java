package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.Composite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CompositeRepository extends JpaRepository<Composite,Long> {
    public List<Composite> findAll();
    public int countByFundManagerEmailAndAdviserMemberid(String email, long memberid);
    public List<Composite> findByFundManagerEmail(String email);

}
