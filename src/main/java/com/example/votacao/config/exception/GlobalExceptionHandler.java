package com.example.votacao.config.exception;

import java.lang.reflect.Method;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // Erros de negócio
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ex.getMessage());
    }

    // Recurso não encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    // Validação de campos (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<String> handleValidation(MethodArgumentNotValidException ex) {
        String mensagem = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));

        String controller = ex.getParameter().getDeclaringClass().getSimpleName();
        Method method = ex.getParameter().getMethod();
        String metodo = method != null ? method.getName() : "construtor";

        LoggerFactory.getLogger(ex.getParameter().getDeclaringClass())
                .warn("Erro de validação em {}.{}: {}", controller, metodo, mensagem);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(mensagem);
    }

    // Erros inesperados de código
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGeneric(Exception ex, HttpServletRequest request) {
        LoggerFactory.getLogger(ex.getClass()).error("Erro inesperado em {}: {}",
                request.getRequestURI(),
                ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Erro interno: " + ex.getMessage());
    }
}