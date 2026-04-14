package com.example.votacao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PautaDto(

        @Schema(description = "Título da pauta", example = "Nova regra de votação") @NotBlank(message = "Título é obrigatório") @Size(min = 3, max = 100, message = "Título deve ter entre 3 e 100 caracteres") String titulo,

        @Schema(description = "Descrição da pauta", example = "Detalhes da pauta") @NotBlank(message = "Descrição é obrigatória") String descricao,

        @JsonProperty(access = JsonProperty.Access.READ_ONLY) @Schema(description = "Identifição da pauta", example = "ID da pauta") Long id

) {
}
