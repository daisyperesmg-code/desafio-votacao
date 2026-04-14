package com.example.votacao.config.exception;

import org.slf4j.LoggerFactory;

// Para erros de negócio
public class BusinessException extends RuntimeException {

    public BusinessException(Class<?> origem, String mensagem) {
        super(mensagem);
        LoggerFactory.getLogger(origem).warn("Erro de negócio: {}", mensagem);
    }
}
