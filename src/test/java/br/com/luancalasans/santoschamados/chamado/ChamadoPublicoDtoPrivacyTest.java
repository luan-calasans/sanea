package br.com.luancalasans.santoschamados.chamado;

import br.com.luancalasans.santoschamados.chamado.dto.ChamadoPublicoDTO;
import br.com.luancalasans.santoschamados.shared.StatusChamado;
import org.junit.jupiter.api.Test;

import java.lang.reflect.RecordComponent;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ChamadoPublicoDtoPrivacyTest {

    @Test
    void dtoPublicoNaoExpoeCamposPessoais() {
        Set<String> componentes = Arrays.stream(ChamadoPublicoDTO.class.getRecordComponents())
                .map(RecordComponent::getName)
                .collect(java.util.stream.Collectors.toSet());

        assertThat(componentes).doesNotContain("nome", "email", "cpf", "logradouro", "complemento", "usuario");

        ChamadoPublicoDTO dto = new ChamadoPublicoDTO(
                "SAN-2026-ABCDEFGH",
                "Vazamento",
                StatusChamado.ABERTO,
                "Aberto",
                Instant.now(),
                List.of(),
                List.of(new ChamadoPublicoDTO.MensagemPublicaDTO("Sabesp", Instant.now(), "Em análise")),
                2
        );

        assertThat(dto.protocolo()).startsWith("SAN-");
        assertThat(dto.mensagensPublicas().get(0).perfilAutor()).isEqualTo("Sabesp");
    }
}
