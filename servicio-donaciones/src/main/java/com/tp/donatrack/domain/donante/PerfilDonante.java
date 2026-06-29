package com.tp.donatrack.domain.donante;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.donatrack.domain.bien.CategoriaBien;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PerfilDonante {
    @Builder.Default
    private boolean visibilidadInsignia = true;

    private Long misionActualId;

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
        this.progreso = (double) 0;
        this.historialDonaciones = new ArrayList<>();
        this.insigniasGanadas = new ArrayList<>();
        this.metricasPerfil = new Metrica();
    }

    public void registrarEntrega(CategoriaBien categoria) {
        if (this.historialDonaciones == null) {
            this.historialDonaciones = new ArrayList<>();
        }
        ItemDonacionSegmentada item = ItemDonacionSegmentada.builder()
                .fecha(LocalDate.now())
                .categoria(categoria)
                .build();
        this.historialDonaciones.add(item);

        //if (this.metricas == null) this.metricas = new Metrica();
        //this.metricas.setTotalDonacionesExitosas(this.metricas.getTotalDonacionesExitosas() + 1);
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

        List<LocalDate> fechas = this.historialDonaciones.stream()
                .map(ItemDonacionSegmentada::getFecha)
                .sorted()
                .collect(Collectors.toList());

        int racha = 1;
        for (int i = 0; i < fechas.size() - 1; i++) {
            LocalDate actual = fechas.get(i);
            LocalDate siguiente = fechas.get(i + 1);

            if (actual.getMonthValue() == siguiente.getMonthValue() - 1 ||
                    (actual.getMonthValue() == 12 && siguiente.getMonthValue() == 1)) {
                racha++;
            } else {
                racha = 1;
            }
        }
        return racha;
    }

    public int calcularCantidadDonacionesEntregadas(){
        return this.historialDonaciones.size();
    }
}
