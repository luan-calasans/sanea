package br.com.luancalasans.santoschamados.chamado.dto;

import br.com.luancalasans.santoschamados.shared.StatusChamado;

import java.time.Instant;
import java.util.List;

public record ChamadoPublicoDTO(
        String protocolo,
        String tipoNome,
        StatusChamado status,
        String statusRotulo,
        Instant abertoEm,
        List<StatusHistoricoPublicoDTO> historico,
        List<MensagemPublicaDTO> mensagensPublicas,
        long quantidadeAnexos
) {
    public record StatusHistoricoPublicoDTO(
            StatusChamado statusAnterior,
            StatusChamado statusNovo,
            Instant criadoEm,
            String observacao
    ) {
    }

    public record MensagemPublicaDTO(
            String perfilAutor,
            Instant criadoEm,
            String corpo
    ) {
    }
}
