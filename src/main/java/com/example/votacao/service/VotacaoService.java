
package com.example.votacao.service;

import java.time.Duration;
import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.votacao.client.CpfClient;
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
    private final CpfClient cpfClient;

    @Transactional
    public PautaDto criarPauta(PautaDto dto) {

        log.info("Criando nova pauta - titulo={}, descricao={}", dto.titulo(), dto.descricao());

        Pauta pauta = PautaMapper.toEntity(dto);

        Pauta salva = salvarPauta(pauta);

        PautaDto pautaDto = PautaMapper.toDTO(salva);

        log.info("Pauta criada com sucesso - pautaId={}", salva.getId());

        return pautaDto;
    }

    public PautaDto obterPauta(Long id) {

        log.info("Obtendo pauta - id={}", id);

        Pauta pauta = pautaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(VotacaoService.class,
                        "Pauta não encontrada=".concat(id.toString())));

        PautaDto pautaDto = PautaMapper.toDTO(pauta);

        log.info("Pauta encontrada - pautaId={}", pauta.getId());

        return pautaDto;
    }

    private Pauta salvarPauta(Pauta pauta) {
        try {
            return pautaRepository.saveAndFlush(pauta);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(VotacaoService.class, "Já existe uma pauta com o mesmo título");
        }
    }

    @Transactional
    public void abrirSessao(Long pautaId, Integer minutos) {

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
        Instant inicio = Instant.now();
        s.setInicio(inicio);
        s.setFim(inicio.plus(Duration.ofMinutes(minutos != null ? minutos : 1)));

        salvarSessao(s);

        log.info("Sessão aberta com sucesso - sessaoId={}", s.getId());
    }

    private void salvarSessao(SessaoVotacao s) {
        try {
            sessaoRepository.saveAndFlush(s);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(VotacaoService.class,
                    "Já existe uma sessão para a pauta=".concat(s.getPauta().getId().toString()));
        }
    }

    @Transactional
    public void votar(Long pautaId, String associadoId, TipoVoto voto) {

        log.info("Registrando voto - pautaId={}, voto={}", pautaId, voto);

        SessaoVotacao sessao = sessaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new ResourceNotFoundException(VotacaoService.class,
                        "Sessão não encontrada para a pauta=".concat(pautaId.toString())));

        if (!sessao.sessaoEstaAberta()) {
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

        salvarVoto(v);

        log.info("Voto registrado com sucesso - pautaId={}, voto={}", pautaId, voto);
    }

    private void salvarVoto(Voto v) {
        try {
            votoRepository.saveAndFlush(v);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(VotacaoService.class, "Já votou");
        }
    }

    public ResultadoDto resultado(Long pautaId) {

        SessaoVotacao sessao = sessaoRepository.findByPautaId(pautaId)
                .orElseThrow(() -> new ResourceNotFoundException(VotacaoService.class, "Sessão não encontrada"));

        if (sessao.sessaoEstaAberta()) {
            throw new BusinessException(VotacaoService.class, "Sessão ainda está aberta");
        }

        long sim = votoRepository.countByPautaIdAndVoto(pautaId, TipoVoto.SIM);
        long nao = votoRepository.countByPautaIdAndVoto(pautaId, TipoVoto.NAO);

        String resultado;
        if (sim == nao) {
            resultado = "EMPATE";
        } else {
            resultado = sim > nao ? "APROVADA" : "REJEITADA";
        }

        return new ResultadoDto(sim, nao, resultado);
    }
}
