package com.helpdesk.helpdesk.service;

import com.helpdesk.helpdesk.domain.*;
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
    public TicketResponseDTO createTicket(TicketCreateDTO createDTO, Long creatingUserId) {

        // 1. Correção: Trocado RuntimeException por ResourceNotFoundException
        User creatingUser = userRepository.findById(creatingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado com ID: " + creatingUserId));

        Priority priority = priorityRepository.findById(createDTO.getPriorityId())
                .orElseThrow(() -> new ResourceNotFoundException("Prioridade não encontrada com ID: " + createDTO.getPriorityId()));

        Status openStatus = statusRepository.findByName("Aberto")
                .orElseThrow(() -> new ResourceNotFoundException("Status padrão 'Aberto' não configurado no sistema."));

        Ticket newTicket = new Ticket();
        newTicket.setTitle(createDTO.getTitle());
        newTicket.setDescription(createDTO.getDescription());
        newTicket.setPriority(priority);
        newTicket.setStatus(openStatus);
        newTicket.setCreatedBy(creatingUser);
        newTicket.setOrganization(creatingUser.getOrganization());

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

        if (!"ROLE_TECH".equals(updatingUser.getRole().getName())) {
            throw new AccessDeniedException("Somente técnicos podem atualizar chamados.");
        }

        Ticket ticket = ticketRepository.findByIdAndOrganizationId(ticketId, updatingUser.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado ou acesso negado."));

        // 2. Lógica de Auditoria para STATUS
        if (updateDTO.getStatusId() != null) {
            String oldStatusName = ticket.getStatus().getName(); // Captura o valor antigo

            Status newStatus = statusRepository.findById(updateDTO.getStatusId())
                    .orElseThrow(() -> new ResourceNotFoundException("Status " + updateDTO.getStatusId() + " não encontrado."));

            System.out.println("DEBUG: Tentando auditar. Antigo: " + oldStatusName + " | Novo: " + newStatus.getName());

            // Só audita se o valor realmente mudou
            if (!oldStatusName.equals(newStatus.getName())) {
                ticket.setStatus(newStatus);
                saveAudit(ticket, updatingUser, "status", oldStatusName, newStatus.getName());
            }
        }

        // 3. Lógica de Auditoria para ASSIGNED_TO (Técnico Atribuído)
        if (updateDTO.getAssignedToId() != null) {
            String oldAssignedEmail = (ticket.getAssignedTo() != null) ? ticket.getAssignedTo().getEmail() : "Nenhum";

            User assignedUser = userRepository.findById(updateDTO.getAssignedToId())
                    .orElseThrow(() -> new ResourceNotFoundException("Técnico " + updateDTO.getAssignedToId() + " não encontrado."));

            if (!"ROLE_TECH".equals(assignedUser.getRole().getName())) {
                throw new AccessDeniedException("Erro: Só é possível atribuir chamados a técnicos.");
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
        // Garantimos que o ticket pertence à organização do usuário logado
        ticketRepository.findByIdAndOrganizationId(ticketId, orgId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado nesta organização."));

        // Buscamos as auditorias ordenadas pela mais recente
        return auditRepository.findAllByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(TicketAuditResponseDTO::new)
                .collect(Collectors.toList());
    }


    private void saveAudit(Ticket ticket, User user, String field, String oldValue, String newValue) {
        System.out.println("DEBUG: Salvando auditoria no banco...");
        TicketAudit audit = new TicketAudit(ticket, user, field, oldValue, newValue);
        auditRepository.save(audit);
    }
}