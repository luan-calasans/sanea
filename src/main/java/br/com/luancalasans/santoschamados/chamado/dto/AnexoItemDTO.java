package br.com.luancalasans.santoschamados.chamado.dto;

import br.com.luancalasans.santoschamados.anexo.entity.Anexo;

public record AnexoItemDTO(
        Long id,
        String contentType
) {
    public static AnexoItemDTO from(Anexo anexo) {
        return new AnexoItemDTO(anexo.getId(), anexo.getContentType());
    }
}
