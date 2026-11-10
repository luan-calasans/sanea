package br.com.luancalasans.santoschamados.mensagem.dto;

import br.com.luancalasans.santoschamados.shared.VisibilidadeMensagem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class NovaMensagemForm {

    @NotBlank
    @Size(max = 4000)
    private String corpo;

    @NotNull
    private VisibilidadeMensagem visibilidade = VisibilidadeMensagem.PUBLICA;

    public String getCorpo() {
        return corpo;
    }

    public void setCorpo(String corpo) {
        this.corpo = corpo;
    }

    public VisibilidadeMensagem getVisibilidade() {
        return visibilidade;
    }

    public void setVisibilidade(VisibilidadeMensagem visibilidade) {
        this.visibilidade = visibilidade;
    }
}
