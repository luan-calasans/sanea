package br.com.luancalasans.santoschamados.auditoria.service;

import br.com.luancalasans.santoschamados.auditoria.entity.AuditoriaEvento;
import br.com.luancalasans.santoschamados.auditoria.repository.AuditoriaEventoRepository;
import br.com.luancalasans.santoschamados.shared.Authorities;
import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import br.com.luancalasans.santoschamados.usuario.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditoriaService {

    private final AuditoriaEventoRepository auditoriaEventoRepository;
    private final UsuarioRepository usuarioRepository;

    public AuditoriaService(
            AuditoriaEventoRepository auditoriaEventoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.auditoriaEventoRepository = auditoriaEventoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public void registrar(
            TipoEventoAuditoria tipo,
            Long usuarioId,
            String recursoTipo,
            String recursoId,
            String resultado,
            String ip,
            String detalhes
    ) {
        AuditoriaEvento evento = new AuditoriaEvento();
        evento.setTipoEvento(tipo);
        if (usuarioId != null) {
            usuarioRepository.findById(usuarioId).ifPresent(evento::setUsuario);
        }
        evento.setRecursoTipo(recursoTipo);
        evento.setRecursoId(recursoId);
        evento.setResultado(resultado);
        evento.setIpAddress(ip);
        if (detalhes != null && detalhes.length() > 1000) {
            evento.setDetalhes(detalhes.substring(0, 1000));
        } else {
            evento.setDetalhes(detalhes);
        }
        auditoriaEventoRepository.save(evento);
    }

    @Transactional(readOnly = true)
    public Page<AuditoriaEvento> listar(TipoEventoAuditoria tipo, Pageable pageable, Usuario solicitante) {
        if (solicitante.getPerfil().getAuthorities().stream()
                .noneMatch(a -> Authorities.AUDITORIA_VISUALIZAR.equals(a.getCodigo()))) {
            throw new AccessDeniedException("Sem permissão para visualizar auditoria");
        }
        return auditoriaEventoRepository.buscar(tipo, pageable);
    }
}
