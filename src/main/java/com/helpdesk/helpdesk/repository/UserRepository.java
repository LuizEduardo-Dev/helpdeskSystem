package com.helpdesk.helpdesk.repository;

import com.helpdesk.helpdesk.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Para listar todas as contas que esse e-mail possui (Ex: para a tela de "Escolha sua empresa")
    List<User> findAllByEmail(String email);

    // A busca exata e segura para autenticação após a empresa ser definida!
    Optional<User> findByEmailAndOrganizationId(String email, Long organizationId);
}