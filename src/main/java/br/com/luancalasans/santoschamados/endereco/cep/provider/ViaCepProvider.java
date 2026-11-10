package br.com.luancalasans.santoschamados.endereco.cep.provider;

import br.com.luancalasans.santoschamados.endereco.cep.dto.EnderecoCep;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Component
public class ViaCepProvider extends AbstractHttpCepProvider {

    public ViaCepProvider(RestClient.Builder restClientBuilder) {
        super(restClientBuilder);
    }

    @Override
    public String nome() {
        return "ViaCEP";
    }

    @Override
    @CircuitBreaker(name = "viaCep", fallbackMethod = "fallback")
    public br.com.luancalasans.santoschamados.endereco.cep.dto.ResultadoCep consultar(String cep) {
        return super.consultar(cep);
    }

    @SuppressWarnings("unused")
    private br.com.luancalasans.santoschamados.endereco.cep.dto.ResultadoCep fallback(String cep, Throwable ex) {
        return new br.com.luancalasans.santoschamados.endereco.cep.dto.ResultadoCep.Indisponivel(nome(), "Circuit open");
    }

    @Override
    protected String url(String cep) {
        return "https://viacep.com.br/ws/" + cep + "/json/";
    }

    @Override
    protected boolean isNotFound(Map<String, Object> body) {
        return Boolean.TRUE.equals(body.get("erro")) || "true".equalsIgnoreCase(String.valueOf(body.get("erro")));
    }

    @Override
    protected EnderecoCep mapear(Map<String, Object> body, String cep) {
        return normalize(
                firstNonBlank(body, "cep", cep),
                text(body, "logradouro"),
                text(body, "bairro"),
                firstNonBlank(body, "localidade", "cidade"),
                text(body, "uf")
        );
    }
}
