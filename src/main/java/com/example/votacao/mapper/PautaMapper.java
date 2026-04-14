package com.example.votacao.mapper;

import com.example.votacao.dto.PautaDto;
import com.example.votacao.entity.Pauta;

public class PautaMapper {

    public static Pauta toEntity(PautaDto dto) {
        Pauta p = new Pauta();
        p.setTitulo(dto.titulo());
        p.setDescricao(dto.descricao());
        return p;
    }

    public static PautaDto toDTO(Pauta entity) {
        return new PautaDto(entity.getTitulo(), entity.getDescricao(), entity.getId());
    }
}