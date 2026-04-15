package com.example.votacao.client;

import java.util.Random;

import org.springframework.stereotype.Component;

import com.example.votacao.config.exception.ResourceNotFoundException;
import com.example.votacao.enums.StatusVoto;

@Component
public class CpfClientFake implements CpfClient {

    private final Random random = new Random();

    public StatusVoto validarCPF(String cpf) {

        // simula CPF inválido → 404
        if (random.nextInt(10) < 3) {
            throw new ResourceNotFoundException(CpfClientFake.class, "CPF inválido");
        }

        // simula apto ou não apto
        return random.nextBoolean()
                ? StatusVoto.ABLE_TO_VOTE
                : StatusVoto.UNABLE_TO_VOTE;
    }
}