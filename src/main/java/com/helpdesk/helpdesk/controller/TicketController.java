package com.helpdesk.helpdesk.controller;

import com.helpdesk.helpdesk.domain.User;
import com.helpdesk.helpdesk.dto.TicketAuditResponseDTO;
import com.helpdesk.helpdesk.dto.TicketCreateDTO;
import com.helpdesk.helpdesk.dto.TicketResponseDTO;
import com.helpdesk.helpdesk.dto.TicketUpdateDTO;
import com.helpdesk.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    @Autowired
    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponseDTO> createTicket(
            @Valid @RequestBody TicketCreateDTO dto,
            @AuthenticationPrincipal User user) {
        // Passamos o objeto User inteiro ou apenas os IDs necessários
        TicketResponseDTO response = ticketService.createTicket(dto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets(@AuthenticationPrincipal User user) {
        // O isolamento acontece aqui: enviamos o Organization ID do usuário logado
        List<TicketResponseDTO> tickets = ticketService.getAllTickets(user.getOrganization().getId());
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        TicketResponseDTO ticket = ticketService.getTicketById(id, user.getOrganization().getId());
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TicketAuditResponseDTO>> getTicketHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {

        List<TicketAuditResponseDTO> history = ticketService.getTicketHistory(id, user.getOrganization().getId());
        return ResponseEntity.ok(history);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketUpdateDTO updateDTO,
            @AuthenticationPrincipal User user) { // x-user-id REMOVIDO com sucesso!

        TicketResponseDTO updatedTicket = ticketService.updateTicket(id, updateDTO, user);
        return ResponseEntity.ok(updatedTicket);
    }
}