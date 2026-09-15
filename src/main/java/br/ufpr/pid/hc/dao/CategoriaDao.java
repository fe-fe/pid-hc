package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Categoria;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;

import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class CategoriaDao extends AbstractDao<Categoria, UUID> {
    public CategoriaDao() { super(Categoria.class); }
}