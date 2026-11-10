package br.com.luancalasans.santoschamados.dashboard.service;

import br.com.luancalasans.santoschamados.chamado.entity.Chamado;
import br.com.luancalasans.santoschamados.chamado.repository.ChamadoRepository;
import br.com.luancalasans.santoschamados.dashboard.dto.DashboardIndicadoresDTO;
import br.com.luancalasans.santoschamados.infrastructure.security.UsuarioAutenticado;
import br.com.luancalasans.santoschamados.shared.Authorities;
import br.com.luancalasans.santoschamados.shared.StatusChamado;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Service
public class DashboardService {

    private final ChamadoRepository chamadoRepository;
    private final DashboardCacheLoader cacheLoader;

    public DashboardService(ChamadoRepository chamadoRepository, DashboardCacheLoader cacheLoader) {
        this.chamadoRepository = chamadoRepository;
        this.cacheLoader = cacheLoader;
    }

    @Transactional(readOnly = true)
    public DashboardIndicadoresDTO carregar(UsuarioAutenticado user) {
        if (!user.possuiAuthority(Authorities.DASHBOARD_VISUALIZAR)) {
            throw new AccessDeniedException("Sem permissão para dashboard");
        }
        return cacheLoader.load();
    }

    @Service
    public static class DashboardCacheLoader {
        private final ChamadoRepository chamadoRepository;

        public DashboardCacheLoader(ChamadoRepository chamadoRepository) {
            this.chamadoRepository = chamadoRepository;
        }

        @Cacheable(cacheNames = "dashboardIndicadores", key = "'main'")
        @Transactional(readOnly = true)
        public DashboardIndicadoresDTO load() {
            Map<StatusChamado, Long> porStatus = new EnumMap<>(StatusChamado.class);
            for (StatusChamado status : StatusChamado.values()) {
                porStatus.put(status, 0L);
            }
            for (Object[] row : chamadoRepository.countGroupByStatus()) {
                StatusChamado status = (StatusChamado) row[0];
                Long count = (Long) row[1];
                porStatus.put(status, count);
            }

            long total = porStatus.values().stream().mapToLong(Long::longValue).sum();
            List<Chamado> recentes = chamadoRepository.findTop10ByOrderByAbertoEmDesc();

            return new DashboardIndicadoresDTO(
                    total,
                    porStatus.getOrDefault(StatusChamado.ABERTO, 0L),
                    porStatus.getOrDefault(StatusChamado.EM_ANALISE, 0L),
                    porStatus.getOrDefault(StatusChamado.EM_ATENDIMENTO, 0L),
                    porStatus.getOrDefault(StatusChamado.RESOLVIDO, 0L),
                    porStatus.getOrDefault(StatusChamado.DUPLICADO, 0L),
                    porStatus.getOrDefault(StatusChamado.CANCELADO, 0L),
                    porStatus,
                    recentes.stream()
                            .map(c -> {
                                var item = br.com.luancalasans.santoschamados.chamado.dto.ChamadoListaItemDTO.from(c);
                                return new DashboardIndicadoresDTO.ChamadoRecenteDTO(
                                        item.id(),
                                        item.protocolo(),
                                        item.tipoNome(),
                                        item.status(),
                                        item.statusRotulo(),
                                        c.getAbertoEm(),
                                        item.abertoEmFormatado(),
                                        item.bairro()
                                );
                            })
                            .toList()
            );
        }
    }
}
