package com.helpdesk.helpdesk.repository;

import com.helpdesk.helpdesk.domain.entity.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    List<User> findAllByEmail(String email);

    @EntityGraph(attributePaths = {"role", "organization"})
    Optional<User> findByEmailAndOrganizationId(String email, Long organizationId);
}