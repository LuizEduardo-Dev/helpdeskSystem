package com.helpdesk.helpdesk.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import com.helpdesk.helpdesk.domain.Ticket;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    List<Ticket> findAllByOrganizationId(Long organizationId, Pageable pageable);
    Optional<Ticket> findByIdAndOrganizationId(Long id, Long organizationId);

    @Query("SELECT t FROM Ticket t WHERE t.organization.id = :orgId " +
            "AND (:statusId IS NULL OR t.status.id = :statusId) " +
            "AND (:priorityId IS NULL OR t.priority.id = :priorityId)")
    Page<Ticket> findByFilters(Long orgId, Integer statusId, Integer priorityId, Pageable pageable);
}
