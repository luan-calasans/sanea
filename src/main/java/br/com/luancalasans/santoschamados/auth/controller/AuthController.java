package br.com.luancalasans.santoschamados.auth.controller;

import br.com.luancalasans.santoschamados.auth.dto.CadastroCidadaoForm;
import br.com.luancalasans.santoschamados.auth.service.CadastroService;
import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.security.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final CadastroService cadastroService;
    private final AppProperties appProperties;
    private final ClientIpResolver clientIpResolver;

    public AuthController(
            CadastroService cadastroService,
            AppProperties appProperties,
            ClientIpResolver clientIpResolver
    ) {
        this.cadastroService = cadastroService;
        this.appProperties = appProperties;
        this.clientIpResolver = clientIpResolver;
    }

    @GetMapping("/login")
    public String login(
            @RequestParam(value = "erro", required = false) String erro,
            @RequestParam(value = "logout", required = false) String logout,
            Model model
    ) {
        model.addAttribute("recaptchaEnabled", appProperties.getRecaptcha().isEnabled());
        model.addAttribute("recaptchaSiteKey", appProperties.getRecaptcha().getSiteKey());
        if (erro != null) {
            model.addAttribute("erro", switch (erro) {
                case "bloqueado" -> "Muitas tentativas. Aguarde alguns minutos e tente novamente.";
                case "inativo" -> "Conta inativa ou e-mail não verificado.";
                case "recaptcha" -> "Confirme o reCAPTCHA para continuar.";
                default -> "Não foi possível autenticar com os dados informados.";
            });
        }
        if (logout != null) {
            model.addAttribute("sucesso", "Sessão encerrada com sucesso.");
        }
        return "auth/login";
    }

    @GetMapping("/cadastro")
    public String cadastroForm(Model model) {
        model.addAttribute("form", new CadastroCidadaoForm());
        model.addAttribute("recaptchaEnabled", appProperties.getRecaptcha().isEnabled());
        model.addAttribute("recaptchaSiteKey", appProperties.getRecaptcha().getSiteKey());
        return "auth/cadastro";
    }

    @PostMapping("/cadastro")
    public String cadastrar(
            @Valid @ModelAttribute("form") CadastroCidadaoForm form,
            BindingResult bindingResult,
            @RequestParam(value = "g-recaptcha-response", required = false) String recaptcha,
            HttpServletRequest request,
            Model model,
            RedirectAttributes ra
    ) {
        form.setRecaptchaToken(recaptcha);
        model.addAttribute("recaptchaEnabled", appProperties.getRecaptcha().isEnabled());
        model.addAttribute("recaptchaSiteKey", appProperties.getRecaptcha().getSiteKey());
        if (bindingResult.hasErrors()) {
            model.addAttribute("erro", "Verifique os campos obrigatórios.");
            return "auth/cadastro";
        }
        cadastroService.cadastrarCidadao(form, clientIpResolver.resolve(request));
        ra.addFlashAttribute("sucesso", "Cadastro realizado. Verifique seu e-mail para ativar a conta.");
        return "redirect:/login";
    }

    @GetMapping("/verificar-email")
    public String verificarEmail(@RequestParam("token") String token, RedirectAttributes ra) {
        cadastroService.verificarEmail(token);
        ra.addFlashAttribute("sucesso", "E-mail verificado com sucesso. Você já pode entrar.");
        return "redirect:/login";
    }
}
