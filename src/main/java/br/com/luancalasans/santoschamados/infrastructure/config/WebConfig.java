package br.com.luancalasans.santoschamados.infrastructure.config;

import br.com.luancalasans.santoschamados.infrastructure.web.TimeFormattingAdvice;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

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
