package br.com.luancalasans.santoschamados.infrastructure.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class ForcePasswordChangeFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof UsuarioAutenticado user
                && user.isDeveTrocarSenha()) {
            String path = request.getRequestURI();
            String context = request.getContextPath() == null ? "" : request.getContextPath();
            String relative = path.startsWith(context) ? path.substring(context.length()) : path;
            if (!isPermitidoDuranteTrocaObrigatoria(relative, request.getMethod())) {
                response.sendRedirect(context + "/conta/trocar-senha?obrigatorio=true");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean isPermitidoDuranteTrocaObrigatoria(String path, String method) {
        if (path == null || path.isBlank() || "/".equals(path)) {
            return true;
        }
        if (path.startsWith("/css/")
                || path.startsWith("/js/")
                || path.startsWith("/img/")
                || path.startsWith("/icons/")
                || path.startsWith("/actuator/")
                || path.startsWith("/error")
                || "/favicon.ico".equals(path)
                || "/favicon.webp".equals(path)
                || "/manifest.webmanifest".equals(path)
                || "/sw.js".equals(path)
                || "/offline.html".equals(path)) {
            return true;
        }
        if ("/logout".equals(path) && "POST".equalsIgnoreCase(method)) {
            return true;
        }
        return path.startsWith("/conta/trocar-senha");
    }
}
