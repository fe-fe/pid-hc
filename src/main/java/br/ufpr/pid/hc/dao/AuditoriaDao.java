package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.dto.Alteracao;
import br.ufpr.pid.hc.dto.EventoAuditoria;
import br.ufpr.pid.hc.dto.FiltroAuditoria;
import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.ResultadoTecnico;
import br.ufpr.pid.hc.enumeration.TipoEvento;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@ApplicationScoped
public class AuditoriaDao {

    private static final String EVENTOS = """
            SELECT r.id AS revisao, r.data, r.usuario AS email, 'PLANILHA' AS tipo, i.id AS registro,
                   CAST(i.nome_arquivo AS text) AS texto, CAST(NULL AS text) AS complemento, i.total_registros AS quantidade
            FROM importacao_tecnovigilancia_aud i JOIN revisao r ON r.id = i.rev
            WHERE i.revtype = 0
            UNION ALL
            SELECT r.id, r.data, r.usuario, 'ANVISA', a.id, to_char(a.dt_atualizacao_dado, 'DD/MM/YYYY'), NULL, NULL
            FROM importacaoanvisa_aud a JOIN revisao r ON r.id = a.rev
            WHERE a.revtype = 0
            UNION ALL
            SELECT r.id, r.data, r.usuario, CASE a.revtype WHEN 0 THEN 'AVALIACAO_CRIADA' ELSE 'AVALIACAO_EDITADA' END,
                   a.id, m.codigo, m.nome, NULL
            FROM avaliacao_aud a JOIN revisao r ON r.id = a.rev LEFT JOIN material m ON m.id = a.material_id
            WHERE a.revtype IN (0, 1)
            UNION ALL
            SELECT 0, a.data, a.usuario, a.tipo, a.id, NULL, NULL, NULL
            FROM acesso a
            UNION ALL
            SELECT r.id, r.data, r.usuario, 'USUARIO_' || v.situacao, v.id, v.nome, v.email, NULL
            FROM (%s) v JOIN revisao r ON r.id = v.rev
            UNION ALL
            SELECT r.id, r.data, r.usuario, 'CADASTRO_' || v.situacao, v.id, 'Material', v.codigo || ' — ' || v.nome, NULL
            FROM (%s) v JOIN revisao r ON r.id = v.rev
            WHERE NOT EXISTS (SELECT 1 FROM importacao_tecnovigilancia_aud i WHERE i.rev = v.rev)
            UNION ALL
            SELECT r.id, r.data, r.usuario, 'CADASTRO_' || v.situacao, v.id, 'Fabricante', v.nome, NULL
            FROM (%s) v JOIN revisao r ON r.id = v.rev
            WHERE NOT EXISTS (SELECT 1 FROM importacao_tecnovigilancia_aud i WHERE i.rev = v.rev)
            UNION ALL
            SELECT r.id, r.data, r.usuario, 'CADASTRO_' || v.situacao, v.id, 'Marca', v.nome, NULL
            FROM (%s) v JOIN revisao r ON r.id = v.rev
            WHERE NOT EXISTS (SELECT 1 FROM importacao_tecnovigilancia_aud i WHERE i.rev = v.rev)
            UNION ALL
            SELECT r.id, r.data, r.usuario, 'DESVIO_EDITADO', d.id, d.vigihosp, m.codigo, NULL
            FROM desvio_aud d JOIN revisao r ON r.id = d.rev LEFT JOIN material m ON m.id = d.material_id
            WHERE d.revtype = 1
            """.formatted(versoes("usuario_aud"), versoes("material_aud"), versoes("fabricante_aud"),
            versoes("marca_aud"));

    private static final String VERSOES_AVALIACAO = """
            SELECT to_char(a.data, 'DD/MM/YYYY'), a.resultado, a.local, a.registroanvisa, a.descricao, a.parecer,
                   m.codigo || ' — ' || m.nome, f.nome, ma.nome
            FROM avaliacao_aud a
            LEFT JOIN material m ON m.id = a.material_id
            LEFT JOIN fabricante f ON f.id = a.fabricante_id
            LEFT JOIN marca ma ON ma.id = a.marca_id
            WHERE a.id = :id AND a.rev <= :revisao
            ORDER BY a.rev DESC
            """;

    private static final List<String> CAMPOS_AVALIACAO = List.of(
            "Data", "Resultado", "Local", "Registro ANVISA", "Descrição", "Parecer", "Material", "Fabricante", "Marca");

    @PersistenceContext
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public List<EventoAuditoria> buscar(FiltroAuditoria filtro, int pagina, int tamanhoPagina) {
        Query consulta = entityManager.createNativeQuery("""
                SELECT e.revisao, e.data, COALESCE(u.nome, e.email), e.tipo, e.registro, e.texto, e.complemento,
                       e.quantidade,
                       COALESCE(e.data < (SELECT installed_on FROM flyway_schema_history WHERE version = '2'), false)
                FROM (%s) e LEFT JOIN usuario u ON u.email = e.email
                %s
                ORDER BY e.data DESC, e.revisao DESC
                """.formatted(EVENTOS, condicoes(filtro)));
        parametros(consulta, filtro);
        return ((List<Object[]>) consulta
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList()).stream()
                .map(AuditoriaDao::evento)
                .toList();
    }

