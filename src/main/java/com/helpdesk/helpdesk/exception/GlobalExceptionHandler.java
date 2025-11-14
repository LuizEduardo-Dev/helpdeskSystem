package com.helpdesk.helpdesk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import java.time.Instant;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Captura exceções do tipo ResourceNotFoundException (404)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDTO> handleResourceNotFound(ResourceNotFoundException ex, WebRequest request) {

        ErrorResponseDTO errorDTO = new ErrorResponseDTO(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(), // 404
                "Resource Not Found",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "") // O path da URL
        );

        return new ResponseEntity<>(errorDTO, HttpStatus.NOT_FOUND);
    }

    /**
     * Captura exceções do tipo AccessDeniedException (403)
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDTO> handleAccessDenied(AccessDeniedException ex, WebRequest request) {

        ErrorResponseDTO errorDTO = new ErrorResponseDTO(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(), // 403
                "Access Denied",
                ex.getMessage(),
                request.getDescription(false).replace("uri=", "")
        );

        return new ResponseEntity<>(errorDTO, HttpStatus.FORBIDDEN);
    }

 }