package com.tp.donatrack.domain.donante;

import com.tp.commons.domain.donantes.Nivel;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
@Embeddable
public class PerfilDonante {
    @Builder.Default
    @Column(name = "visibilidad_insignia")
    private boolean visibilidadInsignia = true;

    @Builder.Default
    @Column(name = "mision_actual_id")
    private Long misionActualId = 1L;

    @Builder.Default
    @Transient
    private List<String> insigniasGanadas = new ArrayList<>();

    @Column(name = "progreso_mision_actual")
    private Double progreso;

    @Builder.Default
    @Convert(converter = NivelDonanteConverter.class)
    @Column(name = "nivel_donante", length = 30)
    private Nivel nivelDonante = Nivel.COLABORADOR;

    @Builder.Default
    @Column(name = "total_donaciones_exitosas")
    private int totalDonacionesExitosas = 0;

    @Builder.Default
    @Column(name = "total_misiones_completadas")
    private int totalMisionesCompletadas = 0;

    @Column(name = "fecha_ultima_donacion")
    private LocalDate fechaUltimaDonacion;

    @Builder.Default
    @Transient
    private Metrica metricasPerfil = new Metrica();

    @Builder.Default
    @Transient
    private List<ItemHistoralDonaciones> historialDonaciones = new ArrayList<>();

    public PerfilDonante() {
        this.visibilidadInsignia = true;
        this.nivelDonante = Nivel.COLABORADOR;
        this.misionActualId = 1L;
        this.progreso = (double) 0;
        this.totalDonacionesExitosas = 0;
        this.totalMisionesCompletadas = 0;
        this.historialDonaciones = new ArrayList<>();
        this.insigniasGanadas = new ArrayList<>();
        this.metricasPerfil = new Metrica();
    }

    public void registrarEntrega(DonacionSegmentada segmentada) {
        if (this.historialDonaciones == null) {
            this.historialDonaciones = new ArrayList<>();
        }
        ItemHistoralDonaciones item = ItemHistoralDonaciones.builder()
                .id(segmentada.getId())
                .fecha(LocalDate.now())
                .entidadBeneficiariaId(segmentada.getEntidadBeneficiariaAsignadaId())
                .categoria(segmentada.getSubCategoria().getCategoria())
                .estado(EstadoDonacionSegmentada.ENTREGADA)
                .build();
        this.historialDonaciones.add(item);

        this.totalDonacionesExitosas++;
        this.fechaUltimaDonacion = LocalDate.now();

        this.metricasPerfil.setTotalDonacionesExitosas(
                this.metricasPerfil.getTotalDonacionesExitosas() + 1);

        this.metricasPerfil.getCategoriasAyudadas().add(segmentada.getSubCategoria().getCategoria());

        this.metricasPerfil.getEntidadesAyudadas().add(
                new EntidadAyudada(
                        segmentada.getEntidadBeneficiariaAsignadaId(),
                        segmentada.getId()));
    }

    public int contarCategoriasUnicas() {
        if (this.historialDonaciones == null || this.historialDonaciones.isEmpty()) {
            return 0;
        }
        return Math.toIntExact(this.historialDonaciones.stream()
                .map(ItemHistoralDonaciones::getCategoria)
                .distinct()
                .count());
    }

    public int calcularRachaMeses() {
        if (this.historialDonaciones == null || this.historialDonaciones.isEmpty()) {
            return 0;
        }

        List<YearMonth> mesesUnicos = this.historialDonaciones.stream()
                .map(d -> YearMonth.from(d.getFecha()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        YearMonth mesActual = YearMonth.now();
        YearMonth ultimoMesDonado = mesesUnicos.get(mesesUnicos.size() - 1);

        if (ultimoMesDonado.isBefore(mesActual.minusMonths(1))) {
            return 0;
        }

        int racha = 1;

        for (int i = 0; i < mesesUnicos.size() - 1; i++) {
            YearMonth actual = mesesUnicos.get(i);
            YearMonth siguiente = mesesUnicos.get(i + 1);

            if (actual.plusMonths(1).equals(siguiente)) {
                racha++;
            } else {
                racha = 1;
            }
        }

        return racha;
    }

    public int calcularDonacionesAEntidadesBeneficiarias() {
        return (int) this.historialDonaciones.stream()
                .map(ItemHistoralDonaciones::getEntidadBeneficiariaId)
                .distinct()
                .count();
    }

    public int calcularCantidadDonacionesEntregadas() {
        return this.historialDonaciones.size();
    }
}
