package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.ProdutoAnvisa;
import jakarta.annotation.Resource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Stream;

@ApplicationScoped
public class ProdutoAnvisaDao {

    private static final int TAMANHO_LOTE = 1000;

    private static final String FILTRO_TERMO = " WHERE p.numeroRegistroCadastro LIKE :inicio"
            + " OR p.cnpjDetentorRegistroCadastro LIKE :inicio"
            + " OR lower(p.nomeComercial) LIKE :contem"
            + " OR lower(p.nomeTecnico) LIKE :contem"
            + " OR lower(p.nomeFabricante) LIKE :contem"
            + " OR lower(p.detentorRegistroCadastro) LIKE :contem";

    @Resource(lookup = "java:/pidhcDS")
    private DataSource dataSource;

    @PersistenceContext
    private EntityManager entityManager;

    public List<ProdutoAnvisa> buscar(String termo, int pagina, int tamanhoPagina) {
        return filtrar(entityManager.createQuery(
                        "SELECT p FROM ProdutoAnvisa p" + filtro(termo)
                                + " ORDER BY p.numeroRegistroCadastro, p.nomeFabricante", ProdutoAnvisa.class), termo)
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public List<ProdutoAnvisa> buscarPorRegistro(String registro) {
        return entityManager.createQuery(
                        "SELECT p FROM ProdutoAnvisa p WHERE p.numeroRegistroCadastro = :registro", ProdutoAnvisa.class)
                .setParameter("registro", registro)
                .getResultList();
    }

    public long contar(String termo) {
        return filtrar(entityManager.createQuery(
                "SELECT count(p) FROM ProdutoAnvisa p" + filtro(termo), Long.class), termo)
                .getSingleResult();
    }

    private String filtro(String termo) {
        return termo == null || termo.isBlank() ? "" : FILTRO_TERMO;
    }

    private <R> TypedQuery<R> filtrar(TypedQuery<R> query, String termo) {
        if (termo == null || termo.isBlank()) {
            return query;
        }
        String limpo = termo.trim().toLowerCase(Locale.ROOT);
        boolean numerico = limpo.matches("[\\d./\\- ]+");
        return query
                .setParameter("inicio", numerico ? limpo.replaceAll("\\D", "") + "%" : limpo)
                .setParameter("contem", "%" + limpo + "%");
    }

    public long substituirTodos(List<String> colunas, Stream<String[]> linhas) throws SQLException {
        String sql = "INSERT INTO anvisa_produto_saude (id, " + String.join(", ", colunas) + ") "
                + "VALUES (?" + ", ?".repeat(colunas.size()) + ")";

        try (Connection conexao = dataSource.getConnection();
             Statement delete = conexao.createStatement();
             PreparedStatement insert = conexao.prepareStatement(sql)) {

            delete.executeUpdate("DELETE FROM anvisa_produto_saude");

            long total = 0;
            int pendentes = 0;
            Iterator<String[]> iterador = linhas.iterator();
            while (iterador.hasNext()) {
                String[] linha = iterador.next();
                insert.setObject(1, UUID.randomUUID());
                for (int i = 0; i < linha.length; i++) {
                    insert.setString(i + 2, linha[i].isEmpty() ? null : linha[i]);
                }
                insert.addBatch();
                total++;
                if (++pendentes == TAMANHO_LOTE) {
                    insert.executeBatch();
                    pendentes = 0;
                }
            }
            insert.executeBatch();
            return total;
        }
    }
}
