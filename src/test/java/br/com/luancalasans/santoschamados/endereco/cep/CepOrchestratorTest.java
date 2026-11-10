package br.com.luancalasans.santoschamados.endereco.cep;

import br.com.luancalasans.santoschamados.endereco.cep.dto.EnderecoCep;
import br.com.luancalasans.santoschamados.endereco.cep.dto.ResultadoCep;
import br.com.luancalasans.santoschamados.endereco.cep.orchestration.CepOrchestrator;
import br.com.luancalasans.santoschamados.endereco.cep.provider.BrasilApiCepProvider;
import br.com.luancalasans.santoschamados.endereco.cep.provider.OpenCepProvider;
import br.com.luancalasans.santoschamados.endereco.cep.provider.ViaCepProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CepOrchestratorTest {

    @Mock ViaCepProvider viaCepProvider;
    @Mock OpenCepProvider openCepProvider;
    @Mock BrasilApiCepProvider brasilApiCepProvider;

    CepOrchestrator orchestrator;

    @BeforeEach
    void setUp() {
        orchestrator = new CepOrchestrator(
                viaCepProvider, openCepProvider, brasilApiCepProvider,
                new ConcurrentMapCacheManager("cepPositivos")
        );
    }

    @Test
    void viaCepEncontra() {
        EnderecoCep endereco = new EnderecoCep("11010000", "Rua A", "Centro", "Santos", "SP");
        when(viaCepProvider.consultar("11010000")).thenReturn(new ResultadoCep.Encontrado(endereco, "ViaCEP"));

        ResultadoCep resultado = orchestrator.consultar("11010-000");

        assertThat(resultado).isInstanceOf(ResultadoCep.Encontrado.class);
        assertThat(((ResultadoCep.Encontrado) resultado).endereco().cidade()).isEqualTo("Santos");
    }

    @Test
    void fallbackOpenCep() {
        when(viaCepProvider.consultar("11010000")).thenReturn(new ResultadoCep.NaoEncontrado("ViaCEP"));
        EnderecoCep endereco = new EnderecoCep("11010000", "Rua B", "Centro", "Santos", "SP");
        when(openCepProvider.consultar("11010000")).thenReturn(new ResultadoCep.Encontrado(endereco, "OpenCEP"));

        ResultadoCep resultado = orchestrator.consultar("11010000");
        assertThat(resultado).isInstanceOf(ResultadoCep.Encontrado.class);
        assertThat(((ResultadoCep.Encontrado) resultado).provedor()).isEqualTo("OpenCEP");
    }

    @Test
    void fallbackBrasilApi() {
        when(viaCepProvider.consultar("11010000")).thenReturn(new ResultadoCep.Indisponivel("ViaCEP", "timeout"));
        when(openCepProvider.consultar("11010000")).thenReturn(new ResultadoCep.Indisponivel("OpenCEP", "5xx"));
        EnderecoCep endereco = new EnderecoCep("11010000", "Rua C", "Centro", "Santos", "SP");
        when(brasilApiCepProvider.consultar("11010000")).thenReturn(new ResultadoCep.Encontrado(endereco, "BrasilAPI"));

        assertThat(orchestrator.consultar("11010000")).isInstanceOf(ResultadoCep.Encontrado.class);
    }

    @Test
    void todosNaoEncontrados() {
        when(viaCepProvider.consultar("00000000")).thenReturn(new ResultadoCep.NaoEncontrado("ViaCEP"));
        when(openCepProvider.consultar("00000000")).thenReturn(new ResultadoCep.NaoEncontrado("OpenCEP"));
        when(brasilApiCepProvider.consultar("00000000")).thenReturn(new ResultadoCep.NaoEncontrado("BrasilAPI"));

        assertThat(orchestrator.consultar("00000000")).isInstanceOf(ResultadoCep.NaoEncontrado.class);
    }

    @Test
    void todosIndisponiveis() {
        when(viaCepProvider.consultar("11010000")).thenReturn(new ResultadoCep.Indisponivel("ViaCEP", "down"));
        when(openCepProvider.consultar("11010000")).thenReturn(new ResultadoCep.Indisponivel("OpenCEP", "down"));
        when(brasilApiCepProvider.consultar("11010000")).thenReturn(new ResultadoCep.Indisponivel("BrasilAPI", "down"));

        assertThat(orchestrator.consultar("11010000")).isInstanceOf(ResultadoCep.Indisponivel.class);
    }

    @Test
    void cachePositivo() {
        EnderecoCep endereco = new EnderecoCep("11010000", "Rua A", "Centro", "Santos", "SP");
        when(viaCepProvider.consultar("11010000")).thenReturn(new ResultadoCep.Encontrado(endereco, "ViaCEP"));

        orchestrator.consultar("11010000");
        ResultadoCep cached = orchestrator.consultar("11010000");

        assertThat(cached).isInstanceOf(ResultadoCep.Encontrado.class);
        assertThat(((ResultadoCep.Encontrado) cached).provedor()).isEqualTo("cache");
    }
}
