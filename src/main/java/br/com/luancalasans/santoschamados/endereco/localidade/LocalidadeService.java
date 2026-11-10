package br.com.luancalasans.santoschamados.endereco.localidade;

import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class LocalidadeService {

    private static final Logger log = LoggerFactory.getLogger(LocalidadeService.class);
    private static final String IBGE_MUNICIPIOS =
            "https://servicodados.ibge.gov.br/api/v1/localidades/estados/{uf}/municipios";

    private final RestClient restClient;

    public LocalidadeService(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder.build();
    }

    public List<UnidadeFederativa> listarUfs() {
        return UnidadeFederativa.ordenadas();
    }

    @Cacheable(cacheNames = "municipiosPorUf", key = "#uf.toUpperCase()")
    public List<String> listarCidades(String uf) {
        UnidadeFederativa estado = validarUf(uf);
        try {
            List<Map<String, Object>> response = restClient.get()
                    .uri(IBGE_MUNICIPIOS, estado.getSigla())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {
                    });
            if (response == null || response.isEmpty()) {
                return List.of();
            }
            return response.stream()
                    .map(item -> String.valueOf(item.getOrDefault("nome", "")).trim())
                    .filter(nome -> !nome.isBlank())
                    .distinct()
                    .sorted(Comparator.comparing(String::toLowerCase))
                    .toList();
        } catch (RestClientException ex) {
            log.warn("Falha ao consultar municípios IBGE para UF {}: {}", estado.getSigla(), ex.getMessage());
            return List.of();
        }
    }

    private UnidadeFederativa validarUf(String uf) {
        if (uf == null || uf.isBlank()) {
            throw new BusinessException("Informe a UF.");
        }
        try {
            return UnidadeFederativa.valueOf(uf.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new BusinessException("UF inválida.");
        }
    }
}
