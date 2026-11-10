package br.com.luancalasans.santoschamados.infrastructure.config;

import br.com.luancalasans.santoschamados.perfil.entity.Perfil;
import br.com.luancalasans.santoschamados.perfil.repository.PerfilRepository;
import br.com.luancalasans.santoschamados.shared.CodigoPerfil;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import br.com.luancalasans.santoschamados.usuario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
@Order(2)
public class InitialUsersInitializer implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialUsersInitializer.class);

    private final AppProperties appProperties;
    private final UsuarioRepository usuarioRepository;
    private final PerfilRepository perfilRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialUsersInitializer(
            AppProperties appProperties,
            UsuarioRepository usuarioRepository,
            PerfilRepository perfilRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.appProperties = appProperties;
        this.usuarioRepository = usuarioRepository;
        this.perfilRepository = perfilRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        criarSeNecessario(CodigoPerfil.CIDADAO, appProperties.getInitialUsers().getCidadao());
        criarSeNecessario(CodigoPerfil.SABESP, appProperties.getInitialUsers().getSabesp());
        criarSeNecessario(CodigoPerfil.PREFEITURA, appProperties.getInitialUsers().getPrefeitura());
        criarSeNecessario(CodigoPerfil.ADMIN, appProperties.getInitialUsers().getAdmin());
    }

    private void criarSeNecessario(CodigoPerfil codigoPerfil, AppProperties.InitialUser config) {
        if (!config.isConfigured()) {
            if (appProperties.isProduction()) {
                throw new IllegalStateException("Conta inicial " + codigoPerfil + " não configurada");
            }
            log.warn("Conta inicial {} não configurada; pulando em ambiente não produtivo", codigoPerfil);
            return;
        }

        String email = config.getEmail().trim().toLowerCase();
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            log.info("Conta inicial {} já existe; senha preservada", codigoPerfil);
            return;
        }

        Perfil perfil = perfilRepository.findByCodigo(codigoPerfil.name())
                .orElseThrow(() -> new IllegalStateException("Perfil não encontrado: " + codigoPerfil));

        Usuario usuario = new Usuario();
        usuario.setNome(config.getNome());
        usuario.setEmail(email);
        usuario.setSenhaHash(passwordEncoder.encode(config.getPassword()));
        usuario.setAtivo(true);
        usuario.setEmailVerificado(true);
        usuario.setDeveTrocarSenha(true);
        usuario.setPerfil(perfil);
        usuario.setCriadoEm(Instant.now());
        usuario.setAtualizadoEm(Instant.now());
        usuarioRepository.save(usuario);
        log.info("Conta inicial {} criada com exigência de troca de senha", codigoPerfil);
    }
}
