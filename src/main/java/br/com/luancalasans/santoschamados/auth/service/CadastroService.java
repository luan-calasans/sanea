package br.com.luancalasans.santoschamados.auth.service;

import br.com.luancalasans.santoschamados.auditoria.service.AuditoriaService;
import br.com.luancalasans.santoschamados.auth.dto.CadastroCidadaoForm;
import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import br.com.luancalasans.santoschamados.infrastructure.mail.EmailService;
import br.com.luancalasans.santoschamados.infrastructure.recaptcha.RecaptchaService;
import br.com.luancalasans.santoschamados.perfil.repository.PerfilRepository;
import br.com.luancalasans.santoschamados.shared.CodigoPerfil;
import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import br.com.luancalasans.santoschamados.usuario.entity.EmailVerificacaoToken;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import br.com.luancalasans.santoschamados.usuario.repository.EmailVerificacaoTokenRepository;
import br.com.luancalasans.santoschamados.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;

@Service
public class CadastroService {

    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final EmailVerificacaoTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RecaptchaService recaptchaService;
    private final AppProperties appProperties;
    private final AuditoriaService auditoriaService;
    private final SecureRandom secureRandom = new SecureRandom();

    public CadastroService(
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            EmailVerificacaoTokenRepository tokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService,
            RecaptchaService recaptchaService,
            AppProperties appProperties,
            AuditoriaService auditoriaService
    ) {
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.recaptchaService = recaptchaService;
        this.appProperties = appProperties;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public void cadastrarCidadao(CadastroCidadaoForm form, String ip) {
        recaptchaService.validate(form.getRecaptchaToken(), ip);

        if (!form.getSenha().equals(form.getConfirmacaoSenha())) {
            throw new BusinessException("A confirmação de senha não confere.");
        }
        if (form.getSenha().length() < 8) {
            throw new BusinessException("A senha deve ter pelo menos 8 caracteres.");
        }

        String email = form.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException("Não foi possível concluir o cadastro com os dados informados.");
        }

        var perfil = perfilRepository.findByCodigo(CodigoPerfil.CIDADAO.name())
                .orElseThrow(() -> new BusinessException("Perfil de cidadão não configurado."));

        Usuario usuario = new Usuario();
        usuario.setNome(form.getNome().trim());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(form.getSenha()));
        usuario.setAtivo(false);
        usuario.setEmailVerificado(false);
        usuario.setDeveTrocarSenha(false);
        usuario.setPerfil(perfil);
        usuario.setCriadoEm(Instant.now());
        usuario.setAtualizadoEm(Instant.now());
        usuarioRepository.save(usuario);

        String token = criarEEnviarToken(usuario, EmailVerificacaoToken.PROPOSITO_CADASTRO, null);

        auditoriaService.registrar(
                TipoEventoAuditoria.CADASTRO_CIDADAO,
                usuario.getId(),
                "USUARIO",
                String.valueOf(usuario.getId()),
                "SUCESSO",
                ip,
                "Cadastro pendente de verificação"
        );

        if (token.isBlank()) {
            throw new BusinessException("Falha ao gerar token de verificação.");
        }
    }

    @Transactional
    public void verificarEmail(String tokenRaw) {
        if (tokenRaw == null || tokenRaw.isBlank()) {
            throw new BusinessException("Link de verificação inválido ou expirado.");
        }
        String hash = hashToken(tokenRaw);
        EmailVerificacaoToken token = tokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BusinessException("Link de verificação inválido ou expirado."));

        if (token.isUsado() || token.isExpirado()) {
            throw new BusinessException("Link de verificação inválido ou expirado.");
        }

        Usuario usuario = token.getUsuario();
        if (EmailVerificacaoToken.PROPOSITO_CADASTRO.equals(token.getProposito())) {
            usuario.setEmailVerificado(true);
            usuario.setAtivo(true);
        } else if (EmailVerificacaoToken.PROPOSITO_ALTERACAO_EMAIL.equals(token.getProposito())) {
            if (token.getNovoEmail() == null || token.getNovoEmail().isBlank()) {
                throw new BusinessException("Link de verificação inválido ou expirado.");
            }
            if (usuarioRepository.existsByEmailIgnoreCase(token.getNovoEmail())) {
                throw new BusinessException("Não foi possível concluir a alteração de e-mail.");
            }
            usuario.setEmail(token.getNovoEmail().toLowerCase());
            usuario.setEmailVerificado(true);
        } else {
            throw new BusinessException("Link de verificação inválido ou expirado.");
        }

        usuario.setAtualizadoEm(Instant.now());
        token.setUsadoEm(Instant.now());
        usuarioRepository.save(usuario);
        tokenRepository.save(token);

        auditoriaService.registrar(
                TipoEventoAuditoria.EMAIL_VERIFICADO,
                usuario.getId(),
                "USUARIO",
                String.valueOf(usuario.getId()),
                "SUCESSO",
                null,
                token.getProposito()
        );
    }

    @Transactional
    public String criarEEnviarToken(Usuario usuario, String proposito, String novoEmail) {
        tokenRepository.invalidarTokensPendentes(usuario.getId(), proposito);

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);
        String tokenRaw = HexFormat.of().formatHex(bytes);
        String hash = hashToken(tokenRaw);

        EmailVerificacaoToken token = new EmailVerificacaoToken();
        token.setUsuario(usuario);
        token.setTokenHash(hash);
        token.setProposito(proposito);
        token.setNovoEmail(novoEmail);
        token.setExpiraEm(Instant.now().plus(24, ChronoUnit.HOURS));
        token.setCriadoEm(Instant.now());
        tokenRepository.save(token);

        String link = appProperties.getBaseUrl().replaceAll("/$", "") + "/verificar-email?token=" + tokenRaw;
        String destino = novoEmail != null ? novoEmail : usuario.getEmail();
        if (EmailVerificacaoToken.PROPOSITO_ALTERACAO_EMAIL.equals(proposito)) {
            emailService.enviarVerificacaoAlteracaoEmail(destino, usuario.getNome(), link);
        } else {
            emailService.enviarVerificacaoCadastro(destino, usuario.getNome(), link);
        }
        return tokenRaw;
    }

    public static String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (Exception ex) {
            throw new IllegalStateException("Falha ao gerar hash de token", ex);
        }
    }
}
