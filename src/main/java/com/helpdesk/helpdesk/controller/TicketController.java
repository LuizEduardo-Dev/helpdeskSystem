package com.helpdesk.helpdesk.controller;

import com.helpdesk.helpdesk.domain.User;
import com.helpdesk.helpdesk.dto.*;
import com.helpdesk.helpdesk.service.CommentService;
import com.helpdesk.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;
    private final CommentService commentService; // 1. Injetado aqui

    @Autowired
    public TicketController(TicketService ticketService, CommentService commentService) {
        this.ticketService = ticketService;
        this.commentService = commentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'TECH', 'ADMIN')")
    public ResponseEntity<TicketResponseDTO> createTicket(
            @Valid @RequestBody TicketCreateDTO dto,
            @AuthenticationPrincipal User user) {
        TicketResponseDTO response = ticketService.createTicket(dto, user.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets(@AuthenticationPrincipal User user) {
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
    @PreAuthorize("hasRole('TECH')") // Apenas técnicos atualizam status/técnico
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketUpdateDTO updateDTO,
            @AuthenticationPrincipal User user) {
        TicketResponseDTO updatedTicket = ticketService.updateTicket(id, updateDTO, user);
        return ResponseEntity.ok(updatedTicket);
    }

    // --- COMENTÁRIOS ---

    @PostMapping("/{id}/comments") // 2. Usamos POST para criar
    @PreAuthorize("hasAnyRole('USER', 'TECH', 'ADMIN')")
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequestDTO requestDTO, // 3. Recebe RequestDTO
            @AuthenticationPrincipal User user) {

        // 4. Chama o método do service corretamente
        CommentResponseDTO response = commentService.addComment(id, requestDTO, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("hasAnyRole('USER', 'TECH', 'ADMIN')")
    public ResponseEntity<List<CommentResponseDTO>> getComments(
            @PathVariable Long id,
            @AuthenticationPrincipal User user) {
        List<CommentResponseDTO> comments = commentService.getCommentsByTicket(id, user);
        return ResponseEntity.ok(comments);
    }
}