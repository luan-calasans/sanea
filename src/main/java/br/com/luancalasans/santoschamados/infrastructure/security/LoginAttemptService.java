package br.com.luancalasans.santoschamados.infrastructure.security;

import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.usuario.entity.LoginTentativa;
import br.com.luancalasans.santoschamados.usuario.repository.LoginTentativaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class LoginAttemptService {

    private final LoginTentativaRepository loginTentativaRepository;
    private final AppProperties appProperties;

    public LoginAttemptService(LoginTentativaRepository loginTentativaRepository, AppProperties appProperties) {
        this.loginTentativaRepository = loginTentativaRepository;
        this.appProperties = appProperties;
    }

    @Transactional(readOnly = true)
    public boolean isBlocked(String email, String ip) {
        String normalized = normalize(email);
        return loginTentativaRepository
                .findFirstByEmailNormalizadoAndIpAddressAndBloqueadoAteAfterOrderByCriadoEmDesc(
                        normalized, ip, Instant.now()
                )
                .isPresent();
    }

    @Transactional
    public void registerSuccess(String email, String ip) {
        LoginTentativa tentativa = new LoginTentativa();
        tentativa.setEmailNormalizado(normalize(email));
        tentativa.setIpAddress(ip);
        tentativa.setSucesso(true);
        loginTentativaRepository.save(tentativa);
    }

    @Transactional
    public void registerFailure(String email, String ip) {
        String normalized = normalize(email);
        Instant desde = Instant.now().minus(
                appProperties.getLogin().getLockMinutes(),
                ChronoUnit.MINUTES
        );
        long falhas = loginTentativaRepository.contarFalhasRecentes(normalized, ip, desde) + 1;

        LoginTentativa tentativa = new LoginTentativa();
        tentativa.setEmailNormalizado(normalized);
        tentativa.setIpAddress(ip);
        tentativa.setSucesso(false);
        if (falhas >= appProperties.getLogin().getMaxAttempts()) {
            tentativa.setBloqueadoAte(Instant.now().plus(
                    appProperties.getLogin().getLockMinutes(),
                    ChronoUnit.MINUTES
            ));
        }
        loginTentativaRepository.save(tentativa);
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
