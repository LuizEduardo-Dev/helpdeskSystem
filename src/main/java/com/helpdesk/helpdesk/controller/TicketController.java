package com.helpdesk.helpdesk.controller;

import com.helpdesk.helpdesk.domain.entity.User;
import com.helpdesk.helpdesk.dto.*;
import com.helpdesk.helpdesk.security.CustomUserDetails;
import com.helpdesk.helpdesk.service.CommentService;
import com.helpdesk.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
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
    private final CommentService commentService;

    @Autowired
    public TicketController(TicketService ticketService, CommentService commentService) {
        this.ticketService = ticketService;
        this.commentService = commentService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_TECH', 'ROLE_ADMIN')")
    public ResponseEntity<TicketResponseDTO> createTicket(
            @Valid @RequestBody TicketCreateDTO dto,
            @AuthenticationPrincipal CustomUserDetails userDetails) { // Alterado o tipo!

        User user = userDetails.getUser(); // Extrai a entidade real

        // Passamos a entidade User inteira, como o Service novo exige
        TicketResponseDTO response = ticketService.createTicket(dto, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<Page<TicketResponseDTO>> getAllTickets(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer statusId,
            @RequestParam(required = false) Integer priorityId,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        User user = userDetails.getUser();
        Page<TicketResponseDTO> tickets = ticketService.getAllTickets(user.getOrganization().getId(), statusId, priorityId, pageable);
        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        TicketResponseDTO ticket = ticketService.getTicketById(id, user.getOrganization().getId());
        return ResponseEntity.ok(ticket);
    }

    @GetMapping("/{id}/history")
    public ResponseEntity<List<TicketAuditResponseDTO>> getTicketHistory(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        List<TicketAuditResponseDTO> history = ticketService.getTicketHistory(id, user.getOrganization().getId());
        return ResponseEntity.ok(history);
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_TECH', 'ROLE_ADMIN')") // Admin precisa acessar para distribuir!
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketUpdateDTO updateDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        TicketResponseDTO updatedTicket = ticketService.updateTicket(id, updateDTO, user);
        return ResponseEntity.ok(updatedTicket);
    }

    // --- COMENTÁRIOS ---

    @PostMapping("/{id}/comments")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_TECH', 'ROLE_ADMIN')")
    public ResponseEntity<CommentResponseDTO> addComment(
            @PathVariable Long id,
            @Valid @RequestBody CommentRequestDTO requestDTO,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        CommentResponseDTO response = commentService.addComment(id, requestDTO, user);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}/comments")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_TECH', 'ROLE_ADMIN')")
    public ResponseEntity<List<CommentResponseDTO>> getComments(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        List<CommentResponseDTO> comments = commentService.getCommentsByTicket(id, user);
        return ResponseEntity.ok(comments);
    }

    // Rota: GET /api/v1/tickets/queue/unassigned
    @GetMapping("/queue/unassigned")
    @PreAuthorize("hasAnyAuthority('ROLE_TECH', 'ROLE_ADMIN')")
    public ResponseEntity<List<TicketResponseDTO>> getUnassignedQueue(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        List<TicketResponseDTO> queue = ticketService.getUnassignedTickets(user.getOrganization().getId());
        return ResponseEntity.ok(queue);
    }

    // Rota: GET /api/v1/tickets/queue/me
    @GetMapping("/queue/me")
    @PreAuthorize("hasAnyAuthority('ROLE_TECH')")
    public ResponseEntity<List<TicketResponseDTO>> getMyQueue(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        User user = userDetails.getUser();
        // Passamos o ID do técnico logado e o ID da empresa dele
        List<TicketResponseDTO> myTickets = ticketService.getMyAssignedTickets(user.getId(), user.getOrganization().getId());
        return ResponseEntity.ok(myTickets);
    }

}