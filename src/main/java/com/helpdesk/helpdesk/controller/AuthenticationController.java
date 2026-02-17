package com.helpdesk.helpdesk.controller;

import com.helpdesk.helpdesk.domain.User;
import com.helpdesk.helpdesk.dto.LoginRequestDTO;
import com.helpdesk.helpdesk.dto.LoginResponseDTO;
import com.helpdesk.helpdesk.security.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.tokenService = tokenService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody @Valid LoginRequestDTO data) {
        // 1. Criamos um "token" interno do Spring com as credenciais recebidas
        var usernamePassword = new UsernamePasswordAuthenticationToken(data.email(), data.password());

        // 2. O AuthenticationManager vai chamar o nosso UserDetailsService e o PasswordEncoder
        // para verificar se o e-mail existe e se a senha (hash) bate.
        var auth = this.authenticationManager.authenticate(usernamePassword);

        // 3. Se a autenticação falhar, o Spring lança uma exceção automaticamente aqui.
        // Se passar, pegamos o usuário autenticado.
        var user = (User) auth.getPrincipal();

        // 4. Geramos o nosso Token JWT para o usuário
        var token = tokenService.generateToken(user);

        // 5. Devolvemos o token no corpo da resposta
        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}