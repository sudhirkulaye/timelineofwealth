package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.Sip;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface SipRepository extends JpaRepository<Sip, Sip.SipKey> {
    public List<Sip> findByKeyMemberidInOrderByKeyMemberidAscKeySipidAsc(List<Long> memberids);
    public List<Sip> findByKeyMemberidAndSchemeCodeOrderByKeySipid(long memberids, long schemeCode);
    public int countByKeyMemberid(long memberid);
    public Sip findTopByKeyMemberidOrderByKeySipidDesc(Long memberid);
}
