package br.com.luancalasans.santoschamados.chamado.dto;

import br.com.luancalasans.santoschamados.mensagem.dto.MensagemItemDTO;
import br.com.luancalasans.santoschamados.shared.StatusChamado;

import java.util.List;

public record ChamadoDetalheDTO(
        Long id,
        String protocolo,
        String tipoExibicao,
        String statusName,
        String statusRotulo,
        String statusBadgeClass,
        String abertoEmFormatado,
        String dataOcorrenciaFormatada,
        String logradouro,
        String numero,
        String complemento,
        String bairro,
        String cidade,
        String uf,
        String cep,
        boolean enderecoVerificado,
        String enderecoVerificadoRotulo,
        String descricao,
        Long versao,
        List<StatusHistoricoItemDTO> historicos,
        List<AnexoItemDTO> anexos,
        List<MensagemItemDTO> mensagens,
        String ultimaMensagemEm,
        List<StatusOpcaoDTO> proximosStatus,
        boolean temProximosStatus
) {
    public record StatusOpcaoDTO(String valor, String rotulo) {
        public static StatusOpcaoDTO from(StatusChamado status) {
            return new StatusOpcaoDTO(status.name(), status.getRotulo());
        }
    }
}
