package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.SetupDates;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.sql.Date;
import java.util.List;

@RepositoryRestResource
@EnableCaching
public interface SetupDatesRepository  extends JpaRepository<SetupDates, Date> {

    @Cacheable("SetupDates")
    public List<SetupDates> findAll();

}
