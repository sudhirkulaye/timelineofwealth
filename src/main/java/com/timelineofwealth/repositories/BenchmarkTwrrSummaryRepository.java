package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.BenchmarkTwrrSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface BenchmarkTwrrSummaryRepository extends JpaRepository<BenchmarkTwrrSummary, Long> {
    public List<BenchmarkTwrrSummary> findAll();
    @Query (value = "select b.benchmark_type, b.benchmark_name, a.* from benchmark_twrr_summary a, benchmark b " +
            " Where a.benchmarkid = b.benchmarkid order by b.benchmark_type, b.is_mutual_fund, b.benchmark_name ", nativeQuery = true)
    public List<Object[]> findAllBenchmarks();
}
