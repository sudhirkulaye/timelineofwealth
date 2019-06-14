package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.DailyDataB;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface DailyDataBRepository extends JpaRepository<DailyDataB, DailyDataB.DailyDataBKey> {
    public List<DailyDataB> findAllByKeyTickerBAndKeyDateGreaterThanOrderByKeyDateAsc(String name, Date date);

}
