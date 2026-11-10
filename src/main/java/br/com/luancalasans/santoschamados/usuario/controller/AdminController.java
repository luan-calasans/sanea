package br.com.luancalasans.santoschamados.usuario.controller;

import br.com.luancalasans.santoschamados.auditoria.service.AuditoriaService;
import br.com.luancalasans.santoschamados.catalogo.service.TipoChamadoService;
import br.com.luancalasans.santoschamados.infrastructure.security.ClientIpResolver;
import br.com.luancalasans.santoschamados.infrastructure.security.SecurityUtils;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.perfil.repository.PerfilRepository;
import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import br.com.luancalasans.santoschamados.usuario.repository.UsuarioRepository;
import br.com.luancalasans.santoschamados.usuario.service.UsuarioAdminService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AdminController {

    private final UsuarioAdminService usuarioAdminService;
    private final PerfilRepository perfilRepository;
    private final TipoChamadoService tipoChamadoService;
    private final AuditoriaService auditoriaService;
    private final UsuarioRepository usuarioRepository;
    private final ClientIpResolver clientIpResolver;

    public AdminController(
            UsuarioAdminService usuarioAdminService,
            PerfilRepository perfilRepository,
            TipoChamadoService tipoChamadoService,
            AuditoriaService auditoriaService,
            UsuarioRepository usuarioRepository,
            ClientIpResolver clientIpResolver
    ) {
        this.usuarioAdminService = usuarioAdminService;
        this.perfilRepository = perfilRepository;
        this.tipoChamadoService = tipoChamadoService;
        this.auditoriaService = auditoriaService;
        this.usuarioRepository = usuarioRepository;
        this.clientIpResolver = clientIpResolver;
    }

    @GetMapping("/admin")
    public String index() {
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/admin/usuarios")
    public String usuarios(
            @RequestParam(required = false) String termo,
            @RequestParam(required = false) String perfil,
            @RequestParam(required = false) Boolean ativo,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        UsuarioAutenticado admin = SecurityUtils.requireUser();
        model.addAttribute("page", usuarioAdminService.listar(
                termo, perfil, ativo, PageRequest.of(page, 20, Sort.by("nome")), admin
        ));
        model.addAttribute("perfis", perfilRepository.findAll());
        model.addAttribute("termo", termo);
        model.addAttribute("perfil", perfil);
        model.addAttribute("ativo", ativo);
        return "admin/usuarios";
    }

    @PostMapping("/admin/usuarios")
    public String criar(
            @RequestParam String nome,
            @RequestParam String email,
            @RequestParam String senha,
            @RequestParam String perfilCodigo,
            HttpServletRequest request,
            RedirectAttributes ra
    ) {
        usuarioAdminService.criarInterno(
                nome, email, senha, perfilCodigo, SecurityUtils.requireUser(), clientIpResolver.resolve(request)
        );
        ra.addFlashAttribute("sucesso", "Usuário interno criado.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/{id}/ativo")
    public String ativo(
            @PathVariable Long id,
            @RequestParam boolean ativo,
            HttpServletRequest request,
            RedirectAttributes ra
    ) {
        usuarioAdminService.alterarAtivo(id, ativo, SecurityUtils.requireUser(), clientIpResolver.resolve(request));
        ra.addFlashAttribute("sucesso", ativo ? "Usuário ativado." : "Usuário desativado.");
        return "redirect:/admin/usuarios";
    }

    @PostMapping("/admin/usuarios/{id}/perfil")
    public String perfil(
            @PathVariable Long id,
            @RequestParam String perfilCodigo,
            HttpServletRequest request,
            RedirectAttributes ra
    ) {
        usuarioAdminService.alterarPerfil(id, perfilCodigo, SecurityUtils.requireUser(), clientIpResolver.resolve(request));
        ra.addFlashAttribute("sucesso", "Perfil atualizado.");
        return "redirect:/admin/usuarios";
    }

    @GetMapping("/admin/tipos")
    public String tipos(Model model) {
        model.addAttribute("tipos", tipoChamadoService.listarTodos(SecurityUtils.requireUser()));
        return "admin/tipos";
    }

    @PostMapping("/admin/tipos")
    public String salvarTipo(
            @RequestParam(required = false) Long id,
            @RequestParam String codigo,
            @RequestParam String nome,
            @RequestParam(required = false) String descricao,
            @RequestParam(defaultValue = "true") boolean ativo,
            @RequestParam(defaultValue = "0") int ordem,
            HttpServletRequest request,
            RedirectAttributes ra
    ) {
        tipoChamadoService.salvar(id, codigo, nome, descricao, ativo, ordem,
                SecurityUtils.requireUser(), clientIpResolver.resolve(request));
        ra.addFlashAttribute("sucesso", "Tipo de chamado salvo.");
        return "redirect:/admin/tipos";
    }

    @GetMapping("/admin/auditoria")
    public String auditoria(
            @RequestParam(required = false) TipoEventoAuditoria tipo,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        var user = SecurityUtils.requireUser();
        var usuario = usuarioRepository.findById(user.getId()).orElseThrow();
        model.addAttribute("page", auditoriaService.listar(
                tipo, PageRequest.of(page, 30, Sort.by(Sort.Direction.DESC, "criadoEm")), usuario
        ));
        model.addAttribute("tipo", tipo);
        model.addAttribute("tiposEvento", TipoEventoAuditoria.values());
        return "admin/auditoria";
    }
}
