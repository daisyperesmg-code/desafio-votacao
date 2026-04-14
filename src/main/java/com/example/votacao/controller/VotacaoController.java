
package com.example.votacao.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.example.votacao.dto.PautaDto;
import com.example.votacao.dto.ResultadoDto;
import com.example.votacao.dto.SessaoDto;
import com.example.votacao.dto.VotoDto;
import com.example.votacao.entity.SessaoVotacao;
import com.example.votacao.service.VotacaoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class VotacaoController {

    private final VotacaoService service;

    @PostMapping("/pauta")
    @Operation(summary = "Criar uma nova pauta")
    @ApiResponse(responseCode = "201", description = "Pauta criada com sucesso")
    @ApiResponse(responseCode = "400", description = "Erro de validação")
    @ResponseStatus(HttpStatus.CREATED)
    public PautaDto criar(@RequestBody @Valid PautaDto dto) {
        return service.criarPauta(dto);
    }

    @PostMapping("/pauta/sessao")
    @Operation(summary = "Criar sessão de votação para uma pauta")
    @ApiResponse(responseCode = "201", description = "Sessão aberta com sucesso")
    @ApiResponse(responseCode = "400", description = "Erro de validação")
    @ApiResponse(responseCode = "404", description = "Pauta não encontrada")
    @ApiResponse(responseCode = "422", description = "Já existe uma sessão para a pauta")
    @ResponseStatus(HttpStatus.CREATED)
    public SessaoVotacao abrir(@RequestBody @Valid SessaoDto dto) {
        return service.abrirSessao(dto.idPauta(), dto.minutos());
    }

    @PostMapping("/sessao/voto")
    @Operation(summary = "Registrar voto em uma sessao de votação")
    @ApiResponse(responseCode = "201", description = "Voto registrado com sucesso")
    @ApiResponse(responseCode = "400", description = "Erro de validação")
    @ApiResponse(responseCode = "404", description = "Sessão não encontrada, CPF inválido")
    @ApiResponse(responseCode = "422", description = "Sessão encerrada, Já votou, Usuário não pode votar")
    @ResponseStatus(HttpStatus.CREATED)
    public void votar(@RequestBody @Valid VotoDto dto) {
        service.votar(dto.idPauta(), dto.associadoId(), dto.voto());
    }

    @GetMapping("/pauta/{id}/resultado")
    @Operation(summary = "Obter resultado da votação")
    @ApiResponse(responseCode = "200", description = "Resultado da sessao de votação")
    @ApiResponse(responseCode = "404", description = "Sessão não encontrada")
    public ResultadoDto resultado(@PathVariable Long id) {
        return service.resultado(id);
    }
}
