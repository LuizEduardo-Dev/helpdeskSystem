package com.helpdesk.helpdesk.repository;

import com.helpdesk.helpdesk.domain.entity.TicketAudit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface TicketAuditRepository extends JpaRepository<TicketAudit, Long> {
    // Blindado com organizationId
    List<TicketAudit> findAllByTicketIdAndOrganizationIdOrderByCreatedAtDesc(Long ticketId, Long organizationId);
}