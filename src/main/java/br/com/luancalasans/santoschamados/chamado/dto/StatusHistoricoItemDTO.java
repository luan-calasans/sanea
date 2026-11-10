package br.com.luancalasans.santoschamados.chamado.dto;

import br.com.luancalasans.santoschamados.chamado.entity.StatusHistorico;
import br.com.luancalasans.santoschamados.shared.StatusChamado;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record StatusHistoricoItemDTO(
        StatusChamado statusNovo,
        String statusNovoRotulo,
        String criadoEmFormatado,
        String observacao
) {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));

    public static StatusHistoricoItemDTO from(StatusHistorico historico) {
        StatusChamado statusNovo = historico.getStatusNovo();
        return new StatusHistoricoItemDTO(
                statusNovo,
                statusNovo == null ? "" : statusNovo.getRotulo(),
                FORMATTER.format(historico.getCriadoEm()),
                historico.getObservacao()
        );
    }
}
