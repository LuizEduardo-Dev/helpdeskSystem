package com.helpdesk.helpdesk.dto;

import com.helpdesk.helpdesk.domain.entity.TicketAudit;
import java.time.LocalDateTime;

public record TicketAuditResponseDTO(
        Long id,
        String fieldName,
        String oldValue,
        String newValue,
        String changedByEmail,
        LocalDateTime createdAt
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