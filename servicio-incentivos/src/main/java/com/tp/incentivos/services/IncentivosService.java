package com.tp.incentivos.services;

import com.tp.incentivos.domain.*;
import com.tp.incentivos.dtos.*;
import com.tp.incentivos.repositories.IncentivosRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
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

        PerfilIncentivosDonante perfil = repository.findByDonanteId(donanteId);
        if (perfil == null) {
            perfil = new PerfilIncentivosDonante(donanteId);
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

    /**
     * Retorna el perfil completo de incentivos del donante, incluyendo métricas,
     * misiones, insignias, historial mensual y posición en el ranking.
     */
    public PerfilIncentivosDTO obtenerPerfil(Integer donanteId) {
        PerfilIncentivosDonante perfil = repository.findByDonanteId(donanteId);
        if (perfil == null) {
            perfil = new PerfilIncentivosDonante(donanteId);
        }

        Mision misionActual = perfil.getMisionActual();

        return PerfilIncentivosDTO.builder()
                .donanteId(perfil.getDonanteId())
                .totalDonacionesExitosas(perfil.getTotalDonacionesExitosas())
                .entidadesAyudadasCount(perfil.getEntidadesAyudadasIds().size())
                .entidadesAyudadasIds(perfil.getEntidadesAyudadasIds())
                .categoriaDonante(perfil.getCategoriaDonante().name())
                .posicionRanking(servicioRanking.calcularPosicion(donanteId))
                .comparacionesMensuales(mapearHistorialMensual(perfil))
                .insigniasGanadas(mapearInsignias(perfil))
                .misionActual(misionActual != null ? mapearMision(misionActual) : null)
                .todasLasMisiones(mapearTodasLasMisiones(perfil))
                .build();
    }

    /**
     * Cambia la visibilidad de una insignia específica del donante.
     */
    public void cambiarVisibilidadInsignia(Integer donanteId, String tituloInsignia, boolean visible) {
        PerfilIncentivosDonante perfil = repository.findByDonanteId(donanteId);
        if (perfil == null) {
            throw new IllegalArgumentException("Donante no encontrado: " + donanteId);
        }

        perfil.getInsigniasGanadas().stream()
                .filter(i -> i.getTitulo().equalsIgnoreCase(tituloInsignia))
                .findFirst()
                .ifPresent(i -> i.setVisible(visible));

        repository.create(perfil);
    }

    // ── Métodos privados auxiliares ───────────────────────────────────────────

    private void actualizarHistorialMensual(PerfilIncentivosDonante perfil, LocalDate fecha) {
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

    private List<RegistroDonacionMensualDTO> mapearHistorialMensual(PerfilIncentivosDonante perfil) {
        List<RegistroDonacionMensualDTO> resultado = new ArrayList<>();
        for (RegistroDonacionMensual r : perfil.getHistorialMensual()) {
            resultado.add(new RegistroDonacionMensualDTO(r.getAnio(), r.getMes(), r.getTotalDonaciones()));
        }
        return resultado;
    }

    private List<InsigniaDTO> mapearInsignias(PerfilIncentivosDonante perfil) {
        List<InsigniaDTO> resultado = new ArrayList<>();
        for (Insignia i : perfil.getInsigniasGanadas()) {
            resultado.add(new InsigniaDTO(i.getTitulo(), i.getDescripcion(), i.isVisible()));
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

    private List<MisionDTO> mapearTodasLasMisiones(PerfilIncentivosDonante perfil) {
        List<MisionDTO> resultado = new ArrayList<>();
        for (Mision m : perfil.getMisionesActuales()) {
            resultado.add(mapearMision(m));
        }
        return resultado;
    }
}
