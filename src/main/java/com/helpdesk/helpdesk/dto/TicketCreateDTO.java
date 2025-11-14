package com.helpdesk.helpdesk.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TicketCreateDTO {

    @NotBlank(message = "O título é obrigatório.")
    @Size(min = 5, max = 150, message = "O título deve ter entre 5 e 150")
    private String title;

    @NotBlank(message = "A descrição não pode estar em branco.")
    @Size(min = 10, message = "A descrição deve ter pelo menos 10 caracteres.")
    private String description;

    @NotNull(message = "O ID da prioriedade é obrigatório.")
    private Integer priorityId;


}


