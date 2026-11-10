package br.com.luancalasans.santoschamados.infrastructure.exception;

public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
