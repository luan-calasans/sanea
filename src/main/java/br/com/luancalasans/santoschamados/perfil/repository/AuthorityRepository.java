package br.com.luancalasans.santoschamados.perfil.repository;

import br.com.luancalasans.santoschamados.perfil.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorityRepository extends JpaRepository<Authority, Long> {

    Optional<Authority> findByCodigo(String codigo);
}
