package com.helpdesk.helpdesk.service;

import com.helpdesk.helpdesk.domain.entity.Organization;
import com.helpdesk.helpdesk.domain.entity.Ticket;
import com.helpdesk.helpdesk.domain.entity.User;
import com.helpdesk.helpdesk.dto.TicketResponseDTO;
import com.helpdesk.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk.repository.TicketRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TicketServiceTest {

    @Mock
    private TicketRepository ticketRepository;

    @InjectMocks
    private TicketService ticketService;

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando o ticket não pertencer à empresa do usuário")
    void shouldThrowExceptionWhenTicketBelongsToAnotherOrganization() {
        Long ticketId = 1L;

        Organization hackerOrg = new Organization();
        hackerOrg.setId(2L);

        User hackerUser = new User();
        hackerUser.setOrganization(hackerOrg);

        when(ticketRepository.findByIdAndOrganizationId(ticketId, hackerOrg.getId()))
                .thenReturn(Optional.empty());


        assertThrows(ResourceNotFoundException.class, () -> {
            ticketService.getTicketById(ticketId, hackerOrg.getId());
        });
    }

    @Test
    @DisplayName("Deve retornar o Ticket com sucesso quando pertencer à mesma empresa")
    void shouldReturnTicketWhenOrganizationMatches() {
         Long ticketId = 1L;
        Long orgId = 1L;

         Ticket mockTicket = new Ticket();
        mockTicket.setId(ticketId);
        mockTicket.setTitle("Meu Servidor Queimou");


        when(ticketRepository.findByIdAndOrganizationId(ticketId, orgId))
                .thenReturn(Optional.of(mockTicket));

        TicketResponseDTO response = ticketService.getTicketById(ticketId, orgId);

        assertNotNull(response);
        assertEquals("Meu Servidor Queimou", response.title());
    }

}