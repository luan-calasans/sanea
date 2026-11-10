package br.com.luancalasans.santoschamados.endereco.cep.provider;

import br.com.luancalasans.santoschamados.endereco.cep.dto.EnderecoCep;
import br.com.luancalasans.santoschamados.endereco.cep.dto.ResultadoCep;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class OpenCepProvider extends AbstractHttpCepProvider {

    public OpenCepProvider(RestClient.Builder restClientBuilder) {
        super(restClientBuilder);
    }

    @Override
    public String nome() {
        return "OpenCEP";
    }

    @Override
    @CircuitBreaker(name = "openCep", fallbackMethod = "fallback")
    public ResultadoCep consultar(String cep) {
        return super.consultar(cep);
    }

    @SuppressWarnings("unused")
    private ResultadoCep fallback(String cep, Throwable ex) {
        return new ResultadoCep.Indisponivel(nome(), "Circuit open");
    }

    @Override
    protected String url(String cep) {
        return "https://opencep.com/v1/" + cep + ".json";
    }

    @Override
    protected boolean isNotFound(Map<String, Object> body) {
        return body.containsKey("error") || body.get("city") == null && body.get("localidade") == null;
    }

    @Override
    protected EnderecoCep mapear(Map<String, Object> body, String cep) {
        return normalize(
                firstNonBlank(body, "cep", cep),
                firstNonBlank(body, "logradouro", "street"),
                firstNonBlank(body, "bairro", "district"),
                firstNonBlank(body, "localidade", "city"),
                firstNonBlank(body, "uf", "state")
        );
    }
}
