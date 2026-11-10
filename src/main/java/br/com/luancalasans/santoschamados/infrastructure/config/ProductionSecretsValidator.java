package br.com.luancalasans.santoschamados.infrastructure.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Order(1)
public class ProductionSecretsValidator implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ProductionSecretsValidator.class);

    private final AppProperties appProperties;
    private final Environment environment;

    public ProductionSecretsValidator(AppProperties appProperties, Environment environment) {
        this.appProperties = appProperties;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!appProperties.isProduction()) {
            return;
        }

        List<String> problemas = new ArrayList<>();
        String dbPassword = environment.getProperty("spring.datasource.password", "");
        if (dbPassword.isBlank() || dbPassword.startsWith("CHANGE_ME")) {
            problemas.add("POSTGRES_PASSWORD/SPRING_DATASOURCE_PASSWORD");
        }
        if (appProperties.getRecaptcha().isEnabled()
                && (isPlaceholder(appProperties.getRecaptcha().getSecretKey())
                || isPlaceholder(appProperties.getRecaptcha().getSiteKey()))) {
            problemas.add("RECAPTCHA_SITE_KEY/RECAPTCHA_SECRET_KEY");
        }
        if (isPlaceholder(environment.getProperty("SMTP_HOST", ""))
                || isPlaceholder(environment.getProperty("SMTP_PASSWORD", ""))) {
            problemas.add("SMTP_*");
        }
        validateInitial(appProperties.getInitialUsers().getAdmin(), "INITIAL_ADMIN", problemas);
        validateInitial(appProperties.getInitialUsers().getSabesp(), "INITIAL_SABESP", problemas);
        validateInitial(appProperties.getInitialUsers().getPrefeitura(), "INITIAL_PREFEITURA", problemas);
        validateInitial(appProperties.getInitialUsers().getCidadao(), "INITIAL_CIDADAO", problemas);

        if (!problemas.isEmpty()) {
            throw new IllegalStateException(
                    "Produção não pode iniciar com segredos placeholder. Configure: " + String.join(", ", problemas)
            );
        }
        log.info("Validação de segredos de produção concluída");
    }

    private void validateInitial(AppProperties.InitialUser user, String prefix, List<String> problemas) {
        if (!user.isConfigured()) {
            problemas.add(prefix + "_EMAIL/PASSWORD");
        }
    }

    private boolean isPlaceholder(String value) {
        return value == null || value.isBlank() || value.startsWith("CHANGE_ME");
    }
}
