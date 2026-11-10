package br.com.luancalasans.santoschamados.infrastructure.web;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.time.ZoneId;

@ControllerAdvice
public class TimeFormattingAdvice {

    public static final ZoneId ZONE_BRASILIA = ZoneId.of("America/Sao_Paulo");

    @ModelAttribute("zoneId")
    public ZoneId zoneId() {
        return ZONE_BRASILIA;
    }
}
