package com.helpdesk.helpdesk.dto;

import com.helpdesk.helpdesk.domain.TicketAudit;
import java.time.Instant;

public record TicketAuditResponseDTO(
        Long id,
        String fieldName,
        String oldValue,
        String newValue,
        String changedByEmail,
        Instant createdAt
) {
    public TicketAuditResponseDTO(TicketAudit audit) {
        this(
                audit.getId(),
                audit.getFieldName(),
                audit.getOldValue(),
                audit.getNewValue(),
                audit.getChangedBy().getEmail(),
                audit.getCreatedAt()
        );
    }
}