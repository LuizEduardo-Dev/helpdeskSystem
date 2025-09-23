package com.helpdesk.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.helpdesk.helpdesk.domain.Status;

public interface StatusRepository extends  JpaRepository<Status, Integer> {
}
