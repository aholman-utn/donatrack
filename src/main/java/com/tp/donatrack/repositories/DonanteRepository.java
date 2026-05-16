package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.entidad.Donante;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DonanteRepository {

    //por ahora, en memoria
    private List<Donante> donantes;
    public DonanteRepository() {
        this.donantes = new ArrayList<>();
    }

    //TODO: este seria el repo
    public Donante darDeAlta(Donante donante){
        this.donantes.add(donante);
        return donante;
    }

    public Donante buscarDonante(String email) {
        return this.donantes.stream()
                .filter(donante ->
                        email.equals(
                                donante.getPersona()
                                        .getMediosDeContacto()
                                        .get("email")
                        )
                )
                .findAny()
                .orElse(null);
    }

    public List<Donante> findAll(){
        return this.donantes;
    }

}
