package br.com.luancalasans.santoschamados.anexo.controller;

import br.com.luancalasans.santoschamados.anexo.service.AnexoService;
import br.com.luancalasans.santoschamados.infrastructure.security.ClientIpResolver;
import br.com.luancalasans.santoschamados.infrastructure.security.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AnexoController {

    private final AnexoService anexoService;
    private final ClientIpResolver clientIpResolver;

    public AnexoController(AnexoService anexoService, ClientIpResolver clientIpResolver) {
        this.anexoService = anexoService;
        this.clientIpResolver = clientIpResolver;
    }

    @GetMapping("/anexos/{id}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id, HttpServletRequest request) {
        return anexoService.download(id, SecurityUtils.requireUser(), clientIpResolver.resolve(request));
    }
}
