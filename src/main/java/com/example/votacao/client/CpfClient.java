package com.example.votacao.client;

import com.example.votacao.enums.StatusVoto;

public interface CpfClient {
    StatusVoto validarCPF(String cpf);
}