package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.StockUniverse;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
@EnableCaching
public interface StockUniverseRepository  extends JpaRepository<StockUniverse, String> {

    public List<StockUniverse> findAll();

}
