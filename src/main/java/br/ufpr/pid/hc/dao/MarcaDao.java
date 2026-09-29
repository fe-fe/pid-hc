package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Marca;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class MarcaDao extends AbstractDao<Marca, UUID> {
    public MarcaDao() { super(Marca.class); }
}
