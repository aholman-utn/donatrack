package com.tp.incentivos.services;

import com.tp.incentivos.domain.*;
import com.tp.incentivos.domain.misiones.Mision;
import com.tp.incentivos.dtos.*;
import com.tp.incentivos.repositories.IncentivosRepository;
import com.tp.incentivos.repositories.MisionRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class IncentivosService {
    private final MisionRepository misionRepository;
    public IncentivosService(
            MisionRepository misionRepository
    ) {
        this.misionRepository = misionRepository;
    }

    /**
     * Procesa una nueva entrega de donación:
     * 1. Obtiene o crea el perfil del donante (resiliencia/fallback).
     * 2. Registra la entrega básica (total y entidad).
     * 3. Registra la categoría del bien donado.
     * 4. Actualiza el historial mensual.
     * 5. Construye el InfoDonacion y delega la evaluación de misiones.
     * 6. Persiste el perfil.
     */
    public void procesarNuevaEntrega(EntregaDonacionDTO dto) {
        Long donanteId = dto.getDonanteId();
        Long donacionSegmendatadId = dto.getDonacionSegmentadaId();
        Long ultimaMisionId = dto.getUltimaMisionId();
        double progreso = dto.getProgreso();

        Mision mision = this.misionRepository.findById(ultimaMisionId)
                .orElseThrow(() -> new RuntimeException("Misión no encontrada con ID: " + ultimaMisionId));

        boolean cumplida = mision.estaCumplida(dto);

        // Imprimiendo los valores
        System.out.println("Donante ID: " + donanteId);
        System.out.println("Donación Segmentada ID: " + donacionSegmendatadId);
        System.out.println("Última Misión ID: " + ultimaMisionId);
        System.out.println("Progreso: " + progreso);
        /*
            1. Le pedimos al servicio de donaciones el rol y la ultima mision del del donante
            2. Vemos si con estos datos cumple para subir de misión
            3. SI sube de misión actualizamos la info del donante (POST donaciones)
            4. SI no sube no hago nada
         */

        //1. POST Crear perfilDonante en donante (Servicio donaciones)
        // Ultima mision y progreso actual del donante
        //Vemos que mision matchea en base al rol del usuario
        // 1. Actualizamos el progreso
        // Vemos


        /*
        Perfil perfil = repository.findByDonanteId(donanteId);
        if (perfil == null) {
            perfil = new Perfil(donanteId);
            // NombreUsuario queda null aquí por fallback, se podría buscar pero lo creamos resiliente
        }

        if (dto.getNombreUsuario() != null) {
            perfil.setNombreUsuario(dto.getNombreUsuario());
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

         */
    }
}
