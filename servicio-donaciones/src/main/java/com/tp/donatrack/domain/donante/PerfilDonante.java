package com.tp.donatrack.domain.donante;

import com.tp.commons.domain.donantes.Nivel;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.donacion.EstadoDonacionSegmentada;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PerfilDonante {
    @Builder.Default
    private boolean visibilidadInsignia = true;

    @Builder.Default
    private Long misionActualId = 1L;

    @Builder.Default
    private List<String> insigniasGanadas = new ArrayList<>();

    private Double progreso;

    @Builder.Default
    private Nivel nivelDonante = Nivel.COLABORADOR;

    @Builder.Default
    private Metrica metricasPerfil = new Metrica();

    @Builder.Default
    private List<ItemDonacionSegmentada> historialDonaciones = new ArrayList<>();

    public PerfilDonante() {
        this.visibilidadInsignia = true;
        this.nivelDonante = Nivel.COLABORADOR;
        this.misionActualId = 1L;
        this.progreso = (double) 0;
        this.historialDonaciones = new ArrayList<>();
        this.insigniasGanadas = new ArrayList<>();
        this.metricasPerfil = new Metrica();
    }

    public void registrarEntrega(DonacionSegmentada segmentada) {
        if (this.historialDonaciones == null) {
            this.historialDonaciones = new ArrayList<>();
        }
        ItemDonacionSegmentada item = ItemDonacionSegmentada.builder()
                .id(segmentada.getId())
                .fecha(LocalDate.now())
                .entidadBeneficiariaId(segmentada.getEntidadBeneficiariaAsignadaId())
                .categoria(segmentada.getSubCategoria().getCategoria())
                .estado(EstadoDonacionSegmentada.ENTREGADA)
                .build();
        this.historialDonaciones.add(item);

        this.metricasPerfil.setTotalDonacionesExitosas(
            this.metricasPerfil.getTotalDonacionesExitosas() + 1
        );

        this.metricasPerfil.getCategoriasAyudadas().add(segmentada.getSubCategoria().getCategoria());

        this.metricasPerfil.getEntidadesAyudadas().add(
            new EntidadAyudada(
                segmentada.getEntidadBeneficiariaAsignadaId(),
                segmentada.getId()
            )
        );
    }

    public int contarCategoriasUnicas() {
        if (this.historialDonaciones == null || this.historialDonaciones.isEmpty()) {
            return 0;
        }
        return Math.toIntExact(this.historialDonaciones.stream()
                .map(ItemDonacionSegmentada::getCategoria)
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
                .map(ItemDonacionSegmentada::getEntidadBeneficiariaId)
                .distinct()
                .count();
    }

    public int calcularCantidadDonacionesEntregadas(){
        return this.historialDonaciones.size();
    }
}
