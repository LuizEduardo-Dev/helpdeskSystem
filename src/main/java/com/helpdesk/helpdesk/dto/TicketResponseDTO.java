package com.helpdesk.helpdesk.dto;

import com.helpdesk.helpdesk.domain.entity.Ticket;
import java.time.LocalDateTime;

public record TicketResponseDTO(
        Long id,
        String title,
        String description,
        String priorityName,
        String statusName,
        String createdByEmail,
        String assignedToEmail,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public TicketResponseDTO(Ticket ticket) {
        this(
                ticket.getId(),
                ticket.getTitle(),
                ticket.getDescription(),
                ticket.getPriority().getName(),
                ticket.getStatus().getName(),
                ticket.getCreatedBy().getEmail(),
                ticket.getAssignedTo() != null ? ticket.getAssignedTo().getEmail() : "Não atribuído",
                ticket.getCreatedAt(),
                ticket.getUpdatedAt()
        );
    }
}
