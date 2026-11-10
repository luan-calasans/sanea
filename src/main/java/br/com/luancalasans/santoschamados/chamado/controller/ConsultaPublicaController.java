package br.com.luancalasans.santoschamados.chamado.controller;

import br.com.luancalasans.santoschamados.auditoria.service.AuditoriaService;
import br.com.luancalasans.santoschamados.chamado.dto.ChamadoPublicoDTO;
import br.com.luancalasans.santoschamados.chamado.service.ChamadoService;
import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.ratelimit.PublicProtocolRateLimiter;
import br.com.luancalasans.santoschamados.infrastructure.recaptcha.RecaptchaService;
import br.com.luancalasans.santoschamados.infrastructure.security.ClientIpResolver;
import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ConsultaPublicaController {

    private final ChamadoService chamadoService;
    private final PublicProtocolRateLimiter rateLimiter;
    private final ClientIpResolver clientIpResolver;
    private final AuditoriaService auditoriaService;
    private final RecaptchaService recaptchaService;
    private final AppProperties appProperties;

    public ConsultaPublicaController(
            ChamadoService chamadoService,
            PublicProtocolRateLimiter rateLimiter,
            ClientIpResolver clientIpResolver,
            AuditoriaService auditoriaService,
            RecaptchaService recaptchaService,
            AppProperties appProperties
    ) {
        this.chamadoService = chamadoService;
        this.rateLimiter = rateLimiter;
        this.clientIpResolver = clientIpResolver;
        this.auditoriaService = auditoriaService;
        this.recaptchaService = recaptchaService;
        this.appProperties = appProperties;
    }

    @GetMapping("/consulta")
    public String form(
            @RequestParam(value = "protocolo", required = false) String protocolo,
            Model model
    ) {
        if (estaAutenticado()) {
            return "redirect:/chamados";
        }
        popularRecaptcha(model);
        model.addAttribute("protocolo", protocolo);
        return "consulta/index";
    }

    @PostMapping("/consulta")
    public String consultar(
            @RequestParam("protocolo") String protocolo,
            @RequestParam(value = "g-recaptcha-response", required = false) String recaptchaToken,
            HttpServletRequest request,
            Model model
    ) {
        if (estaAutenticado()) {
            return "redirect:/chamados";
        }
        popularRecaptcha(model);
        model.addAttribute("protocolo", protocolo);

        String ip = clientIpResolver.resolve(request);
        recaptchaService.validate(recaptchaToken, ip);
        rateLimiter.check(ip);

        ChamadoPublicoDTO dto = chamadoService.consultaPublica(protocolo);
        auditoriaService.registrar(
                TipoEventoAuditoria.CONSULTA_PUBLICA,
                null,
                "CHAMADO",
                protocolo,
                dto == null ? "NAO_ENCONTRADO" : "SUCESSO",
                ip,
                null
        );

        if (dto == null) {
            model.addAttribute("naoEncontrado", true);
        } else {
            model.addAttribute("chamado", dto);
        }
        return "consulta/index";
    }

    private void popularRecaptcha(Model model) {
        model.addAttribute("recaptchaEnabled", appProperties.getRecaptcha().isEnabled());
        model.addAttribute("recaptchaSiteKey", appProperties.getRecaptcha().getSiteKey());
    }

    private boolean estaAutenticado() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
                && auth.isAuthenticated()
                && !(auth instanceof AnonymousAuthenticationToken);
    }
}
