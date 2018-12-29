package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.WealthDetails;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface WealthDetailsRepository extends JpaRepository<WealthDetails, WealthDetails.WealthDetailsKey> {

    public List<WealthDetails> findByKeyMemberidInOrderByKeyMemberidAscAssetClassidAscKeyTickerAscKeyBuyDateAsc(List<Long> memberids);
    @Query(value = "SELECT w.memberid, w.ticker, w.name, w.short_name, w.asset_classid, w.subindustryid, " +
            "sum(w.quantity), avg(w.rate), sum(w.brokerage), sum(w.tax), sum(w.total_cost), sum(w.total_cost)/sum(quantity), " +
            "cmp, sum(w.market_value), sum(w.net_profit), (sum(w.market_value)/sum(w.total_cost) - 1), 0 " +
            "from  wealth_details w WHERE w.memberid IN ?1 GROUP BY w.memberid, w.ticker ORDER BY w.memberid, w.asset_classid, w.ticker ", nativeQuery = true)
    public List<Object[]> findByKeyMemberidInGroupByKeyMemberidAndKeyTicker(List<Long> memberids);
    public int countByKeyMemberidAndKeyBuyDateAndKeyTicker(long memberid, Date buyDate, String ticker);
    public WealthDetails findByKeyMemberidAndKeyBuyDateAndKeyTicker(long memberid, Date buyDate, String ticker);
    public List<WealthDetails>  findByKeyMemberidAndKeyTicker(long memberid, String ticker);

}
