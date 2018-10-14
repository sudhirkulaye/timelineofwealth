package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.Liability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface LiabilityRepository extends JpaRepository<Liability, Liability.LiabilityKey> {

    public List<Liability> findByKeyMemberidInOrderByKeyMemberidAscKeyLoanidAsc(List<Long> memberids);
    public int countByKeyMemberid(long memberid);
}
