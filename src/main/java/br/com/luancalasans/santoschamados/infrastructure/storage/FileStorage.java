package br.com.luancalasans.santoschamados.infrastructure.storage;

import java.io.InputStream;
import java.nio.file.Path;

public interface FileStorage {

    StoredFile store(String relativePath, byte[] content);

    InputStream open(String relativePath);

    void delete(String relativePath);

    Path resolve(String relativePath);

    record StoredFile(String relativePath, long sizeBytes) {
    }
}
