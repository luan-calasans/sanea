package br.com.luancalasans.santoschamados.mensagem.controller;

import br.com.luancalasans.santoschamados.infrastructure.security.SecurityUtils;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.mensagem.dto.MensagemItemDTO;
import br.com.luancalasans.santoschamados.mensagem.dto.NovaMensagemForm;
import br.com.luancalasans.santoschamados.mensagem.service.MensagemService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class MensagemController {

    private final MensagemService mensagemService;

    public MensagemController(MensagemService mensagemService) {
        this.mensagemService = mensagemService;
    }

    @PostMapping("/chamados/{id}/mensagens")
    public String publicar(
            @PathVariable Long id,
            @Valid @ModelAttribute NovaMensagemForm form,
            RedirectAttributes ra
    ) {
        UsuarioAutenticado user = SecurityUtils.requireUser();
        mensagemService.publicar(id, form, user);
        ra.addFlashAttribute("sucesso", "Mensagem enviada.");
        return "redirect:/chamados/" + id + "#mensagens";
    }

    @GetMapping(value = "/chamados/{id}/mensagens.json", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    public List<Map<String, Object>> polling(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant desde
    ) {
        UsuarioAutenticado user = SecurityUtils.requireUser();
        return mensagemService.listarDto(id, user, desde).stream().map(this::toMap).toList();
    }

    @GetMapping("/chamados/{id}/mensagens/fragment")
    public String fragment(@PathVariable Long id, Model model) {
        UsuarioAutenticado user = SecurityUtils.requireUser();
        model.addAttribute("mensagens", mensagemService.listarDto(id, user, null));
        model.addAttribute("user", user);
        return "fragments/mensagens :: thread";
    }

    private Map<String, Object> toMap(MensagemItemDTO m) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", m.id());
        map.put("corpo", m.corpo());
        map.put("visibilidade", m.visibilidade());
        map.put("criadoEm", m.criadoEmFormatado());
        map.put("perfilAutor", m.autorNome());
        return map;
    }
}
