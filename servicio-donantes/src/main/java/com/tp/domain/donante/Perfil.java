package com.tp.domain.donante;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Perfil {
    private String nombre;
    private Integer posicionEnElRanking;
    //TODO: lo de las insignias, la lista de donaciones realizadas, serian entidades de dominio de este servicio? me generar dudas tener esas entidades acá
}
