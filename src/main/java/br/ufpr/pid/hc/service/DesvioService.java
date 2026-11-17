package br.ufpr.pid.hc.service;

import br.ufpr.pid.hc.dao.AbstractDao;
import br.ufpr.pid.hc.dao.DesvioDao;
import br.ufpr.pid.hc.entity.Desvio;
import br.ufpr.pid.hc.enumeration.CampoOrdenacao;
import br.ufpr.pid.hc.exception.DomainException;
import br.ufpr.pid.hc.exception.DuplicateRecordException;
import br.ufpr.pid.hc.exception.MissingRequiredFieldsException;
import jakarta.annotation.security.PermitAll;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.Stateless;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Stateless
public class DesvioService extends AbstractService<Desvio, UUID> {
    @Inject
    private DesvioDao desvioDao;

    @Inject
    private MaterialService materialService;

    @Inject
    private FabricanteService fabricanteService;

    @Inject
    private MarcaService marcaService;

    @Override
    protected AbstractDao<Desvio, UUID> getDao() { return desvioDao; }

    @PermitAll
    public List<Desvio> buscarComFiltros(int pagina, int tamanhoPagina, CampoOrdenacao ordenacao,
            String termo, LocalDate dataInicial, LocalDate dataFinal, boolean incluirInativos) {
        return desvioDao.buscarComFiltros(pagina, tamanhoPagina, ordenacao, termo,
                dataInicial, dataFinal, incluirInativos);
    }

    @PermitAll
    public long contarComFiltros(String termo, LocalDate dataInicial, LocalDate dataFinal,
            boolean incluirInativos) {
        return desvioDao.contarComFiltros(termo, dataInicial, dataFinal, incluirInativos);
    }

    @RolesAllowed({"ADMINISTRADOR", "ANALISTA"})
    public Desvio atualizar(Desvio desvio, String codigoMaterial, String nomeMaterial,
            String fabricante, String marca) {
        if (desvio == null || desvio.getId() == null) {
            throw new DomainException("Selecione um registro de tecnovigilância para editar.");
        }
        if (desvio.getVigihosp() == null || desvio.getVigihosp().isBlank()
                || desvio.getData() == null || desvio.getMotivo() == null || desvio.getMotivo().isBlank()
                || codigoMaterial == null || codigoMaterial.isBlank()
                || fabricante == null || fabricante.isBlank()) {
            throw new MissingRequiredFieldsException();
        }
        if (desvio.getData().isAfter(LocalDate.now())) {
            throw new DomainException("A data da notificação não pode ser futura.");
        }
        String registroAnvisa = desvio.getRegistroAnvisa();
        if (registroAnvisa != null && !registroAnvisa.isBlank() && !registroAnvisa.matches("[0-9]{11}")) {
            throw new DomainException("Registro ANVISA deve ter 11 dígitos, só números.");
        }
        String processoSei = desvio.getProcessoSei();
        if (processoSei != null && !processoSei.isBlank()
                && !processoSei.matches("[0-9]{5}\\.[0-9]{6}/[0-9]{4}-[0-9]{2}")) {
            throw new DomainException("Processo SEI deve estar no formato 00000.000000/0000-00.");
        }
        Desvio existente = desvioDao.buscarPorId(desvio.getId());
        if (existente == null) {
            throw new DomainException("O registro de tecnovigilância não foi encontrado.");
        }
        desvio.setVigihosp(desvio.getVigihosp().trim());
        if (desvioDao.existeOutroVigihosp(desvio.getVigihosp(), desvio.getId())) {
            throw new DuplicateRecordException("Já existe um registro com o VigiHosp " + desvio.getVigihosp());
        }
        desvio.setMaterial(materialService.obterOuCriar(codigoMaterial, nomeMaterial));
        desvio.setFabricante(fabricanteService.obterOuCriar(fabricante));
        desvio.setMarca(marca == null || marca.isBlank() ? null : marcaService.obterOuCriar(marca));
        // A edição mantém a origem da importação e os dados de auditoria do registro.
        desvio.setImportacao(existente.getImportacao());
        desvio.setAtivo(existente.getAtivo());
        desvio.setCriadoEm(existente.getCriadoEm());
        desvio.setCriadoPor(existente.getCriadoPor());
        desvio.setAtualizadoEm(existente.getAtualizadoEm());
        desvio.setAtualizadoPor(existente.getAtualizadoPor());
        return salvar(desvio);
    }
}
