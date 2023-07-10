package com.timelineofwealth.repositories;

import com.timelineofwealth.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@RepositoryRestResource
@Repository
//@EnableCaching
public interface UserRepository extends JpaRepository<User, String> {

    public Optional<User> findByEmail(String email);
    public List<User> findAll();
}
