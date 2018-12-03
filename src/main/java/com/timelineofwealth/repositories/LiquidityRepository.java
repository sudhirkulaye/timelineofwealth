package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.Liquidity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface LiquidityRepository extends JpaRepository<Liquidity, Liquidity.LiquidityKey> {
    public List<Liquidity> findByKeyMemberidInOrderByExpectedStartDateAsc(List<Long> memberids);
    public int countByKeyMemberid(long memberid);
    public Liquidity findTopByKeyMemberidOrderByKeyLiquidityidDesc(Long memberid);
}
