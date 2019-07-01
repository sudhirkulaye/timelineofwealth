package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.PortfolioHistoricalHoldings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PortfolioHistoricalHoldingsRepository extends JpaRepository<PortfolioHistoricalHoldings,PortfolioHistoricalHoldings.PortfolioHistoricalHoldingsKey> {

    public List<PortfolioHistoricalHoldings> findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeySellDateDesc(List<Long> memberids);


    @Query(value="SELECT memberid, portfolioid, fin_year, sum(net_profit) " +
            "FROM portfolio_historical_holdings a " +
            "WHERE holding_period >= 1 " +
            "AND memberid IN ( :memberids ) " +
            "GROUP BY memberid, portfolioid, fin_year " +
            "ORDER BY memberid, portfolioid, fin_year desc;", nativeQuery = true)
    public List<Object[]> getLongTermProfit(@Param("memberids")List<Long> memberids);

    @Query(value="SELECT memberid, portfolioid, fin_year, sum(net_profit) " +
            "FROM portfolio_historical_holdings a " +
            "WHERE holding_period < 1 " +
            "AND memberid IN ( :memberids ) " +
            "GROUP BY memberid, portfolioid, fin_year " +
            "ORDER BY memberid, portfolioid, fin_year desc;", nativeQuery = true)
    public List<Object[]> getShortTermProfit(@Param("memberids")List<Long> memberids);
}
