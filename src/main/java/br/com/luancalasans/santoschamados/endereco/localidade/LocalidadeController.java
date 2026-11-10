package br.com.luancalasans.santoschamados.endereco.localidade;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/localidades")
public class LocalidadeController {

    private final LocalidadeService localidadeService;

    public LocalidadeController(LocalidadeService localidadeService) {
        this.localidadeService = localidadeService;
    }

    @GetMapping("/ufs")
    public List<Map<String, String>> ufs() {
        return localidadeService.listarUfs().stream()
                .map(uf -> {
                    Map<String, String> item = new LinkedHashMap<>();
                    item.put("sigla", uf.getSigla());
                    item.put("nome", uf.getNome());
                    return item;
                })
                .toList();
    }

    @GetMapping("/ufs/{uf}/cidades")
    public ResponseEntity<List<String>> cidades(@PathVariable String uf) {
        return ResponseEntity.ok(localidadeService.listarCidades(uf));
    }
}
