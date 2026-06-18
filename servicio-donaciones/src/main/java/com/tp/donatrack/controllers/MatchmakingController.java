package com.tp.donatrack.controllers;

import com.tp.donatrack.dtos.AsignarDonacionRequest;
import com.tp.donatrack.dtos.ResultadoMatchmakingDTO;
import com.tp.donatrack.routes.MatchmakingRoutes;
import com.tp.donatrack.services.MatchmakingService;
import com.tp.donatrack.repositories.DonanteRepository;
import com.tp.donatrack.repositories.DonacionRepository;
import com.tp.donatrack.repositories.EntidadBeneficiariaRepository;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.donacion.DonacionSegmentada;
import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.necesidad.NecesidadExtraordinaria;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import com.tp.donatrack.domain.persona.TipoOrganizacion;
import com.tp.donatrack.domain.bien.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Controller REST para el proceso de matchmaking de donaciones.
 * Expone endpoints para:
 * - Obtener el ranking de entidades beneficiarias para una donación segmentada
 * - Obtener rankings para todas las donaciones en depósito de un donante
 * - Asignar una donación segmentada a la entidad seleccionada del ranking
 */
@RestController
@RequestMapping(MatchmakingRoutes.BASE)
public class MatchmakingController {

    private final MatchmakingService matchmakingService;
    private final DonanteRepository donanteRepository;
    private final DonacionRepository donacionRepository;
    private final EntidadBeneficiariaRepository entidadBeneficiariaRepository;

    public MatchmakingController(
            MatchmakingService matchmakingService,
            DonanteRepository donanteRepository,
            DonacionRepository donacionRepository,
            EntidadBeneficiariaRepository entidadBeneficiariaRepository) {
        this.matchmakingService = matchmakingService;
        this.donanteRepository = donanteRepository;
        this.donacionRepository = donacionRepository;
        this.entidadBeneficiariaRepository = entidadBeneficiariaRepository;
    }

