package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.YahooConsensus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource(exported = false)
public interface YahooConsensusRepository extends JpaRepository<YahooConsensus, YahooConsensus.YahooConsensusKey> {
    List<YahooConsensus> findAllByKeyTickerOrderByKeyDateAscKeyFinYearAsc(String ticker);
}
