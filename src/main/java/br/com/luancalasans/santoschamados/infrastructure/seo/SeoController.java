package br.com.luancalasans.santoschamados.infrastructure.seo;

import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Controller
public class SeoController {

    private final AppProperties appProperties;

    public SeoController(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @GetMapping(value = "/robots.txt", produces = MediaType.TEXT_PLAIN_VALUE)
    @ResponseBody
    public String robotsTxt() {
        String base = baseUrl();
        return """
                User-agent: *
                Allow: /consulta
                Allow: /css/
                Allow: /js/
                Allow: /img/
                Allow: /icons/
                Allow: /favicon.webp
                Allow: /manifest.webmanifest
                Disallow: /login
                Disallow: /cadastro
                Disallow: /verificar-email
                Disallow: /inicio
                Disallow: /chamados
                Disallow: /conta
                Disallow: /admin
                Disallow: /dashboard
                Disallow: /api/
                Disallow: /actuator
                Disallow: /error

                Sitemap: %s/sitemap.xml
                """.formatted(base);
    }

    @GetMapping(value = "/sitemap.xml", produces = MediaType.APPLICATION_XML_VALUE)
    @ResponseBody
    public String sitemapXml() {
        String base = baseUrl();
        String lastmod = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <urlset xmlns="http://www.sitemaps.org/schemas/sitemap/0.9">
                  <url>
                    <loc>%s/consulta</loc>
                    <lastmod>%s</lastmod>
                    <changefreq>weekly</changefreq>
                    <priority>1.0</priority>
                  </url>
                </urlset>
                """.formatted(base, lastmod);
    }

    private String baseUrl() {
        String base = appProperties.getBaseUrl();
        if (base == null || base.isBlank()) {
            return "http://localhost:8080";
        }
        return base.endsWith("/") ? base.substring(0, base.length() - 1) : base;
    }
}
