package com.tp.donatrack.repositories;
import com.tp.donatrack.domain.donante.Donante;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class DonanteRepository {

    //por ahora, en memoria
    private final List<Donante> donantes;
    public DonanteRepository() {
        this.donantes = new ArrayList<>();
    }

    private final AtomicInteger secuencia = new AtomicInteger(1);

    public Donante create(Donante donante){
        donante.setId(secuencia.getAndIncrement()); //agrego el ID del donante
        this.donantes.add(donante);
        return donante;
    }

    public Donante find(String email) {
        return this.donantes.stream()
                .filter(donante -> {
                    Map<String,List<String>> medios = donante.getPersona().getMedioDeContacto();
                    List<String> emails = medios.get("EMAIL");
                    if (emails == null) {
                        emails = medios.get("email");
                    }
                    return emails != null && emails.contains(email);
                })
                .findFirst()
                .orElse(null);
    }


    public List<Donante> findAll(){
        return this.donantes;
    }

    public void delete(Donante donante) {
        donantes.remove(donante);
    }

}