
package com.example.votacao.service;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.votacao.client.CpfClientFake;
import com.example.votacao.config.exception.BusinessException;
import com.example.votacao.config.exception.ResourceNotFoundException;
import com.example.votacao.dto.PautaDto;
import com.example.votacao.dto.ResultadoDto;
import com.example.votacao.entity.Pauta;
import com.example.votacao.entity.SessaoVotacao;
import com.example.votacao.entity.Voto;
import com.example.votacao.enums.StatusVoto;
import com.example.votacao.enums.TipoVoto;
import com.example.votacao.mapper.PautaMapper;
import com.example.votacao.repository.PautaRepository;
import com.example.votacao.repository.SessaoRepository;
import com.example.votacao.repository.VotoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VotacaoService {

    private static final Logger log = LoggerFactory.getLogger(VotacaoService.class);

    private final PautaRepository pautaRepository;
    private final SessaoRepository sessaoRepository;
    private final VotoRepository votoRepository;
    private final CpfClientFake cpfClient;

    public PautaDto criarPauta(PautaDto dto) {

        log.info("Criando nova pauta - titulo={}, descricao={}", dto.titulo(), dto.descricao());

        Pauta pauta = PautaMapper.toEntity(dto);
        Pauta salva = pautaRepository.save(pauta);

        PautaDto pautaDto = PautaMapper.toDTO(salva);

        log.info("Pauta criada com sucesso - pautaId={}", salva.getId());

        return pautaDto;
    }

    public SessaoVotacao abrirSessao(Long pautaId, Integer minutos) {

        log.info("Abrindo sessão - pautaId={}, minutos={}", pautaId, minutos);

        Pauta pauta = pautaRepository.findById(pautaId)
                .orElseThrow(() -> new ResourceNotFoundException(VotacaoService.class,
                        "Pauta não encontrada=".concat(pautaId.toString())));
        SessaoVotacao s = new SessaoVotacao();

        if (sessaoRepository.findByPautaId(pauta.getId()).isPresent()) {
            throw new BusinessException(VotacaoService.class,
                    "Já existe uma sessão para a pauta=".concat(pautaId.toString()));
        }

        s.setPauta(pauta);
        LocalDateTime inicio = LocalDateTime.now();
        s.setInicio(inicio);
        s.setFim(inicio.plusMinutes(minutos != null ? minutos : 1));

        SessaoVotacao sessaoVotacao = sessaoRepository.save(s);

        log.info("Sessão aberta com sucesso - sessaoId={}", s.getId());

        return sessaoVotacao;
    }

    public void votar(Long pautaId, String associadoId, TipoVoto voto) {

        log.info("Registrando voto - pautaId={}, voto={}", pautaId, voto);

        SessaoVotacao sessao = sessaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new ResourceNotFoundException(VotacaoService.class,
                        "Sessão não encontrada para a pauta=".concat(pautaId.toString())));

        if (LocalDateTime.now().isAfter(sessao.getFim())) {
            throw new BusinessException(VotacaoService.class, "Sessão encerrada=".concat(sessao.getId().toString()));
        }

        if (votoRepository.existsByPautaIdAndAssociadoId(pautaId, associadoId)) {
            throw new BusinessException(VotacaoService.class, "Já votou");
        }

        // valida CPF via serviço externo
        StatusVoto status = cpfClient.validarCPF(associadoId);

        if (status == StatusVoto.UNABLE_TO_VOTE) {
            throw new BusinessException(VotacaoService.class, "Usuário não pode votar");
        }

        Voto v = new Voto();
        v.setPauta(sessao.getPauta());
        v.setAssociadoId(associadoId);
        v.setVoto(voto);

        votoRepository.save(v);

        log.info("Voto registrado com sucesso - pautaId={}, voto={}", pautaId, voto);
    }

    public ResultadoDto resultado(Long pautaId) {

        sessaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new ResourceNotFoundException(VotacaoService.class, "Sessão não encontrada"));

        long sim = votoRepository.countByPautaIdAndVoto(pautaId, TipoVoto.SIM);
        long nao = votoRepository.countByPautaIdAndVoto(pautaId, TipoVoto.NAO);

        return new ResultadoDto(
                sim,
                nao,
                sim > nao ? "APROVADA" : "REJEITADA");
    }
}
