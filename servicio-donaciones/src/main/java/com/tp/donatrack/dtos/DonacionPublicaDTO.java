package com.tp.donatrack.dtos;

import java.time.LocalDateTime;

/**
 * Vista pública de una donación entregada, para la landing y el mapa.
 * No incluye datos personales del donante: solo la entidad que la recibió
 * (que es pública) y qué se entregó.
 *
 * @param latitud  nula si la dirección de la entidad no tiene coordenadas
 * @param longitud nula si la dirección de la entidad no tiene coordenadas
 */
public record DonacionPublicaDTO(
        Long id,
        String categoria,
        String subCategoria,
        int cantidad,
        String unidad,
        String entidad,
        String direccion,
        Double latitud,
        Double longitud,
        LocalDateTime fechaEntrega
) {}
