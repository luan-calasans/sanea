package br.com.luancalasans.santoschamados.infrastructure.config;

import br.com.luancalasans.santoschamados.auditoria.service.AuditoriaService;
import br.com.luancalasans.santoschamados.infrastructure.recaptcha.RecaptchaService;
import br.com.luancalasans.santoschamados.infrastructure.security.ClientIpResolver;
import br.com.luancalasans.santoschamados.infrastructure.security.ForcePasswordChangeFilter;
import br.com.luancalasans.santoschamados.infrastructure.security.LoginAttemptService;
import br.com.luancalasans.santoschamados.infrastructure.security.RecaptchaLoginFilter;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import br.com.luancalasans.santoschamados.usuario.repository.UsuarioRepository;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;
import org.springframework.security.web.session.HttpSessionEventPublisher;

import java.io.IOException;
import java.time.Instant;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final AppProperties appProperties;
    private final LoginAttemptService loginAttemptService;
    private final ClientIpResolver clientIpResolver;
    private final AuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;
    private final RecaptchaService recaptchaService;

    public SecurityConfig(
            AppProperties appProperties,
            LoginAttemptService loginAttemptService,
            ClientIpResolver clientIpResolver,
            AuditoriaService auditoriaService,
            UsuarioRepository usuarioRepository,
            RecaptchaService recaptchaService
    ) {
        this.appProperties = appProperties;
        this.loginAttemptService = loginAttemptService;
        this.clientIpResolver = clientIpResolver;
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
        this.recaptchaService = recaptchaService;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse()))
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                "script-src 'self' https://www.google.com https://www.gstatic.com https://cdn.jsdelivr.net https://static.cloudflareinsights.com; " +
                                "style-src 'self' 'unsafe-inline' https://cdn.jsdelivr.net https://fonts.googleapis.com https://use.typekit.net https://p.typekit.net; " +
                                "font-src 'self' data: https://fonts.gstatic.com https://use.typekit.net https://p.typekit.net https://cdn.jsdelivr.net; " +
                                "img-src 'self' data: blob: https:; " +
                                "frame-src https://www.google.com https://recaptcha.google.com; " +
                                "connect-src 'self' https://www.google.com https://cloudflareinsights.com https://*.cloudflareinsights.com https://static.cloudflareinsights.com; " +
                                "frame-ancestors 'none'; " +
                                "base-uri 'self'; " +
                                "form-action 'self'"
                        ))
                        .referrerPolicy(r -> r.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))
                        .frameOptions(frame -> frame.deny())
                        .xssProtection(Customizer.withDefaults())
                )
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/cadastro",
                                "/cadastro/**",
                                "/verificar-email",
                                "/consulta",
                                "/consulta/**",
                                "/css/**",
                                "/js/**",
                                "/img/**",
                                "/icons/**",
                                "/favicon.ico",
                                "/favicon.webp",
                                "/manifest.webmanifest",
                                "/sw.js",
                                "/offline.html",
                                "/actuator/health",
                                "/actuator/info",
                                "/error",
                                "/error/**"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/cep/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/localidades/**").authenticated()
                        .requestMatchers("/admin/**").hasAuthority("USUARIO_GERENCIAR")
                        .requestMatchers("/dashboard/**").hasAuthority("DASHBOARD_VISUALIZAR")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("senha")
                        .successHandler(successHandler())
                        .failureHandler(failureHandler())
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .deleteCookies("SANTOS_SESSION")
                        .permitAll()
                )
                .sessionManagement(session -> session
                        .sessionFixation(fixation -> fixation.migrateSession())
                        .maximumSessions(3)
                )
                .addFilterBefore(
                        new RecaptchaLoginFilter(recaptchaService, clientIpResolver),
                        UsernamePasswordAuthenticationFilter.class
                )
                .addFilterAfter(new ForcePasswordChangeFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public HttpSessionEventPublisher httpSessionEventPublisher() {
        return new HttpSessionEventPublisher();
    }

    private AuthenticationSuccessHandler successHandler() {
        return (HttpServletRequest request, HttpServletResponse response, Authentication authentication) -> {
            String ip = clientIpResolver.resolve(request);
            String email = authentication.getName();
            loginAttemptService.registerSuccess(email, ip);
            if (authentication.getPrincipal() instanceof UsuarioAutenticado user) {
                usuarioRepository.findById(user.getId()).ifPresent(u -> {
                    u.setUltimoAcessoEm(Instant.now());
                    usuarioRepository.save(u);
                });
                auditoriaService.registrar(
                        TipoEventoAuditoria.LOGIN_SUCESSO,
                        user.getId(),
                        "USUARIO",
                        String.valueOf(user.getId()),
                        "SUCESSO",
                        ip,
                        null
                );
                if (user.isDeveTrocarSenha()) {
                    response.sendRedirect(request.getContextPath() + "/conta/trocar-senha?obrigatorio=true");
                    return;
                }
            }
            response.sendRedirect(request.getContextPath() + "/inicio");
        };
    }

    private AuthenticationFailureHandler failureHandler() {
        return new SimpleUrlAuthenticationFailureHandler() {
            @Override
            public void onAuthenticationFailure(
                    HttpServletRequest request,
                    HttpServletResponse response,
                    AuthenticationException exception
            ) throws IOException, ServletException {
                String email = request.getParameter("email");
                String ip = clientIpResolver.resolve(request);
                if (email != null) {
                    loginAttemptService.registerFailure(email, ip);
                    auditoriaService.registrar(
                            TipoEventoAuditoria.LOGIN_FALHA,
                            null,
                            "USUARIO",
                            null,
                            "FALHA",
                            ip,
                            "Tentativa de autenticação rejeitada"
                    );
                }
                String reason = "credenciais";
                if (exception instanceof LockedException) {
                    reason = "bloqueado";
                } else if (exception instanceof DisabledException) {
                    reason = "inativo";
                }
                setDefaultFailureUrl("/login?erro=" + reason);
                super.onAuthenticationFailure(request, response, exception);
            }
        };
    }
}
