package br.com.luancalasans.santoschamados.usuario.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "login_tentativa")
public class LoginTentativa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email_normalizado", nullable = false, length = 180)
    private String emailNormalizado;

    @Column(name = "ip_address", nullable = false, length = 64)
    private String ipAddress;

    @Column(name = "sucesso", nullable = false)
    private boolean sucesso;

    @Column(name = "bloqueado_ate")
    private Instant bloqueadoAte;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmailNormalizado() {
        return emailNormalizado;
    }

    public void setEmailNormalizado(String emailNormalizado) {
        this.emailNormalizado = emailNormalizado;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public boolean isSucesso() {
        return sucesso;
    }

    public void setSucesso(boolean sucesso) {
        this.sucesso = sucesso;
    }

    public Instant getBloqueadoAte() {
        return bloqueadoAte;
    }

    public void setBloqueadoAte(Instant bloqueadoAte) {
        this.bloqueadoAte = bloqueadoAte;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
