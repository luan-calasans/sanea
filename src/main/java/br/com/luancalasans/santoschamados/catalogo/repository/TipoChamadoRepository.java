package br.com.luancalasans.santoschamados.catalogo.repository;

import br.com.luancalasans.santoschamados.catalogo.entity.TipoChamado;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TipoChamadoRepository extends JpaRepository<TipoChamado, Long> {

    List<TipoChamado> findByAtivoTrueOrderByOrdemAsc();

    List<TipoChamado> findAllByOrderByOrdemAsc();

    Optional<TipoChamado> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);
}
