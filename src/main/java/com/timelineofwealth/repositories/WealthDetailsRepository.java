package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.WealthDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface WealthDetailsRepository extends JpaRepository<WealthDetails, WealthDetails.WealthDetailsKey> {

    public List<WealthDetails> findByKeyMemberidInOrderByKeyMemberidAscAssetClassidAscKeyTickerAscKeyBuyDateAsc(List<Long> memberids);
    public int countByKeyMemberidAndKeyBuyDateAndKeyTicker(long memberid, Date buyDate, String ticker);
    public WealthDetails findByKeyMemberidAndKeyBuyDateAndKeyTicker(long memberid, Date buyDate, String ticker);
    public List<WealthDetails>  findByKeyMemberidAndKeyTicker(long memberid, String ticker);

}