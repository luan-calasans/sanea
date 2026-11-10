package br.com.luancalasans.santoschamados.infrastructure.ratelimit;

import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PublicProtocolRateLimiter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final Map<String, Instant> blockedUntil = new ConcurrentHashMap<>();
    private final AppProperties appProperties;

    public PublicProtocolRateLimiter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public void check(String ip) {
        String key = ip == null || ip.isBlank() ? "unknown" : ip;
        Instant agora = Instant.now();
        Instant bloqueio = blockedUntil.get(key);
        if (bloqueio != null) {
            if (bloqueio.isAfter(agora)) {
                throw new BusinessException(
                        "Limite de consultas excedido. Aguarde 5 minutos e tente novamente."
                );
            }
            blockedUntil.remove(key, bloqueio);
            buckets.remove(key);
        }

        Bucket bucket = buckets.computeIfAbsent(key, this::newBucket);
        if (!bucket.tryConsume(1)) {
            int blockSeconds = appProperties.getRateLimit().getPublicProtocolBlockSeconds();
            blockedUntil.put(key, agora.plusSeconds(blockSeconds));
            buckets.remove(key);
            throw new BusinessException(
                    "Limite de consultas excedido. Aguarde 5 minutos e tente novamente."
            );
        }
    }

    private Bucket newBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(appProperties.getRateLimit().getPublicProtocolLimit())
                .refillIntervally(
                        appProperties.getRateLimit().getPublicProtocolLimit(),
                        Duration.ofSeconds(appProperties.getRateLimit().getPublicProtocolWindowSeconds())
                )
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
