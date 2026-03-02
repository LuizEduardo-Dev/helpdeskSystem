package com.helpdesk.helpdesk.dto;

import com.helpdesk.helpdesk.domain.Comment;

import java.time.Instant;

public record CommentResponseDTO(
        Long id,
        String content,
        String authorName,
        String authorEmail,
        Instant createdAt
) {
    public CommentResponseDTO(Comment comment) {
        this(
                comment.getId(),
                comment.getContent(),
                comment.getUser().getName(),
                comment.getUser().getEmail(),
                comment.getCreatedAt()
        );
    }
}
