package br.com.luancalasans.santoschamados.usuario.repository;

import br.com.luancalasans.santoschamados.usuario.entity.LoginTentativa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface LoginTentativaRepository extends JpaRepository<LoginTentativa, Long> {

    @Query("""
            SELECT COUNT(t) FROM LoginTentativa t
            WHERE t.emailNormalizado = :email
              AND t.ipAddress = :ip
              AND t.sucesso = false
              AND t.criadoEm >= :desde
            """)
    long contarFalhasRecentes(
            @Param("email") String email,
            @Param("ip") String ip,
            @Param("desde") Instant desde
    );

    Optional<LoginTentativa> findFirstByEmailNormalizadoAndIpAddressAndBloqueadoAteAfterOrderByCriadoEmDesc(
            String emailNormalizado,
            String ipAddress,
            Instant agora
    );
}
