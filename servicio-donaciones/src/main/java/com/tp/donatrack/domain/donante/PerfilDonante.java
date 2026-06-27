package com.tp.donatrack.domain.donante;

import com.tp.commons.domain.donantes.Nivel;
import com.tp.donatrack.domain.bien.CategoriaBien;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@Builder
public class PerfilDonante {
    @Builder.Default
    private boolean visibilidadInsignia = true;

    private Long misionActualId;

    private List<String> insignasGanadas = new ArrayList<>();

    private Double progreso;

    private Metrica metricas;

    @Builder.Default
    private Nivel categoriaDonante = Nivel.COLABORADOR;

    @Builder.Default
    private List<ItemDonacion> historialDonaciones = new ArrayList<>();

    public PerfilDonante() {
        this.visibilidadInsignia = true;
        this.historialDonaciones = new ArrayList<>();
        this.categoriaDonante = Nivel.COLABORADOR;
        this.progreso = (double) 0;
    }

    public void registrarEntrega(Long entidadBeneficiariaId, CategoriaBien categoria) {
        if (this.historialDonaciones == null) {
            this.historialDonaciones = new ArrayList<>();
        }
        ItemDonacion item = ItemDonacion.builder()
                .fecha(LocalDate.now())
                .categoria(categoria)
                .build();
        this.historialDonaciones.add(item);
    }

    public void registrarCategoria(CategoriaBien categoria) {
        if (categoria != null) {
            registrarEntrega(null, categoria);
        }
    }

    public void subirCategoria() {
        if (this.historialDonaciones == null) {
            return;
        }
        int size = this.historialDonaciones.size();
        if (this.categoriaDonante == Nivel.COLABORADOR && size >= 5) {
            this.categoriaDonante = Nivel.SOSTENEDOR;
        } else if (this.categoriaDonante == Nivel.SOSTENEDOR && size >= 15) {
            this.categoriaDonante = Nivel.TRANSFORMADOR;
        }
    }
}
