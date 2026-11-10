package br.com.luancalasans.santoschamados.chamado.dto;

import br.com.luancalasans.santoschamados.chamado.entity.Chamado;
import br.com.luancalasans.santoschamados.shared.StatusChamado;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record ChamadoListaItemDTO(
        Long id,
        String protocolo,
        String tipoNome,
        StatusChamado status,
        String statusRotulo,
        String bairro,
        String abertoEmFormatado
) {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

    public static ChamadoListaItemDTO from(Chamado chamado) {
        return new ChamadoListaItemDTO(
                chamado.getId(),
                chamado.getProtocolo(),
                chamado.getTipoExibicao(),
                chamado.getStatus(),
                chamado.getStatus().getRotulo(),
                chamado.getBairro(),
                FORMATTER.format(chamado.getAbertoEm())
        );
    }
}
