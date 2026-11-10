package br.com.luancalasans.santoschamados.anexo.service;

import br.com.luancalasans.santoschamados.anexo.entity.Anexo;
import br.com.luancalasans.santoschamados.anexo.repository.AnexoRepository;
import br.com.luancalasans.santoschamados.auditoria.service.AuditoriaService;
import br.com.luancalasans.santoschamados.chamado.entity.Chamado;
import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import br.com.luancalasans.santoschamados.infrastructure.exception.ResourceNotFoundException;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.infrastructure.storage.FileStorage;
import br.com.luancalasans.santoschamados.shared.Authorities;
import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import br.com.luancalasans.santoschamados.usuario.repository.UsuarioRepository;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.security.SecureRandom;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

@Service
public class AnexoService {

    private final AnexoRepository anexoRepository;
    private final FileStorage fileStorage;
    private final ImageProcessingService imageProcessingService;
    private final AppProperties appProperties;
    private final UsuarioRepository usuarioRepository;
    private final AuditoriaService auditoriaService;
    private final SecureRandom secureRandom = new SecureRandom();

    public AnexoService(
            AnexoRepository anexoRepository,
            FileStorage fileStorage,
            ImageProcessingService imageProcessingService,
            AppProperties appProperties,
            UsuarioRepository usuarioRepository,
            AuditoriaService auditoriaService
    ) {
        this.anexoRepository = anexoRepository;
        this.fileStorage = fileStorage;
        this.imageProcessingService = imageProcessingService;
        this.appProperties = appProperties;
        this.usuarioRepository = usuarioRepository;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public List<Anexo> salvarAnexosAbertura(Chamado chamado, List<MultipartFile> arquivos, Usuario autor) {
        List<MultipartFile> validos = arquivos == null ? List.of() : arquivos.stream()
                .filter(f -> f != null && !f.isEmpty())
                .toList();
        if (validos.size() > appProperties.getAttachment().getMaxFiles()) {
            throw new BusinessException("É permitido enviar no máximo "
                    + appProperties.getAttachment().getMaxFiles() + " fotografias.");
        }

        List<String> caminhosCriados = new ArrayList<>();
        List<Anexo> salvos = new ArrayList<>();
        try {
            for (MultipartFile arquivo : validos) {
                ImageProcessingService.ProcessedImage processed = imageProcessingService.process(arquivo);
                String relative = buildRelativePath();
                fileStorage.store(relative, processed.content());
                caminhosCriados.add(relative);

                Anexo anexo = new Anexo();
                anexo.setChamado(chamado);
                anexo.setNomeOriginal(processed.originalName());
                anexo.setNomeArmazenado(PathFileName(relative));
                anexo.setCaminhoRelativo(relative);
                anexo.setContentType(processed.contentType());
                anexo.setTamanhoBytes(processed.content().length);
                anexo.setLargura(processed.width());
                anexo.setAltura(processed.height());
                anexo.setEnviadoPor(autor);
                anexo.setCriadoEm(Instant.now());
                salvos.add(anexoRepository.save(anexo));
            }
            return salvos;
        } catch (RuntimeException ex) {
            for (String caminho : caminhosCriados) {
                try {
                    fileStorage.delete(caminho);
                } catch (Exception ignored) {
                }
            }
            throw ex;
        }
    }

    @Transactional(readOnly = true)
    public List<Anexo> listarPorChamado(Long chamadoId) {
        return anexoRepository.findByChamadoIdOrderByCriadoEmAsc(chamadoId);
    }

    @Transactional(readOnly = true)
    public long contar(Long chamadoId) {
        return anexoRepository.countByChamadoId(chamadoId);
    }

    @Transactional
    public ResponseEntity<InputStreamResource> download(Long anexoId, UsuarioAutenticado user, String ip) {
        if (!user.possuiAuthority(Authorities.ANEXO_BAIXAR)) {
            throw new BusinessException("Sem permissão para baixar anexos.");
        }
        Anexo anexo = anexoRepository.findDetailedById(anexoId)
                .orElseThrow(() -> new ResourceNotFoundException("Anexo não encontrado"));

        Long chamadoId = anexo.getChamado() != null ? anexo.getChamado().getId() : null;
        auditoriaService.registrar(
                TipoEventoAuditoria.ANEXO_DOWNLOAD,
                user.getId(),
                "ANEXO",
                String.valueOf(anexoId),
                "SUCESSO",
                ip,
                chamadoId == null ? null : "Chamado " + chamadoId
        );

        String contentType = anexo.getContentType();
        MediaType mediaType;
        try {
            mediaType = (contentType == null || contentType.isBlank())
                    ? MediaType.IMAGE_JPEG
                    : MediaType.parseMediaType(contentType);
        } catch (Exception ex) {
            mediaType = MediaType.IMAGE_JPEG;
        }

        String filename = anexo.getNomeOriginal() == null || anexo.getNomeOriginal().isBlank()
                ? "anexo-" + anexo.getId() + ".jpg"
                : anexo.getNomeOriginal().replace("\"", "");

        InputStreamResource resource = new InputStreamResource(fileStorage.open(anexo.getCaminhoRelativo()));
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(mediaType)
                .contentLength(anexo.getTamanhoBytes())
                .body(resource);
    }

    private String buildRelativePath() {
        YearMonth ym = YearMonth.now(ZoneOffset.UTC);
        byte[] random = new byte[16];
        secureRandom.nextBytes(random);
        String name = HexFormat.of().formatHex(random) + ".jpg";
        return ym.getYear() + "/" + String.format("%02d", ym.getMonthValue()) + "/" + name;
    }

    private String PathFileName(String relative) {
        int idx = relative.lastIndexOf('/');
        return idx >= 0 ? relative.substring(idx + 1) : relative;
    }
}
