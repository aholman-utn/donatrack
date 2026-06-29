package com.tp.donatrack.repositories;
import com.tp.donatrack.domain.donante.Donante;
import com.tp.donatrack.services.HttpDonacionEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(DonanteRepository.class);

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

    public void update(Donante donanteActualizado) {
        for (int i = 0; i < donantes.size(); i++) {
            if (donantes.get(i).getPersona().getId().equals(donanteActualizado.getPersona().getId())) {
                donantes.set(i, donanteActualizado);
                logger.info(">>> REPOSITORIO: Donante ID {} reemplazado exitosamente en la lista.", donanteActualizado.getPersona().getId());
                return;
            }
        }
        logger.warn(">>> REPOSITORIO: No se pudo actualizar, ID {} no encontrado en la lista.", donanteActualizado.getPersona().getId());
    }

}