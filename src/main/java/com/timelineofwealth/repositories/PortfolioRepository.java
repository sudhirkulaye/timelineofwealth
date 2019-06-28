package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioRepository extends JpaRepository<Portfolio,Portfolio.PortfolioKey> {
    public List<Portfolio> findAllByKeyMemberidInAndStatusOrderByKeyPortfolioid(List<Long> memberids, String status);

}
