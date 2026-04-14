package com.example.votacao.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record SessaoDto(

        @Schema(description = "ID da Pauta") @NotNull(message = "ID da Pauta é obrigatório") Long idPauta,

        @Schema(description = "Minutos da sessão", example = "5") Integer minutos

) {
}
