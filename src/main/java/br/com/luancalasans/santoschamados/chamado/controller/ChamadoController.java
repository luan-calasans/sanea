package br.com.luancalasans.santoschamados.chamado.controller;

import br.com.luancalasans.santoschamados.catalogo.service.TipoChamadoService;
import br.com.luancalasans.santoschamados.chamado.dto.AberturaChamadoForm;
import br.com.luancalasans.santoschamados.chamado.dto.AlterarStatusForm;
import br.com.luancalasans.santoschamados.chamado.dto.ChamadoDetalheDTO;
import br.com.luancalasans.santoschamados.chamado.entity.Chamado;
import br.com.luancalasans.santoschamados.chamado.service.ChamadoService;
import br.com.luancalasans.santoschamados.infrastructure.security.ClientIpResolver;
import br.com.luancalasans.santoschamados.infrastructure.security.SecurityUtils;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.mensagem.dto.NovaMensagemForm;
import br.com.luancalasans.santoschamados.shared.Authorities;
import br.com.luancalasans.santoschamados.shared.StatusChamado;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
public class ChamadoController {

    private final ChamadoService chamadoService;
    private final TipoChamadoService tipoChamadoService;
    private final ClientIpResolver clientIpResolver;

    public ChamadoController(
            ChamadoService chamadoService,
            TipoChamadoService tipoChamadoService,
            ClientIpResolver clientIpResolver
    ) {
        this.chamadoService = chamadoService;
        this.tipoChamadoService = tipoChamadoService;
        this.clientIpResolver = clientIpResolver;
    }

    @GetMapping("/chamados")
    public String listar(
            @RequestParam(required = false) StatusChamado status,
            @RequestParam(required = false) Long tipoId,
            @RequestParam(required = false) String bairro,
            @RequestParam(required = false) String cidade,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "0") int page,
            Model model
    ) {
        UsuarioAutenticado user = SecurityUtils.requireUser();
        var pageable = PageRequest.of(page, 20, Sort.by(Sort.Direction.DESC, "abertoEm"));
        var resultado = chamadoService.listar(user, status, tipoId, bairro, cidade, desde, ate, pageable);
        model.addAttribute("page", resultado);
        model.addAttribute("status", status);
        model.addAttribute("tipoId", tipoId);
        model.addAttribute("bairro", bairro);
        model.addAttribute("cidade", cidade);
        model.addAttribute("desde", desde);
        model.addAttribute("ate", ate);
        model.addAttribute("tipos", tipoChamadoService.listarAtivos());
        model.addAttribute("statusList", StatusChamado.values());
        model.addAttribute("user", user);
        return "chamado/lista";
    }

    @GetMapping("/chamados/novo")
    public String novo(Model model) {
        SecurityUtils.requireAuthority(Authorities.CHAMADO_CRIAR);
        AberturaChamadoForm form = new AberturaChamadoForm();
        form.setCidade("Santos");
        form.setUf("SP");
        model.addAttribute("form", form);
        model.addAttribute("tipos", tipoChamadoService.listarAtivos());
        return "chamado/novo";
    }

    @PostMapping("/chamados")
    public String abrir(
            @Valid @ModelAttribute("form") AberturaChamadoForm form,
            BindingResult bindingResult,
            HttpServletRequest request,
            Model model,
            RedirectAttributes ra
    ) {
        UsuarioAutenticado user = SecurityUtils.requireUser();
        if (bindingResult.hasErrors()) {
            model.addAttribute("tipos", tipoChamadoService.listarAtivos());
            model.addAttribute("erro", "Verifique os campos obrigatórios.");
            return "chamado/novo";
        }
        Chamado chamado = chamadoService.abrir(form, user, clientIpResolver.resolve(request));
        ra.addFlashAttribute("sucesso", "Chamado aberto com protocolo " + chamado.getProtocolo());
        return "redirect:/chamados/" + chamado.getId();
    }

    @GetMapping("/chamados/{id}")
    public String detalhe(@PathVariable Long id, Model model) {
        UsuarioAutenticado user = SecurityUtils.requireUser();
        ChamadoDetalheDTO chamado = chamadoService.obterDetalhe(id, user);

        model.addAttribute("chamado", chamado);
        model.addAttribute("mensagemForm", new NovaMensagemForm());
        model.addAttribute("statusForm", new AlterarStatusForm());
        model.addAttribute("user", user);
        model.addAttribute("podeAlterarStatus", user.possuiAuthority(Authorities.CHAMADO_ALTERAR_STATUS));
        model.addAttribute("podeBaixarAnexo", user.possuiAuthority(Authorities.ANEXO_BAIXAR));
        model.addAttribute("podeMensagemInterna", user.possuiAuthority(Authorities.MENSAGEM_INTERNA));
        model.addAttribute("podePublicar", user.possuiAuthority(Authorities.MENSAGEM_PUBLICA)
                && !"PREFEITURA".equals(user.getPerfilCodigo()));
        return "chamado/detalhe";
    }

    @PostMapping("/chamados/{id}/status")
    public String alterarStatus(
            @PathVariable Long id,
            @Valid @ModelAttribute AlterarStatusForm form,
            HttpServletRequest request,
            RedirectAttributes ra
    ) {
        UsuarioAutenticado user = SecurityUtils.requireUser();
        chamadoService.alterarStatus(id, form, user, clientIpResolver.resolve(request));
        ra.addFlashAttribute("sucesso", "Status atualizado.");
        return "redirect:/chamados/" + id;
    }
}
