package br.com.luancalasans.santoschamados.endereco.cep;

import br.com.luancalasans.santoschamados.endereco.cep.dto.ResultadoCep;
import br.com.luancalasans.santoschamados.endereco.cep.provider.ViaCepProvider;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.web.client.RestClient;

import java.time.Duration;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;

class ViaCepProviderWireMockTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    ViaCepProvider provider;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder(new RestTemplateBuilder()
                .rootUri(wireMock.baseUrl())
                .setConnectTimeout(Duration.ofSeconds(2))
                .setReadTimeout(Duration.ofSeconds(2))
                .build());
        provider = new ViaCepProvider(builder) {
            @Override
            protected String url(String cep) {
                return wireMock.baseUrl() + "/ws/" + cep + "/json/";
            }
        };
    }

    @Test
    void respostaValida() {
        wireMock.stubFor(get(urlEqualTo("/ws/11010000/json/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"cep":"11010-000","logradouro":"Rua XV","bairro":"Centro","localidade":"Santos","uf":"SP"}
                                """)));

        ResultadoCep resultado = provider.consultar("11010000");
        assertThat(resultado).isInstanceOf(ResultadoCep.Encontrado.class);
    }

    @Test
    void cepInexistente() {
        wireMock.stubFor(get(urlEqualTo("/ws/00000000/json/"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("{\"erro\": true}")));

        assertThat(provider.consultar("00000000")).isInstanceOf(ResultadoCep.NaoEncontrado.class);
    }

    @Test
    void erroHttp() {
        wireMock.stubFor(get(urlEqualTo("/ws/11010000/json/"))
                .willReturn(aResponse().withStatus(500)));

        assertThat(provider.consultar("11010000")).isInstanceOf(ResultadoCep.Indisponivel.class);
    }
}
