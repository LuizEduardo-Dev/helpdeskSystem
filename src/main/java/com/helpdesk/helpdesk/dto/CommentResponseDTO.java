package com.helpdesk.helpdesk.dto;

import com.helpdesk.helpdesk.domain.entity.TicketComment;
import java.time.LocalDateTime;

public record CommentResponseDTO(
        Long id,
        String content,
        String authorName,
        String authorEmail,
        Boolean isInternal,
        LocalDateTime createdAt
) {
    // Construtor facilitador que recebe a entidade
    public CommentResponseDTO(TicketComment comment) {
        this(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getName(),
                comment.getUser().getEmail(),
                comment.getIsInternal(),
                comment.getCreatedAt()
        );
    }
}