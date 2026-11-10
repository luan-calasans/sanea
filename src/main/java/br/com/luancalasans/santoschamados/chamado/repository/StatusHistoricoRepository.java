package br.com.luancalasans.santoschamados.chamado.repository;

import br.com.luancalasans.santoschamados.chamado.entity.StatusHistorico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface StatusHistoricoRepository extends JpaRepository<StatusHistorico, Long> {

    @Query("""
            SELECT h FROM StatusHistorico h
            LEFT JOIN FETCH h.autor
            WHERE h.chamado.id = :chamadoId
            ORDER BY h.criadoEm ASC
            """)
    List<StatusHistorico> findByChamadoIdOrderByCriadoEmAsc(@Param("chamadoId") Long chamadoId);

    void deleteByChamadoId(Long chamadoId);
}
