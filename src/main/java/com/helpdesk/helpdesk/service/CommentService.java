package com.helpdesk.helpdesk.service;

import com.helpdesk.helpdesk.domain.entity.TicketComment;
import com.helpdesk.helpdesk.domain.entity.Ticket;
import com.helpdesk.helpdesk.domain.entity.User;
import com.helpdesk.helpdesk.dto.CommentRequestDTO;
import com.helpdesk.helpdesk.dto.CommentResponseDTO;
import com.helpdesk.helpdesk.exception.AccessDeniedException;
import com.helpdesk.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk.repository.TicketCommentRepository;
import com.helpdesk.helpdesk.repository.TicketRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    private final TicketCommentRepository commentRepository;
    private final TicketRepository ticketRepository;

    public CommentService(TicketCommentRepository commentRepository, TicketRepository ticketRepository) {
        this.commentRepository = commentRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public CommentResponseDTO addComment(Long ticketId, CommentRequestDTO dto, User user) {
        Ticket ticket = ticketRepository.findByIdAndOrganizationId(ticketId, user.getOrganization().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Ticket não encontrado ou acesso negado."));

        // Proteção da Regra de Negócio: Cliente não pode criar nota interna
        boolean isInternal = dto.isInternal() != null && dto.isInternal();
        if (isInternal && "ROLE_USER".equals(user.getRole().getName())) {
            throw new AccessDeniedException("Clientes não possuem permissão para criar comentários internos.");
        }

        TicketComment comment = new TicketComment();
        comment.setTicket(ticket);
        comment.setUser(user);
        comment.setOrganization(user.getOrganization()); // FK Composta obriga isso!
        comment.setContent(dto.content());
        comment.setIsInternal(isInternal);

        TicketComment savedComment = commentRepository.save(comment);

        return new CommentResponseDTO(savedComment);
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDTO> getCommentsByTicket(Long ticketId, User user) {
        // Busca duplamente blindada (Ticket + Organização)
        return commentRepository.findAllByTicketIdAndOrganizationIdOrderByCreatedAtAsc(ticketId, user.getOrganization().getId())
                .stream()
                // Aqui no futuro adicionaremos um filtro: se for ROLE_USER, filter(c -> !c.getIsInternal())
                .map(CommentResponseDTO::new)
                .toList();
    }
}