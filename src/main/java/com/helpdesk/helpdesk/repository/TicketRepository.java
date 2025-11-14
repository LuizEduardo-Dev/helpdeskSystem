package com.helpdesk.helpdesk.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.helpdesk.helpdesk.domain.Ticket;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
}
