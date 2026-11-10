package br.com.luancalasans.santoschamados.auth.controller;

import br.com.luancalasans.santoschamados.infrastructure.security.SecurityUtils;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.shared.Authorities;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/inicio")
    public String inicio(Model model) {
        UsuarioAutenticado user = SecurityUtils.requireUser();
        model.addAttribute("usuario", user);
        if (user.possuiAuthority(Authorities.DASHBOARD_VISUALIZAR)) {
            return "redirect:/dashboard";
        }
        if (user.possuiAuthority(Authorities.CHAMADO_VISUALIZAR_TODOS)) {
            return "redirect:/chamados";
        }
        return "redirect:/conta";
    }
}
