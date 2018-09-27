package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
public interface MemberRepository extends JpaRepository<Member, Integer> {

    public Member findByMemberid(long memberid);
    public List<Member> findAll();
    public Member findTopByFirstNameAndLastNameOrderByMemberidDesc(String firstName, String lastName);
}