    public long contar(FiltroAuditoria filtro) {
        Query consulta = entityManager.createNativeQuery(
                "SELECT count(*) FROM (%s) e %s".formatted(EVENTOS, condicoes(filtro)));
        parametros(consulta, filtro);
        return ((Number) consulta.getSingleResult()).longValue();
    }

    @SuppressWarnings("unchecked")
    public List<Alteracao> alteracoesAvaliacao(UUID avaliacao, long revisao) {
        List<Object[]> versoes = entityManager.createNativeQuery(VERSOES_AVALIACAO)
                .setParameter("id", avaliacao)
                .setParameter("revisao", revisao)
                .setMaxResults(2)
                .getResultList();
        if (versoes.size() < 2) {
            return List.of();
        }
        Object[] depois = versoes.get(0);
        Object[] antes = versoes.get(1);
        List<Alteracao> alteracoes = new ArrayList<>();
        for (int i = 0; i < CAMPOS_AVALIACAO.size(); i++) {
            if (!Objects.equals(antes[i], depois[i])) {
                alteracoes.add(new Alteracao(CAMPOS_AVALIACAO.get(i), valor(i, antes[i]), valor(i, depois[i])));
            }
        }
        return alteracoes;
    }

    public List<Usuario> listarUsuarios() {
        return entityManager.createQuery("SELECT u FROM Usuario u ORDER BY u.nome", Usuario.class).getResultList();
    }

    private static String versoes(String tabela) {
        return """
                SELECT *, CASE
                        WHEN revtype = 0 THEN 'CRIADO'
                        WHEN anterior AND NOT atual THEN 'DESATIVADO'
                        WHEN atual AND NOT anterior THEN 'REATIVADO'
                        ELSE 'EDITADO' END AS situacao
                FROM (SELECT t.*, COALESCE(t.ativo, true) AS atual,
                             COALESCE(LAG(t.ativo) OVER (PARTITION BY t.id ORDER BY t.rev), true) AS anterior
                      FROM %s t) x
                WHERE revtype IN (0, 1)
                """.formatted(tabela);
    }

    private static String condicoes(FiltroAuditoria filtro) {
        List<String> condicoes = new ArrayList<>();
        if (filtro.tipo() != null) {
            condicoes.add("e.tipo = :tipo");
        }
        if (filtro.usuario() != null) {
            condicoes.add("e.email = :usuario");
        }
        if (filtro.inicio() != null) {
            condicoes.add("e.data >= :inicio");
        }
        if (filtro.fim() != null) {
            condicoes.add("e.data < :fim");
        }
        return condicoes.isEmpty() ? "" : "WHERE " + String.join(" AND ", condicoes);
    }

    private static void parametros(Query consulta, FiltroAuditoria filtro) {
        Map<String, Object> valores = new LinkedHashMap<>();
        if (filtro.tipo() != null) {
            valores.put("tipo", filtro.tipo().name());
        }
        if (filtro.usuario() != null) {
            valores.put("usuario", filtro.usuario());
        }
        if (filtro.inicio() != null) {
            valores.put("inicio", filtro.inicio().atStartOfDay());
        }
        if (filtro.fim() != null) {
            valores.put("fim", filtro.fim().plusDays(1).atStartOfDay());
        }
        valores.forEach(consulta::setParameter);
    }

    private static EventoAuditoria evento(Object[] linha) {
        TipoEvento tipo = TipoEvento.valueOf((String) linha[3]);
        return new EventoAuditoria(
                ((Number) linha[0]).longValue(),
                dataHora(linha[1]),
                (String) linha[2],
                (Boolean) linha[8],
                tipo,
                (UUID) linha[4],
                descricao(tipo, (String) linha[5], (String) linha[6], (Number) linha[7]),
                List.of());
    }

    private static String descricao(TipoEvento tipo, String texto, String complemento, Number quantidade) {
        return switch (tipo) {
            case PLANILHA -> texto + " · " + quantidade + (quantidade != null && quantidade.intValue() == 1 ? " desvio" : " desvios");
            case ANVISA -> texto == null ? "Base de produtos atualizada" : "Base de produtos de " + texto;
            case AVALIACAO_CRIADA, AVALIACAO_EDITADA -> texto == null ? "Material removido" : texto + " — " + complemento;
            case LOGIN -> "Entrou no sistema";
            case LOGIN_RECUSADO -> "E-mail ou senha inválidos, ou usuário desativado";
            case LOGOUT -> "Saiu do sistema";
            case USUARIO_CRIADO, USUARIO_EDITADO, USUARIO_DESATIVADO, USUARIO_REATIVADO -> texto + " — " + complemento;
            case CADASTRO_CRIADO, CADASTRO_EDITADO, CADASTRO_DESATIVADO, CADASTRO_REATIVADO -> texto + ": " + complemento;
            case DESVIO_EDITADO -> "VigiHosp " + texto + (complemento == null ? "" : " — material " + complemento);
        };
    }

    private static String valor(int campo, Object valor) {
        if (valor == null || valor.toString().isBlank()) {
            return "—";
        }
        return CAMPOS_AVALIACAO.get(campo).equals("Resultado")
                ? ResultadoTecnico.valueOf(valor.toString()).getRotulo()
                : valor.toString();
    }

    private static LocalDateTime dataHora(Object valor) {
        return valor instanceof Timestamp timestamp ? timestamp.toLocalDateTime() : (LocalDateTime) valor;
    }
}
