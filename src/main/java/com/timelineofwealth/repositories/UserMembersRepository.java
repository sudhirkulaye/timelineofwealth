package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.UserMembers;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

import java.util.List;

@RepositoryRestResource
public interface UserMembersRepository extends JpaRepository<UserMembers, Long> {
    public List<UserMembers> findAllByEmail(String email);
    public UserMembers findByMemberid(Long memberid);
}
