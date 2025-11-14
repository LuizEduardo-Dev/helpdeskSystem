package com.helpdesk.helpdesk.controller;

import com.helpdesk.helpdesk.dto.TicketCreateDTO;
import com.helpdesk.helpdesk.dto.TicketResponseDTO;
import com.helpdesk.helpdesk.dto.TicketUpdateDTO;
import com.helpdesk.helpdesk.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
        @Valid @RequestBody TicketCreateDTO createDTO,
        @RequestHeader("X-User-Id") Long creatingUserId){


        TicketResponseDTO responseDTO = ticketService.createTicket(createDTO, creatingUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDTO);
    }

    @GetMapping
    public ResponseEntity<List<TicketResponseDTO>> getAllTickets(){
        List<TicketResponseDTO> tickets = ticketService.getAllTickets();
        return ResponseEntity.ok(tickets);

    }

    /**
     * Endpoint para buscar um chamado por ID.
     * HTTP GET -> /api/v1/tickets/1 (ou /2, /3, etc.)
     */
    @GetMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> getTicketById(@PathVariable Long id) {
        // @PathVariable pega o "id" da URL e o passa para o método
        TicketResponseDTO ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ticket);
    }

    /**
     * Endpoint para atualizar parcialmente um chamado (status ou técnico).
     * HTTP PATCH -> /api/v1/tickets/1 (ou /2, /3, etc.)
     */
    @PatchMapping("/{id}")
    public ResponseEntity<TicketResponseDTO> updateTicket(
            @PathVariable Long id,
            @RequestBody TicketUpdateDTO updateDTO,
            @RequestHeader("x-user-id") Long updatingUserId) {

        TicketResponseDTO updatedTicket = ticketService.updateTicket(id, updateDTO, updatingUserId);
        return ResponseEntity.ok(updatedTicket); // Retorna 200 OK com o ticket atualizado
    }
}

