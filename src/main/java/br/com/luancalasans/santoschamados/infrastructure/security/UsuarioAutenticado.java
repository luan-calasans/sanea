package br.com.luancalasans.santoschamados.infrastructure.security;

import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

public class UsuarioAutenticado implements UserDetails {

    private final Long id;
    private final String nome;
    private final String email;
    private final String senhaHash;
    private final boolean ativo;
    private final boolean emailVerificado;
    private final boolean deveTrocarSenha;
    private final String perfilCodigo;
    private final Collection<? extends GrantedAuthority> authorities;

    public UsuarioAutenticado(Usuario usuario) {
        this.id = usuario.getId();
        this.nome = usuario.getNome();
        this.email = usuario.getEmail();
        this.senhaHash = usuario.getSenhaHash();
        this.ativo = usuario.isAtivo();
        this.emailVerificado = usuario.isEmailVerificado();
        this.deveTrocarSenha = usuario.isDeveTrocarSenha();
        this.perfilCodigo = usuario.getPerfil().getCodigo();
        this.authorities = usuario.getPerfil().getAuthorities().stream()
                .map(a -> new SimpleGrantedAuthority(a.getCodigo()))
                .collect(Collectors.toUnmodifiableSet());
    }

    public Long getId() {
        return id;
    }

    public String getNome() {
        return nome;
    }

    public String getPerfilCodigo() {
        return perfilCodigo;
    }

    public boolean isDeveTrocarSenha() {
        return deveTrocarSenha;
    }

    public boolean isEmailVerificado() {
        return emailVerificado;
    }

    public boolean possuiAuthority(String authority) {
        return authorities.stream().anyMatch(a -> a.getAuthority().equals(authority));
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return senhaHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return ativo;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return ativo && emailVerificado;
    }
}
