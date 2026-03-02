package com.helpdesk.helpdesk.repository;

import com.helpdesk.helpdesk.domain.TicketAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketAuditRepository extends JpaRepository<TicketAudit, Long> {
    // No futuro, podemos listar o histórico de um ticket específico:
    List<TicketAudit> findAllByTicketIdOrderByCreatedAtDesc(Long ticketId);
}