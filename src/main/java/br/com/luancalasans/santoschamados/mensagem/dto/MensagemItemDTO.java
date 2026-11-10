package br.com.luancalasans.santoschamados.mensagem.dto;

import br.com.luancalasans.santoschamados.mensagem.entity.MensagemChamado;
import br.com.luancalasans.santoschamados.shared.VisibilidadeMensagem;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public record MensagemItemDTO(
        Long id,
        String autorNome,
        String corpo,
        String visibilidade,
        boolean interna,
        String criadoEmFormatado
) {
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss").withZone(ZoneId.of("America/Sao_Paulo"));

    public static MensagemItemDTO from(MensagemChamado mensagem) {
        String autorNome = "Usuário";
        if (mensagem.getAutor() != null && mensagem.getAutor().getPerfil() != null) {
            autorNome = mensagem.getAutor().getPerfil().getNome();
        }
        VisibilidadeMensagem visibilidade = mensagem.getVisibilidade();
        return new MensagemItemDTO(
                mensagem.getId(),
                autorNome,
                mensagem.getCorpo(),
                visibilidade == null ? VisibilidadeMensagem.PUBLICA.name() : visibilidade.name(),
                visibilidade == VisibilidadeMensagem.INTERNA,
                FORMATTER.format(mensagem.getCriadoEm())
        );
    }
}
