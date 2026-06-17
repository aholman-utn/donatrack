package com.tp.incentivos.domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.*;

/**
 * Perfil de incentivos de un donante.
 * Almacena métricas de donación, categoría actual, misiones en curso e
 * insignias ganadas.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class PerfilIncentivosDonante {

    private Integer donanteId;
    private String nombreUsuario;
    private int totalDonacionesExitosas;
    private Set<Integer> entidadesAyudadasIds;
    private Set<CategoriaBien> categoriasAyudadas;
    private CategoriaDonante categoriaDonante;
    private List<Mision> misionesActuales;
    private List<Insignia> insigniasGanadas;
    private List<RegistroDonacionMensual> historialMensual;

    public PerfilIncentivosDonante(Integer donanteId) {
        this.donanteId = donanteId;
        this.totalDonacionesExitosas = 0;
        this.entidadesAyudadasIds = new HashSet<>();
        this.categoriasAyudadas = new HashSet<>();
        this.categoriaDonante = CategoriaDonante.COLABORADOR;
        this.misionesActuales = inicializarMisionesColaborador();
        this.insigniasGanadas = new ArrayList<>();
        this.historialMensual = new ArrayList<>();
    }

    public void registrarEntrega(Integer entidadBeneficiariaId) {
        this.totalDonacionesExitosas++;
        if (entidadBeneficiariaId != null) {
            this.entidadesAyudadasIds.add(entidadBeneficiariaId);
        }
    }

    public void registrarCategoria(CategoriaBien categoria) {
        if (categoria != null) {
            this.categoriasAyudadas.add(categoria);
        }
    }

    public Mision getMisionActual() {
        return misionesActuales.stream()
                .filter(m -> !m.isCompletada())
                .findFirst()
                .orElse(null);
    }

    public void subirCategoria() {
        if (categoriaDonante == CategoriaDonante.COLABORADOR) {
            categoriaDonante = CategoriaDonante.SOSTENEDOR;
            misionesActuales = inicializarMisionesSostenedor();
        } else if (categoriaDonante == CategoriaDonante.SOSTENEDOR) {
            categoriaDonante = CategoriaDonante.TRANSFORMADOR;
            misionesActuales = inicializarMisionesTransformador();
        }
    }

    // Por cada categoria del Donante tengo una lista con misiones que debe cumplir
    // (por ahora hardcodeadas)
    private List<Mision> inicializarMisionesColaborador() {
        return new ArrayList<>(List.of(
                new MisionDonacionesExitosas(
                        3, "Primer Paso",
                        "Completa 3 donaciones exitosas",
                        new Insignia(nombreUsuario, "Donacion Exitosa",
                                "Completaste tus primeras 3 donaciones exitosas")),
                new MisionRacha(
                        2, "Constancia Inicial",
                        "Dona durante 2 meses consecutivos",
                        new Insignia(nombreUsuario, "Racha", "Donaste durante 2 meses seguidos"))));
    }

    private List<Mision> inicializarMisionesSostenedor() {
        return new ArrayList<>(List.of(
                new MisionCompletitud(
                        3, "Diversificador",
                        "Ayuda con donaciones de 3 categorías de bienes distintas",
                        new Insignia(nombreUsuario, "Completitud", "Ayudaste en 3 categorías diferentes de donación")),
                new MisionHabilDonador(
                        10, "Gran Gesto",
                        "Realiza una donación de más de 10 bienes",
                        new Insignia(nombreUsuario, "Habil Donador", "Hiciste una donación de gran escala")),
                new MisionRacha(
                        4, "Racha Sostenida",
                        "Dona durante 4 meses consecutivos",
                        new Insignia(nombreUsuario, "Racha", "Donaste durante 4 meses seguidos"))));
    }

    private List<Mision> inicializarMisionesTransformador() {
        return new ArrayList<>(List.of(
                new MisionDonacionesExitosas(
                        20, "Centenar",
                        "Completa 20 donaciones exitosas",
                        new Insignia(nombreUsuario, "Donacion Exitosa", "Alcanzaste 20 donaciones exitosas en total")),
                new MisionRacha(
                        6, "Racha Legendaria",
                        "Dona durante 6 meses consecutivos",
                        new Insignia(nombreUsuario, "Racha", "Donaste durante 6 meses seguidos sin parar"))));
    }
}
