package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.IncomeExpenseSavings;
import com.timelineofwealth.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface IncomeExpenseSavingsRepository extends JpaRepository<IncomeExpenseSavings, IncomeExpenseSavings.IncomeExpenseSavingsKey> {

    //public Iterable<IncomeExpenseSavings> findAllByKeyMemberidAndIncomeExpenseSavingsKeyFinyear(Long memebrid, int finyear);

    public List<IncomeExpenseSavings> findByKeyMemberidInOrderByKeyFinyearDescKeyMemberidAsc(List<Long> memberids);
}
