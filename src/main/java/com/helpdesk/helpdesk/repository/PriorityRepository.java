package com.helpdesk.helpdesk.repository;

import com.helpdesk.helpdesk.domain.entity.Priority;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PriorityRepository extends JpaRepository<Priority, Integer> {
    Optional<Priority> findByName(String name);
}