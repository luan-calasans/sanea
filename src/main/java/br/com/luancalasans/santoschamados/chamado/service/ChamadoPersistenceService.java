package br.com.luancalasans.santoschamados.chamado.service;

import br.com.luancalasans.santoschamados.catalogo.entity.TipoChamado;
import br.com.luancalasans.santoschamados.chamado.dto.AberturaChamadoForm;
import br.com.luancalasans.santoschamados.chamado.entity.Chamado;
import br.com.luancalasans.santoschamados.chamado.entity.StatusHistorico;
import br.com.luancalasans.santoschamados.chamado.repository.ChamadoRepository;
import br.com.luancalasans.santoschamados.shared.StatusChamado;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Locale;

@Service
public class ChamadoPersistenceService {

    private final ChamadoRepository chamadoRepository;

    public ChamadoPersistenceService(ChamadoRepository chamadoRepository) {
        this.chamadoRepository = chamadoRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Chamado persistirNovo(
            AberturaChamadoForm form,
            Usuario autor,
            TipoChamado tipo,
            String cep,
            String protocolo
    ) {
        Instant agora = Instant.now();
        Chamado chamado = new Chamado();
        chamado.setProtocolo(protocolo);
        chamado.setUsuario(autor);
        chamado.setTipoChamado(tipo);
        chamado.setStatus(StatusChamado.ABERTO);
        chamado.setDescricao(form.getDescricao().trim());
        if ("OUTROS".equalsIgnoreCase(tipo.getCodigo())) {
            chamado.setTipoOutro(blankToNull(form.getTipoOutro()));
        } else {
            chamado.setTipoOutro(null);
        }
        chamado.setDataOcorrencia(form.getDataOcorrencia());
        chamado.setAbertoEm(agora);
        chamado.setAtualizadoEm(agora);
        chamado.setCep(cep);
        chamado.setLogradouro(form.getLogradouro().trim());
        chamado.setNumero(blankToNull(form.getNumero()));
        chamado.setComplemento(blankToNull(form.getComplemento()));
        chamado.setBairro(form.getBairro().trim());
        chamado.setCidade(form.getCidade().trim());
        chamado.setUf(form.getUf().trim().toUpperCase(Locale.ROOT));
        chamado.setEnderecoVerificado(form.isEnderecoVerificado());

        StatusHistorico historico = new StatusHistorico();
        historico.setStatusAnterior(null);
        historico.setStatusNovo(StatusChamado.ABERTO);
        historico.setAutor(autor);
        historico.setObservacao("Chamado aberto");
        historico.setCriadoEm(agora);
        chamado.adicionarHistorico(historico);

        return chamadoRepository.saveAndFlush(chamado);
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
