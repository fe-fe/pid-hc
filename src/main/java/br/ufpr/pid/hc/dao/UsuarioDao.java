package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import br.ufpr.pid.hc.enumeration.Perfil;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class UsuarioDao extends AbstractDao<Usuario, UUID> {

    public UsuarioDao() {
        super(Usuario.class);
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

    @Override
    public List<Usuario> buscar(int pagina, int tamanhoPagina, CampoOrdenacao campoOrdenacao, boolean incluirInativos) {
        return entityManager.createQuery(
                        "SELECT u FROM Usuario u " +
                                "LEFT JOIN FETCH u.setor " +
                                filtroAtivo("u", incluirInativos) +
                                "ORDER BY u." + campoOrdenacao.getCampoBanco(),
                        Usuario.class
                )
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }
}
