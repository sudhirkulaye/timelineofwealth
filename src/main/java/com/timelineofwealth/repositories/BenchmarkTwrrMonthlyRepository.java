package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.BenchmarkTwrrMonthly;
import com.timelineofwealth.entities.PortfolioTwrrMonthly;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BenchmarkTwrrMonthlyRepository extends JpaRepository<BenchmarkTwrrMonthly, BenchmarkTwrrMonthly.BenchmarkTwrrMonthlyKey> {
    public List<BenchmarkTwrrMonthly> findAll();
    @Query(value = "select b.benchmark_type, b.benchmark_name, a.* from benchmark_twrr_monthly a, benchmark b " +
            "Where a.benchmarkid = b.benchmarkid AND year > 2017 order by year desc, b.benchmark_type, benchmarkid ", nativeQuery = true)
    public List<Object[]> findAllBenchmarks();
}
