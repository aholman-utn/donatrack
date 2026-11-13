package com.tp.donatrack.logistica.controllers;

import com.tp.commons.dtos.logistica.SegmentoLogistico;
import com.tp.donatrack.logistica.clients.DonacionesPanelClient;
import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.domain.Chofer;
import com.tp.donatrack.logistica.domain.Envio;
import com.tp.donatrack.logistica.domain.EstadoEnvio;
import com.tp.donatrack.logistica.domain.Parada;
import com.tp.donatrack.logistica.domain.Ruta;
import com.tp.donatrack.logistica.repository.EnvioRepository;
import com.tp.donatrack.logistica.repository.RutaRepository;
import com.tp.donatrack.logistica.services.CamionService;
import com.tp.donatrack.logistica.services.ChoferService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/logistica/panel")
public class PanelLogisticaController {
    private final RutaRepository rutas;
    private final EnvioRepository envios;
    private final CamionService camiones;
    private final ChoferService choferes;
    private final DonacionesPanelClient donaciones;

    public PanelLogisticaController(RutaRepository rutas, EnvioRepository envios, CamionService camiones,
                                    ChoferService choferes, DonacionesPanelClient donaciones) {
        this.rutas = rutas;
        this.envios = envios;
        this.camiones = camiones;
        this.choferes = choferes;
        this.donaciones = donaciones;
    }

    @GetMapping("/camiones")
    public List<Camion> camiones() {
        return camiones.listarCamiones();
    }

    @PostMapping("/camiones")
    @ResponseStatus(HttpStatus.CREATED)
    public Camion crearCamion(@RequestBody Camion camion) {
        camion.setId(null);
        camion.setPatente(texto(camion.getPatente(), "patente").toUpperCase(java.util.Locale.ROOT));
        camion.setMarca(texto(camion.getMarca(), "marca"));
        camion.setModelo(texto(camion.getModelo(), "modelo"));
        if (!positivo(camion.getCapacidadCarga()) || !positivo(camion.getVolumen()) || !positivo(camion.getAltura())) {
            throw invalido("La capacidad, el volumen y la altura deben ser mayores que cero.");
        }
        return camiones.registrarCamion(camion);
    }

    @GetMapping("/choferes")
    public List<Chofer> choferes() {
        return choferes.listarChoferes();
    }

    @PostMapping("/choferes")
    @ResponseStatus(HttpStatus.CREATED)
    public Chofer crearChofer(@RequestBody Chofer chofer) {
        chofer.setId(null);
        chofer.setNombre(texto(chofer.getNombre(), "nombre"));
        chofer.setApellido(texto(chofer.getApellido(), "apellido"));
        chofer.setDni(texto(chofer.getDni(), "DNI"));
        if (!chofer.getDni().matches("[0-9]{7,8}")) throw invalido("Ingresá un DNI de 7 u 8 dígitos.");
        return choferes.registrarChofer(chofer);
    }

    @GetMapping("/segmentos")
    public List<SegmentoLogistico> disponibles() {
        return donaciones.disponibles();
    }

    @GetMapping("/rutas")
    public List<Recorrido> recorridos() {
        return recorridos(false);
    }

    @GetMapping("/rutas/historial")
    public List<Recorrido> historial() {
        return recorridos(true);
    }

    private List<Recorrido> recorridos(boolean completadas) {
        List<Ruta> lista = rutas.findByPanelListoTrueOrderByFechaCreacionDesc();
        if (lista.isEmpty()) return List.of();
        var estados = donaciones.estados(lista.stream().map(Ruta::getId).toList()).stream()
                .collect(Collectors.groupingBy(SegmentoLogistico::rutaId));
        return lista.stream().map(ruta -> new Recorrido(ruta, estados.getOrDefault(ruta.getId(), List.of())))
                .filter(recorrido -> esCompletada(recorrido) == completadas).toList();
    }

    private boolean esCompletada(Recorrido recorrido) {
        return recorrido.ruta().getSegmentosPanel() != null && !recorrido.ruta().getSegmentosPanel().isEmpty()
                && recorrido.ruta().getSegmentosPanel().stream().allMatch(id -> recorrido.segmentos().stream()
                .anyMatch(segmento -> segmento.id().equals(id) && "ENTREGADA".equals(segmento.estado())));
    }

