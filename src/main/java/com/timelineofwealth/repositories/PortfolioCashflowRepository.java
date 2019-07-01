package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.PortfolioCashflow;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PortfolioCashflowRepository  extends JpaRepository<PortfolioCashflow,PortfolioCashflow.PortfolioCashflowKey> {
    public List<PortfolioCashflow> findAllByKeyMemberidInOrderByKeyMemberidAscKeyPortfolioidAscKeyDateDesc(List<Long> memberids);

}
