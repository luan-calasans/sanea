package br.com.luancalasans.santoschamados.usuario.aparencia;

import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;

public record AparenciaView(
        TemaModo modo,
        String corSidebar,
        String corDestaque,
        String corFonte,
        boolean personalizada,
        boolean fontePersonalizada
) {

    public static AparenciaView padrao() {
        return new AparenciaView(
                AparenciaDefaults.TEMA,
                AparenciaDefaults.COR_SIDEBAR,
                AparenciaDefaults.COR_DESTAQUE,
                AparenciaDefaults.COR_FONTE,
                false,
                false
        );
    }

    public static AparenciaView from(Usuario usuario) {
        if (usuario == null) {
            return padrao();
        }
        TemaModo modo = TemaModo.from(usuario.getTemaModo());
        boolean fontePersonalizada = usuario.getCorFonte() != null && !usuario.getCorFonte().isBlank();
        String sidebar = AparenciaDefaults.normalizarHex(usuario.getCorSidebar(), AparenciaDefaults.COR_SIDEBAR);
        String destaque = AparenciaDefaults.normalizarHex(usuario.getCorDestaque(), AparenciaDefaults.COR_DESTAQUE);
        String fonte = resolverFonte(usuario.getCorFonte(), modo);
        boolean personalizada = modo != AparenciaDefaults.TEMA
                || (usuario.getCorSidebar() != null && !usuario.getCorSidebar().isBlank())
                || (usuario.getCorDestaque() != null && !usuario.getCorDestaque().isBlank())
                || fontePersonalizada;
        return new AparenciaView(modo, sidebar, destaque, fonte, personalizada, fontePersonalizada);
    }

    public static AparenciaView from(UsuarioAutenticado user) {
        if (user == null) {
            return padrao();
        }
        TemaModo modo = user.getTemaModo() == null ? AparenciaDefaults.TEMA : user.getTemaModo();
        boolean fontePersonalizada = user.getCorFonte() != null && !user.getCorFonte().isBlank();
        String sidebar = AparenciaDefaults.normalizarHex(user.getCorSidebar(), AparenciaDefaults.COR_SIDEBAR);
        String destaque = AparenciaDefaults.normalizarHex(user.getCorDestaque(), AparenciaDefaults.COR_DESTAQUE);
        String fonte = resolverFonte(user.getCorFonte(), modo);
        boolean personalizada = modo != AparenciaDefaults.TEMA
                || (user.getCorSidebar() != null && !user.getCorSidebar().isBlank())
                || (user.getCorDestaque() != null && !user.getCorDestaque().isBlank())
                || fontePersonalizada;
        return new AparenciaView(modo, sidebar, destaque, fonte, personalizada, fontePersonalizada);
    }

    private static String resolverFonte(String salva, TemaModo modo) {
        if (AparenciaDefaults.isHexValido(salva)) {
            return AparenciaDefaults.normalizarHex(salva, AparenciaDefaults.COR_FONTE);
        }
        return AparenciaDefaults.fontePadrao(modo);
    }

    public boolean isDark() {
        return modo == TemaModo.DARK;
    }

    public String cssVariables() {
        String primaryHover = escurecer(corDestaque, 0.18);
        StringBuilder css = new StringBuilder();
        css.append("--sidebar:").append(corSidebar).append(';')
                .append("--primary:").append(corDestaque).append(';')
                .append("--primary-hover:").append(primaryHover).append(';')
                .append("--info:").append(primaryHover).append(';')
                .append("--status-aberto:").append(primaryHover).append(';')
                .append("--status-atendimento:").append(corDestaque).append(';')
                .append("--focus:0 0 0 3px ").append(comAlpha(corDestaque, 0.28)).append(';');
        if (fontePersonalizada) {
            String textSecondary = misturar(corFonte, modo == TemaModo.DARK ? "#FFFFFF" : "#000000", 0.38);
            css.append("--text:").append(corFonte).append(';')
                    .append("--text-secondary:").append(textSecondary).append(';');
        }
        return css.toString();
    }

    private static String escurecer(String hex, double amount) {
        int[] rgb = parse(hex);
        return toHex(
                (int) Math.round(rgb[0] * (1 - amount)),
                (int) Math.round(rgb[1] * (1 - amount)),
                (int) Math.round(rgb[2] * (1 - amount))
        );
    }

    private static String misturar(String hex, String other, double otherRatio) {
        int[] a = parse(hex);
        int[] b = parse(other);
        return toHex(
                (int) Math.round(a[0] + (b[0] - a[0]) * otherRatio),
                (int) Math.round(a[1] + (b[1] - a[1]) * otherRatio),
                (int) Math.round(a[2] + (b[2] - a[2]) * otherRatio)
        );
    }

    private static String comAlpha(String hex, double alpha) {
        int[] rgb = parse(hex);
        return "rgba(" + rgb[0] + "," + rgb[1] + "," + rgb[2] + "," + alpha + ")";
    }

    private static int[] parse(String hex) {
        String h = hex.substring(1);
        return new int[]{
                Integer.parseInt(h.substring(0, 2), 16),
                Integer.parseInt(h.substring(2, 4), 16),
                Integer.parseInt(h.substring(4, 6), 16)
        };
    }

    private static String toHex(int r, int g, int b) {
        return String.format("#%02X%02X%02X",
                Math.max(0, Math.min(255, r)),
                Math.max(0, Math.min(255, g)),
                Math.max(0, Math.min(255, b)));
    }
}
