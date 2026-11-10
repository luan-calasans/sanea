package br.com.luancalasans.santoschamados.infrastructure.recaptcha;

import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class RecaptchaService {

    private static final Logger log = LoggerFactory.getLogger(RecaptchaService.class);

    private final AppProperties appProperties;
    private final RestClient restClient;

    public RecaptchaService(AppProperties appProperties, RestClient.Builder restClientBuilder) {
        this.appProperties = appProperties;
        this.restClient = restClientBuilder.build();
    }

    public void validate(String responseToken, String remoteIp) {
        if (!appProperties.getRecaptcha().isEnabled()) {
            return;
        }
        if (responseToken == null || responseToken.isBlank()) {
            throw new BusinessException("Confirme o reCAPTCHA para continuar.");
        }
        if (appProperties.getRecaptcha().getSecretKey() == null
                || appProperties.getRecaptcha().getSecretKey().isBlank()
                || appProperties.getRecaptcha().getSecretKey().startsWith("CHANGE_ME")) {
            throw new BusinessException("reCAPTCHA não configurado corretamente no servidor.");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("secret", appProperties.getRecaptcha().getSecretKey());
        form.add("response", responseToken);
        if (remoteIp != null && !remoteIp.isBlank()) {
            form.add("remoteip", remoteIp);
        }

        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.post()
                    .uri(appProperties.getRecaptcha().getVerifyUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(Map.class);

            boolean success = body != null && Boolean.TRUE.equals(body.get("success"));
            if (!success) {
                throw new BusinessException("Validação do reCAPTCHA falhou. Tente novamente.");
            }
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            log.warn("Falha ao validar reCAPTCHA: {}", ex.getMessage());
            throw new BusinessException("Não foi possível validar o reCAPTCHA no momento.");
        }
    }
}
