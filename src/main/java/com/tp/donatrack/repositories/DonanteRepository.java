package com.tp.donatrack.repositories;

import com.tp.donatrack.domain.entidad.Donante;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class DonanteRepository {

    //por ahora, en memoria
    private List<Donante> donantes;
    private Integer id;
    
    public DonanteRepository() {
        this.donantes = new ArrayList<>();
        this.id = this.donantes.size() + 1;
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