    /**
     * Obtiene el ranking de entidades beneficiarias para una donación segmentada específica.
     * GET /matchmaking/ranking?donacionSegmentadaId=1
     */
    @GetMapping(MatchmakingRoutes.RANKING)
    public ResponseEntity<?> obtenerRanking(@RequestParam Integer donacionSegmentadaId) {
        try {
            ResultadoMatchmakingDTO resultado = matchmakingService.obtenerRanking(donacionSegmentadaId);
            return ResponseEntity.ok(resultado);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Obtiene el ranking de matchmaking para TODAS las donaciones en depósito de un donante.
     * GET /matchmaking/ranking/donante/{donanteId}
     */
    @GetMapping(MatchmakingRoutes.RANKING + "/donante/{donanteId}")
    public ResponseEntity<?> obtenerRankingPorDonante(@PathVariable Integer donanteId) {
        try {
            List<ResultadoMatchmakingDTO> resultados = matchmakingService.obtenerRankingPorDonante(donanteId);
            return ResponseEntity.ok(resultados);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Asigna una donación segmentada a la entidad beneficiaria seleccionada del ranking.
     * POST /matchmaking/asignar
     * Body: { "donacionSegmentadaId": 1, "entidadBeneficiariaId": 2 }
     */
    @PostMapping(MatchmakingRoutes.ASIGNAR)
    public ResponseEntity<String> asignarDonacion(@Valid @RequestBody AsignarDonacionRequest request) {
        try {
            matchmakingService.asignarDonacion(
                    request.getDonacionSegmentadaId(),
                    request.getEntidadBeneficiariaId());
            return ResponseEntity.ok("Donación segmentada asignada exitosamente a la entidad beneficiaria.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Carga el escenario de prueba de 10 entidades con sus necesidades y donaciones previas,
     * simulando exactamente el test unitario.
     * POST /matchmaking/seed-test
     */
    @PostMapping("/seed-test")
    public ResponseEntity<?> seedTestScenario() {
        try {
            // 1. Limpiar el estado anterior en memoria
            donanteRepository.clear();
            donacionRepository.clear();
            entidadBeneficiariaRepository.clear();

            // 2. Definir SubCategorías
            SubCategoria arroz = new SubCategoria(Categoria.ALIMENTOS, "Arroz", Unidad.KG);
            SubCategoria sillasCat = new SubCategoria(Categoria.MOBILIARIO, "Sillas", Unidad.UNIDADES);

            // 3. Crear Donante de prueba
            PersonaJuridica pjDonante = new PersonaJuridica();
            pjDonante.setRazonSocial("Donante Test S.A.");
            pjDonante.setTipo(TipoOrganizacion.EMPRESA);
            Map<String, List<String>> medios = new HashMap<>();
            medios.put("EMAIL", List.of("donantetest@donatrack.org"));
            pjDonante.setMedioDeContacto(medios);
            
            Donante donante = new Donante(pjDonante);
            donanteRepository.create(donante);

            // 4. Crear Grupo C: Ganadoras (3 entidades) -> Necesitan Arroz, 0 donaciones previas
            for (int i = 1; i <= 3; i++) {
                EntidadBeneficiaria e = crearEntidadParaSeed("Ganadora " + i);
                e.agregarNecesidad(new NecesidadExtraordinaria(arroz, 100, new Date(), "Comedor"));
                entidadBeneficiariaRepository.create(e);
            }

            // 5. Crear Grupo A: Solo Semántica (3 entidades) -> Necesitan Arroz, pero con muchas donaciones previas (50)
            for (int i = 1; i <= 3; i++) {
                EntidadBeneficiaria e = crearEntidadParaSeed("Solo Semantica " + i);
                NecesidadExtraordinaria nec = new NecesidadExtraordinaria(arroz, 100, new Date(), "Comedor");
                for (int j = 0; j < 50; j++) {
                    nec.recibirDonacion(crearDonacionSegmentadaParaSeed(arroz, 1, donante));
                }
                e.agregarNecesidad(nec);
                entidadBeneficiariaRepository.create(e);
            }

            // 6. Crear Grupo B: Solo Sub-atendidos (8 entidades) -> Necesitan Sillas (otra categoria), 0 donaciones previas
            for (int i = 1; i <= 8; i++) {
                EntidadBeneficiaria e = crearEntidadParaSeed("Solo Subatendida " + i);
                e.agregarNecesidad(new NecesidadExtraordinaria(sillasCat, 10, new Date(), "Equipamiento"));
                entidadBeneficiariaRepository.create(e);
            }

            // 7. Crear Donación en Depósito a evaluar (Arroz, cantidad 10)
            Donacion donacionPrueba = crearDonacionParaSeed(arroz, 10, donante);
            donacionRepository.save(donacionPrueba);

            // Buscar la segmentada que se autogeneró
            DonacionSegmentada segmentada = donacionPrueba.getDonacionesSegmentadas().get(0);

            // 8. Retornar JSON informativo
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Escenario de matchmaking de 10 entidades cargado exitosamente.");
            response.put("donacionSegmentadaId", segmentada.getId());
            response.put("donanteId", donante.getId());
            response.put("bienesDonados", segmentada.getCantidad());
            response.put("subcategoria", segmentada.getSubCategoria().getDescripcion());
            response.put("nota", "Ejecute GET http://localhost:8080/matchmaking/ranking?donacionSegmentadaId=" + segmentada.getId() + " para ver la asignación.");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error al inicializar datos de matchmaking: " + e.getMessage());
        }
    }

    private EntidadBeneficiaria crearEntidadParaSeed(String nombre) {
        PersonaJuridica pj = new PersonaJuridica();
        pj.setRazonSocial(nombre);
        pj.setTipo(TipoOrganizacion.ONG);
        return new EntidadBeneficiaria(pj);
    }

    private Donacion crearDonacionParaSeed(SubCategoria sub, int cantidad, Donante donante) {
        Bien bien = new BienDuradero("Bien de prueba", "Detalle de prueba", "imagen.png", sub, EstadoBien.NUEVO);
        List<Bien> bienes = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            bienes.add(bien);
        }
        return new Donacion(donante, "Donación para matchmaking", new Date(), bienes);
    }

    private DonacionSegmentada crearDonacionSegmentadaParaSeed(SubCategoria sub, int cantidad, Donante donante) {
        Bien bien = new BienDuradero("Bien de prueba", "Detalle de prueba", "imagen.png", sub, EstadoBien.NUEVO);
        List<Bien> bienes = new ArrayList<>();
        for (int i = 0; i < cantidad; i++) {
            bienes.add(bien);
        }
        return new DonacionSegmentada(cantidad, sub, bienes, donante != null ? donante.getId() : null);
    }
}
