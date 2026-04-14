
package com.example.votacao.entity;

import com.example.votacao.enums.TipoVoto;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Data;

@Entity
@Data
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = { "pauta_id", "associadoId" })
}, indexes = {
        @Index(name = "idx_voto_pauta_voto", columnList = "pauta_id, voto")
})
public class Voto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private Pauta pauta;

    private String associadoId;

    @Enumerated(EnumType.STRING)
    private TipoVoto voto;
}
