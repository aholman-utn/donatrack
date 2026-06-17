package com.tp.incentivos.services;

import com.tp.incentivos.domain.*;
import com.tp.incentivos.dtos.*;
import com.tp.incentivos.repositories.IncentivosRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.List;

@Service
public class IncentivosService {

    private final IncentivosRepository repository;
    private final ServicioEvaluadorMisiones evaluadorMisiones;
    private final ServicioRanking servicioRanking;

    public IncentivosService(
            IncentivosRepository repository,
            ServicioEvaluadorMisiones evaluadorMisiones,
            ServicioRanking servicioRanking) {
        this.repository = repository;
        this.evaluadorMisiones = evaluadorMisiones;
        this.servicioRanking = servicioRanking;
    }

    /**
     * Procesa una nueva entrega de donación:
     * 1. Obtiene o crea el perfil del donante.
     * 2. Registra la entrega básica (total y entidad).
     * 3. Registra la categoría del bien donado.
     * 4. Actualiza el historial mensual.
     * 5. Construye el InfoDonacion y delega la evaluación de misiones.
     * 6. Persiste el perfil.
     */
    public void procesarNuevaEntrega(EntregaDonacionDTO dto) {
        Integer donanteId = dto.getDonanteId();

        Perfil perfil = repository.findByDonanteId(donanteId);
        if (perfil == null) {
            perfil = new Perfil(donanteId);
        }

        perfil.registrarEntrega(dto.getEntidadBeneficiariaId());

        perfil.registrarCategoria(dto.getCategoriaDonacion());

        LocalDate fecha = dto.getFechaDonacion() != null ? dto.getFechaDonacion() : LocalDate.now();

        actualizarHistorialMensual(perfil, fecha);

        InfoDonacion infoDonacion = InfoDonacion.builder()
                .donanteId(donanteId)
                .entidadBeneficiariaId(dto.getEntidadBeneficiariaId())
                .cantidadBienes(dto.getCantidadBienes())
                .fechaDonacion(fecha)
                .categoriaDonacion(dto.getCategoriaDonacion())
                .categoriasAcumuladas(new ArrayList<>(perfil.getCategoriasAyudadas()))
                .build();

        evaluadorMisiones.evaluar(perfil, infoDonacion);

        repository.create(perfil);
    }

    public PerfilIncentivosDTO obtenerPerfil(Integer donanteId) {
        Perfil perfil = repository.findByDonanteId(donanteId);
        if (perfil == null) {
            perfil = new Perfil(donanteId);
        }

        Mision misionActual = perfil.getMisionActual();

        return PerfilIncentivosDTO.builder()
                .donanteId(perfil.getDonanteId())
                .totalDonacionesExitosas(perfil.getTotalDonacionesExitosas())
                .entidadesAyudadasCount((int) perfil.getEntidadesAyudadasIds().stream().distinct().count())
                .entidadesAyudadasIds(perfil.getEntidadesAyudadasIds())
                .categoriaDonante(perfil.getCategoriaDonante().name())
                .posicionRanking(servicioRanking.calcularPosicion(donanteId))
                .comparacionesMensuales(mapearHistorialMensual(perfil))
                .insigniasGanadas(mapearInsignias(perfil))
                .misionActual(misionActual != null ? mapearMision(misionActual) : null)
                .todasLasMisiones(mapearTodasLasMisiones(perfil))
                .build();
    }

    public MisionesDonanteDTO obtenerMisiones(Integer donanteId) {
        Perfil perfil = repository.findByDonanteId(donanteId);
        if (perfil == null) {
            perfil = new Perfil(donanteId);
        }

        Mision misionActual = perfil.getMisionActual();
        List<MisionDTO> proximasMisiones = new ArrayList<>();
        List<MisionDTO> misionesCompletadas = new ArrayList<>();

        boolean encontroActual = false;
        for (Mision m : perfil.getMisionesActuales()) {
            if (m.isCompletada()) {
                misionesCompletadas.add(mapearMision(m));
            } else if (!encontroActual) {
                encontroActual = true; // esta es la misión actual, la salteamos
            } else {
                proximasMisiones.add(mapearMision(m));
            }
        }

        return MisionesDonanteDTO.builder()
                .donanteId(donanteId)
                .categoriaDonante(perfil.getCategoriaDonante().name())
                .misionActual(misionActual != null ? mapearMision(misionActual) : null)
                .proximasMisiones(proximasMisiones)
                .misionesCompletadas(misionesCompletadas)
                .build();
    }

