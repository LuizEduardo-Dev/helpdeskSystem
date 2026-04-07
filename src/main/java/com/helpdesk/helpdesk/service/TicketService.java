package com.helpdesk.helpdesk.service;

import com.helpdesk.helpdesk.domain.entity.Priority;
import com.helpdesk.helpdesk.domain.entity.Status;
import com.helpdesk.helpdesk.domain.entity.Ticket;
import com.helpdesk.helpdesk.domain.entity.TicketAudit;
import com.helpdesk.helpdesk.domain.entity.User;
import com.helpdesk.helpdesk.dto.TicketAuditResponseDTO;
import com.helpdesk.helpdesk.dto.TicketCreateDTO;
import com.helpdesk.helpdesk.dto.TicketResponseDTO;
import com.helpdesk.helpdesk.dto.TicketUpdateDTO;
import com.helpdesk.helpdesk.exception.AccessDeniedException;
import com.helpdesk.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final PriorityRepository priorityRepository;
    private final StatusRepository statusRepository;
    private final TicketAuditRepository auditRepository;

    @Autowired
    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         PriorityRepository priorityRepository,
                         StatusRepository statusRepository,
                         TicketAuditRepository auditRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.priorityRepository = priorityRepository;
        this.statusRepository = statusRepository;
        this.auditRepository = auditRepository;
    }

    @Transactional
    public TicketResponseDTO createTicket(TicketCreateDTO createDTO, User creatingUser) {
        Priority priority = priorityRepository.findById(createDTO.priorityId())
                .orElseThrow(() -> new ResourceNotFoundException("Prioridade não encontrada."));

        Status openStatus = statusRepository.findByName("Aberto")
                .orElseThrow(() -> new ResourceNotFoundException("Status padrão 'Aberto' não encontrado."));

        Ticket newTicket = new Ticket();
        newTicket.setTitle(createDTO.title());
        newTicket.setDescription(createDTO.description());
        newTicket.setPriority(priority);
        newTicket.setStatus(openStatus);
        newTicket.setCreatedBy(creatingUser);
        newTicket.setOrganization(creatingUser.getOrganization()); // Proteção multi-tenant

        Ticket savedTicket = ticketRepository.save(newTicket);
        return new TicketResponseDTO(savedTicket);
    }

    @Transactional(readOnly = true)
    public Page<TicketResponseDTO> getAllTickets(Long orgId, Integer statusId, Integer priorityId, Pageable pageable) {
        return ticketRepository.findByFilters(orgId, statusId, priorityId, pageable)
                .map(TicketResponseDTO::new);
    }

    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketById(Long id, Long orgId) {
        Ticket ticket = ticketRepository.findByIdAndOrganizationId(id, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado ou acesso negado."));
        return new TicketResponseDTO(ticket);
    }

    @Transactional
    public TicketResponseDTO updateTicket(Long ticketId, TicketUpdateDTO updateDTO, User updatingUser) {
        Ticket ticket = ticketRepository.findByIdAndOrganizationId(ticketId, updatingUser.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado ou acesso negado."));

        // 1. Atualização de Status
        if (updateDTO.statusId() != null) {
            String oldStatusName = ticket.getStatus().getName();

            Status newStatus = statusRepository.findById(updateDTO.statusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Status não encontrado."));

            if (!oldStatusName.equals(newStatus.getName())) {
                ticket.setStatus(newStatus);
                saveAudit(ticket, updatingUser, "status", oldStatusName, newStatus.getName());
            }
        }

        // 2. Lógica de Atribuição (Regra de Negócio: Somente ADMIN)
        if (updateDTO.assignedToId() != null) {
            if (!"ROLE_ADMIN".equals(updatingUser.getRole().getName())) {
                throw new AccessDeniedException("Apenas administradores podem distribuir chamados.");
            }

            String oldAssignedEmail = (ticket.getAssignedTo() != null) ? ticket.getAssignedTo().getEmail() : "Nenhum";

            User assignedUser = userRepository.findById(updateDTO.assignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Técnico não encontrado."));

            if (!"ROLE_TECH".equals(assignedUser.getRole().getName())) {
                throw new IllegalArgumentException("Só é possível atribuir chamados a técnicos.");
            }

            // Evita "roubo" de chamados acidental
            if (ticket.getAssignedTo() != null && !ticket.getAssignedTo().getId().equals(assignedUser.getId())) {
                throw new IllegalArgumentException("Este chamado já está atribuído. Remova a atribuição atual antes de transferir.");
            }

            if (!oldAssignedEmail.equals(assignedUser.getEmail())) {
                ticket.setAssignedTo(assignedUser);
                saveAudit(ticket, updatingUser, "assigned_to", oldAssignedEmail, assignedUser.getEmail());
            }
        }

        return new TicketResponseDTO(ticket);
    }

    @Transactional(readOnly = true)
    public List<TicketAuditResponseDTO> getTicketHistory(Long ticketId, Long orgId) {
        return auditRepository.findAllByTicketIdAndOrganizationIdOrderByCreatedAtDesc(ticketId, orgId)
                .stream()
                .map(TicketAuditResponseDTO::new)
                .collect(Collectors.toList());
    }

    private void saveAudit(Ticket ticket, User user, String field, String oldValue, String newValue) {
        TicketAudit audit = new TicketAudit();
        audit.setTicket(ticket);
        audit.setOrganization(ticket.getOrganization()); // FK Composta
        audit.setChangedBy(user);
        audit.setFieldName(field);
        audit.setOldValue(oldValue);
        audit.setNewValue(newValue);
        auditRepository.save(audit);
    }
}