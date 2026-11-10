package br.com.luancalasans.santoschamados.infrastructure.ratelimit;

import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PublicProtocolRateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final AppProperties appProperties;

    public PublicProtocolRateLimiter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void check(String ip) {
        Bucket bucket = buckets.computeIfAbsent(ip == null ? "unknown" : ip, this::newBucket);
        if (!bucket.tryConsume(1)) {
            throw new BusinessException("Limite de consultas excedido. Aguarde um minuto e tente novamente.");
        }
    }

    private Bucket newBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(appProperties.getRateLimit().getPublicProtocolLimit())
                .refillGreedy(
                        appProperties.getRateLimit().getPublicProtocolLimit(),
                        Duration.ofSeconds(appProperties.getRateLimit().getPublicProtocolWindowSeconds())
                )
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
