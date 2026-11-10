package br.com.luancalasans.santoschamados.infrastructure.storage;

import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Component
public class LocalFileStorage implements FileStorage {

    private final Path root;

    public LocalFileStorage(AppProperties appProperties) {
        this.root = Path.of(appProperties.getAttachment().getStoragePath()).toAbsolutePath().normalize();
    }

    @PostConstruct
    void init() throws IOException {
        Files.createDirectories(root);
    }

    @Override
    public StoredFile store(String relativePath, byte[] content) {
        Path target = resolveSafe(relativePath);
        try {
            Files.createDirectories(target.getParent());
            Files.write(target, content, StandardOpenOption.CREATE_NEW);
            return new StoredFile(relativePath.replace('\\', '/'), content.length);
        } catch (IOException ex) {
            throw new BusinessException("Falha ao armazenar o arquivo.");
        }
    }

    @Override
    public InputStream open(String relativePath) {
        try {
            return Files.newInputStream(resolveSafe(relativePath), StandardOpenOption.READ);
        } catch (IOException ex) {
            throw new BusinessException("Arquivo não encontrado.");
        }
    }

    @Override
    public void delete(String relativePath) {
        try {
            Files.deleteIfExists(resolveSafe(relativePath));
        } catch (IOException ex) {
            throw new BusinessException("Falha ao remover arquivo temporário.");
        }
    }

    @Override
    public Path resolve(String relativePath) {
        return resolveSafe(relativePath);
    }

    private Path resolveSafe(String relativePath) {
        if (relativePath == null || relativePath.isBlank() || relativePath.contains("..")) {
            throw new BusinessException("Caminho de arquivo inválido.");
        }
        Path resolved = root.resolve(relativePath).normalize();
        if (!resolved.startsWith(root)) {
            throw new BusinessException("Caminho de arquivo inválido.");
        }
        return resolved;
    }
}
