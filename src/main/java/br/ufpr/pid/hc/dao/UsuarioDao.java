package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Usuario;
import jakarta.persistence.NoResultException;

import java.util.UUID;

public class UsuarioDao extends AbstractDao<Usuario, UUID> {

    public UsuarioDao() {
        super(Usuario.class);
    }

    public Usuario buscarPorEmail(String email) {
        try {
            return entityManager.createQuery(
                    "SELECT u FROM Usuario WHERE u.email = :email",
                    Usuario.class
            ).setParameter("email", email).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}
