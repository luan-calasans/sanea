package br.com.luancalasans.santoschamados.infrastructure.web;

import br.com.luancalasans.santoschamados.infrastructure.security.SecurityUtils;
import br.com.luancalasans.santoschamados.usuario.aparencia.AparenciaView;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AparenciaAdvice {

    @ModelAttribute("aparencia")
    public AparenciaView aparencia() {
        return SecurityUtils.currentUser()
                .map(AparenciaView::from)
                .orElseGet(AparenciaView::padrao);
    }
}
