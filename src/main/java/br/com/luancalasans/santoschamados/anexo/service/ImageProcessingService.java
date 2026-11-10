package br.com.luancalasans.santoschamados.anexo.service;

import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Set;

@Service
public class ImageProcessingService {

    private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/jpg");

    private final AppProperties appProperties;

    public ImageProcessingService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public ProcessedImage process(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("Arquivo de imagem inválido.");
        }
        if (file.getSize() > appProperties.getAttachment().maxFileSizeBytes()) {
            throw new BusinessException("Cada imagem deve ter no máximo "
                    + appProperties.getAttachment().getMaxFileSizeMb() + " MB.");
        }

        byte[] original;
        try {
            original = file.getBytes();
        } catch (IOException ex) {
            throw new BusinessException("Não foi possível ler a imagem enviada.");
        }

        String detected = detectContentType(original);
        if (!ALLOWED.contains(detected)) {
            throw new BusinessException("Apenas imagens JPEG ou PNG são aceitas.");
        }

        BufferedImage image;
        try {
            image = ImageIO.read(new ByteArrayInputStream(original));
        } catch (IOException ex) {
            throw new BusinessException("A imagem enviada é inválida ou corrompida.");
        }
        if (image == null) {
            throw new BusinessException("A imagem enviada é inválida ou corrompida.");
        }
        if (image.getWidth() > 8000 || image.getHeight() > 8000) {
            throw new BusinessException("Dimensões da imagem excedem o limite permitido.");
        }

        try {
            int maxDim = appProperties.getAttachment().getMaxDimension();
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            Thumbnails.Builder<BufferedImage> builder = Thumbnails.of(image)
                    .outputFormat("jpg")
                    .outputQuality(0.85);
            if (image.getWidth() > maxDim || image.getHeight() > maxDim) {
                builder.size(maxDim, maxDim);
            } else {
                builder.scale(1.0);
            }
            builder.toOutputStream(output);

            BufferedImage processed = ImageIO.read(new ByteArrayInputStream(output.toByteArray()));
            return new ProcessedImage(
                    output.toByteArray(),
                    "image/jpeg",
                    processed != null ? processed.getWidth() : null,
                    processed != null ? processed.getHeight() : null,
                    sanitizeOriginalName(file.getOriginalFilename())
            );
        } catch (IOException ex) {
            throw new BusinessException("Falha ao processar a imagem.");
        }
    }

    private String detectContentType(byte[] data) {
        if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (data.length >= 8
                && (data[0] & 0xFF) == 0x89
                && data[1] == 0x50
                && data[2] == 0x4E
                && data[3] == 0x47) {
            return "image/png";
        }
        return "application/octet-stream";
    }

    private String sanitizeOriginalName(String name) {
        if (name == null || name.isBlank()) {
            return "imagem.jpg";
        }
        String cleaned = name.replaceAll("[\\\\/]+", "_");
        if (cleaned.length() > 180) {
            cleaned = cleaned.substring(cleaned.length() - 180);
        }
        return cleaned.toLowerCase(Locale.ROOT);
    }

    public record ProcessedImage(
            byte[] content,
            String contentType,
            Integer width,
            Integer height,
            String originalName
    ) {
    }
}
