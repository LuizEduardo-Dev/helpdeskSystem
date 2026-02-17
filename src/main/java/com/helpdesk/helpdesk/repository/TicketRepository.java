package com.helpdesk.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.helpdesk.helpdesk.domain.Ticket;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByOrganizationId(Long organizationId);
    Optional<Ticket> findByIdAndOrganizationId(Long id, Long organizationId);
}
