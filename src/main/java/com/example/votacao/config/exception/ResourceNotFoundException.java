package com.example.votacao.config.exception;

import org.slf4j.LoggerFactory;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(Class<?> origem, String mensagem) {
        super(mensagem);
        LoggerFactory.getLogger(origem).warn("Erro de recurso não encontrado: {}", mensagem);
    }
}