package br.com.luancalasans.santoschamados.infrastructure.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CacheConfig {

    @Bean
    public CacheManager cacheManager(AppProperties appProperties) {
        CaffeineCache cepCache = new CaffeineCache(
                "cepPositivos",
                Caffeine.newBuilder()
                        .maximumSize(5_000)
                        .expireAfterWrite(Math.max(1, appProperties.getCep().getCacheDays()), TimeUnit.DAYS)
                        .build()
        );
        CaffeineCache dashboardCache = new CaffeineCache(
                "dashboardIndicadores",
                Caffeine.newBuilder()
                        .maximumSize(10)
                        .expireAfterWrite(3, TimeUnit.MINUTES)
                        .build()
        );
        CaffeineCache municipiosCache = new CaffeineCache(
                "municipiosPorUf",
                Caffeine.newBuilder()
                        .maximumSize(30)
                        .expireAfterWrite(7, TimeUnit.DAYS)
                        .build()
        );
        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(cepCache, dashboardCache, municipiosCache));
        return manager;
    }
}
