package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Material;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class MaterialDao extends AbstractDao<Material, UUID> {
    public MaterialDao() { super(Material.class); }
}