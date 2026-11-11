package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Desvio;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

@ApplicationScoped
public class DesvioDao extends AbstractDao<Desvio, UUID> {
    public DesvioDao() { super(Desvio.class); }

    public List<String> listarVigihospExistentes(Collection<String> vigihosps) {
        if (vigihosps.isEmpty()) {
            return List.of();
        }
        return entityManager.createQuery(
                        "SELECT d.vigihosp FROM Desvio d WHERE upper(d.vigihosp) IN :vigihosps", String.class)
                .setParameter("vigihosps", vigihosps.stream().map(String::toUpperCase).toList())
                .getResultList();
    }
}
