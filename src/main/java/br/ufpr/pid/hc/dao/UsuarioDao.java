package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.Perfil;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@ApplicationScoped
public class UsuarioDao extends AbstractDao<Usuario, UUID> {

    public UsuarioDao() {
        super(Usuario.class);
    }

    public List<Usuario> buscarPorNome(int pagina, int tamanhoPagina, CampoOrdenacao ordenacao,
            String nome, boolean incluirInativos) {
        return entityManager.createQuery(
                        "SELECT u FROM Usuario u " + filtroNome(incluirInativos)
                                + "ORDER BY u." + ordenacao.getCampoBanco(), Usuario.class)
                .setParameter("nome", "%" + nome.trim().toLowerCase(Locale.ROOT) + "%")
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }

    public long contarPorNome(String nome, boolean incluirInativos) {
        return entityManager.createQuery(
                        "SELECT count(u) FROM Usuario u " + filtroNome(incluirInativos), Long.class)
                .setParameter("nome", "%" + nome.trim().toLowerCase(Locale.ROOT) + "%")
                .getSingleResult();
    }

    private String filtroNome(boolean incluirInativos) {
        return filtroAtivo("u", incluirInativos)
                + (incluirInativos ? "WHERE " : "AND ") + "lower(u.nome) LIKE :nome ";
    }

    public Usuario buscarPorEmail(String email) {
        try {
            return entityManager.createQuery(
                    "SELECT u FROM Usuario u WHERE u.email = :email",
                    Usuario.class
            ).setParameter("email", email).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public long contarAtivosPorPerfil(Perfil perfil) {
        return entityManager.createQuery(
                "SELECT count(u) FROM Usuario u WHERE u.perfil = :perfil AND (u.ativo IS NULL OR u.ativo = true)",
                Long.class
        ).setParameter("perfil", perfil).getSingleResult();
    }
}
