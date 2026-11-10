package br.com.luancalasans.santoschamados.mensagem.service;

import br.com.luancalasans.santoschamados.chamado.entity.Chamado;
import br.com.luancalasans.santoschamados.chamado.repository.ChamadoRepository;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import br.com.luancalasans.santoschamados.infrastructure.exception.ResourceNotFoundException;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.mensagem.dto.MensagemItemDTO;
import br.com.luancalasans.santoschamados.mensagem.dto.NovaMensagemForm;
import br.com.luancalasans.santoschamados.mensagem.entity.MensagemChamado;
import br.com.luancalasans.santoschamados.mensagem.repository.MensagemChamadoRepository;
import br.com.luancalasans.santoschamados.shared.Authorities;
import br.com.luancalasans.santoschamados.shared.VisibilidadeMensagem;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import br.com.luancalasans.santoschamados.usuario.repository.UsuarioRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
public class MensagemService {

    private final MensagemChamadoRepository mensagemChamadoRepository;
    private final ChamadoRepository chamadoRepository;
    private final UsuarioRepository usuarioRepository;

    public MensagemService(
            MensagemChamadoRepository mensagemChamadoRepository,
            ChamadoRepository chamadoRepository,
            UsuarioRepository usuarioRepository
    ) {
        this.mensagemChamadoRepository = mensagemChamadoRepository;
        this.chamadoRepository = chamadoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional(readOnly = true)
    public List<MensagemChamado> listar(Long chamadoId, UsuarioAutenticado user, Instant desde) {
        Chamado chamado = chamadoRepository.findDetailedById(chamadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado"));
        autorizarLeitura(chamado, user);
        boolean incluirInternas = user.possuiAuthority(Authorities.MENSAGEM_INTERNA);
        if (desde == null) {
            return incluirInternas
                    ? mensagemChamadoRepository.listarTodasPorChamado(chamadoId)
                    : mensagemChamadoRepository.listarPublicasPorChamado(chamadoId);
        }
        return incluirInternas
                ? mensagemChamadoRepository.listarTodasPorChamadoDesde(chamadoId, desde)
                : mensagemChamadoRepository.listarPublicasPorChamadoDesde(chamadoId, desde);
    }

    @Transactional(readOnly = true)
    public List<MensagemItemDTO> listarDto(Long chamadoId, UsuarioAutenticado user, Instant desde) {
        return listar(chamadoId, user, desde).stream().map(MensagemItemDTO::from).toList();
    }

    @Transactional
    public MensagemChamado publicar(Long chamadoId, NovaMensagemForm form, UsuarioAutenticado user) {
        Chamado chamado = chamadoRepository.findDetailedById(chamadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado"));
        autorizarLeitura(chamado, user);

        VisibilidadeMensagem visibilidade = form.getVisibilidade();
        if (visibilidade == VisibilidadeMensagem.INTERNA) {
            if (!user.possuiAuthority(Authorities.MENSAGEM_INTERNA)) {
                throw new AccessDeniedException("Sem permissão para mensagens internas");
            }
        } else {
            if (!user.possuiAuthority(Authorities.MENSAGEM_PUBLICA)) {
                throw new AccessDeniedException("Sem permissão para mensagens públicas");
            }
            if ("PREFEITURA".equals(user.getPerfilCodigo())) {
                throw new AccessDeniedException("Perfil Prefeitura não publica mensagens");
            }
        }

        Usuario autor = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Usuário inválido"));

        if (form.getCorpo() == null || form.getCorpo().isBlank()) {
            throw new BusinessException("Informe o conteúdo da mensagem.");
        }

        MensagemChamado mensagem = new MensagemChamado();
        mensagem.setChamado(chamado);
        mensagem.setAutor(autor);
        mensagem.setCorpo(form.getCorpo().trim());
        mensagem.setVisibilidade(visibilidade);
        mensagem.setCriadoEm(Instant.now());
        return mensagemChamadoRepository.save(mensagem);
    }

    private void autorizarLeitura(Chamado chamado, UsuarioAutenticado user) {
        if (user.possuiAuthority(Authorities.CHAMADO_VISUALIZAR_TODOS)) {
            return;
        }
        if (user.possuiAuthority(Authorities.CHAMADO_VISUALIZAR_PROPRIO)
                && chamado.getUsuario().getId().equals(user.getId())) {
            return;
        }
        throw new AccessDeniedException("Sem permissão para acessar mensagens deste chamado");
    }
}
