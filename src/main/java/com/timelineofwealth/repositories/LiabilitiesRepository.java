package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.Liabilities;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface LiabilitiesRepository extends JpaRepository<Liabilities, Liabilities.LiabilitiesKey> {

    public List<Liabilities> findByKeyMemberidInOrderByKeyMemberidAscKeyLoanidAsc(List<Long> memberids);
    public int countByKeyMemberid(long memberid);
}
