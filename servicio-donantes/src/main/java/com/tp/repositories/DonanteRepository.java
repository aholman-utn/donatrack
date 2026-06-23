package com.tp.repositories;

import com.tp.domain.donante.Donante;
import com.tp.domain.donante.persona.Persona;
import com.tp.domain.donante.persona.PersonaHumana;
import com.tp.domain.donante.persona.PersonaJuridica;
import com.tp.domain.donante.persona.TipoPersona;
import com.tp.dtos.input.DonanteFiltroDTO;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

@Repository
public class DonanteRepository {
    //por ahora, en memoria
    private List<Donante> donantes;
    public DonanteRepository() {
        this.donantes = new ArrayList<>();
    }

    private final AtomicInteger secuencia = new AtomicInteger(1);

    public Donante create(Donante donante){
        donante.setId((long) secuencia.getAndIncrement()); //agrego el ID del donante
        donante.getPersona().setId((long) secuencia.getAndIncrement());
        this.donantes.add(donante);
        return donante;
    }

    public Donante findByEmail(String email) {
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
                .filter(donante -> donante.getId() != null && donante.getId().equals(id))
                .findFirst()
                .orElse(null);
    }

    //TODO:esta parte la hacia emilio
    public Donante update(Donante donante) {
        return null;
    }

    public List<Donante> findAll(DonanteFiltroDTO filtro) {

        Stream<Donante> stream = donantes.stream();

        if (filtro != null) {

            if (filtro.getEmail() != null) {
                stream = stream.filter(
                        d -> coincideEmail(d, filtro.getEmail())
                );
            }

            if (filtro.getTipoPersona() != null) {
                stream = stream.filter(
                        d -> coincideTipoPersona(d, filtro.getTipoPersona())
                );
            }

            if (filtro.getFechaUltimaInteraccion() != null) {
                stream = stream.filter(
                        d -> Objects.equals(
                                d.getFechaUltimaInteraccion(),
                                filtro.getFechaUltimaInteraccion()
                        )
                );
            }
        }

        return stream.toList();
    }

    private boolean coincideEmail(Donante donante,String email) {

        List<String> emails = donante.getPersona()
                .getMedioDeContacto()
                .get("email");

        return emails != null && emails.stream().anyMatch(e ->e.equalsIgnoreCase(email));
    }

    private boolean coincideTipoPersona(Donante donante,TipoPersona tipoPersona) {

        Persona persona = donante.getPersona();

        return switch (tipoPersona) {
            case HUMANA -> persona instanceof PersonaHumana;
            case JURIDICA -> persona instanceof PersonaJuridica;
        };
    }

    public Donante delete(Long id) {

        Iterator<Donante> iterator = donantes.iterator();

        while (iterator.hasNext()) {
            Donante donante = iterator.next();

            if (Objects.equals(donante.getId(), id)) {
                iterator.remove();
                return donante;
            }
        }

        throw new RuntimeException("Donante no encontrado");
    }

    public void clear() {
        this.donantes.clear();
        this.secuencia.set(1);
    }
}
