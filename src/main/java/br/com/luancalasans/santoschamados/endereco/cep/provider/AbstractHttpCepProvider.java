package br.com.luancalasans.santoschamados.endereco.cep.provider;

import br.com.luancalasans.santoschamados.endereco.cep.dto.EnderecoCep;
import br.com.luancalasans.santoschamados.endereco.cep.dto.ResultadoCep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Map;
import java.util.function.Function;

public abstract class AbstractHttpCepProvider implements CepProvider {

    private final Logger log = LoggerFactory.getLogger(getClass());
    private final RestClient restClient;

    protected AbstractHttpCepProvider(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    @Override
    public ResultadoCep consultar(String cep) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> body = restClient.get()
                    .uri(url(cep))
                    .retrieve()
                    .onStatus(status -> status.value() == 404, (req, res) -> {
                    })
                    .body(Map.class);

            if (body == null || body.isEmpty() || isNotFound(body)) {
                return new ResultadoCep.NaoEncontrado(nome());
            }

            EnderecoCep endereco = mapear(body, cep);
            if (endereco == null || endereco.cidade() == null || endereco.cidade().isBlank()
                    || endereco.uf() == null || endereco.uf().isBlank()) {
                return new ResultadoCep.Indisponivel(nome(), "Resposta malformada");
            }
            return new ResultadoCep.Encontrado(endereco, nome());
        } catch (RestClientException ex) {
            log.warn("Provedor CEP {} indisponivel: {}", nome(), ex.getMessage());
            return new ResultadoCep.Indisponivel(nome(), "Falha de comunicacao");
        } catch (Exception ex) {
            log.warn("Provedor CEP {} erro inesperado: {}", nome(), ex.getMessage());
            return new ResultadoCep.Indisponivel(nome(), "Erro inesperado");
        }
    }

    protected abstract String url(String cep);

    protected abstract boolean isNotFound(Map<String, Object> body);

    protected abstract EnderecoCep mapear(Map<String, Object> body, String cep);

    protected String text(Map<String, Object> body, String key) {
        Object value = body.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    protected String firstNonBlank(Map<String, Object> body, String... keys) {
        for (String key : keys) {
            String value = text(body, key);
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }

    protected EnderecoCep normalize(
            String cep,
            String logradouro,
            String bairro,
            String cidade,
            String uf
    ) {
        String ufNorm = uf == null ? null : uf.trim().toUpperCase();
        String cepNorm = cep == null ? null : cep.replaceAll("\\D", "");
        return new EnderecoCep(
                cepNorm,
                emptyToNull(logradouro),
                emptyToNull(bairro),
                emptyToNull(cidade),
                ufNorm
        );
    }

    private String emptyToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    protected <T> T safe(Function<Map<String, Object>, T> fn, Map<String, Object> body) {
        return fn.apply(body);
    }
}
