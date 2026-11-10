package br.com.luancalasans.santoschamados.chamado.repository;

import br.com.luancalasans.santoschamados.chamado.entity.Chamado;
import br.com.luancalasans.santoschamados.shared.StatusChamado;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface ChamadoRepository extends JpaRepository<Chamado, Long>, JpaSpecificationExecutor<Chamado> {

    Optional<Chamado> findByProtocolo(String protocolo);

    boolean existsByProtocolo(String protocolo);

    Page<Chamado> findByUsuarioId(Long usuarioId, Pageable pageable);

    @Query("""
            SELECT c FROM Chamado c
            JOIN FETCH c.tipoChamado
            WHERE c.protocolo = :protocolo
            """)
    Optional<Chamado> findByProtocoloWithTipo(@Param("protocolo") String protocolo);

    @Query("""
            SELECT c FROM Chamado c
            JOIN FETCH c.tipoChamado
            LEFT JOIN FETCH c.usuario
            WHERE c.id = :id
            """)
    Optional<Chamado> findDetailedById(@Param("id") Long id);

    @Query("SELECT c.status, COUNT(c) FROM Chamado c GROUP BY c.status")
    List<Object[]> countGroupByStatus();

    long countByStatus(StatusChamado status);

    List<Chamado> findTop10ByOrderByAbertoEmDesc();

    static Specification<Chamado> comFiltros(
            StatusChamado status,
            Long tipoId,
            String bairro,
            String cidade,
            Instant desde,
            Instant ate,
            Long usuarioId
    ) {
        return (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (tipoId != null) {
                predicates.add(cb.equal(root.get("tipoChamado").get("id"), tipoId));
            }
            if (bairro != null) {
                predicates.add(cb.equal(cb.lower(root.get("bairro")), bairro.toLowerCase()));
            }
            if (cidade != null) {
                predicates.add(cb.equal(cb.lower(root.get("cidade")), cidade.toLowerCase()));
            }
            if (desde != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("abertoEm"), desde));
            }
            if (ate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("abertoEm"), ate));
            }
            if (usuarioId != null) {
                predicates.add(cb.equal(root.get("usuario").get("id"), usuarioId));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
