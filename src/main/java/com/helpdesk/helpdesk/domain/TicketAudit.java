package com.helpdesk.helpdesk.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;

@Entity
@Table(name = "ticket_audits") // Lembra da nossa tabela da V1?
@Data
@NoArgsConstructor
public class TicketAudit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false)
    private Ticket ticket;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "changed_by", nullable = false)
    private User changedBy;

    @Column(name = "field_name", nullable = false)
    private String fieldName; // Ex: "status" ou "assigned_to"

    @Column(name = "old_value")
    private String oldValue;

    @Column(name = "new_value")
    private String newValue;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    // Construtor auxiliar para facilitar a criação no Service
    public TicketAudit(Ticket ticket, User user, String field, String oldVal, String newVal) {
        this.ticket = ticket;
        this.changedBy = user;
        this.fieldName = field;
        this.oldValue = oldVal;
        this.newValue = newVal;
        this.createdAt = Instant.now();
    }
}