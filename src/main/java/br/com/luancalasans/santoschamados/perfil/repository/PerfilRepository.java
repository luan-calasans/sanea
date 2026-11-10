package br.com.luancalasans.santoschamados.perfil.repository;

import br.com.luancalasans.santoschamados.perfil.entity.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {

    Optional<Perfil> findByCodigo(String codigo);
}
