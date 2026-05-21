package com.tp.donatrack.repositories;
import com.tp.donatrack.domain.donante.Donante;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
                .filter(donante -> {
                    Map<String,List<String>> medios = donante.getPersona().getMedioDeContacto();
                    List<String> emails = medios.get("email");

                    return emails != null && emails.contains(email);
                })
                .findFirst()
                .orElse(null);
    }

    public List<Donante> findAll(){
        return this.donantes;
    }

}