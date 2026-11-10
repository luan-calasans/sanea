package br.com.luancalasans.santoschamados.mensagem.repository;

import br.com.luancalasans.santoschamados.mensagem.entity.MensagemChamado;
import br.com.luancalasans.santoschamados.shared.VisibilidadeMensagem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface MensagemChamadoRepository extends JpaRepository<MensagemChamado, Long> {

    @Query("""
            SELECT m FROM MensagemChamado m
            JOIN FETCH m.autor a
            JOIN FETCH a.perfil
            WHERE m.chamado.id = :chamadoId
            ORDER BY m.criadoEm ASC
            """)
    List<MensagemChamado> listarTodasPorChamado(@Param("chamadoId") Long chamadoId);

    @Query("""
            SELECT m FROM MensagemChamado m
            JOIN FETCH m.autor a
            JOIN FETCH a.perfil
            WHERE m.chamado.id = :chamadoId
              AND m.visibilidade = br.com.luancalasans.santoschamados.shared.VisibilidadeMensagem.PUBLICA
            ORDER BY m.criadoEm ASC
            """)
    List<MensagemChamado> listarPublicasPorChamado(@Param("chamadoId") Long chamadoId);

    @Query("""
            SELECT m FROM MensagemChamado m
            JOIN FETCH m.autor a
            JOIN FETCH a.perfil
            WHERE m.chamado.id = :chamadoId
              AND m.criadoEm > :desde
            ORDER BY m.criadoEm ASC
            """)
    List<MensagemChamado> listarTodasPorChamadoDesde(
            @Param("chamadoId") Long chamadoId,
            @Param("desde") Instant desde
    );

    @Query("""
            SELECT m FROM MensagemChamado m
            JOIN FETCH m.autor a
            JOIN FETCH a.perfil
            WHERE m.chamado.id = :chamadoId
              AND m.visibilidade = br.com.luancalasans.santoschamados.shared.VisibilidadeMensagem.PUBLICA
              AND m.criadoEm > :desde
            ORDER BY m.criadoEm ASC
            """)
    List<MensagemChamado> listarPublicasPorChamadoDesde(
            @Param("chamadoId") Long chamadoId,
            @Param("desde") Instant desde
    );

    List<MensagemChamado> findByChamadoIdAndVisibilidadeOrderByCriadoEmAsc(
            Long chamadoId,
            VisibilidadeMensagem visibilidade
    );

    void deleteByChamadoId(Long chamadoId);
}
