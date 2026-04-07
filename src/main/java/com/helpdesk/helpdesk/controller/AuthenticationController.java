package com.helpdesk.helpdesk.controller;

import com.helpdesk.helpdesk.dto.LoginRequestDTO;
import com.helpdesk.helpdesk.dto.LoginResponseDTO;
import com.helpdesk.helpdesk.security.CustomUserDetails;
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

        // 1. Montamos o "Compound Username" (A Mágica do Multi-tenant)
        String compoundUsername = data.email() + ":" + data.organizationId();

        // 2. Passamos para o Spring validar
        var usernamePassword = new UsernamePasswordAuthenticationToken(compoundUsername, data.password());
        var auth = this.authenticationManager.authenticate(usernamePassword);

        // 3. Pegamos o principal (Agora é o nosso CustomUserDetails)
        var userDetails = (CustomUserDetails) auth.getPrincipal();

        // 4. Extraímos a entidade real do banco de dentro do CustomUserDetails para gerar o token
        var token = tokenService.generateToken(userDetails.getUser());

        return ResponseEntity.ok(new LoginResponseDTO(token));
    }
}