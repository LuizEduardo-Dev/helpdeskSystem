package com.helpdesk.helpdesk.service;

import com.helpdesk.helpdesk.domain.Comment;
import com.helpdesk.helpdesk.domain.Ticket;
import com.helpdesk.helpdesk.domain.User;
import com.helpdesk.helpdesk.dto.CommentRequestDTO;
import com.helpdesk.helpdesk.dto.CommentResponseDTO;
import com.helpdesk.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk.repository.CommentRepository;
import com.helpdesk.helpdesk.repository.TicketRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    public CommentService(CommentRepository commentRepository, TicketRepository ticketRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public CommentResponseDTO addComment(Long ticketId, CommentRequestDTO dto, User user) {
        // 1. Buscamos o ticket garantindo que ele pertence à organização do usuário (Multi-tenant!)
        Ticket ticket = ticketRepository.findByIdAndOrganizationId(ticketId, user.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado ou acesso negado."));

        // 2. Criamos e salvamos o comentário
        Comment comment = new Comment(ticket, user, dto.content());
        Comment savedComment = commentRepository.save(comment);

        return new CommentResponseDTO(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByTicket(Long ticketId, User user) {
        // Validação de segurança: o usuário pode ver os comentários desse ticket?
        ticketRepository.findByIdAndOrganizationId(ticketId, user.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado ou acesso negado."));

        return commentRepository.findAllByTicketIdOrderByCreatedAtAsc(ticketId)
                .stream()
                .map(CommentResponseDTO::new)
                .toList();
    }
}
