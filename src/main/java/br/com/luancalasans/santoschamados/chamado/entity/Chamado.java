package br.com.luancalasans.santoschamados.chamado.entity;

import br.com.luancalasans.santoschamados.catalogo.entity.TipoChamado;
import br.com.luancalasans.santoschamados.shared.StatusChamado;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chamado")
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "protocolo", nullable = false, length = 20)
    private String protocolo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tipo_chamado_id", nullable = false)
    private TipoChamado tipoChamado;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private StatusChamado status;

    @Column(name = "descricao", nullable = false, length = 4000)
    private String descricao;

    @Column(name = "tipo_outro", length = 200)
    private String tipoOutro;

    @Column(name = "data_ocorrencia")
    private LocalDate dataOcorrencia;

    @Column(name = "aberto_em", nullable = false)
    private Instant abertoEm;

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    @Column(name = "cep", nullable = false, length = 8)
    private String cep;

    @Column(name = "logradouro", nullable = false, length = 200)
    private String logradouro;

    @Column(name = "numero", length = 30)
    private String numero;

    @Column(name = "complemento", length = 120)
    private String complemento;

    @Column(name = "bairro", nullable = false, length = 120)
    private String bairro;

    @Column(name = "cidade", nullable = false, length = 120)
    private String cidade;

    @Column(name = "uf", nullable = false, length = 2)
    private String uf;

    @Column(name = "endereco_verificado", nullable = false)
    private boolean enderecoVerificado;

    @Version
    @Column(name = "versao", nullable = false)
    private Long versao;

    @OneToMany(mappedBy = "chamado", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("criadoEm ASC")
    private List<StatusHistorico> historicos = new ArrayList<>();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getProtocolo() {
        return protocolo;
    }

    public void setProtocolo(String protocolo) {
        this.protocolo = protocolo;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoChamado getTipoChamado() {
        return tipoChamado;
    }

    public void setTipoChamado(TipoChamado tipoChamado) {
        this.tipoChamado = tipoChamado;
    }

    public StatusChamado getStatus() {
        return status;
    }

    public void setStatus(StatusChamado status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public String getTipoOutro() {
        return tipoOutro;
    }

    public void setTipoOutro(String tipoOutro) {
        this.tipoOutro = tipoOutro;
    }

    public String getTipoExibicao() {
        if (tipoOutro != null && !tipoOutro.isBlank() && tipoChamado != null) {
            return tipoChamado.getNome() + ": " + tipoOutro.trim();
        }
        return tipoChamado != null ? tipoChamado.getNome() : null;
    }

    public LocalDate getDataOcorrencia() {
        return dataOcorrencia;
    }

    public void setDataOcorrencia(LocalDate dataOcorrencia) {
        this.dataOcorrencia = dataOcorrencia;
    }

    public Instant getAbertoEm() {
        return abertoEm;
    }

    public void setAbertoEm(Instant abertoEm) {
        this.abertoEm = abertoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

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

    public boolean isEnderecoVerificado() {
        return enderecoVerificado;
    }

    public void setEnderecoVerificado(boolean enderecoVerificado) {
        this.enderecoVerificado = enderecoVerificado;
    }

    public Long getVersao() {
        return versao;
    }

    public void setVersao(Long versao) {
        this.versao = versao;
    }

    public List<StatusHistorico> getHistoricos() {
        return historicos;
    }

    public void setHistoricos(List<StatusHistorico> historicos) {
        this.historicos = historicos;
    }

    public void adicionarHistorico(StatusHistorico historico) {
        historicos.add(historico);
        historico.setChamado(this);
    }
}
