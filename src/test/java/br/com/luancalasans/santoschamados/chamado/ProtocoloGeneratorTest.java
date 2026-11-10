package br.com.luancalasans.santoschamados.chamado;

import br.com.luancalasans.santoschamados.chamado.repository.ChamadoRepository;
import br.com.luancalasans.santoschamados.chamado.service.ProtocoloGenerator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Year;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProtocoloGeneratorTest {

    @Mock ChamadoRepository chamadoRepository;
    @InjectMocks ProtocoloGenerator protocoloGenerator;

    @Test
    void formatoCorretoEUnicoEntreGeracoes() {
        when(chamadoRepository.existsByProtocolo(anyString())).thenReturn(false);
        int ano = Year.now(ZoneOffset.UTC).getValue();
        Set<String> gerados = new HashSet<>();
        for (int i = 0; i < 50; i++) {
            String protocolo = protocoloGenerator.gerarUnico();
            assertThat(protocolo).matches("^SAN-" + ano + "-[A-Z0-9]{8}$");
            String sufixo = protocolo.substring(protocolo.lastIndexOf('-') + 1);
            assertThat(sufixo).doesNotContain("I", "O", "0", "1");
            gerados.add(protocolo);
        }
        assertThat(gerados).hasSize(50);
    }
}
