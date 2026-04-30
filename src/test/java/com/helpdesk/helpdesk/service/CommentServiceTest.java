package com.helpdesk.helpdesk.service;

import com.helpdesk.helpdesk.dto.CommentResponseDTO;
import com.helpdesk.helpdesk.domain.entity.Organization;
import com.helpdesk.helpdesk.domain.entity.Role;
import com.helpdesk.helpdesk.domain.entity.TicketComment;
import com.helpdesk.helpdesk.domain.entity.User;


import com.helpdesk.helpdesk.repository.TicketCommentRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private TicketCommentRepository commentRepository;

    @InjectMocks
    private CommentService commentService;

    @Test
    @DisplayName("Cliente (ROLE_USER) não deve receber comentários marcados como internos")
    void clientShouldNotSeeInternalComments() {
        Long ticketId = 1L;

        Organization org = new Organization();
        org.setId(1L);

        Role clientRole = new Role();
        clientRole.setId(1);
        clientRole.setName("ROLE_USER");

        User clientUser = new User();
        clientUser.setOrganization(org);
        clientUser.setRole(clientRole);

        User autorDoComentario = new User();
        autorDoComentario.setName("Luiz Admin");
        autorDoComentario.setEmail("admin@edsystems.com");

        TicketComment publicComment = new TicketComment();
        publicComment.setContent("Olá, estamos verificando.");
        publicComment.setIsInternal(false);
        publicComment.setUser(autorDoComentario);

        TicketComment internalComment = new TicketComment();
        internalComment.setContent("O cliente fez besteira no banco de dados.");
        internalComment.setIsInternal(true);
        internalComment.setUser(autorDoComentario);


        when(commentRepository.findAllByTicketIdAndOrganizationIdOrderByCreatedAtAsc(ticketId, org.getId()))
                .thenReturn(List.of(publicComment, internalComment));

        List<CommentResponseDTO> result = commentService.getCommentsByTicket(ticketId, clientUser);


        assertEquals(1, result.size(), "O tamanho da lista deve ser 1, pois o comentário interno foi filtrado");
        assertFalse(result.get(0).isInternal(), "O comentário retornado não pode ser interno");
        assertEquals("Olá, estamos verificando.", result.get(0).content());
    }
}