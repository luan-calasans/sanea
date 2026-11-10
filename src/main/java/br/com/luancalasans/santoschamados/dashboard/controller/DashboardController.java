package br.com.luancalasans.santoschamados.dashboard.controller;

import br.com.luancalasans.santoschamados.dashboard.service.DashboardService;
import br.com.luancalasans.santoschamados.infrastructure.security.SecurityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("indicadores", dashboardService.carregar(SecurityUtils.requireUser()));
        return "dashboard/index";
    }
}
