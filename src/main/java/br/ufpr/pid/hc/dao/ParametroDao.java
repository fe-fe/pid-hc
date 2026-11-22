package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Parametro;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.Optional;

@ApplicationScoped
public class ParametroDao {

    @PersistenceContext
    private EntityManager entityManager;

    public Optional<String> buscar(String chave) {
        return Optional.ofNullable(entityManager.find(Parametro.class, chave)).map(Parametro::getValor);
    }

    public void salvar(String chave, String valor) {
        Parametro parametro = entityManager.find(Parametro.class, chave);
        if (parametro == null) {
            parametro = new Parametro();
            parametro.setChave(chave);
            parametro.setValor(valor);
            entityManager.persist(parametro);
            return;
        }
        parametro.setValor(valor);
    }
}
