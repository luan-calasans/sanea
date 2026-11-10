package br.com.luancalasans.santoschamados.chamado.dto;

import br.com.luancalasans.santoschamados.shared.StatusChamado;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AlterarStatusForm {

    @NotNull
    private StatusChamado novoStatus;

    @Size(max = 1000)
    private String observacao;

    private Long versao;

    public StatusChamado getNovoStatus() {
        return novoStatus;
    }

    public void setNovoStatus(StatusChamado novoStatus) {
        this.novoStatus = novoStatus;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public Long getVersao() {
        return versao;
    }

    public void setVersao(Long versao) {
        this.versao = versao;
    }
}
