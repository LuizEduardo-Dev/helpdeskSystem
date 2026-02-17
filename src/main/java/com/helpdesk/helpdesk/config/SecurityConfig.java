package com.helpdesk.helpdesk.config;

import com.helpdesk.helpdesk.repository.UserRepository;
import com.helpdesk.helpdesk.security.SecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 1. Definir como STATELESS
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll() // 2. Liberar o login
                        .requestMatchers(HttpMethod.POST, "/api/v1/tickets").hasRole("USER") // 3. Exemplo de permissão
                        .anyRequest().authenticated()
                )
                // 4. Colocar nosso filtro ANTES do filtro padrão de login do Spring
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    /**
     * Bean para criptografar e verificar senhas.
     * Estamos usando o BCrypt, que é o padrão de mercado.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Bean que ensina ao Spring como buscar um usuário pelo seu identificador (no nosso caso, email).
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return email -> {
            // (Isso é um lambda que implementa o método 'loadUserByUsername')
            // O Spring Security chama o e-mail de "username", por isso o nome

            // 1. Buscamos o usuário no nosso repositório
            var user = userRepository.findByEmail(email); // Precisaremos criar este método!

            // 2. Se o usuário existir, nós o retornamos em um formato que o Spring entende
            if (user.isPresent()) {
                return user.get(); // Agora podemos retornar o usuário direto!
            } else {
                throw new UsernameNotFoundException("Usuário não encontrado: " + email);
            }
        };
    }

    /**
     * Bean que gerencia o processo de autenticação.
     * Nós o usaremos no nosso Controller de Login.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

}
