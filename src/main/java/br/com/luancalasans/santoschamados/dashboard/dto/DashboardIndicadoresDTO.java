package br.com.luancalasans.santoschamados.dashboard.dto;

import br.com.luancalasans.santoschamados.shared.StatusChamado;

import java.util.List;
import java.util.Map;

public record DashboardIndicadoresDTO(
        long total,
        long abertos,
        long emAnalise,
        long emAtendimento,
        long resolvidos,
        long duplicados,
        long cancelados,
        Map<StatusChamado, Long> porStatus,
        List<ChamadoRecenteDTO> recentes
) {
    public record ChamadoRecenteDTO(
            Long id,
            String protocolo,
            String tipoNome,
            StatusChamado status,
            String statusRotulo,
            java.time.Instant abertoEm,
            String abertoEmFormatado,
            String bairro
    ) {
    }
}
