package br.com.luancalasans.santoschamados.anexo.entity;

import br.com.luancalasans.santoschamados.chamado.entity.Chamado;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "anexo")
public class Anexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chamado_id", nullable = false)
    private Chamado chamado;

    @Column(name = "nome_original", nullable = false)
    private String nomeOriginal;

    @Column(name = "nome_armazenado", nullable = false, length = 120)
    private String nomeArmazenado;

    @Column(name = "caminho_relativo", nullable = false)
    private String caminhoRelativo;

    @Column(name = "content_type", nullable = false, length = 80)
    private String contentType;

    @Column(name = "tamanho_bytes", nullable = false)
    private long tamanhoBytes;

    @Column(name = "largura")
    private Integer largura;

    @Column(name = "altura")
    private Integer altura;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enviado_por_id", nullable = false)
    private Usuario enviadoPor;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Chamado getChamado() {
        return chamado;
    }

    public void setChamado(Chamado chamado) {
        this.chamado = chamado;
    }

    public String getNomeOriginal() {
        return nomeOriginal;
    }

    public void setNomeOriginal(String nomeOriginal) {
        this.nomeOriginal = nomeOriginal;
    }

    public String getNomeArmazenado() {
        return nomeArmazenado;
    }

    public void setNomeArmazenado(String nomeArmazenado) {
        this.nomeArmazenado = nomeArmazenado;
    }

    public String getCaminhoRelativo() {
        return caminhoRelativo;
    }

    public void setCaminhoRelativo(String caminhoRelativo) {
        this.caminhoRelativo = caminhoRelativo;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getTamanhoBytes() {
        return tamanhoBytes;
    }

    public void setTamanhoBytes(long tamanhoBytes) {
        this.tamanhoBytes = tamanhoBytes;
    }

    public Integer getLargura() {
        return largura;
    }

    public void setLargura(Integer largura) {
        this.largura = largura;
    }

    public Integer getAltura() {
        return altura;
    }

    public void setAltura(Integer altura) {
        this.altura = altura;
    }

    public Usuario getEnviadoPor() {
        return enviadoPor;
    }

    public void setEnviadoPor(Usuario enviadoPor) {
        this.enviadoPor = enviadoPor;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
