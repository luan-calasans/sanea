package br.com.luancalasans.santoschamados.shared;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum StatusChamado {
    ABERTO("Aberto"),
    EM_ANALISE("Em análise"),
    EM_ATENDIMENTO("Em atendimento"),
    RESOLVIDO("Resolvido"),
    DUPLICADO("Duplicado"),
    CANCELADO("Cancelado");

    private final String rotulo;

    private static final Map<StatusChamado, Set<StatusChamado>> TRANSICOES = Map.of(
            ABERTO, EnumSet.of(EM_ANALISE, EM_ATENDIMENTO, RESOLVIDO, CANCELADO, DUPLICADO),
            EM_ANALISE, EnumSet.of(EM_ATENDIMENTO, RESOLVIDO, DUPLICADO, CANCELADO),
            EM_ATENDIMENTO, EnumSet.of(RESOLVIDO, CANCELADO),
            RESOLVIDO, EnumSet.noneOf(StatusChamado.class),
            DUPLICADO, EnumSet.noneOf(StatusChamado.class),
            CANCELADO, EnumSet.noneOf(StatusChamado.class)
    );

    StatusChamado(String rotulo) {
        this.rotulo = rotulo;
    }

    public String getRotulo() {
        return rotulo;
    }

    public boolean podeTransicionarPara(StatusChamado destino) {
        return TRANSICOES.getOrDefault(this, Set.of()).contains(destino);
    }

    public Set<StatusChamado> proximosPermitidos() {
        return Set.copyOf(TRANSICOES.getOrDefault(this, Set.of()));
    }
}
