package com.helpdesk.helpdesk.service;

import com.helpdesk.helpdesk.domain.*;
import com.helpdesk.helpdesk.dto.TicketCreateDTO;
import com.helpdesk.helpdesk.dto.TicketResponseDTO;
import com.helpdesk.helpdesk.dto.TicketUpdateDTO;
import com.helpdesk.helpdesk.exception.AccessDeniedException;
import com.helpdesk.helpdesk.exception.ResourceNotFoundException;
import com.helpdesk.helpdesk.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;



@Service // Marca a classe como um "Serviço" gerenciado pelo Spring.
public class TicketService {

    // --- Injeção de Dependência ---
    // Pedimos ao Spring para "injetar" as instâncias dos repositórios que criamos.

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final PriorityRepository priorityRepository;
    private final StatusRepository statusRepository;

    @Autowired // Informa ao Spring para injetar as dependências via construtor (melhor prática)
    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,
                         PriorityRepository priorityRepository,
                         StatusRepository statusRepository) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.priorityRepository = priorityRepository;
        this.statusRepository = statusRepository;
    }

    /**
     * Lógica de negócio para criar um novo chamado.
     * @param createDTO O DTO com os dados de entrada.
     * @param creatingUserId O ID do usuário logado (que virá do Spring Security no futuro).
     * @return O DTO de resposta com o ticket criado.
     */
    @Transactional // Garante que a operação inteira seja atômica (ou tudo funciona, ou nada é salvo).
    public TicketResponseDTO createTicket(TicketCreateDTO createDTO, Long creatingUserId) {

        // 1. Buscar as entidades relacionadas no banco de dados
        //    Usamos .orElseThrow() para lançar um erro se o ID não for encontrado.
        User creatingUser = userRepository.findById(creatingUserId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));

        Priority priority = priorityRepository.findById(createDTO.getPriorityId())
                .orElseThrow(() -> new RuntimeException("Prioridade não encontrada"));

        // Regra de Negócio: Todo novo ticket começa como "Aberto"
        // (Estamos "hardcoding" o ID 1, mas o ideal seria um método no repositório)
        Status openStatus = statusRepository.findByName("Aberto")
                .orElseThrow(() -> new RuntimeException("Status 'Aberto' não encontrado"));

        // 2. Criar a nova entidade Ticket
        Ticket newTicket = new Ticket();
        newTicket.setTitle(createDTO.getTitle());
        newTicket.setDescription(createDTO.getDescription());
        newTicket.setPriority(priority);
        newTicket.setStatus(openStatus);
        newTicket.setCreatedBy(creatingUser);
        newTicket.setOrganization(creatingUser.getOrganization()); // A organização do ticket é a mesma do usuário
        // assignedTo fica nulo por padrão

        // 3. Salvar o novo ticket no banco de dados
        Ticket savedTicket = ticketRepository.save(newTicket);

        // 4. Mapear a entidade salva para o nosso DTO de resposta e retornar

        return new TicketResponseDTO(savedTicket);
    }
    /**
     * Busca todos os tickets cadastrados.
     * @return Uma lista de DTOs de resposta.
     */
    @Transactional(readOnly = true) // readOnly = true é uma otimização para consultas
    public List<TicketResponseDTO> getAllTickets() {
        // 1. Busca todos os tickets (entidades) do banco
        List<Ticket> tickets = ticketRepository.findAll();

        // 2. Converte a lista de Entidades para uma lista de DTOs
        return tickets.stream() // Usa a Stream API do Java
                .map(TicketResponseDTO::new) // Para cada ticket, cria um new TicketResponseDTO(ticket)
                .collect(Collectors.toList()); // Coleta tudo em uma nova lista
    }

    /**
     * Busca um ticket específico pelo ID.
     * @param id O ID do ticket a ser buscado.
     * @return O DTO de resposta do ticket.
     */
    @Transactional(readOnly = true)
    public TicketResponseDTO getTicketById(Long id) {
        // 1. Busca o ticket no banco ou lança um erro se não encontrar
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket com ID " + id + " não encontrado."));

        // 2. Converte a entidade para o DTO de resposta
        return new TicketResponseDTO(ticket);
    }
    /**
     * Atualiza o status e/ou o técnico atribuído de um chamado.
     * @param ticketId O ID do ticket a ser atualizado.
     * @param updateDTO O DTO com os novos dados (statusId e/ou assignedToId).
     * @param updatingUserId O ID do usuário que está realizando a operação.
     * @return O DTO de resposta com o ticket atualizado.
     */
    @Transactional
    public TicketResponseDTO updateTicket(Long ticketId, TicketUpdateDTO updateDTO, Long updatingUserId) {

        // 1. Buscar o usuário que está realizando a ação
        User updatingUser = userRepository.findById(updatingUserId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário " + updatingUserId + " não encontrado."));

        // 2. REGRA DE NEGÓCIO (Segurança): Verificar se o usuário é um técnico
        if (!"ROLE_TECH".equals(updatingUser.getRole().getName())) {
            throw new AccessDeniedException("Acesso negado: Somente técnicos podem atualizar chamados.");
            // No futuro, isso será uma exceção de acesso negado (403 Forbidden)
        }

        // 3. Buscar o ticket que será atualizado
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket " + ticketId + " não encontrado."));

        // 4. Atualizar os campos, se eles foram fornecidos no DTO

        // Se um novo statusId foi enviado...
        if (updateDTO.getStatusId() != null) {
            Status newStatus = statusRepository.findById(updateDTO.getStatusId())
                    .orElseThrow(() -> new RuntimeException("Status " + updateDTO.getStatusId() + " não encontrado."));
            ticket.setStatus(newStatus);
        }

        // Se um novo assignedToId foi enviado...
        if (updateDTO.getAssignedToId() != null) {
            User assignedUser = userRepository.findById(updateDTO.getAssignedToId())
                    .orElseThrow(() -> new RuntimeException("Técnico " + updateDTO.getAssignedToId() + " não encontrado."));

            // Verificação extra: O usuário sendo atribuído também é um técnico?
            if (!"ROLE_TECH".equals(assignedUser.getRole().getName())) {
                throw new AccessDeniedException("Erro: Só é possível atribuir chamados a técnicos.");
            }
            ticket.setAssignedTo(assignedUser);
        }

        // 5. Salvar o ticket atualizado (o @PreUpdate irá atualizar o updatedAt automaticamente)
        Ticket updatedTicket = ticketRepository.save(ticket);

        // 6. Retornar o DTO de resposta
        return new TicketResponseDTO(updatedTicket);
    }
}