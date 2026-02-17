package com.helpdesk.helpdesk.security;

import com.helpdesk.helpdesk.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
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

        // 1. Extrair o token do cabeçalho "Authorization"
        var token = this.recoverToken(request);

        if (token != null) {
            // 2. Validar o token e extrair o e-mail (subject)
            var login = tokenService.extractEmail(token);

            if (login != null) {
                // 3. BUSCA NO BANCO
                // Mesmo com o token, verificamos se o usuário ainda existe e está ativo.
                UserDetails user = userRepository.findByEmail(login)
                        .orElse(null);

                if (user != null) {
                    // 4. Autenticar no contexto do Spring
                    // Criamos um objeto de autenticação que o Spring entende
                    var authentication = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());

                    // Colocamos essa autenticação no "Contexto de Segurança"
                    // A partir daqui, o Spring sabe QUEM está fazendo a requisição
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        }

        // 5. Continuar a execução para o próximo filtro na corrente
        filterChain.doFilter(request, response);
    }

    /**
     * Helper para extrair o token do formato "Bearer <token>"
     */
    private String recoverToken(HttpServletRequest request) {
        var authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        return authHeader.replace("Bearer ", "");
    }
}