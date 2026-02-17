package com.helpdesk.helpdesk.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Data
@EqualsAndHashCode(of = "id")

public class User implements UserDetails {

   @Id
   @GeneratedValue(strategy = GenerationType.IDENTITY)
   private Long id;

   @Column(nullable = false)
    private String name;

   @Column(nullable = false, unique = true)
    private String email;

   @Column(nullable = false)
    private String password;

   @ManyToOne
   @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @ManyToOne
    @JoinColumn(name = "organization_id", nullable = false)
    private Organization organization;

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

    /**
     * Retorna as "autoridades" (papéis) do usuário.
     * O Spring Security usa isso para o controle de acesso (ex: @PreAuthorize("hasRole('TECH')"))
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Pegamos o nome da nossa Role (ex: "ROLE_TECH") e a transformamos
        // em um formato que o Spring Security entende.
        return List.of(new SimpleGrantedAuthority(this.role.getName()));
    }

    /**
     * Retorna o identificador único do usuário para o Spring Security.
     * No nosso caso, o e-mail é o "username".
     */
    @Override
    public String getUsername() {
        return this.email;
    }

    /**
     * Retorna a senha (já criptografada) do banco.
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    // --- Métodos de status da conta (podemos customizar no futuro) ---

    @Override
    public boolean isAccountNonExpired() {
        return true; // A conta nunca expira
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // A conta nunca é bloqueada
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // As credenciais nunca expiram
    }

    @Override
    public boolean isEnabled() {
        return true; // A conta está sempre habilitada
    }
}
