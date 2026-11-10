package br.com.luancalasans.santoschamados.chamado;

import br.com.luancalasans.santoschamados.shared.StatusChamado;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class StatusChamadoTest {

    @Test
    void transicoesValidas() {
        assertThat(StatusChamado.ABERTO.podeTransicionarPara(StatusChamado.EM_ANALISE)).isTrue();
        assertThat(StatusChamado.ABERTO.podeTransicionarPara(StatusChamado.EM_ATENDIMENTO)).isTrue();
        assertThat(StatusChamado.ABERTO.podeTransicionarPara(StatusChamado.RESOLVIDO)).isTrue();
        assertThat(StatusChamado.ABERTO.podeTransicionarPara(StatusChamado.CANCELADO)).isTrue();
        assertThat(StatusChamado.ABERTO.podeTransicionarPara(StatusChamado.DUPLICADO)).isTrue();

        assertThat(StatusChamado.EM_ANALISE.podeTransicionarPara(StatusChamado.EM_ATENDIMENTO)).isTrue();
        assertThat(StatusChamado.EM_ANALISE.podeTransicionarPara(StatusChamado.RESOLVIDO)).isTrue();
        assertThat(StatusChamado.EM_ANALISE.podeTransicionarPara(StatusChamado.DUPLICADO)).isTrue();
        assertThat(StatusChamado.EM_ANALISE.podeTransicionarPara(StatusChamado.CANCELADO)).isTrue();

        assertThat(StatusChamado.EM_ATENDIMENTO.podeTransicionarPara(StatusChamado.RESOLVIDO)).isTrue();
        assertThat(StatusChamado.EM_ATENDIMENTO.podeTransicionarPara(StatusChamado.CANCELADO)).isTrue();
    }

    @Test
    void transicoesInvalidas() {
        assertThat(StatusChamado.RESOLVIDO.podeTransicionarPara(StatusChamado.ABERTO)).isFalse();
        assertThat(StatusChamado.CANCELADO.podeTransicionarPara(StatusChamado.EM_ANALISE)).isFalse();
        assertThat(StatusChamado.DUPLICADO.podeTransicionarPara(StatusChamado.EM_ATENDIMENTO)).isFalse();
        assertThat(StatusChamado.EM_ATENDIMENTO.podeTransicionarPara(StatusChamado.ABERTO)).isFalse();
    }

    @Test
    void resolvidoDisponivelNosStatusAtivos() {
        assertThat(StatusChamado.ABERTO.proximosPermitidos()).contains(StatusChamado.RESOLVIDO);
        assertThat(StatusChamado.EM_ANALISE.proximosPermitidos()).contains(StatusChamado.RESOLVIDO);
        assertThat(StatusChamado.EM_ATENDIMENTO.proximosPermitidos()).contains(StatusChamado.RESOLVIDO);
    }
}
