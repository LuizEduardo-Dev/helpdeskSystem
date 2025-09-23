package com.helpdesk.helpdesk.repository;

import com.helpdesk.helpdesk.domain.Priority;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PriorityRepository extends  JpaRepository<Priority, Integer> {


}
