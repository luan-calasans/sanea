package br.com.luancalasans.santoschamados.security;

import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import br.com.luancalasans.santoschamados.infrastructure.ratelimit.PublicProtocolRateLimiter;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PublicProtocolRateLimiterTest {

    @Test
    void bloqueiaAposLimite() {
        AppProperties props = new AppProperties();
        props.getRateLimit().setPublicProtocolLimit(3);
        props.getRateLimit().setPublicProtocolWindowSeconds(60);
        PublicProtocolRateLimiter limiter = new PublicProtocolRateLimiter(props);

        assertThatCode(() -> {
            limiter.check("1.1.1.1");
            limiter.check("1.1.1.1");
            limiter.check("1.1.1.1");
        }).doesNotThrowAnyException();

        assertThatThrownBy(() -> limiter.check("1.1.1.1"))
                .isInstanceOf(BusinessException.class);
        assertThatCode(() -> limiter.check("2.2.2.2")).doesNotThrowAnyException();
    }
}
