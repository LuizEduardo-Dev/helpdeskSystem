package com.helpdesk.helpdesk.repository;

import com.helpdesk.helpdesk.domain.entity.Ticket;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    // Busca base segura
    Optional<Ticket> findByIdAndOrganizationId(Long id, Long organizationId);

    // Dashboard: Chamados Recentes
    List<Ticket> findAllByOrganizationIdOrderByCreatedAtDesc(Long organizationId, Pageable pageable);

    // Fila do Técnico: "Meus Chamados"
    List<Ticket> findAllByOrganizationIdAndAssignedToId(Long organizationId, Long assignedToId);

    // Fila de Espera: Chamados Não Atribuídos (Vai usar aquele índice parcial que criamos!)
    List<Ticket> findAllByOrganizationIdAndAssignedToIsNull(Long organizationId);

    // Busca com filtros dinâmicos (O seu estava ótimo, só ajustei os parâmetros)
    @Query("SELECT t FROM Ticket t WHERE t.organization.id = :orgId " +
            "AND (:statusId IS NULL OR t.status.id = :statusId) " +
            "AND (:priorityId IS NULL OR t.priority.id = :priorityId)")
    Page<Ticket> findByFilters(@Param("orgId") Long orgId,
                               @Param("statusId") Integer statusId,
                               @Param("priorityId") Integer priorityId,
                               Pageable pageable);
}