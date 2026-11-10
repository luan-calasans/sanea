package br.com.luancalasans.santoschamados.endereco.cep.provider;

import br.com.luancalasans.santoschamados.endereco.cep.dto.EnderecoCep;
import br.com.luancalasans.santoschamados.endereco.cep.dto.ResultadoCep;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class BrasilApiCepProvider extends AbstractHttpCepProvider {

    public BrasilApiCepProvider(RestClient.Builder restClientBuilder) {
        super(restClientBuilder);
    }

    @Override
    public String nome() {
        return "BrasilAPI";
    }

    @Override
    @CircuitBreaker(name = "brasilApi", fallbackMethod = "fallback")
    public ResultadoCep consultar(String cep) {
        return super.consultar(cep);
    }

    @SuppressWarnings("unused")
    private ResultadoCep fallback(String cep, Throwable ex) {
        return new ResultadoCep.Indisponivel(nome(), "Circuit open");
    }

    @Override
    protected String url(String cep) {
        return "https://brasilapi.com.br/api/cep/v1/" + cep;
    }

    @Override
    protected boolean isNotFound(Map<String, Object> body) {
        return body.containsKey("message") && !body.containsKey("city") && !body.containsKey("localidade");
    }

    @Override
    protected EnderecoCep mapear(Map<String, Object> body, String cep) {
        return normalize(
                firstNonBlank(body, "cep", cep),
                firstNonBlank(body, "street", "logradouro"),
                firstNonBlank(body, "neighborhood", "bairro"),
                firstNonBlank(body, "city", "localidade"),
                firstNonBlank(body, "state", "uf")
        );
    }
}
