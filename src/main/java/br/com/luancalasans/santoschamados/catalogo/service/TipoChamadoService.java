package br.com.luancalasans.santoschamados.catalogo.service;

import br.com.luancalasans.santoschamados.auditoria.service.AuditoriaService;
import br.com.luancalasans.santoschamados.catalogo.entity.TipoChamado;
import br.com.luancalasans.santoschamados.catalogo.repository.TipoChamadoRepository;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import br.com.luancalasans.santoschamados.infrastructure.exception.ResourceNotFoundException;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.shared.Authorities;
import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Locale;

@Service
public class TipoChamadoService {

    private final TipoChamadoRepository tipoChamadoRepository;
    private final AuditoriaService auditoriaService;

    public TipoChamadoService(TipoChamadoRepository tipoChamadoRepository, AuditoriaService auditoriaService) {
        this.tipoChamadoRepository = tipoChamadoRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public List<TipoChamado> listarAtivos() {
        return tipoChamadoRepository.findByAtivoTrueOrderByOrdemAsc();
    }

    @Transactional(readOnly = true)
    public List<TipoChamado> listarTodos(UsuarioAutenticado user) {
        requireManage(user);
        return tipoChamadoRepository.findAllByOrderByOrdemAsc();
    }

    @Transactional
    public TipoChamado salvar(Long id, String codigo, String nome, String descricao, boolean ativo, int ordem,
                              UsuarioAutenticado user, String ip) {
        requireManage(user);
        TipoChamado tipo = id == null ? new TipoChamado() : tipoChamadoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo não encontrado"));

        String codigoNorm = codigo.trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9_]", "_");
        if (id == null && tipoChamadoRepository.existsByCodigo(codigoNorm)) {
            throw new BusinessException("Já existe um tipo com este código.");
        }
        if (id == null) {
            tipo.setCodigo(codigoNorm);
            tipo.setCriadoEm(Instant.now());
        }
        tipo.setNome(nome.trim());
        tipo.setDescricao(descricao);
        tipo.setAtivo(ativo);
        tipo.setOrdem(ordem);
        tipo.setAtualizadoEm(Instant.now());
        TipoChamado salvo = tipoChamadoRepository.save(tipo);

        auditoriaService.registrar(
                TipoEventoAuditoria.TIPO_CHAMADO_ALTERADO,
                user.getId(),
                "TIPO_CHAMADO",
                salvo.getCodigo(),
                "SUCESSO",
                ip,
                null
        );
        return salvo;
    }

    private void requireManage(UsuarioAutenticado user) {
        if (!user.possuiAuthority(Authorities.TIPO_CHAMADO_GERENCIAR)) {
            throw new AccessDeniedException("Sem permissão para gerenciar tipos");
        }
    }
}
