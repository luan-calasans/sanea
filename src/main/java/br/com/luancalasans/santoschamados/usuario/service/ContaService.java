package br.com.luancalasans.santoschamados.usuario.service;

import br.com.luancalasans.santoschamados.auditoria.service.AuditoriaService;
import br.com.luancalasans.santoschamados.auth.service.CadastroService;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import br.com.luancalasans.santoschamados.usuario.aparencia.AparenciaDefaults;
import br.com.luancalasans.santoschamados.usuario.aparencia.TemaModo;
import br.com.luancalasans.santoschamados.usuario.entity.EmailVerificacaoToken;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import br.com.luancalasans.santoschamados.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
public class ContaService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final CadastroService cadastroService;
    private final AuditoriaService auditoriaService;

    public ContaService(
            UsuarioRepository usuarioRepository,
            PasswordEncoder passwordEncoder,
            CadastroService cadastroService,
            AuditoriaService auditoriaService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.cadastroService = cadastroService;
        this.auditoriaService = auditoriaService;
    }

    @Transactional(readOnly = true)
    public Usuario obter(UsuarioAutenticado user) {
        Usuario usuario = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new BusinessException("Conta não encontrada."));
        usuario.getPerfil().getNome();
        return usuario;
    }

    @Transactional
    public void atualizarNome(UsuarioAutenticado user, String nome) {
        Usuario usuario = obter(user);
        if (nome == null || nome.isBlank()) {
            throw new BusinessException("Informe o nome.");
        }
        usuario.setNome(nome.trim());
        usuario.setAtualizadoEm(Instant.now());
        usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario trocarSenha(UsuarioAutenticado user, String senhaAtual, String novaSenha, String confirmacao) {
        Usuario usuario = obter(user);
        if (!passwordEncoder.matches(senhaAtual, usuario.getSenhaHash())) {
            throw new BusinessException("Senha atual incorreta.");
        }
        if (novaSenha == null || novaSenha.length() < 8) {
            throw new BusinessException("A nova senha deve ter pelo menos 8 caracteres.");
        }
        if (!novaSenha.equals(confirmacao)) {
            throw new BusinessException("A confirmação da nova senha não confere.");
        }
        usuario.setSenhaHash(passwordEncoder.encode(novaSenha));
        usuario.setDeveTrocarSenha(false);
        usuario.setAtualizadoEm(Instant.now());
        Usuario salvo = usuarioRepository.save(usuario);
        salvo.getPerfil().getAuthorities().size();
        auditoriaService.registrar(
                TipoEventoAuditoria.ALTERACAO_SENHA,
                user.getId(),
                "USUARIO",
                String.valueOf(user.getId()),
                "SUCESSO",
                null,
                null
        );
        return salvo;
    }

    @Transactional
    public void solicitarAlteracaoEmail(UsuarioAutenticado user, String novoEmail) {
        Usuario usuario = obter(user);
        if (novoEmail == null || novoEmail.isBlank()) {
            throw new BusinessException("Informe o novo e-mail.");
        }
        String email = novoEmail.trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Não foi possível solicitar a alteração com o e-mail informado.");
        }
        cadastroService.criarEEnviarToken(usuario, EmailVerificacaoToken.PROPOSITO_ALTERACAO_EMAIL, email);
        auditoriaService.registrar(
                TipoEventoAuditoria.SOLICITACAO_ALTERACAO_EMAIL,
                user.getId(),
                "USUARIO",
                String.valueOf(user.getId()),
                "SUCESSO",
                null,
                null
        );
    }

    @Transactional
    public Usuario atualizarAparencia(
            UsuarioAutenticado user,
            String temaModo,
            String corSidebar,
            String corDestaque,
            String corFonte
    ) {
        Usuario usuario = obter(user);
        TemaModo modo = TemaModo.from(temaModo);
        if (!AparenciaDefaults.isHexValido(corSidebar)) {
            throw new BusinessException("Cor do menu lateral inválida.");
        }
        if (!AparenciaDefaults.isHexValido(corDestaque)) {
            throw new BusinessException("Cor de destaque inválida.");
        }
        if (!AparenciaDefaults.isHexValido(corFonte)) {
            throw new BusinessException("Cor da fonte inválida.");
        }
        usuario.setTemaModo(modo.name());
        usuario.setCorSidebar(AparenciaDefaults.normalizarHex(corSidebar, AparenciaDefaults.COR_SIDEBAR));
        usuario.setCorDestaque(AparenciaDefaults.normalizarHex(corDestaque, AparenciaDefaults.COR_DESTAQUE));
        usuario.setCorFonte(AparenciaDefaults.normalizarHex(corFonte, AparenciaDefaults.COR_FONTE));
        usuario.setAtualizadoEm(Instant.now());
        Usuario salvo = usuarioRepository.save(usuario);
        salvo.getPerfil().getAuthorities().size();
        return salvo;
    }

    @Transactional
    public Usuario redefinirAparencia(UsuarioAutenticado user) {
        Usuario usuario = obter(user);
        usuario.setTemaModo(AparenciaDefaults.TEMA.name());
        usuario.setCorSidebar(null);
        usuario.setCorDestaque(null);
        usuario.setCorFonte(null);
        usuario.setAtualizadoEm(Instant.now());
        Usuario salvo = usuarioRepository.save(usuario);
        salvo.getPerfil().getAuthorities().size();
        return salvo;
    }
}
