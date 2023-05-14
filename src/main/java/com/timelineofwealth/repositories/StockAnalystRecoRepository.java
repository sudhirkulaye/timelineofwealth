package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.StockAnalystReco;
import com.timelineofwealth.entities.StockQuarter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface StockAnalystRecoRepository extends JpaRepository<StockAnalystReco, StockAnalystReco.StockAnalystRecoKey> {
    public StockAnalystReco findAllByKeyTickerAndKeyQuarterAndKeyBrokerOrderByKeyQuarterDesc(String ticker, String quarter, String broker);

    @Query("SELECT MAX(s.key.quarter) FROM StockAnalystReco s")
    public String findMaxKeyQuarter();

    public List<StockAnalystReco> findAllByKeyTickerAndKeyBrokerAndKeyQuarterIn(String ticker, String broker, List<String> quarters);
}
