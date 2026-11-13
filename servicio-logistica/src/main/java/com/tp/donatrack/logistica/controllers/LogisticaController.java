package com.tp.donatrack.logistica.controllers;

import com.tp.commons.dtos.logistica.DonacionSegmentadaListaParaEntregarALogisticaDTO;
import com.tp.donatrack.logistica.domain.Camion;
import com.tp.donatrack.logistica.domain.Chofer;
import com.tp.donatrack.logistica.domain.Envio;
import com.tp.donatrack.logistica.domain.Ruta;
import com.tp.donatrack.logistica.services.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/logistica")
public class LogisticaController {
    private final EnviosService enviosService;
    private final CamionService camionService;
    private final RutaService rutaService;
    private final ChoferService choferService;

    public LogisticaController(
            EnviosService enviosService,
            CamionService camionService,
            RutaService rutaService,
            ChoferService choferService
    ) {
        this.enviosService = enviosService;
        this.camionService = camionService;
        this.rutaService = rutaService;
        this.choferService = choferService;
    }

    @PostMapping("/camiones")
    public ResponseEntity<Camion> registrarCamion(@RequestBody Camion camion) {
        camion.setId(null);
        camion.setPatente(texto(camion.getPatente(), "patente").toUpperCase(java.util.Locale.ROOT));
        camion.setMarca(texto(camion.getMarca(), "marca"));
        camion.setModelo(texto(camion.getModelo(), "modelo"));
        if (!positivo(camion.getCapacidadCarga()) || !positivo(camion.getVolumen()) || !positivo(camion.getAltura())) {
            throw invalido("La capacidad, el volumen y la altura deben ser mayores que cero.");
        }
        return ResponseEntity.ok(camionService.registrarCamion(camion));
    }

    @GetMapping("/camiones")
    public ResponseEntity<List<Camion>> listarCamiones() {
        return ResponseEntity.ok(camionService.listarCamiones());
    }

    @PostMapping("/choferes")
    public ResponseEntity<Chofer> registrarChofer(@RequestBody Chofer chofer) {
        chofer.setId(null);
        chofer.setNombre(texto(chofer.getNombre(), "nombre"));
        chofer.setApellido(texto(chofer.getApellido(), "apellido"));
        chofer.setDni(texto(chofer.getDni(), "DNI"));
        if (!chofer.getDni().matches("[0-9]{7,8}")) throw invalido("Ingresá un DNI de 7 u 8 dígitos.");
        return ResponseEntity.ok(choferService.registrarChofer(chofer));
    }

    @GetMapping("/choferes")
    public ResponseEntity<List<Chofer>> listarChoferes() {
        return ResponseEntity.ok(choferService.listarChoferes());
    }

    @PostMapping("/envios")
    public ResponseEntity<Envio> registrarEnvio(@RequestBody Envio envio) {
        validarFlujoAnterior(envio.getId());
        validarFlujoAnterior(envio.getRutaId());
        return ResponseEntity.ok(enviosService.registrarEnvio(envio));
    }

    @PostMapping("/planificar")
    public ResponseEntity<Void> planificarRutas(
            @RequestBody List<DonacionSegmentadaListaParaEntregarALogisticaDTO> loteDonaciones
    ) {
        rutaService.planificarLote(loteDonaciones);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/envios")
    public ResponseEntity<List<Envio>> listarEnvios() {
        return ResponseEntity.ok(enviosService.listarEnvios());
    }

    @PostMapping("/rutas")
    public ResponseEntity<Ruta> registrarRuta(@RequestBody Ruta ruta) {
        validarFlujoAnterior(ruta.getId());
        if (ruta.getSegmentosPanel() != null || ruta.isPanelListo()) throw conflictoFlujo();
        if (ruta.getParadas() != null) {
            ruta.getParadas().forEach(parada -> {
                if (parada.getEnviosIds() != null) parada.getEnviosIds().forEach(this::validarFlujoAnterior);
            });
        }
        return ResponseEntity.ok(rutaService.registrarRuta(ruta));
    }

    @GetMapping("/rutas")
    public ResponseEntity<List<Ruta>> listarRutas() {
        return ResponseEntity.ok(rutaService.listarRutas());
    }

    @PostMapping("/rutas/{id}/iniciar")
    public ResponseEntity<Void> iniciarRuta(@PathVariable("id") String id) {
        validarFlujoAnterior(id);
        rutaService.iniciarRuta(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/envios/{id}/llegada")
    public ResponseEntity<Void> registrarLlegada(@PathVariable("id") String id) {
        validarFlujoAnterior(id);
        enviosService.registrarLlegadaADestino(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/envios/{id}/recibir")
    public ResponseEntity<Void> recibirEnvio(@PathVariable("id") String id, @RequestBody(required = false) Map<String, String> body) {
        validarFlujoAnterior(id);
        String detalles = (body != null) ? body.get("detalles") : null;
        enviosService.registrarEntregaExitosa(id, detalles);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/envios/{id}/fallar")
    public ResponseEntity<Void> fallarEnvio(@PathVariable("id") String id, @RequestBody(required = false) Map<String, String> body) {
        validarFlujoAnterior(id);
        String motivo = (body != null) ? body.get("motivo") : null;
        enviosService.registrarEntregaFallida(id, motivo);
        return ResponseEntity.ok().build();
    }
    private void validarFlujoAnterior(String id) {
        if (Ruta.esDelPanel(id)) throw conflictoFlujo();
    }
    private ResponseStatusException conflictoFlujo() {
        return new ResponseStatusException(HttpStatus.CONFLICT, "Este recorrido se gestiona desde el panel de Logística.");
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
}
