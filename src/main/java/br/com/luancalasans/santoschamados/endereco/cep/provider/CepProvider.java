package br.com.luancalasans.santoschamados.endereco.cep.provider;

import br.com.luancalasans.santoschamados.endereco.cep.dto.ResultadoCep;

public interface CepProvider {

    String nome();

    ResultadoCep consultar(String cep);
}
