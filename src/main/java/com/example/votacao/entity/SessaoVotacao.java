
package com.example.votacao.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDateTime;

@Entity
@Data
public class SessaoVotacao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    private Pauta pauta;

    private Instant inicio;
    private Instant fim;

    public boolean sessaoEstaAberta() {
        Instant agora = Instant.now();
        return agora.isBefore(fim);
    }
}
