package com.tp.donatrack.logistica.domain.planificacion;

import com.tp.commons.dtos.logistica.DonacionSegmentadaListaParaEntregarALogisticaDTO;
import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.domain.Chofer;
import com.tp.donatrack.logistica.domain.Parada;
import com.tp.donatrack.logistica.domain.Ruta;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class CapacidadFisica implements Planificacion {

    private final CalculadorDimensiones calculadorDimensiones;

    public CapacidadFisica(CalculadorDimensiones calculadorDimensiones) {
        this.calculadorDimensiones = calculadorDimensiones;
    }

    @Override
    public List<Ruta> planificar(
        List<DonacionSegmentadaListaParaEntregarALogisticaDTO> donaciones,
        List<Camion> camionesDisponibles,
        List<Chofer> choferesDisponibles
    ) {

        double maxPesoFlota = camionesDisponibles.stream()
                .mapToDouble(Camion::getCapacidadCarga)
                .max().orElse(0);

        double maxVolumenFlota = camionesDisponibles.stream()
                .mapToDouble(Camion::getVolumen)
                .max().orElse(0);

        List<Ruta> rutasGeneradas = new ArrayList<>();
        int indiceCamion = 0;
        int indiceChofer = 0;

        Camion camionActual = camionesDisponibles.get(indiceCamion);
        Chofer choferActual = choferesDisponibles.get(indiceChofer);

        double pesoAcumulado = 0.0;
        double volumenAcumulado = 0.0;
        Map<String, List<Long>> enviosPorDireccionActual = new HashMap<>();

        for (DonacionSegmentadaListaParaEntregarALogisticaDTO donacion : donaciones) {
            DimensionesFisicas dimensiones = calculadorDimensiones.calcular(donacion.cantidad(), donacion.unidad());

            if (dimensiones.pesoKg() > maxPesoFlota || dimensiones.volumenM3() > maxVolumenFlota) {
                log.error("La donación ID {} excede la capacidad máxima. Se dejará en el depósito.",
                        donacion.donacionSegmentadaId());
                continue;
            }

            if (
                pesoAcumulado + dimensiones.pesoKg() > camionActual.getCapacidadCarga() ||
                volumenAcumulado + dimensiones.volumenM3() > camionActual.getVolumen()
            ) {
                log.info("Camión ID {} lleno. Cerrando ruta y pasando al siguiente.", camionActual.getId());

                rutasGeneradas.add(construirRuta(camionActual, choferActual, enviosPorDireccionActual));

                indiceCamion++;
                indiceChofer++;

                if (indiceCamion >= camionesDisponibles.size() || indiceChofer >= choferesDisponibles.size()) {
                    throw new IllegalStateException("Capacidad de flota superada. Faltan camiones o choferes.");
                }

                camionActual = camionesDisponibles.get(indiceCamion);
                choferActual = choferesDisponibles.get(indiceChofer);

                pesoAcumulado = 0.0;
                volumenAcumulado = 0.0;
                enviosPorDireccionActual = new HashMap<>();
            }

            pesoAcumulado += dimensiones.pesoKg();
            volumenAcumulado += dimensiones.volumenM3();

            // Agrupa los envíos por dirección. computeIfAbsent busca la dirección en el mapa:
            // Si no existe, inicializa una nueva ArrayList. Si ya existe, trae la lista actual.
            // Luego, el .add() mete el ID de la donación en esa lista (nueva o existente).
            enviosPorDireccionActual
                    .computeIfAbsent(donacion.direccionEntidadBeneficiaria(), k -> new ArrayList<>())
                    .add(donacion.donacionSegmentadaId());
        }

        if (!enviosPorDireccionActual.isEmpty()) {
            rutasGeneradas.add(construirRuta(camionActual, choferActual, enviosPorDireccionActual));
        }

        return rutasGeneradas;
    }


    private Ruta construirRuta(Camion camion, Chofer chofer, Map<String, List<Long>> enviosPorDireccion) {
        List<Parada> paradas = new ArrayList<>();
        int ordenActual = 1;

        for (Map.Entry<String, List<Long>> entry : enviosPorDireccion.entrySet()) {
            paradas.add(Parada.builder()
                    .orden(ordenActual++)
                    .direccion(entry.getKey())
                    .enviosIds(entry.getValue())
                    .build());
        }

        return Ruta.builder()
                .paradas(paradas)
                .camion(camion)
                .chofer(chofer)
                .iniciada(false)
                .build();
    }
}