    public InsigniasDonanteDTO obtenerInsignias(Integer donanteId) {
        Perfil perfil = repository.findByDonanteId(donanteId);
        if (perfil == null) {
            perfil = new Perfil(donanteId);
        }

        List<InsigniaDTO> insignias = mapearInsignias(perfil);

        return InsigniasDonanteDTO.builder()
                .donanteId(donanteId)
                .visibilidadInsignia(perfil.getVisibilidadInsignia())
                .insignias(insignias)
                .totalInsignias(perfil.getInsigniasGanadas().size())
                .build();
    }

    /**
     * Retorna las métricas de actividad de un donante (acumuladas y del período
     * actual).
     */
    public MetricasActividadDTO obtenerMetricas(Integer donanteId) {
        Perfil perfil = repository.findByDonanteId(donanteId);
        if (perfil == null) {
            perfil = new Perfil(donanteId);
        }

        LocalDate ahora = LocalDate.now();
        int mesActual = ahora.getMonthValue();
        int anioActual = ahora.getYear();

        int donacionesMesActual = perfil.getHistorialMensual().stream()
                .filter(r -> r.getAnio() == anioActual && r.getMes() == mesActual)
                .findFirst()
                .map(RegistroDonacionMensual::getTotalDonaciones)
                .orElse(0);

        return MetricasActividadDTO.builder()
                .donanteId(donanteId)
                .categoriaDonante(perfil.getCategoriaDonante().name())
                .totalDonacionesExitosas(perfil.getTotalDonacionesExitosas())
                .entidadesAyudadasCount((int) perfil.getEntidadesAyudadasIds().stream().distinct().count())
                .entidadesAyudadasIds(perfil.getEntidadesAyudadasIds())
                .posicionRanking(servicioRanking.calcularPosicion(donanteId))
                .donacionesMesActual(donacionesMesActual)
                .mesPeriodoActual(mesActual)
                .anioPeriodoActual(anioActual)
                .comparacionesMensuales(mapearHistorialMensual(perfil))
                .build();
    }

    // ── Métodos privados auxiliares ───────────────────────────────────────────

    private void actualizarHistorialMensual(Perfil perfil, LocalDate fecha) {
        int anio = fecha.getYear();
        int mes = fecha.getMonthValue();

        perfil.getHistorialMensual().stream()
                .filter(r -> r.getAnio() == anio && r.getMes() == mes)
                .findFirst()
                .ifPresentOrElse(
                        RegistroDonacionMensual::registrar,
                        () -> {
                            RegistroDonacionMensual nuevo = new RegistroDonacionMensual(anio, mes);
                            nuevo.registrar();
                            perfil.getHistorialMensual().add(nuevo);
                        });
    }

    private List<RegistroDonacionMensualDTO> mapearHistorialMensual(Perfil perfil) {
        List<RegistroDonacionMensualDTO> resultado = new ArrayList<>();
        for (RegistroDonacionMensual r : perfil.getHistorialMensual()) {
            resultado.add(new RegistroDonacionMensualDTO(r.getAnio(), r.getMes(), r.getTotalDonaciones()));
        }
        return resultado;
    }

    private List<InsigniaDTO> mapearInsignias(Perfil perfil) {
        List<InsigniaDTO> resultado = new ArrayList<>();
        if (Boolean.TRUE.equals(perfil.getVisibilidadInsignia())) {
            for (Insignia i : perfil.getInsigniasGanadas()) {
                resultado.add(new InsigniaDTO(i.getTitulo(), i.getDescripcion()));
            }
        }
        return resultado;
    }

    private MisionDTO mapearMision(Mision mision) {
        return new MisionDTO(
                mision.getTitulo(),
                mision.getDescripcion(),
                mision.getClass().getSimpleName(),
                mision.getProgresoActual(),
                mision.getObjetivo(),
                mision.isCompletada(),
                mision.getFechaObtencion());
    }

    private List<MisionDTO> mapearTodasLasMisiones(Perfil perfil) {
        List<MisionDTO> resultado = new ArrayList<>();
        for (Mision m : perfil.getMisionesActuales()) {
            resultado.add(mapearMision(m));
        }
        return resultado;
    }
}
