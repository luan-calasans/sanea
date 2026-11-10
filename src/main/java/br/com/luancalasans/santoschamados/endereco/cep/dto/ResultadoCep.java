package br.com.luancalasans.santoschamados.endereco.cep.dto;

public sealed interface ResultadoCep permits ResultadoCep.Encontrado, ResultadoCep.NaoEncontrado, ResultadoCep.Indisponivel {

    record Encontrado(EnderecoCep endereco, String provedor) implements ResultadoCep {
    }

    record NaoEncontrado(String provedor) implements ResultadoCep {
    }

    record Indisponivel(String provedor, String motivo) implements ResultadoCep {
    }
}
