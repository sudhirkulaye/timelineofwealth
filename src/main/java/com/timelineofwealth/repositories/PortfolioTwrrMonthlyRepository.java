package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.PortfolioTwrrMonthly;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioTwrrMonthlyRepository extends JpaRepository<PortfolioTwrrMonthly, PortfolioTwrrMonthly.PortfolioTwrrMonthlyKey> {
    public List<PortfolioTwrrMonthly> findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyReturnsYearDesc(List<Long> memberids);
}
