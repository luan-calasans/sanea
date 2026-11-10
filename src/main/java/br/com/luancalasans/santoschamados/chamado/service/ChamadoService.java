package br.com.luancalasans.santoschamados.chamado.service;

import br.com.luancalasans.santoschamados.anexo.entity.Anexo;
import br.com.luancalasans.santoschamados.anexo.service.AnexoService;
import br.com.luancalasans.santoschamados.auditoria.service.AuditoriaService;
import br.com.luancalasans.santoschamados.catalogo.entity.TipoChamado;
import br.com.luancalasans.santoschamados.catalogo.repository.TipoChamadoRepository;
import br.com.luancalasans.santoschamados.chamado.dto.AberturaChamadoForm;
import br.com.luancalasans.santoschamados.chamado.dto.AlterarStatusForm;
import br.com.luancalasans.santoschamados.chamado.dto.AnexoItemDTO;
import br.com.luancalasans.santoschamados.chamado.dto.ChamadoDetalheDTO;
import br.com.luancalasans.santoschamados.chamado.dto.ChamadoListaItemDTO;
import br.com.luancalasans.santoschamados.chamado.dto.ChamadoPublicoDTO;
import br.com.luancalasans.santoschamados.chamado.dto.StatusHistoricoItemDTO;
import br.com.luancalasans.santoschamados.chamado.entity.Chamado;
import br.com.luancalasans.santoschamados.chamado.entity.StatusHistorico;
import br.com.luancalasans.santoschamados.chamado.repository.ChamadoRepository;
import br.com.luancalasans.santoschamados.chamado.repository.StatusHistoricoRepository;
import br.com.luancalasans.santoschamados.endereco.cep.orchestration.CepOrchestrator;
import br.com.luancalasans.santoschamados.infrastructure.config.AppProperties;
import br.com.luancalasans.santoschamados.infrastructure.exception.BusinessException;
import br.com.luancalasans.santoschamados.infrastructure.exception.ResourceNotFoundException;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.mensagem.dto.MensagemItemDTO;
import br.com.luancalasans.santoschamados.mensagem.entity.MensagemChamado;
import br.com.luancalasans.santoschamados.mensagem.repository.MensagemChamadoRepository;
import br.com.luancalasans.santoschamados.shared.Authorities;
import br.com.luancalasans.santoschamados.shared.HtmlSanitizer;
import br.com.luancalasans.santoschamados.shared.StatusChamado;
import br.com.luancalasans.santoschamados.shared.TipoEventoAuditoria;
import br.com.luancalasans.santoschamados.shared.VisibilidadeMensagem;
import br.com.luancalasans.santoschamados.usuario.entity.Usuario;
import br.com.luancalasans.santoschamados.usuario.repository.UsuarioRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class ChamadoService {

    private final ChamadoRepository chamadoRepository;
    private final StatusHistoricoRepository statusHistoricoRepository;
    private final TipoChamadoRepository tipoChamadoRepository;
    private final UsuarioRepository usuarioRepository;
    private final ProtocoloGenerator protocoloGenerator;
    private final ChamadoPersistenceService chamadoPersistenceService;
    private final CepOrchestrator cepOrchestrator;
    private final AnexoService anexoService;
    private final MensagemChamadoRepository mensagemChamadoRepository;
    private final AppProperties appProperties;
    private final AuditoriaService auditoriaService;

    private static final DateTimeFormatter DATE_TIME_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm").withZone(ZoneId.of("America/Sao_Paulo"));
    private static final DateTimeFormatter DATE_BR =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public ChamadoService(
            ChamadoRepository chamadoRepository,
            StatusHistoricoRepository statusHistoricoRepository,
            TipoChamadoRepository tipoChamadoRepository,
            UsuarioRepository usuarioRepository,
            ProtocoloGenerator protocoloGenerator,
            ChamadoPersistenceService chamadoPersistenceService,
            CepOrchestrator cepOrchestrator,
            AnexoService anexoService,
            MensagemChamadoRepository mensagemChamadoRepository,
            AppProperties appProperties,
            AuditoriaService auditoriaService
    ) {
        this.chamadoRepository = chamadoRepository;
        this.statusHistoricoRepository = statusHistoricoRepository;
        this.tipoChamadoRepository = tipoChamadoRepository;
        this.usuarioRepository = usuarioRepository;
        this.protocoloGenerator = protocoloGenerator;
        this.chamadoPersistenceService = chamadoPersistenceService;
        this.cepOrchestrator = cepOrchestrator;
        this.anexoService = anexoService;
        this.mensagemChamadoRepository = mensagemChamadoRepository;
        this.appProperties = appProperties;
        this.auditoriaService = auditoriaService;
    }

    @Transactional
    public Chamado abrir(AberturaChamadoForm form, UsuarioAutenticado user, String ip) {
        if (!user.possuiAuthority(Authorities.CHAMADO_CRIAR)) {
            throw new AccessDeniedException("Sem permissão para abrir chamados");
        }

        Usuario autor = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Usário inválido"));

        TipoChamado tipo = tipoChamadoRepository.findById(form.getTipoChamadoId())
                .orElseThrow(() -> new BusinessException("Tipo de chamado inválido."));
        if (!tipo.isAtivo()) {
            throw new BusinessException("O tipo de chamado selecionado não está disponível.");
        }
        if ("OUTROS".equalsIgnoreCase(tipo.getCodigo())) {
            if (form.getTipoOutro() == null || form.getTipoOutro().isBlank()) {
                throw new BusinessException("Descreva o tipo de ocorrência quando selecionar Outros.");
            }
            if (form.getTipoOutro().trim().length() > 200) {
                throw new BusinessException("O detalhe do tipo Outros deve ter no máximo 200 caracteres.");
            }
        }

        String cep = cepOrchestrator.normalizarCep(form.getCep());
        if (cep.length() != 8) {
            throw new BusinessException("Informe um CEP com 8 dígitos.");
        }
        if (form.getDataOcorrencia() != null && form.getDataOcorrencia().isAfter(LocalDate.now(ZoneId.of("America/Sao_Paulo")))) {
            throw new BusinessException("A data da ocorrência não pode ser futura.");
        }

        validarEnderecoMunicipal(form);

        Chamado chamado = criarComProtocoloUnico(form, autor, tipo, cep);
        List<MultipartFile> fotos = form.getFotos();
        anexoService.salvarAnexosAbertura(chamado, fotos, autor);

        auditoriaService.registrar(
                TipoEventoAuditoria.CHAMADO_CRIADO,
                autor.getId(),
                "CHAMADO",
                chamado.getProtocolo(),
                "SUCESSO",
                ip,
                null
        );
        return chamado;
    }

    private Chamado criarComProtocoloUnico(AberturaChamadoForm form, Usuario autor, TipoChamado tipo, String cep) {
        int tentativas = 0;
        while (tentativas < 10) {
            tentativas++;
            try {
                return chamadoPersistenceService.persistirNovo(
                        form, autor, tipo, cep, protocoloGenerator.gerarUnico()
                );
            } catch (DataIntegrityViolationException ex) {
            }
        }
        throw new BusinessException("Não foi possível gerar protocolo único. Tente novamente.");
    }

    private void validarEnderecoMunicipal(AberturaChamadoForm form) {
        String cidadeEsperada = appProperties.getAtendimento().getCidade();
        String ufEsperada = appProperties.getAtendimento().getUf();
        if (!cidadeEsperada.equalsIgnoreCase(form.getCidade().trim())
                || !ufEsperada.equalsIgnoreCase(form.getUf().trim())) {
            throw new BusinessException("Na fase atual, somente ocorrências em "
                    + cidadeEsperada + "/" + ufEsperada + " são aceitas.");
        }
        if (form.getLogradouro() == null || form.getLogradouro().isBlank()
                || form.getBairro() == null || form.getBairro().isBlank()) {
            throw new BusinessException("Preencha logradouro e bairro.");
        }
    }

    @Transactional(readOnly = true)
    public Chamado obterParaUsuario(Long id, UsuarioAutenticado user) {
        Chamado chamado = chamadoRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado"));
        autorizarVisualizacao(chamado, user);
        chamado.getTipoExibicao();
        if (chamado.getUsuario() != null) {
            chamado.getUsuario().getId();
        }
        return chamado;
    }

    @Transactional(readOnly = true)
    public ChamadoDetalheDTO obterDetalhe(Long id, UsuarioAutenticado user) {
        Chamado chamado = chamadoRepository.findDetailedById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado"));
        autorizarVisualizacao(chamado, user);

        List<StatusHistoricoItemDTO> historicos = statusHistoricoRepository
                .findByChamadoIdOrderByCriadoEmAsc(id).stream()
                .map(StatusHistoricoItemDTO::from)
                .toList();
        List<AnexoItemDTO> anexos = anexoService.listarPorChamado(id).stream()
                .map(AnexoItemDTO::from)
                .toList();
        boolean incluirInternas = user.possuiAuthority(Authorities.MENSAGEM_INTERNA);
        List<MensagemItemDTO> mensagens = (incluirInternas
                ? mensagemChamadoRepository.listarTodasPorChamado(id)
                : mensagemChamadoRepository.listarPublicasPorChamado(id)
        ).stream().map(MensagemItemDTO::from).toList();

        List<ChamadoDetalheDTO.StatusOpcaoDTO> proximos = chamado.getStatus().proximosPermitidos().stream()
                .map(ChamadoDetalheDTO.StatusOpcaoDTO::from)
                .toList();
        String ultimaMensagemEm = mensagens.isEmpty()
                ? null
                : mensagens.get(mensagens.size() - 1).criadoEmFormatado();

        return new ChamadoDetalheDTO(
                chamado.getId(),
                chamado.getProtocolo(),
                chamado.getTipoExibicao(),
                chamado.getStatus().name(),
                chamado.getStatus().getRotulo(),
                "badge-" + chamado.getStatus().name(),
                DATE_TIME_BR.format(chamado.getAbertoEm()),
                chamado.getDataOcorrencia() == null ? null : DATE_BR.format(chamado.getDataOcorrencia()),
                chamado.getLogradouro(),
                chamado.getNumero(),
                chamado.getComplemento(),
                chamado.getBairro(),
                chamado.getCidade(),
                chamado.getUf(),
                chamado.getCep(),
                chamado.isEnderecoVerificado(),
                chamado.isEnderecoVerificado() ? " (verificado)" : " (preenchimento manual)",
                HtmlSanitizer.sanitize(chamado.getDescricao()),
                chamado.getVersao(),
                historicos,
                anexos,
                mensagens,
                ultimaMensagemEm,
                proximos,
                !proximos.isEmpty()
        );
    }

    @Transactional(readOnly = true)
    public Page<ChamadoListaItemDTO> listar(
            UsuarioAutenticado user,
            StatusChamado status,
            Long tipoId,
            String bairro,
            String cidade,
            LocalDate desde,
            LocalDate ate,
            Pageable pageable
    ) {
        Long usuarioFiltro = null;
        if (user.possuiAuthority(Authorities.CHAMADO_VISUALIZAR_TODOS)) {
            usuarioFiltro = null;
        } else if (user.possuiAuthority(Authorities.CHAMADO_VISUALIZAR_PROPRIO)) {
            usuarioFiltro = user.getId();
        } else {
            throw new AccessDeniedException("Sem permissão para listar chamados");
        }

        Instant desdeInst = desde == null ? null : desde.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant ateInst = ate == null ? null : ate.atTime(LocalTime.MAX).toInstant(ZoneOffset.UTC);

        return chamadoRepository.findAll(
                ChamadoRepository.comFiltros(
                        status,
                        tipoId,
                        blankToNull(bairro),
                        blankToNull(cidade),
                        desdeInst,
                        ateInst,
                        usuarioFiltro
                ),
                pageable
        ).map(ChamadoListaItemDTO::from);
    }

    @Transactional(readOnly = true)
    public Page<ChamadoListaItemDTO> listarMeus(UsuarioAutenticado user, Pageable pageable) {
        if (!user.possuiAuthority(Authorities.CHAMADO_VISUALIZAR_PROPRIO)
                && !user.possuiAuthority(Authorities.CHAMADO_VISUALIZAR_TODOS)
                && !user.possuiAuthority(Authorities.CHAMADO_CRIAR)) {
            throw new AccessDeniedException("Sem permissão para listar chamados");
        }
        return chamadoRepository.findByUsuarioId(user.getId(), pageable).map(ChamadoListaItemDTO::from);
    }

    @Transactional
    public void remover(Long chamadoId, UsuarioAutenticado user, String ip) {
        if (!user.possuiAuthority(Authorities.CHAMADO_REMOVER)) {
            throw new AccessDeniedException("Sem permissão para remover chamados");
        }
        Chamado chamado = chamadoRepository.findById(chamadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado"));
        String protocolo = chamado.getProtocolo();

        anexoService.removerPorChamado(chamadoId);
        mensagemChamadoRepository.deleteByChamadoId(chamadoId);
        statusHistoricoRepository.deleteByChamadoId(chamadoId);
        chamadoRepository.delete(chamado);

        auditoriaService.registrar(
                TipoEventoAuditoria.CHAMADO_REMOVIDO,
                user.getId(),
                "CHAMADO",
                protocolo,
                "SUCESSO",
                ip,
                "Chamado removido pelo administrador"
        );
    }

    @Transactional
    public Chamado alterarStatus(Long chamadoId, AlterarStatusForm form, UsuarioAutenticado user, String ip) {
        if (!user.possuiAuthority(Authorities.CHAMADO_ALTERAR_STATUS)) {
            throw new AccessDeniedException("Sem permissão para alterar status");
        }
        Chamado chamado = chamadoRepository.findDetailedById(chamadoId)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado"));

        if (form.getVersao() != null && !form.getVersao().equals(chamado.getVersao())) {
            throw new ObjectOptimisticLockingFailureException(Chamado.class.getName(), chamadoId);
        }

        StatusChamado atual = chamado.getStatus();
        StatusChamado novo = form.getNovoStatus();
        if (!atual.podeTransicionarPara(novo)) {
            throw new BusinessException("Transição de status não permitida: " + atual + " -> " + novo);
        }

        Usuario autor = usuarioRepository.findById(user.getId())
                .orElseThrow(() -> new AccessDeniedException("Usuário inválido"));

        StatusHistorico historico = new StatusHistorico();
        historico.setChamado(chamado);
        historico.setStatusAnterior(atual);
        historico.setStatusNovo(novo);
        historico.setAutor(autor);
        historico.setObservacao(blankToNull(form.getObservacao()));
        historico.setCriadoEm(Instant.now());

        chamado.setStatus(novo);
        chamado.setAtualizadoEm(Instant.now());
        statusHistoricoRepository.save(historico);
        Chamado salvo = chamadoRepository.save(chamado);

        auditoriaService.registrar(
                TipoEventoAuditoria.STATUS_ALTERADO,
                user.getId(),
                "CHAMADO",
                chamado.getProtocolo(),
                "SUCESSO",
                ip,
                atual + "->" + novo
        );
        return salvo;
    }

    @Transactional(readOnly = true)
    public ChamadoPublicoDTO consultaPublica(String protocoloInformado) {
        String protocolo = protocoloInformado == null ? "" : protocoloInformado.trim().toUpperCase(Locale.ROOT);
        if (!protocolo.matches("^SAN-\\d{4}-[A-Z0-9]{8}$")) {
            return null;
        }

        return chamadoRepository.findByProtocoloWithTipo(protocolo)
                .map(this::toPublico)
                .orElse(null);
    }

    private ChamadoPublicoDTO toPublico(Chamado chamado) {
        List<StatusHistorico> historicos = statusHistoricoRepository.findByChamadoIdOrderByCriadoEmAsc(chamado.getId());
        List<MensagemChamado> mensagens = mensagemChamadoRepository
                .findByChamadoIdAndVisibilidadeOrderByCriadoEmAsc(chamado.getId(), VisibilidadeMensagem.PUBLICA);

        return new ChamadoPublicoDTO(
                chamado.getProtocolo(),
                chamado.getTipoExibicao(),
                chamado.getStatus(),
                chamado.getStatus().getRotulo(),
                chamado.getAbertoEm(),
                historicos.stream()
                        .map(h -> new ChamadoPublicoDTO.StatusHistoricoPublicoDTO(
                                h.getStatusAnterior(),
                                h.getStatusNovo(),
                                h.getCriadoEm(),
                                h.getObservacao()
                        ))
                        .toList(),
                mensagens.stream()
                        .map(m -> new ChamadoPublicoDTO.MensagemPublicaDTO(
                                m.getAutor().getPerfil().getNome(),
                                m.getCriadoEm(),
                                m.getCorpo()
                        ))
                        .toList(),
                anexoService.contar(chamado.getId())
        );
    }

    private void autorizarVisualizacao(Chamado chamado, UsuarioAutenticado user) {
        if (user.possuiAuthority(Authorities.CHAMADO_VISUALIZAR_TODOS)) {
            return;
        }
        if (user.possuiAuthority(Authorities.CHAMADO_VISUALIZAR_PROPRIO)
                && chamado.getUsuario().getId().equals(user.getId())) {
            return;
        }
        throw new AccessDeniedException("Sem permissão para visualizar este chamado");
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
