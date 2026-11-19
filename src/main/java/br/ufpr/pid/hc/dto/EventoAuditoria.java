package br.ufpr.pid.hc.dto;

import br.ufpr.pid.hc.enumeration.TipoEvento;
import lombok.Value;
import lombok.With;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Value
public class EventoAuditoria implements Serializable {
    long revisao;
    LocalDateTime data;
    String usuario;
    boolean anteriorAoHistorico;
    TipoEvento tipo;
    UUID registro;
    String descricao;
    @With
    List<Alteracao> alteracoes;

    public String getAutor() {
        if (usuario != null) {
            return usuario;
        }
        return anteriorAoHistorico ? "Não registrado" : "Sistema";
    }

    public String getObservacao() {
        if (tipo != TipoEvento.AVALIACAO_EDITADA || !alteracoes.isEmpty()) {
            return null;
        }
        return anteriorAoHistorico ? "Alteração anterior ao histórico, sem detalhes" : "Salva sem mudar nenhum campo";
    }

    public boolean isSemUsuario() {
        return usuario == null;
    }
}
