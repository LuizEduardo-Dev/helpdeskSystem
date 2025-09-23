package com.helpdesk.helpdesk.repository;

import com.helpdesk.helpdesk.domain.Organization;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrganizationRepository  extends JpaRepository<Organization, Integer> {

}
