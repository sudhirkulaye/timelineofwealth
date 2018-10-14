package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.Insurance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface InsuranceRepository extends JpaRepository<Insurance, Insurance.InsuranceKey> {
    //public List<Insurance> findByKeyMemberidInOrderByKeyMemberidAscKeyInsuranceidAsc(List<Long> memberids);
    public List<Insurance> findByKeyMemberidInOrderByExpiryDateAsc(List<Long> memberids);
    public int countByKeyMemberid(long memberid);
}
