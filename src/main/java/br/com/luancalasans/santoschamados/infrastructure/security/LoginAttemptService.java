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
    public boolean isBlocked(String ip) {
        return loginTentativaRepository
                .findFirstByIpAddressAndBloqueadoAteAfterOrderByCriadoEmDesc(ip, Instant.now())
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
    public LoginAttemptResult registerFailure(String email, String ip) {
        Instant agora = Instant.now();
        int maxAttempts = appProperties.getLogin().getMaxAttempts();
        int lockMinutes = appProperties.getLogin().getLockMinutes();

        Instant desde = agora.minus(lockMinutes, ChronoUnit.MINUTES);
        Instant ultimoBloqueio = loginTentativaRepository.findUltimoBloqueadoAtePorIp(ip).orElse(null);
        if (ultimoBloqueio != null && ultimoBloqueio.isAfter(desde) && !ultimoBloqueio.isAfter(agora)) {
            desde = ultimoBloqueio;
        }

        long falhas = loginTentativaRepository.contarFalhasRecentesPorIp(ip, desde) + 1;
        boolean bloqueado = falhas >= maxAttempts;

        LoginTentativa tentativa = new LoginTentativa();
        tentativa.setEmailNormalizado(normalize(email));
        tentativa.setIpAddress(ip);
        tentativa.setSucesso(false);
        if (bloqueado) {
            tentativa.setBloqueadoAte(agora.plus(lockMinutes, ChronoUnit.MINUTES));
        }
        loginTentativaRepository.save(tentativa);

        int restantes = (int) Math.max(0, maxAttempts - falhas);
        return new LoginAttemptResult(falhas, restantes, bloqueado);
    }

    private String normalize(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    public record LoginAttemptResult(long falhas, int restantes, boolean bloqueado) {
    }
}
