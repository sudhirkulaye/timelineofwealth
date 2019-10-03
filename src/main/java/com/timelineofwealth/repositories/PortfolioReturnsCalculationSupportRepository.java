package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.PortfolioReturnsCalculationSupport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioReturnsCalculationSupportRepository  extends JpaRepository<PortfolioReturnsCalculationSupport,PortfolioReturnsCalculationSupport.PortfolioReturnsCalculationSupportKey> {
    public List<PortfolioReturnsCalculationSupport> findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyDateDesc(List<Long> memberids);
}
