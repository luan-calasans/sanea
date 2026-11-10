package br.com.luancalasans.santoschamados.usuario.entity;

import br.com.luancalasans.santoschamados.perfil.entity.Perfil;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.time.Instant;

@Entity
@Table(name = "usuario")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Column(name = "email", nullable = false, length = 180)
    private String email;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    @Column(name = "ativo", nullable = false)
    private boolean ativo;

    @Column(name = "email_verificado", nullable = false)
    private boolean emailVerificado;

    @Column(name = "deve_trocar_senha", nullable = false)
    private boolean deveTrocarSenha;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    @Column(name = "atualizado_em", nullable = false)
    private Instant atualizadoEm = Instant.now();

    @Column(name = "ultimo_acesso_em")
    private Instant ultimoAcessoEm;

    @Column(name = "desativado_em")
    private Instant desativadoEm;

    @Column(name = "tema_modo", nullable = false, length = 10)
    private String temaModo = "LIGHT";

    @Column(name = "cor_sidebar", length = 7)
    private String corSidebar;

    @Column(name = "cor_destaque", length = 7)
    private String corDestaque;

    @Column(name = "cor_fonte", length = 7)
    private String corFonte;

    @Version
    @Column(name = "versao", nullable = false)
    private Long versao;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenhaHash() {
        return senhaHash;
    }

    public void setSenhaHash(String senhaHash) {
        this.senhaHash = senhaHash;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public boolean isEmailVerificado() {
        return emailVerificado;
    }

    public void setEmailVerificado(boolean emailVerificado) {
        this.emailVerificado = emailVerificado;
    }

    public boolean isDeveTrocarSenha() {
        return deveTrocarSenha;
    }

    public void setDeveTrocarSenha(boolean deveTrocarSenha) {
        this.deveTrocarSenha = deveTrocarSenha;
    }

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }

    public Instant getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(Instant atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public Instant getUltimoAcessoEm() {
        return ultimoAcessoEm;
    }

    public void setUltimoAcessoEm(Instant ultimoAcessoEm) {
        this.ultimoAcessoEm = ultimoAcessoEm;
    }

    public Instant getDesativadoEm() {
        return desativadoEm;
    }

    public void setDesativadoEm(Instant desativadoEm) {
        this.desativadoEm = desativadoEm;
    }

    public String getTemaModo() {
        return temaModo;
    }

    public void setTemaModo(String temaModo) {
        this.temaModo = temaModo;
    }

    public String getCorSidebar() {
        return corSidebar;
    }

    public void setCorSidebar(String corSidebar) {
        this.corSidebar = corSidebar;
    }

    public String getCorDestaque() {
        return corDestaque;
    }

    public void setCorDestaque(String corDestaque) {
        this.corDestaque = corDestaque;
    }

    public String getCorFonte() {
        return corFonte;
    }

    public void setCorFonte(String corFonte) {
        this.corFonte = corFonte;
    }

    public Long getVersao() {
        return versao;
    }

    public void setVersao(Long versao) {
        this.versao = versao;
    }
}
