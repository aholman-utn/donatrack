package com.tp.donatrack.services;

import com.tp.commons.domain.donaciones.Unidad;
import com.tp.donatrack.domain.donacion.Donacion;
import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.dtos.ActualizarDonacionRequest;
import com.tp.donatrack.dtos.CrearDonacionRequest;
import com.tp.donatrack.dtos.DonacionHistorialDTO;
import com.tp.donatrack.repositories.DonacionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DonacionCrudTest {

    @Autowired
    private DonacionService donacionService;

    @Autowired
    private DonanteService donanteService;

    @Autowired
    private DonacionRepository donacionRepository;

    @BeforeEach
    void setUp() {
        donacionRepository.clear();
    }

    @Test
    void testCrudCompletoDonaciones() {
        // Registrar un donante de prueba
        PersonaHumana persona = new PersonaHumana("Juan", "MASCULINO", "Perez", new java.util.Date(), 25, "12345678");
        persona.agregarMedioDeContacto("email", "juan@mail.com");
        com.tp.donatrack.domain.donante.Donante donante = donanteService.registrar(persona);
        assertNotNull(donante.getPersona().getId());

        // 1. Crear donación
        CrearDonacionRequest request = new CrearDonacionRequest();
        request.setDonanteId(donante.getPersona().getId());
        request.setDescripcion("Donación de prueba CRUD");
        
        CrearDonacionRequest.BienRequest bien = new CrearDonacionRequest.BienRequest();
        bien.setTipo("DURADERO");
        bien.setNombre("Mesa");
        bien.setDescripcion("Mesa de madera");
        bien.setEstadoBien(com.tp.donatrack.domain.bien.EstadoBien.NUEVO);
        
        CrearDonacionRequest.SubCategoriaRequest sub = new CrearDonacionRequest.SubCategoriaRequest();
        sub.setCategoria(com.tp.donatrack.domain.bien.CategoriaBien.MOBILIARIO);
        sub.setDescripcion("Mesas");
        sub.setUnidad(Unidad.UNIDADES);
        bien.setSubCategoria(sub);
        
        request.setBienes(List.of(bien));

        Donacion creada = donacionService.registrarDonacion(request);
        assertNotNull(creada.getId());
        assertEquals("Donación de prueba CRUD", creada.getDescripcion());

        // 2. Obtener todas
        List<DonacionHistorialDTO> todas = donacionService.obtenerTodas();
        assertEquals(1, todas.size());
        assertEquals(creada.getId(), todas.get(0).getId());

        // 3. Obtener por ID
        DonacionHistorialDTO porId = donacionService.obtenerPorId(creada.getId());
        assertEquals("Donación de prueba CRUD", porId.getDescripcion());

        // 4. Actualizar descripción
        ActualizarDonacionRequest actualizarReq = new ActualizarDonacionRequest("Descripción actualizada");
        DonacionHistorialDTO actualizada = donacionService.actualizarDonacion(creada.getId(), actualizarReq);
        assertEquals("Descripción actualizada", actualizada.getDescripcion());

        // 5. Eliminar
        donacionService.eliminarDonacion(creada.getId());
        assertTrue(donacionService.obtenerTodas().isEmpty());
        assertThrows(IllegalArgumentException.class, () -> donacionService.obtenerPorId(creada.getId()));
    }
}
