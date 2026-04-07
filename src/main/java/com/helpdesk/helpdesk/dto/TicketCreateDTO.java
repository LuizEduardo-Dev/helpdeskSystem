package com.helpdesk.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TicketCreateDTO(
        @NotBlank(message = "O título é obrigatório.")
        @Size(min = 5, max = 150, message = "O título deve ter entre 5 e 150 caracteres.")
        String title,

        @NotBlank(message = "A descrição não pode estar em branco.")
        @Size(min = 10, message = "A descrição deve ter pelo menos 10 caracteres.")
        String description,

        @NotNull(message = "O ID da prioridade é obrigatório.")
        Integer priorityId
) {}