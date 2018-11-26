package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.DailyDataB;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyDataBRepository extends JpaRepository<DailyDataB, DailyDataB.DailyDataBKey> {

}
