package br.com.luancalasans.santoschamados.infrastructure.config;

import br.com.luancalasans.santoschamados.infrastructure.web.TimeFormattingAdvice;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final AppProperties appProperties;

    public WebConfig(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("redirect:/inicio");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/sw.js")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.noStore());
        registry.addResourceHandler("/manifest.webmanifest")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.HOURS).cachePublic());
        registry.addResourceHandler("/offline.html")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic());
    }

    @Bean
    public RestClient.Builder restClientBuilder(RestTemplateBuilder restTemplateBuilder) {
        return RestClient.builder(restTemplateBuilder
                .setConnectTimeout(Duration.ofMillis(appProperties.getCep().getTimeoutMs()))
                .setReadTimeout(Duration.ofMillis(appProperties.getCep().getTimeoutMs()))
                .build());
    }

    @Bean
    public TimeFormattingAdvice timeFormattingAdvice() {
        return new TimeFormattingAdvice();
    }
}
