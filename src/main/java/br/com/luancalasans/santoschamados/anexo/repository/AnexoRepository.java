package br.com.luancalasans.santoschamados.anexo.repository;

import br.com.luancalasans.santoschamados.anexo.entity.Anexo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnexoRepository extends JpaRepository<Anexo, Long> {

    List<Anexo> findByChamadoIdOrderByCriadoEmAsc(Long chamadoId);

    long countByChamadoId(Long chamadoId);

    void deleteByChamadoId(Long chamadoId);

    @Query("""
            SELECT a FROM Anexo a
            JOIN FETCH a.chamado
            WHERE a.id = :id
            """)
    Optional<Anexo> findDetailedById(@Param("id") Long id);
}
