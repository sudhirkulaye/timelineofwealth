package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.IndexValuation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.sql.Date;
import java.util.List;

public interface IndexValuationRepository extends JpaRepository<IndexValuation, IndexValuation.IndexValuationKey> {
    public List<IndexValuation> findAllByKeyTickerOrderByKeyDate(String ticker);
    public List<IndexValuation> findAllByKeyTickerOrderByKeyDateDesc(String ticker);
    public List<IndexValuation> findAllByKeyTickerAndKeyDateBetweenOrderByKeyDate(String ticker, Date startDate, Date endDate);
    public IndexValuation findByKeyTickerAndKeyDate(String ticker, Date date);

    @Query("SELECT MAX(v.key.date) FROM IndexValuation v WHERE v.key.ticker = :ticker and v.key.date < :date")
    Date findMaxKeyDateForKeyTickerBeforeKeyDate(@Param("ticker") String ticker, @Param("date") Date date);

//    Date findMaxKeyDateByKeyTicker(String ticker);

    @Query("SELECT MAX(v.key.date) FROM IndexValuation v WHERE v.key.ticker = :ticker")
    Date findMaxKeyDateForKeyTicker(@Param("ticker") String ticker);

    @Query("SELECT MAX(v.key.date) FROM IndexValuation v WHERE v.key.ticker = :ticker AND YEAR(v.key.date) = :year AND MONTH(v.key.date) = :month")
    Date findMaxDateForMonth(@Param("ticker") String ticker, @Param("year") int year, @Param("month") int month);

    @Query("SELECT MIN(YEAR(v.key.date)) FROM IndexValuation v WHERE v.key.ticker = :ticker")
    Integer findMinYearForTicker(@Param("ticker") String ticker);


}