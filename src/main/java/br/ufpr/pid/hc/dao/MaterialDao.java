package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Material;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class MaterialDao extends AbstractDao<Material, UUID> {
    public MaterialDao() { super(Material.class); }

    public Optional<Material> buscarPorCodigo(String codigo) {
        return entityManager.createQuery(
                        "SELECT m FROM Material m WHERE m.codigo = :codigo", Material.class)
                .setParameter("codigo", codigo)
                .getResultStream()
                .findFirst();
    }
}
