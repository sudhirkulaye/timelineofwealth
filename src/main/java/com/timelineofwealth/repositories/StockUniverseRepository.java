package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.StockUniverse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
@EnableCaching
public interface StockUniverseRepository  extends JpaRepository<StockUniverse, String> {

    public StockUniverse findByTicker(String ticker);
    public List<StockUniverse> findAll();
    public List<StockUniverse> findAllByIsNse500OrIsBse500OrderByMarketcapDesc(int isNse500, int isBse500);

    /** Top-bar search: ticker prefix or name substring, biggest companies first. Returns [ticker, shortName]. */
    @Query("SELECT s.ticker, s.shortName FROM StockUniverse s " +
           "WHERE UPPER(s.ticker) LIKE CONCAT(:q, '%') OR UPPER(s.name) LIKE CONCAT('%', :q, '%') " +
           "OR UPPER(s.shortName) LIKE CONCAT('%', :q, '%') " +
           "ORDER BY CASE WHEN UPPER(s.ticker) = :q THEN 0 WHEN UPPER(s.ticker) LIKE CONCAT(:q, '%') THEN 1 ELSE 2 END, " +
           "s.marketcap DESC")
    public List<Object[]> searchByTickerOrName(@Param("q") String q, Pageable pageable);

}
