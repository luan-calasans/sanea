package br.com.luancalasans.santoschamados.usuario.aparencia;

public enum TemaModo {
    LIGHT,
    DARK;

    public static TemaModo from(String value) {
        if (value == null || value.isBlank()) {
            return LIGHT;
        }
        try {
            return TemaModo.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return LIGHT;
        }
    }
}
