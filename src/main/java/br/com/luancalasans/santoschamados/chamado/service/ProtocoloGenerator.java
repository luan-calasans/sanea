package br.com.luancalasans.santoschamados.chamado.service;

import br.com.luancalasans.santoschamados.chamado.repository.ChamadoRepository;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.time.Year;
import java.time.ZoneOffset;

@Component
public class ProtocoloGenerator {

    private static final char[] ALFABETO = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789".toCharArray();
    private static final int SUFIXO_TAMANHO = 8;
    private static final int MAX_TENTATIVAS = 20;

    private final SecureRandom secureRandom = new SecureRandom();
    private final ChamadoRepository chamadoRepository;

    public ProtocoloGenerator(ChamadoRepository chamadoRepository) {
        this.chamadoRepository = chamadoRepository;
    }

    public String gerarUnico() {
        int ano = Year.now(ZoneOffset.UTC).getValue();
        for (int i = 0; i < MAX_TENTATIVAS; i++) {
            String protocolo = "SAN-" + ano + "-" + sufixo();
            if (!chamadoRepository.existsByProtocolo(protocolo)) {
                return protocolo;
            }
        }
        throw new BusinessException("Não foi possível gerar um protocolo único. Tente novamente.");
    }

    public String gerarFormato() {
        int ano = Year.now(ZoneOffset.UTC).getValue();
        return "SAN-" + ano + "-" + sufixo();
    }

    private String sufixo() {
        StringBuilder sb = new StringBuilder(SUFIXO_TAMANHO);
        for (int i = 0; i < SUFIXO_TAMANHO; i++) {
            sb.append(ALFABETO[secureRandom.nextInt(ALFABETO.length)]);
        }
        return sb.toString();
    }
}
