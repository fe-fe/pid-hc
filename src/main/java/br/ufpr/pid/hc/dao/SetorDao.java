package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Setor;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class SetorDao extends AbstractDao<Setor, UUID> {
    public SetorDao() { super(Setor.class); }
}