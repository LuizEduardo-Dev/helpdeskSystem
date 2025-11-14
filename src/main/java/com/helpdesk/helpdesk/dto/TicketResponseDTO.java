package com.helpdesk.helpdesk.dto;

import com.helpdesk.helpdesk.domain.Ticket;
import lombok.Data;

import java.time.Instant;

@Data
public class TicketResponseDTO {
    private Long id;
    private String title;
    private String description;
    private String priorityName;
    private String statusName;
    private String createdByEmail;
    private String assignedToEmail;
    private Instant createdAt;
    private Instant updatedAt;


    public TicketResponseDTO(Ticket ticket) {
        this.id = ticket.getId();
        this.title = ticket.getTitle();
        this.description = ticket.getDescription();
        this.priorityName = ticket.getPriority().getName();
        this.statusName = ticket.getStatus().getName();
        this.createdByEmail = ticket.getCreatedBy().getEmail();
        this.updatedAt = ticket.getUpdatedAt();
        this.createdAt = ticket.getCreatedAt();

        if (ticket.getAssignedTo() != null) {
            this.assignedToEmail = ticket.getAssignedTo().getEmail();
        } else {
            this.assignedToEmail = "Não atribuído";
        }

    }


}
