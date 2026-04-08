package com.helpdesk.helpdesk.config;

import com.helpdesk.helpdesk.domain.entity.*;
import com.helpdesk.helpdesk.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@Profile("dev") // Só executa no seu computador (ambiente de desenvolvimento)
public class DataSeeder implements CommandLineRunner {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(OrganizationRepository organizationRepository,
                      UserRepository userRepository,
                      RoleRepository roleRepository,
                      PasswordEncoder passwordEncoder) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // Banco já populado
        }

        // 1. Buscamos as Roles (que já foram criadas pelo script V1 do Flyway)
        Role adminRole = roleRepository.findByName("ROLE_ADMIN").orElseThrow();
        Role techRole = roleRepository.findByName("ROLE_TECH").orElseThrow();
        Role userRole = roleRepository.findByName("ROLE_USER").orElseThrow();

        // 2. Criamos a primeira Organização (Sua empresa principal)
        Organization edSystems = new Organization();
        edSystems.setName("EDSystems");
        organizationRepository.save(edSystems);

        // 3. Criamos a segunda Organização (Para testar o isolamento/Multi-tenant)
        Organization vraSystems = new Organization();
        vraSystems.setName("VRA Systems");
        organizationRepository.save(vraSystems);

        // 4. Criamos os usuários para a EDSystems
        createDevUser("Luiz Admin", "admin@edsystems.com", "123456", adminRole, edSystems);
        createDevUser("Técnico Nível 1", "tech1@edsystems.com", "123456", techRole, edSystems);
        createDevUser("Técnico Nível 2", "tech2@edsystems.com", "123456", techRole, edSystems);
        createDevUser("Cliente VIP", "client@edsystems.com", "123456", userRole, edSystems);

        // 5. Criamos um usuário intruso na VRA Systems
        // Ele servirá para provarmos que, mesmo com o mesmo e-mail (se quiséssemos),
        // ou tentando acessar IDs de tickets, ele estaria preso na VRA.
        createDevUser("Usuario VRA", "user@vrasystems.com", "123456", userRole, vraSystems);

        System.out.println("✅ Seed de dados finalizado com sucesso!");
    }

    private void createDevUser(String name, String email, String password, Role role, Organization org) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password)); // Criptografia BCrypt ativa!
        user.setRole(role);
        user.setOrganization(org);
        userRepository.save(user);
    }
}