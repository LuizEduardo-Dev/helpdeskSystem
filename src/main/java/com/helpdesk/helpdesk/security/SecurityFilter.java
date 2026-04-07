package com.helpdesk.helpdesk.security;

import com.helpdesk.helpdesk.domain.entity.User;
import com.helpdesk.helpdesk.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityFilter extends OncePerRequestFilter {

    private final TokenService tokenService;
    private final UserRepository userRepository;

    @Autowired
    public SecurityFilter(TokenService tokenService, UserRepository userRepository) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        var token = this.recoverToken(request);

        if (token != null && tokenService.isTokenValid(token)) {

            // 1. Extraímos o Email e a Organização do Token JWT
            var email = tokenService.extractEmail(token);
            var organizationId = tokenService.extractOrganizationId(token);

            if (email != null && organizationId != null) {

                // 2. Busca segura e exclusiva do Tenant!
                User user = userRepository.findByEmailAndOrganizationId(email, organizationId)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found in this organization"));

                // 3. Empacotamos na nossa classe de segurança isolada
                CustomUserDetails userDetails = new CustomUserDetails(user);

                // 4. Autenticamos no contexto do Spring
                var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) return null;
        return authHeader.replace("Bearer ", "");
    }
}