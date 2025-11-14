package com.helpdesk.helpdesk.dto;

import lombok.Data;

@Data
public class TicketUpdateDTO {

    private Integer statusId;
    private Long assignedToId;
}
