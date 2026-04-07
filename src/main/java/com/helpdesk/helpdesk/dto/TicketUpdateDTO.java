package com.helpdesk.helpdesk.dto;

public record TicketUpdateDTO(
        Integer statusId,
        Long assignedToId
) {}