    @PostMapping("/rutas")
    @ResponseStatus(HttpStatus.CREATED)
    public Ruta crearRuta(@RequestBody CrearRuta request) {
        if (request.solicitudId() == null || request.segmentoIds() == null || request.segmentoIds().isEmpty()
                || request.segmentoIds().stream().anyMatch(id -> id == null || id <= 0)) {
            throw invalido("Seleccioná al menos un segmento e incluí el identificador de solicitud.");
        }
        List<Long> ids = request.segmentoIds().stream().distinct().sorted().toList();
        String rutaId = "panel-" + request.solicitudId();
        Ruta ruta = rutas.findById(rutaId).orElseGet(() -> prepararRuta(rutaId, request, ids));
        if (!Objects.equals(ruta.getCamionId(), request.camionId())
                || !Objects.equals(ruta.getChoferId(), request.choferId())
                || !ids.equals(ruta.getSegmentosPanel())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "La solicitud ya se usó con otra selección.");
        }
        if (ruta.isPanelListo()) return ruta;

        var porDireccion = donaciones.reservar(rutaId, ids).stream().collect(Collectors.groupingBy(
                SegmentoLogistico::direccion, LinkedHashMap::new, Collectors.toList()));
        List<Parada> paradas = new ArrayList<>();
        for (var parada : porDireccion.entrySet()) {
            List<String> enviosIds = new ArrayList<>();
            for (SegmentoLogistico segmento : parada.getValue()) {
                String envioId = rutaId + "-" + segmento.id();
                Envio envio = new Envio();
                envio.setId(envioId);
                envio.setRutaId(rutaId);
                envio.setDonacionSegmentadaId(segmento.id());
                envio.setEntidadBeneficiariaId(segmento.entidadId());
                envio.setEstado(EstadoEnvio.ASIGNACION_REALIZADA);
                envios.save(envio);
                enviosIds.add(envioId);
            }
            paradas.add(Parada.builder().orden(paradas.size() + 1).direccion(parada.getKey()).enviosIds(enviosIds).build());
        }
        ruta.setParadas(paradas);
        ruta.setPanelListo(true);
        return rutas.save(ruta);
    }

    private Ruta prepararRuta(String id, CrearRuta request, List<Long> segmentos) {
        if (camiones.buscarCamionPorId(request.camionId()) == null || choferes.buscarChoferPorId(request.choferId()) == null) {
            throw invalido("Seleccioná un camión y un chofer registrados.");
        }
        try {
            return rutas.insert(Ruta.builder().id(id).camionId(request.camionId()).choferId(request.choferId())
                    .segmentosPanel(segmentos).build());
        } catch (DuplicateKeyException concurrente) {
            return rutas.findById(id).orElseThrow(() -> concurrente);
        }
    }

    @PostMapping("/rutas/{id}/iniciar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void iniciar(@PathVariable String id) {
        Ruta ruta = rutaLista(id);
        donaciones.iniciar(id, ruta.getSegmentosPanel());
    }

    @PostMapping("/rutas/{id}/segmentos/{segmentoId}/llegada")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void llegada(@PathVariable String id, @PathVariable Long segmentoId) {
        Ruta ruta = rutaLista(id);
        if (!ruta.getSegmentosPanel().contains(segmentoId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "El segmento no pertenece a la ruta.");
        }
        donaciones.llegada(id, segmentoId);
    }

    private Ruta rutaLista(String id) {
        return rutas.findById(id).filter(Ruta::isPanelListo)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No se encontró la ruta."));
    }

    private String texto(String valor, String campo) {
        if (valor == null || valor.isBlank()) throw invalido("Completá el campo " + campo + ".");
        return valor.trim();
    }

    private boolean positivo(double valor) {
        return Double.isFinite(valor) && valor > 0;
    }

    private ResponseStatusException invalido(String detalle) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, detalle);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ProblemDetail error(ResponseStatusException error) {
        return error.getBody();
    }

    public record CrearRuta(UUID solicitudId, String camionId, String choferId, List<Long> segmentoIds) {}
    public record Recorrido(Ruta ruta, List<SegmentoLogistico> segmentos) {}
}
