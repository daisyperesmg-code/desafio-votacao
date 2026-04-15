package com.example.votacao.service;

import com.example.votacao.client.CpfClient;
import com.example.votacao.config.exception.BusinessException;
import com.example.votacao.config.exception.ResourceNotFoundException;
import com.example.votacao.entity.SessaoVotacao;
import com.example.votacao.enums.TipoVoto;
import com.example.votacao.enums.StatusVoto;
import com.example.votacao.repository.PautaRepository;
import com.example.votacao.repository.SessaoRepository;
import com.example.votacao.repository.VotoRepository;
import com.example.votacao.entity.Voto;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class VotacaoServiceTest {

        @InjectMocks
        private VotacaoService service;

        @Mock
        private SessaoRepository sessaoRepository;

        @Mock
        private VotoRepository votoRepository;

        @Mock
        private PautaRepository pautaRepository;

        @Mock
        private CpfClient cpfClient;

        @BeforeEach
        void setup() {
                MockitoAnnotations.openMocks(this);
        }

        // sessao encerrada
        @Test
        void deveLancarExcecaoQuandoSessaoEncerrada() {

                SessaoVotacao sessao = new SessaoVotacao();
                sessao.setId(1L);
                Instant inicio = Instant.now();
                sessao.setInicio(inicio);
                sessao.setFim(inicio.minus(Duration.ofMinutes(1)));

                when(sessaoRepository.findByPautaId(1L))
                                .thenReturn(Optional.of(sessao));

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> service.votar(1L, "12345678901", TipoVoto.SIM));

                assertEquals("Sessão encerrada=".concat(sessao.getId().toString()), ex.getMessage());

                verifyNoInteractions(cpfClient);
        }

        // ja votou
        @Test
        void deveLancarExcecaoQuandoAssociadoJaVotou() {

                SessaoVotacao sessao = new SessaoVotacao();
                Instant inicio = Instant.now();
                sessao.setInicio(inicio);
                sessao.setFim(inicio.plus(Duration.ofMinutes(1)));

                when(sessaoRepository.findByPautaId(1L))
                                .thenReturn(Optional.of(sessao));

                when(votoRepository.existsByPautaIdAndAssociadoId(1L, "123"))
                                .thenReturn(true);

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> service.votar(1L, "123", TipoVoto.SIM));

                assertEquals("Já votou", ex.getMessage());

                verifyNoInteractions(cpfClient); // 🔥 ainda não chama CPF
        }

        // cpf invalido
        @Test
        void deveLancarExcecaoQuandoCPFInvalido() {

                SessaoVotacao sessao = new SessaoVotacao();
                Instant inicio = Instant.now();
                sessao.setInicio(inicio);
                sessao.setFim(inicio.plus(Duration.ofMinutes(1)));

                when(sessaoRepository.findByPautaId(1L))
                                .thenReturn(Optional.of(sessao));

                when(votoRepository.existsByPautaIdAndAssociadoId(1L, "123"))
                                .thenReturn(false);

                when(cpfClient.validarCPF("123"))
                                .thenThrow(new ResourceNotFoundException(VotacaoServiceTest.class,
                                                "CPF inválido"));

                ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                                () -> service.votar(1L, "123", TipoVoto.SIM));
                assertEquals("CPF inválido", ex.getMessage());
        }

        // cpf nao pode votar
        @Test
        void deveLancarExcecaoQuandoUsuarioNaoPodeVotar() {

                SessaoVotacao sessao = new SessaoVotacao();
                Instant inicio = Instant.now();
                sessao.setInicio(inicio);
                sessao.setFim(inicio.plus(Duration.ofMinutes(1)));

                when(sessaoRepository.findByPautaId(1L))
                                .thenReturn(Optional.of(sessao));

                when(votoRepository.existsByPautaIdAndAssociadoId(1L, "123"))
                                .thenReturn(false);

                when(cpfClient.validarCPF("123"))
                                .thenReturn(StatusVoto.UNABLE_TO_VOTE);

                BusinessException ex = assertThrows(BusinessException.class,
                                () -> service.votar(1L, "123", TipoVoto.SIM));
                assertEquals("Usuário não pode votar", ex.getMessage());
        }

        // voto com sucesso
        @Test
        void deveRegistrarVotoComSucesso() {

                SessaoVotacao sessao = new SessaoVotacao();
                Instant inicio = Instant.now();
                sessao.setInicio(inicio);
                sessao.setFim(inicio.plus(Duration.ofMinutes(1)));

                when(sessaoRepository.findByPautaId(1L))
                                .thenReturn(Optional.of(sessao));

                when(votoRepository.existsByPautaIdAndAssociadoId(1L, "123"))
                                .thenReturn(false);

                when(cpfClient.validarCPF("123"))
                                .thenReturn(StatusVoto.ABLE_TO_VOTE);

                service.votar(1L, "123", TipoVoto.SIM);

                verify(votoRepository, times(1)).saveAndFlush(any(Voto.class));
        }
}
