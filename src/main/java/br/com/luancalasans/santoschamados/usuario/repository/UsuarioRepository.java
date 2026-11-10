package br.com.luancalasans.santoschamados.usuario.repository;

import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    @Query("""
            SELECT COUNT(u) FROM Usuario u
            WHERE u.perfil.codigo = :codigoPerfil AND u.ativo = true
            """)
    long countAtivosPorPerfil(@Param("codigoPerfil") String codigoPerfil);

    @Query("""
            SELECT u FROM Usuario u
            WHERE (:termo IS NULL OR LOWER(u.nome) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%'))
                OR LOWER(u.email) LIKE LOWER(CONCAT('%', CAST(:termo AS string), '%')))
              AND (:perfilCodigo IS NULL OR u.perfil.codigo = :perfilCodigo)
              AND (:ativo IS NULL OR u.ativo = :ativo)
            """)
    Page<Usuario> buscar(
            @Param("termo") String termo,
            @Param("perfilCodigo") String perfilCodigo,
            @Param("ativo") Boolean ativo,
            Pageable pageable
    );
}
