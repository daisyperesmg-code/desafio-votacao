
package com.example.votacao.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.votacao.entity.Voto;
import com.example.votacao.enums.TipoVoto;

public interface VotoRepository extends JpaRepository<Voto, Long> {

    boolean existsByPautaIdAndAssociadoId(Long pautaId, String associadoId);

    long countByPautaIdAndVoto(Long pautaId, TipoVoto voto);

    Optional<Voto> findByPautaIdAndAssociadoId(Long pautaId, String associadoId);
}
