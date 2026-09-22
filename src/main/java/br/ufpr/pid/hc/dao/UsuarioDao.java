package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Usuario;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
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

    @Override
    public List<Usuario> buscar(int pagina, int tamanhoPagina, CampoOrdenacao campoOrdenacao) {
        return entityManager.createQuery(
                        "SELECT u FROM Usuario u " +
                                "LEFT JOIN FETCH u.setor " +
                                "ORDER BY u." + campoOrdenacao.getCampoBanco(),
                        Usuario.class
                )
                .setFirstResult(pagina * tamanhoPagina)
                .setMaxResults(tamanhoPagina)
                .getResultList();
    }
}
