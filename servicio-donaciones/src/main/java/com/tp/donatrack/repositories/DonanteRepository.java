package com.tp.donatrack.repositories;
import com.tp.donatrack.domain.donante.Donante;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class DonanteRepository {

    //por ahora, en memoria
    private static final AtomicLong ID_GENERATOR = new AtomicLong(1);
    private final List<Donante> donantes;
    public DonanteRepository() {
        this.donantes = new ArrayList<>();
    }

    public Donante create(Donante donante){
        if (donante.getPersona() != null && donante.getPersona().getId() == null) {
            donante.getPersona().setId(com.tp.donatrack.domain.persona.Persona.nextId());
            donante.setDonanteId(ID_GENERATOR.getAndIncrement());
        }
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

    public Donante findById(Long id) {
        return this.donantes.stream()
                .filter(donante -> donante.getPersona() != null && donante.getPersona().getId() != null && donante.getPersona().getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    //TODO
    //public Donante update(Integer id){ return new Donante(); }

    public List<Donante> findAll(){
        return this.donantes;
    }

    public void delete(Donante donante) {
        donantes.remove(donante);
    }

    public void clear() {
        this.donantes.clear();
        com.tp.donatrack.domain.persona.Persona.resetIdGenerator();
    }

}