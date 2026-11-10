package br.com.luancalasans.santoschamados.chamado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class AberturaChamadoForm {

    @NotBlank
    @Size(min = 8, max = 9)
    private String cep;

    @NotBlank
    @Size(max = 200)
    private String logradouro;

    @Size(max = 30)
    private String numero;

    @Size(max = 120)
    private String complemento;

    @NotBlank
    @Size(max = 120)
    private String bairro;

    @NotBlank
    @Size(max = 120)
    private String cidade;

    @NotBlank
    @Size(min = 2, max = 2)
    private String uf;

    @NotNull
    private Long tipoChamadoId;

    @Size(max = 200)
    private String tipoOutro;

    @NotBlank
    @Size(max = 4000)
    private String descricao;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dataOcorrencia;

    private boolean enderecoVerificado;

    private List<MultipartFile> fotos = new ArrayList<>();

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public void setLogradouro(String logradouro) {
        this.logradouro = logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public void setComplemento(String complemento) {
        this.complemento = complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public void setCidade(String cidade) {
        this.cidade = cidade;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public Long getTipoChamadoId() {
        return tipoChamadoId;
    }

    public void setTipoChamadoId(Long tipoChamadoId) {
        this.tipoChamadoId = tipoChamadoId;
    }

    public String getTipoOutro() {
        return tipoOutro;
    }

    public void setTipoOutro(String tipoOutro) {
        this.tipoOutro = tipoOutro;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDate getDataOcorrencia() {
        return dataOcorrencia;
    }

    public void setDataOcorrencia(LocalDate dataOcorrencia) {
        this.dataOcorrencia = dataOcorrencia;
    }

    public boolean isEnderecoVerificado() {
        return enderecoVerificado;
    }

    public void setEnderecoVerificado(boolean enderecoVerificado) {
        this.enderecoVerificado = enderecoVerificado;
    }

    public List<MultipartFile> getFotos() {
        return fotos;
    }

    public void setFotos(List<MultipartFile> fotos) {
        this.fotos = fotos;
    }
}
