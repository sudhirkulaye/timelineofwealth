package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.DailyDataS;
import org.springframework.data.jpa.repository.JpaRepository;

import java.sql.Date;
import java.util.List;

public interface DailyDataSRepository extends JpaRepository<DailyDataS, DailyDataS.DailyDataSKey> {
    public DailyDataS findByKeyNameAndKeyDate(String name, Date date);
    public List<DailyDataS> findAllByKeyDate(Date date);

}
