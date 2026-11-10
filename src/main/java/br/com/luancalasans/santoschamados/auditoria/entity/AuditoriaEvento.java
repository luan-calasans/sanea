package br.com.luancalasans.santoschamados.auditoria.entity;

import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
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
import jakarta.persistence.Table;

import java.time.Instant;

@Entity
@Table(name = "auditoria_evento")
public class AuditoriaEvento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento", nullable = false, length = 80)
    private TipoEventoAuditoria tipoEvento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(name = "recurso_tipo", length = 80)
    private String recursoTipo;

    @Column(name = "recurso_id", length = 80)
    private String recursoId;

    @Column(name = "resultado", nullable = false, length = 40)
    private String resultado;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "detalhes", length = 1000)
    private String detalhes;

    @Column(name = "criado_em", nullable = false)
    private Instant criadoEm = Instant.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoEventoAuditoria getTipoEvento() {
        return tipoEvento;
    }

    public void setTipoEvento(TipoEventoAuditoria tipoEvento) {
        this.tipoEvento = tipoEvento;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public String getRecursoTipo() {
        return recursoTipo;
    }

    public void setRecursoTipo(String recursoTipo) {
        this.recursoTipo = recursoTipo;
    }

    public String getRecursoId() {
        return recursoId;
    }

    public void setRecursoId(String recursoId) {
        this.recursoId = recursoId;
    }

    public String getResultado() {
        return resultado;
    }

    public void setResultado(String resultado) {
        this.resultado = resultado;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getDetalhes() {
        return detalhes;
    }

    public void setDetalhes(String detalhes) {
        this.detalhes = detalhes;
    }

    public Instant getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(Instant criadoEm) {
        this.criadoEm = criadoEm;
    }
}
