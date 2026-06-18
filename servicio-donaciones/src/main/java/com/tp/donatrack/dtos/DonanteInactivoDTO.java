package com.tp.donatrack.dtos;

import com.tp.donatrack.domain.notificador.TipoNotificador;

public class DonanteInactivoDTO {
    
    private Integer id;
    private String contacto; 
    private TipoNotificador tipoNotificadorPreferido; 

    public DonanteInactivoDTO() {}

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getContacto() {
        return contacto;
    }

    public void setContacto(String contacto) {
        this.contacto = contacto;
    }

    public TipoNotificador getTipoNotificadorPreferido() {
        return tipoNotificadorPreferido;
    }

    public void setTipoNotificadorPreferido(TipoNotificador tipoNotificadorPreferido) {
        this.tipoNotificadorPreferido = tipoNotificadorPreferido;
    }
}