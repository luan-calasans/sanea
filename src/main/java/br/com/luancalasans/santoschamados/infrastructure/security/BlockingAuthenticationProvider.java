package br.com.luancalasans.santoschamados.infrastructure.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class BlockingAuthenticationProvider extends DaoAuthenticationProvider {

    private final LoginAttemptService loginAttemptService;
    private final ClientIpResolver clientIpResolver;

    public BlockingAuthenticationProvider(
            UserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder,
            LoginAttemptService loginAttemptService,
            ClientIpResolver clientIpResolver
    ) {
        super(passwordEncoder);
        setUserDetailsService(userDetailsService);
        setHideUserNotFoundExceptions(true);
        this.loginAttemptService = loginAttemptService;
        this.clientIpResolver = clientIpResolver;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String ip = resolveIp();
        if (loginAttemptService.isBlocked(email, ip)) {
            throw new org.springframework.security.authentication.BadCredentialsException("Credenciais inválidas");
        }
        return super.authenticate(authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private String resolveIp() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return "unknown";
        }
        HttpServletRequest request = attrs.getRequest();
        return clientIpResolver.resolve(request);
    }
}
