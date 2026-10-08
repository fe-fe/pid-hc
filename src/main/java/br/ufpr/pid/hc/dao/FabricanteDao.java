package br.ufpr.pid.hc.dao;

import br.ufpr.pid.hc.entity.Fabricante;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ApplicationScoped
public class FabricanteDao extends AbstractDao<Fabricante, UUID> {
    public FabricanteDao() { super(Fabricante.class); }

    public Optional<Fabricante> buscarPorNome(String nome) {
        return entityManager.createQuery(
                        "SELECT f FROM Fabricante f WHERE upper(f.nome) = upper(:nome)", Fabricante.class)
                .setParameter("nome", nome)
                .getResultStream()
                .findFirst();
    }

    public List<String> listarNomes() {
        return entityManager.createQuery(
                "SELECT f.nome FROM Fabricante f " + filtroAtivo("f", false) + "ORDER BY f.nome",
                String.class
        ).getResultList();
    }
}
