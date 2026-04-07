package com.helpdesk.helpdesk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
public class GlobalExceptionHandler {

    // 1. Recurso não encontrado (404)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.NOT_FOUND, "Resource Not Found", request, null);
    }

    // 2. Acesso negado por regra de negócio ou Role (403)
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.FORBIDDEN, "Access Denied", request, null);
    }

    // 3. Falha de Login - Senha errada (401)
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponseDTO> handleBadCredentials(BadCredentialsException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.UNAUTHORIZED, "Unauthorized", request, null);
    }

    // 4. Regras de Negócio e Lógica Violadas (400)
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponseDTO> handleIllegalArgument(IllegalArgumentException ex, WebRequest request) {
        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Bad Request", request, null);
    }

    // 5. Erros de Validação dos DTOs (@NotBlank, @Size, etc) (400)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDTO> handleValidationExceptions(MethodArgumentNotValidException ex, WebRequest request) {
        List<String> details = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.toList());

        return buildErrorResponse(ex, HttpStatus.BAD_REQUEST, "Validation Failed", request, details);
    }

    // 6. O "Cata-tudo" de Segurança (500)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDTO> handleAllUncaughtException(Exception ex, WebRequest request) {

        ErrorResponseDTO errorDTO = new ErrorResponseDTO(
                LocalDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                "Ocorreu um erro inesperado no servidor.", // Não vaza o erro original para o cliente!
                request.getDescription(false).replace("uri=", ""),
                null
        );
        return new ResponseEntity<>(errorDTO, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Método utilitário privado para não repetir código
    private ResponseEntity<ErrorResponseDTO> buildErrorResponse(Exception ex, HttpStatus status, String error, WebRequest request, List<String> details) {
        ErrorResponseDTO errorDTO = new ErrorResponseDTO(
                LocalDateTime.now(),
                status.value(),
                error,
                ex.getMessage(),
                request.getDescription(false).replace("uri=", ""),
                details
        );
        return new ResponseEntity<>(errorDTO, status);
    }
}