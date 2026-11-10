package br.com.luancalasans.santoschamados.endereco.cep;

import br.com.luancalasans.santoschamados.endereco.cep.dto.ResultadoCep;
import br.com.luancalasans.santoschamados.endereco.cep.orchestration.CepOrchestrator;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/cep")
public class CepController {

    private final CepOrchestrator cepOrchestrator;

    public CepController(CepOrchestrator cepOrchestrator) {
        this.cepOrchestrator = cepOrchestrator;
    }

    @GetMapping("/{cep}")
    public ResponseEntity<?> consultar(@PathVariable String cep) {
        ResultadoCep resultado = cepOrchestrator.consultar(cep);
        if (resultado instanceof ResultadoCep.Encontrado encontrado) {
            return ResponseEntity.ok(Map.of(
                    "status", "ENCONTRADO",
                    "provedor", encontrado.provedor(),
                    "endereco", encontrado.endereco()
            ));
        }
        if (resultado instanceof ResultadoCep.NaoEncontrado) {
            return ResponseEntity.ok(Map.of(
                    "status", "NAO_ENCONTRADO",
                    "manual", true
            ));
        }
        return ResponseEntity.ok(Map.of(
                "status", "INDISPONIVEL",
                "manual", true
        ));
    }
}
