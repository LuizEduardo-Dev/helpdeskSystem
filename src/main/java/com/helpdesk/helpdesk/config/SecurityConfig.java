package com.helpdesk.helpdesk.config;

import com.helpdesk.helpdesk.domain.entity.User;
import com.helpdesk.helpdesk.repository.UserRepository;
import com.helpdesk.helpdesk.security.CustomUserDetails;
import com.helpdesk.helpdesk.security.SecurityFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
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
@EnableMethodSecurity
public class SecurityConfig {

    private final SecurityFilter securityFilter;

    public SecurityConfig(SecurityFilter securityFilter) {
        this.securityFilter = securityFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * O parâmetro 'compoundUsername' chegará no formato "email:organizationId"
     */
    @Bean
    public UserDetailsService userDetailsService(UserRepository userRepository) {
        return compoundUsername -> {
            // 1. Quebramos a string para separar o e-mail do ID da organização
            String[] parts = compoundUsername.split(":");
            if (parts.length != 2) {
                throw new UsernameNotFoundException("Formato de login inválido. Use email:organizationId");
            }

            String email = parts[0];
            Long organizationId;
            try {
                organizationId = Long.parseLong(parts[1]);
            } catch (NumberFormatException e) {
                throw new UsernameNotFoundException("ID da Organização inválido.");
            }

            // 2. Buscamos o usuário usando nosso repositório blindado
            User user = userRepository.findByEmailAndOrganizationId(email, organizationId)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado para esta organização."));

            // 3. Retornamos empacotado no nosso CustomUserDetails
            return new CustomUserDetails(user);
        };
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers(
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/webjars/**"
        );
    }
}