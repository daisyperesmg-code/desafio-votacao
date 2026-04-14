package com.example.votacao.dto;

import com.example.votacao.enums.TipoVoto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VotoDto(

        @Schema(description = "ID da Pauta") @NotNull(message = "ID da Pauta é obrigatório") Long idPauta,

        @Schema(description = "CPF do associado", example = "12345678901") @NotBlank(message = "AssociadoId é obrigatório") String associadoId,

        @Schema(description = "Tipo do voto", example = "SIM") @NotNull(message = "Voto é obrigatório") TipoVoto voto

) {
}