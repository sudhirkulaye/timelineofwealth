package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.Sip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface SipRepository extends JpaRepository<Sip, Sip.SipKey> {
    public List<Sip> findByKeyMemberidInOrderByKeySipid(List<Long> memberids);
    public int countByKeyMemberid(long memberid);
}
