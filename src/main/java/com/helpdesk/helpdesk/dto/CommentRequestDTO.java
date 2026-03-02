package com.helpdesk.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;

public record CommentRequestDTO(
        @NotBlank(message = "O conteúdo do comentário não pode estar vazio.")
        String content
) {}
