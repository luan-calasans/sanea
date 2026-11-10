package br.com.luancalasans.santoschamados.endereco.cep.orchestration;

import br.com.luancalasans.santoschamados.endereco.cep.dto.EnderecoCep;
import br.com.luancalasans.santoschamados.endereco.cep.dto.ResultadoCep;
import br.com.luancalasans.santoschamados.endereco.cep.provider.BrasilApiCepProvider;
import br.com.luancalasans.santoschamados.endereco.cep.provider.CepProvider;
import br.com.luancalasans.santoschamados.endereco.cep.provider.OpenCepProvider;
import br.com.luancalasans.santoschamados.endereco.cep.provider.ViaCepProvider;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CepOrchestrator {

    private final List<CepProvider> providers;
    private final CacheManager cacheManager;

    public CepOrchestrator(
            ViaCepProvider viaCepProvider,
            OpenCepProvider openCepProvider,
            BrasilApiCepProvider brasilApiCepProvider,
            CacheManager cacheManager
    ) {
        this.providers = List.of(viaCepProvider, openCepProvider, brasilApiCepProvider);
        this.cacheManager = cacheManager;
    }

    public ResultadoCep consultar(String cepInformado) {
        String cep = normalizarCep(cepInformado);
        if (cep.length() != 8) {
            throw new BusinessException("Informe um CEP com 8 dígitos.");
        }

        EnderecoCep cached = getCached(cep);
        if (cached != null) {
            return new ResultadoCep.Encontrado(cached, "cache");
        }

        boolean algumNaoEncontrado = false;
        for (CepProvider provider : providers) {
            ResultadoCep resultado = provider.consultar(cep);
            if (resultado instanceof ResultadoCep.Encontrado encontrado) {
                putCache(encontrado.endereco());
                return encontrado;
            }
            if (resultado instanceof ResultadoCep.NaoEncontrado) {
                algumNaoEncontrado = true;
            }
        }

        if (algumNaoEncontrado) {
            return new ResultadoCep.NaoEncontrado("orquestrador");
        }
        return new ResultadoCep.Indisponivel("orquestrador", "Todos os provedores indisponiveis");
    }

    public String normalizarCep(String cep) {
        if (cep == null) {
            return "";
        }
        return cep.replaceAll("\\D", "");
    }

    private EnderecoCep getCached(String cep) {
        Cache cache = cacheManager.getCache("cepPositivos");
        if (cache == null) {
            return null;
        }
        return cache.get(cep, EnderecoCep.class);
    }

    private void putCache(EnderecoCep endereco) {
        if (endereco == null || endereco.cep() == null) {
            return;
        }
        Cache cache = cacheManager.getCache("cepPositivos");
        if (cache != null) {
            cache.put(endereco.cep(), endereco);
        }
    }
}
