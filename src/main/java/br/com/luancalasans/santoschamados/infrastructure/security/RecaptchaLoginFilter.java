package br.com.luancalasans.santoschamados.infrastructure.security;

import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import br.com.luancalasans.santoschamados.infrastructure.recaptcha.RecaptchaService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class RecaptchaLoginFilter extends OncePerRequestFilter {

    private final RecaptchaService recaptchaService;
    private final ClientIpResolver clientIpResolver;
    private final AntPathRequestMatcher matcher = new AntPathRequestMatcher("/login", "POST");

    public RecaptchaLoginFilter(RecaptchaService recaptchaService, ClientIpResolver clientIpResolver) {
        this.recaptchaService = recaptchaService;
        this.clientIpResolver = clientIpResolver;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (matcher.matches(request)) {
            try {
                String token = request.getParameter("g-recaptcha-response");
                recaptchaService.validate(token, clientIpResolver.resolve(request));
            } catch (BusinessException ex) {
                response.sendRedirect(request.getContextPath() + "/login?erro=recaptcha");
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
