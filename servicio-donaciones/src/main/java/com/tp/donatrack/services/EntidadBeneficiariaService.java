package com.tp.donatrack.services;

import com.tp.donatrack.domain.entidad.EntidadBeneficiaria;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import com.tp.donatrack.repositories.EntidadBeneficiariaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EntidadBeneficiariaService {
    private final EntidadBeneficiariaRepository entidadBeneficiariaRepository;

    public EntidadBeneficiariaService(EntidadBeneficiariaRepository entidadBeneficiariaRepository) {
        this.entidadBeneficiariaRepository = entidadBeneficiariaRepository;
    }

    // CREATE
    public EntidadBeneficiaria registrar(PersonaJuridica personaJuridica) {
        EntidadBeneficiaria entidadBeneficiaria = new EntidadBeneficiaria(personaJuridica);
        return entidadBeneficiariaRepository.create(entidadBeneficiaria);
    }

    // READ
    public List<EntidadBeneficiaria> listarTodos() {
        return entidadBeneficiariaRepository.findAll();
    }

    public EntidadBeneficiaria buscarEntidad(Integer id) {
        return entidadBeneficiariaRepository.find(id);
    }

    // UPDATE
    public EntidadBeneficiaria actualizarDatosPerosnales(Integer id, PersonaJuridica personaJuridica) {
        EntidadBeneficiaria entidad = buscarEntidad(id);
        entidad.setDatosDeEntidad(personaJuridica);
        return entidadBeneficiariaRepository.create(entidad);
    }

    // DELETE
    public void eliminar(Integer id) {
        EntidadBeneficiaria entidadAeliminar = buscarEntidad(id);
        if (entidadAeliminar != null) {
            entidadBeneficiariaRepository.delete(entidadAeliminar);
        }
    }

}
