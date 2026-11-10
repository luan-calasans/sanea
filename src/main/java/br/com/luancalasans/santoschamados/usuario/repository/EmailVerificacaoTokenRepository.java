package br.com.luancalasans.santoschamados.usuario.repository;

import br.com.luancalasans.santoschamados.usuario.entity.EmailVerificacaoToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface EmailVerificacaoTokenRepository extends JpaRepository<EmailVerificacaoToken, Long> {

    Optional<EmailVerificacaoToken> findByTokenHash(String tokenHash);

    @Modifying
    @Query("""
            UPDATE EmailVerificacaoToken t
            SET t.usadoEm = CURRENT_TIMESTAMP
            WHERE t.usuario.id = :usuarioId
              AND t.proposito = :proposito
              AND t.usadoEm IS NULL
            """)
    void invalidarTokensPendentes(@Param("usuarioId") Long usuarioId, @Param("proposito") String proposito);
}
