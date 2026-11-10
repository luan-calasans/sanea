package br.com.luancalasans.santoschamados.usuario.controller;

import br.com.luancalasans.santoschamados.chamado.service.ChamadoService;
import br.com.luancalasans.santoschamados.infrastructure.security.SecurityUtils;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import br.com.luancalasans.santoschamados.usuario.service.ContaService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContaController {

    private final ContaService contaService;
    private final ChamadoService chamadoService;

    public ContaController(ContaService contaService, ChamadoService chamadoService) {
        this.contaService = contaService;
        this.chamadoService = chamadoService;
    }

    @GetMapping("/conta")
    public String conta(Model model) {
        UsuarioAutenticado user = SecurityUtils.requireUser();
        Usuario usuario = contaService.obter(user);
        model.addAttribute("usuario", usuario);
        model.addAttribute("perfilNome", usuario.getPerfil().getNome());
        model.addAttribute("meusChamados", chamadoService.listarMeus(
                user,
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "abertoEm"))
        ));
        return "conta/index";
    }

    @PostMapping("/conta/nome")
    public String atualizarNome(@RequestParam String nome, RedirectAttributes ra) {
        contaService.atualizarNome(SecurityUtils.requireUser(), nome);
        ra.addFlashAttribute("sucesso", "Nome atualizado.");
        return "redirect:/conta";
    }

    @GetMapping("/conta/trocar-senha")
    public String trocarSenhaForm(@RequestParam(required = false) Boolean obrigatorio, Model model) {
        model.addAttribute("obrigatorio", Boolean.TRUE.equals(obrigatorio));
        return "conta/trocar-senha";
    }

    @PostMapping("/conta/trocar-senha")
    public String trocarSenha(
            @RequestParam String senhaAtual,
            @RequestParam String novaSenha,
            @RequestParam String confirmacao,
            RedirectAttributes ra
    ) {
        UsuarioAutenticado atual = SecurityUtils.requireUser();
        Usuario atualizado = contaService.trocarSenha(atual, senhaAtual, novaSenha, confirmacao);
        atualizarSessao(atualizado);
        ra.addFlashAttribute("sucesso", "Senha alterada com sucesso.");
        return "redirect:/inicio";
    }

    @PostMapping("/conta/email")
    public String alterarEmail(@RequestParam String novoEmail, RedirectAttributes ra) {
        contaService.solicitarAlteracaoEmail(SecurityUtils.requireUser(), novoEmail);
        ra.addFlashAttribute("sucesso", "Enviamos um e-mail de confirmação para o novo endereço.");
        return "redirect:/conta";
    }

    @PostMapping("/conta/aparencia")
    public String atualizarAparencia(
            @RequestParam String temaModo,
            @RequestParam String corSidebar,
            @RequestParam String corDestaque,
            @RequestParam String corFonte,
            RedirectAttributes ra
    ) {
        Usuario atualizado = contaService.atualizarAparencia(
                SecurityUtils.requireUser(),
                temaModo,
                corSidebar,
                corDestaque,
                corFonte
        );
        atualizarSessao(atualizado);
        ra.addFlashAttribute("sucesso", "Aparência salva.");
        return "redirect:/conta";
    }

    @PostMapping("/conta/aparencia/redefinir")
    public String redefinirAparencia(RedirectAttributes ra) {
        Usuario atualizado = contaService.redefinirAparencia(SecurityUtils.requireUser());
        atualizarSessao(atualizado);
        ra.addFlashAttribute("sucesso", "Aparência redefinida para o padrão.");
        return "redirect:/conta";
    }

    private void atualizarSessao(Usuario atualizado) {
        UsuarioAutenticado principal = new UsuarioAutenticado(atualizado);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                principal,
                principal.getPassword(),
                principal.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
