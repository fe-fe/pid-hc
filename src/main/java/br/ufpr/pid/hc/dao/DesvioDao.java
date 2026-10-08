package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Desvio;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class DesvioDao extends AbstractDao<Desvio, UUID> {
    public DesvioDao() { super(Desvio.class); }
}
