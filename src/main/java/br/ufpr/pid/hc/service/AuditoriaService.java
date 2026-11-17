package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AuditoriaDao;
import br.ufpr.pid.hc.dto.EventoAuditoria;
import br.ufpr.pid.hc.dto.FiltroAuditoria;
import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.TipoEvento;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.util.List;

@Stateless
@RolesAllowed("ADMINISTRADOR")
public class AuditoriaService {

    @Inject
    private AuditoriaDao auditoriaDao;

    public List<EventoAuditoria> buscar(FiltroAuditoria filtro, int pagina, int tamanhoPagina) {
        return auditoriaDao.buscar(filtro, pagina, tamanhoPagina).stream()
                .map(evento -> evento.getTipo() == TipoEvento.AVALIACAO_EDITADA
                        ? evento.withAlteracoes(auditoriaDao.alteracoesAvaliacao(evento.getRegistro(), evento.getRevisao()))
                        : evento)
                .toList();
    }

    public long contar(FiltroAuditoria filtro) {
        return auditoriaDao.contar(filtro);
    }

    public List<Usuario> listarUsuarios() {
        return auditoriaDao.listarUsuarios();
    }
}
