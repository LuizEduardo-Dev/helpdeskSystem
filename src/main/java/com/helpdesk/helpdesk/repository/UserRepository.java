package com.helpdesk.helpdesk.repository;

import com.helpdesk.helpdesk.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User,Long> {
}
