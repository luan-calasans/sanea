package br.com.luancalasans.santoschamados.usuario.service;

import br.com.luancalasans.santoschamados.auditoria.service.AuditoriaService;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import br.com.luancalasans.santoschamados.infrastructure.exception.ResourceNotFoundException;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.perfil.entity.Perfil;
import br.com.luancalasans.santoschamados.perfil.repository.PerfilRepository;
import br.com.luancalasans.santoschamados.shared.Authorities;
import br.com.luancalasans.santoschamados.shared.CodigoPerfil;
import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import br.com.luancalasans.santoschamados.usuario.repository.UsuarioRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class UsuarioAdminService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditoriaService auditoriaService;

    public UsuarioAdminService(
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            PasswordEncoder passwordEncoder,
            AuditoriaService auditoriaService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public Page<Usuario> listar(String termo, String perfilCodigo, Boolean ativo, Pageable pageable, UsuarioAutenticado admin) {
        requireAdmin(admin);
        return usuarioRepository.buscar(blank(termo), blank(perfilCodigo), ativo, pageable);
    }

    @Transactional
    public Usuario criarInterno(
            String nome,
            String email,
            String senha,
            String perfilCodigo,
            UsuarioAutenticado admin,
            String ip
    ) {
        requireAdmin(admin);
        if (CodigoPerfil.CIDADAO.name().equals(perfilCodigo)) {
            throw new BusinessException("Use o cadastro público para cidadãos.");
        }
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Já existe um usuário com este e-mail.");
        }
        Perfil perfil = perfilRepository.findByCodigo(perfilCodigo)
                .orElseThrow(() -> new BusinessException("Perfil inválido."));

        Usuario usuario = new Usuario();
        usuario.setNome(nome.trim());
        usuario.setEmail(email.trim().toLowerCase());
        usuario.setSenhaHash(passwordEncoder.encode(senha));
        usuario.setAtivo(true);
        usuario.setEmailVerificado(true);
        usuario.setDeveTrocarSenha(true);
        usuario.setPerfil(perfil);
        usuario.setCriadoEm(Instant.now());
        usuario.setAtualizadoEm(Instant.now());
        Usuario salvo = usuarioRepository.save(usuario);

        auditoriaService.registrar(
                TipoEventoAuditoria.USUARIO_CRIADO,
                admin.getId(),
                "USUARIO",
                String.valueOf(salvo.getId()),
                "SUCESSO",
                ip,
                perfilCodigo
        );
        return salvo;
    }

    @Transactional
    public void alterarAtivo(Long id, boolean ativo, UsuarioAutenticado admin, String ip) {
        requireAdmin(admin);
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (!ativo && CodigoPerfil.ADMIN.name().equals(usuario.getPerfil().getCodigo())) {
            long ativos = usuarioRepository.countAtivosPorPerfil(CodigoPerfil.ADMIN.name());
            if (ativos <= 1) {
                throw new BusinessException("Não é permitido desativar a última conta ADMIN ativa.");
            }
        }

        usuario.setAtivo(ativo);
        usuario.setDesativadoEm(ativo ? null : Instant.now());
        usuario.setAtualizadoEm(Instant.now());
        usuarioRepository.save(usuario);

        auditoriaService.registrar(
                ativo ? TipoEventoAuditoria.USUARIO_ATIVADO : TipoEventoAuditoria.USUARIO_DESATIVADO,
                admin.getId(),
                "USUARIO",
                String.valueOf(id),
                "SUCESSO",
                ip,
                null
        );
    }

    @Transactional
    public void alterarPerfil(Long id, String perfilCodigo, UsuarioAutenticado admin, String ip) {
        requireAdmin(admin);
        if (!admin.possuiAuthority(Authorities.PERMISSAO_GERENCIAR)) {
            throw new AccessDeniedException("Sem permissão para alterar perfis");
        }
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));
        Perfil perfil = perfilRepository.findByCodigo(perfilCodigo)
                .orElseThrow(() -> new BusinessException("Perfil inválido."));

        if (CodigoPerfil.ADMIN.name().equals(usuario.getPerfil().getCodigo())
                && !CodigoPerfil.ADMIN.name().equals(perfilCodigo)
                && usuarioRepository.countAtivosPorPerfil(CodigoPerfil.ADMIN.name()) <= 1
                && usuario.isAtivo()) {
            throw new BusinessException("Não é permitido remover o último ADMIN ativo.");
        }

        String anterior = usuario.getPerfil().getCodigo();
        usuario.setPerfil(perfil);
        usuario.setAtualizadoEm(Instant.now());
        usuarioRepository.save(usuario);

        auditoriaService.registrar(
                TipoEventoAuditoria.PERMISSAO_ALTERADA,
                admin.getId(),
                "USUARIO",
                String.valueOf(id),
                "SUCESSO",
                ip,
                anterior + "->" + perfilCodigo
        );
    }

    private void requireAdmin(UsuarioAutenticado admin) {
        if (!admin.possuiAuthority(Authorities.USUARIO_GERENCIAR)) {
            throw new AccessDeniedException("Sem permissão administrativa");
        }
    }

    private String blank(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
