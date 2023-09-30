package com.timelineofwealth.repositories;


import com.timelineofwealth.entities.Composite;
import com.timelineofwealth.entities.MoslcodeMemberid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface MoslcodeMemberidRepository extends JpaRepository<MoslcodeMemberid, Integer> {

    public MoslcodeMemberid findByMemberid(long memberid);
    public List<MoslcodeMemberid> findAll();
}
