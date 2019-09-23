package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.PortfolioTwrrSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioTwrrSummaryRepository extends JpaRepository<PortfolioTwrrSummary, PortfolioTwrrSummary.PortfolioTwrrSummaryKey> {
    public List<PortfolioTwrrSummary> findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyBenchmarkidAsc(List<Long> memberids);
}
