package br.com.luancalasans.santoschamados.infrastructure.security;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    public static Optional<UsuarioAutenticado> currentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UsuarioAutenticado usuario)) {
            return Optional.empty();
        }
        return Optional.of(usuario);
    }

    public static UsuarioAutenticado requireUser() {
        return currentUser().orElseThrow(() -> new AccessDeniedException("Autenticação necessária"));
    }

    public static void requireAuthority(String authority) {
        UsuarioAutenticado user = requireUser();
        if (!user.possuiAuthority(authority)) {
            throw new AccessDeniedException("Sem permissão para esta operação");
        }
    }
}
