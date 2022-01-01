package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.PortfolioHoldings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioHoldingsRepository  extends JpaRepository<PortfolioHoldings,PortfolioHoldings.PortfolioHoldingsKey> {

    public List<PortfolioHoldings> findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscAssetClassidAscShortNameAscKeyBuyDateDesc(List<Long> memberids);
    @Query(value="SELECT a.memberid, a.portfolioid, a.short_name, sum(a.quantity), sum(a.total_cost), " +
            "sum(a.market_value), sum(a.net_profit), (sum(a.market_value)/max(b.market_value)*100), ((sum(a.net_profit)/sum(a.total_cost))*100) " +
            "FROM portfolio_holdings a, portfolio b " +
            "WHERE a.portfolioid = b.portfolioid " +
            "AND a.memberid = b.memberid " +
            "And a.memberid IN (:memberids) " +
            "GROUP BY a.memberid, a.portfolioid, a.ticker " +
            "ORDER BY memberid, portfolioid,sum(a.market_value) desc; ", nativeQuery = true)
    public List<Object[]> getConsolidatedPortfolioHoldings(@Param("memberids")List<Long> memberids);

}
