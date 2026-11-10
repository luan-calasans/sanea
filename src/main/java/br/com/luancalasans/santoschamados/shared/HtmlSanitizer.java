package br.com.luancalasans.santoschamados.shared;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

public final class HtmlSanitizer {

    private static final Safelist SAFE = Safelist.basic()
            .addTags("h1", "h2", "h3", "u", "p", "br")
            .addAttributes("a", "href", "title", "rel", "target");

    private HtmlSanitizer() {
    }

    public static String sanitize(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.clean(html, SAFE).trim();
    }

    public static String plainText(String html) {
        if (html == null || html.isBlank()) {
            return "";
        }
        return Jsoup.parse(html).text().trim();
    }
}
