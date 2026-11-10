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
            WHERE t.ipAddress = :ip
              AND t.sucesso = false
              AND t.criadoEm >= :desde
            """)
    long contarFalhasRecentesPorIp(
            @Param("ip") String ip,
            @Param("desde") Instant desde
    );

    Optional<LoginTentativa> findFirstByIpAddressAndBloqueadoAteAfterOrderByCriadoEmDesc(
            String ipAddress,
            Instant agora
    );

    @Query("""
            SELECT MAX(t.bloqueadoAte) FROM LoginTentativa t
            WHERE t.ipAddress = :ip
              AND t.bloqueadoAte IS NOT NULL
            """)
    Optional<Instant> findUltimoBloqueadoAtePorIp(@Param("ip") String ip);
}
