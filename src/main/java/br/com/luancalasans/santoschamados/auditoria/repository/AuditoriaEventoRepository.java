package br.com.luancalasans.santoschamados.auditoria.repository;

import br.com.luancalasans.santoschamados.auditoria.entity.AuditoriaEvento;
import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AuditoriaEventoRepository extends JpaRepository<AuditoriaEvento, Long> {

    @Query("""
            SELECT a FROM AuditoriaEvento a
            WHERE (:tipo IS NULL OR a.tipoEvento = :tipo)
            """)
    Page<AuditoriaEvento> buscar(@Param("tipo") TipoEventoAuditoria tipo, Pageable pageable);
}
