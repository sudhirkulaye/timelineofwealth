package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.Subindustry;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
@EnableCaching
public interface SubindustryRepository extends JpaRepository<Subindustry, Long> {
    @Cacheable("subindustries")
    public List<Subindustry> findAll();
    public Subindustry findBySubindustryid(long subindustryid);
    public List<Subindustry> findAllBySectorNameDisplayIgnoreCaseStartingWith(String sectorNameDisplay);
}
