package br.com.luancalasans.santoschamados.usuario.aparencia;

import java.util.Locale;
import java.util.regex.Pattern;

public final class AparenciaDefaults {

    public static final TemaModo TEMA = TemaModo.LIGHT;
    public static final String COR_SIDEBAR = "#012a58";
    public static final String COR_DESTAQUE = "#366AB4";
    public static final String COR_FONTE = "#0C2748";
    public static final String COR_FONTE_DARK = "#E8EEF7";

    private static final Pattern HEX = Pattern.compile("^#[0-9A-Fa-f]{6}$");

    private AparenciaDefaults() {
    }

    public static boolean isHexValido(String value) {
        return value != null && HEX.matcher(value.trim()).matches();
    }

    public static String normalizarHex(String value, String fallback) {
        if (!isHexValido(value)) {
            return fallback;
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
