package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.DailyDataS;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Date;
import java.util.List;
@Repository
public interface DailyDataSRepository extends JpaRepository<DailyDataS, DailyDataS.DailyDataSKey> {
    public DailyDataS findByKeyNameAndKeyDate(String name, Date date);
    public List<DailyDataS> findAllByKeyDate(Date date);
    public List<DailyDataS> findAllByKeyNameAndKeyDateGreaterThanOrderByKeyDateAsc(String name, Date date);
    @Query(value="select count(1), date from daily_data_s " +
            "where date = (select max(date) from daily_data_s) " +
            "group by date order by date desc; ", nativeQuery = true)
    public List<Object[]> findMaxDateAndCount();
}
