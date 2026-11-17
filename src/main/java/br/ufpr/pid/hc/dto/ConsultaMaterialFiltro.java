package br.ufpr.pid.hc.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

public record ConsultaMaterialFiltro(UUID materialId, LocalDate dataInicio, LocalDate dataFim,
        UUID fabricanteId, UUID marcaId) implements Serializable {}
