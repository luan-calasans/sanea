package br.com.luancalasans.santoschamados.endereco.cep.dto;

public record EnderecoCep(
        String cep,
        String logradouro,
        String bairro,
        String cidade,
        String uf
) {
}
