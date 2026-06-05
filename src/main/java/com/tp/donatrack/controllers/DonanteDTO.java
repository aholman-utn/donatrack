package com.tp.donatrack.controllers;
import com.tp.donatrack.domain.persona.PersonaHumana;
import com.tp.donatrack.domain.persona.PersonaJuridica;
import com.tp.donatrack.domain.notificador.TipoNotificador;
import com.tp.donatrack.domain.persona.PersonaRepresentante;
import com.tp.donatrack.domain.persona.TipoOrganizacion;
import com.tp.donatrack.domain.ubicacion.Ciudad;
import com.tp.donatrack.domain.ubicacion.Direccion;
import com.tp.donatrack.domain.ubicacion.Pais;
import com.tp.donatrack.domain.ubicacion.Provincia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;
import java.util.List;
import java.util.Map;

public class DonanteDTO {

    public static class CrearDonanteHumanoRequest {

        @NotBlank(message = "El nombre es obligatorio")
        private String nombre;

        @NotBlank(message = "El apellido es obligatorio")
        private String apellido;

        private String genero;
        private Date fechaNacimiento;
        private int edad;
        private String nroDocumento;

        private DireccionRequest direccion;
        private List<ContactoValorRequest> mediosDeContacto;
        private Map<String, String> medioPredeterminado;


        public PersonaHumana toDomain() {
            PersonaHumana p = new PersonaHumana();
            p.setNombre(nombre);
            p.setApellido(apellido);
            p.setGenero(genero);
            p.setFechaNacimiento(fechaNacimiento);
            p.setEdad(edad);
            p.setNroDocumento(nroDocumento);
            p.setMedioPredeterminado(medioPredeterminado);

            if (direccion != null)        p.setDireccion(direccion.toDomain());
            if (mediosDeContacto != null) {
                Map<String, List<String>> mediosMap = new java.util.HashMap<>();
                for (ContactoValorRequest cr : mediosDeContacto) {
                    mediosMap.computeIfAbsent(cr.getMedio().name(), k -> new java.util.ArrayList<>()).add(cr.getValor());
                }
                p.setMedioDeContacto(mediosMap);
            }
            return p;
        }

        // Getters y setters
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public String getGenero() { return genero; }
        public void setGenero(String genero) { this.genero = genero; }
        public Date getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(Date fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
        public int getEdad() { return edad; }
        public void setEdad(int edad) { this.edad = edad; }
        public String getNroDocumento() { return nroDocumento; }
        public void setNroDocumento(String nroDocumento) { this.nroDocumento = nroDocumento; }
        public DireccionRequest getDireccion() { return direccion; }
        public void setDireccion(DireccionRequest direccion) { this.direccion = direccion; }
        public List<ContactoValorRequest> getMediosDeContacto() { return mediosDeContacto; }
        public void setMediosDeContacto(List<ContactoValorRequest> mediosDeContacto) { this.mediosDeContacto = mediosDeContacto; }
        public Map<String, String> getMedioPredeterminado() { return medioPredeterminado; }
        public void setMedioPredeterminado(Map<String, String> medioPredeterminado) { this.medioPredeterminado = medioPredeterminado; }
    }

    // -------------------------------------------------------------------------
    // REQUEST: creación de un donante jurídico
    // -------------------------------------------------------------------------

    public static class CrearDonanteJuridicoRequest {

        @NotBlank(message = "La razón social es obligatoria")
        private String razonSocial;

        @NotNull(message = "El tipo de organización es obligatorio")
        private TipoOrganizacion tipo;

        private String rubro;
        private List<RepresentanteRequest> representantes;
        private DireccionRequest direccion;
        private List<ContactoValorRequest> mediosDeContacto;
        private Map<String, String> medioPredeterminado;

        public PersonaJuridica toDomain() {
            PersonaJuridica p = new PersonaJuridica();
            p.setRazonSocial(razonSocial);
            p.setTipo(tipo);
            p.setRubro(rubro);
            p.setMedioPredeterminado(medioPredeterminado);

            if (direccion != null) p.setDireccion(direccion.toDomain());
            if (mediosDeContacto != null) {
                Map<String, List<String>> mediosMap = new java.util.HashMap<>();
                for (ContactoValorRequest cr : mediosDeContacto) {
                    mediosMap.computeIfAbsent(cr.getMedio().name(), k -> new java.util.ArrayList<>()).add(cr.getValor());
                }
                p.setMedioDeContacto(mediosMap);
            }
            if (representantes != null)
                p.setPersonasRepresentantes(representantes.stream()
                        .map(RepresentanteRequest::toDomain).toList());
            return p;
        }

        public String getRazonSocial() { return razonSocial; }
        public void setRazonSocial(String razonSocial) { this.razonSocial = razonSocial; }
        public TipoOrganizacion getTipo() { return tipo; }
        public void setTipo(TipoOrganizacion tipo) { this.tipo = tipo; }
        public String getRubro() { return rubro; }
        public void setRubro(String rubro) { this.rubro = rubro; }
        public List<RepresentanteRequest> getRepresentantes() { return representantes; }
        public void setRepresentantes(List<RepresentanteRequest> representantes) { this.representantes = representantes; }
        public DireccionRequest getDireccion() { return direccion; }
        public void setDireccion(DireccionRequest direccion) { this.direccion = direccion; }
        public List<ContactoValorRequest> getMediosDeContacto() { return mediosDeContacto; }
        public void setMediosDeContacto(List<ContactoValorRequest> mediosDeContacto) { this.mediosDeContacto = mediosDeContacto; }
        public Map<String, String> getMedioPredeterminado() { return medioPredeterminado; }
        public void setMedioPredeterminado(Map<String, String> medioPredeterminado) { this.medioPredeterminado = medioPredeterminado; }
    }

    // -------------------------------------------------------------------------
    // Nested DTOs auxiliares
    // -------------------------------------------------------------------------

    public static class DireccionRequest {
        private String calle1;
        private String calle2;
        private int altura;
        private boolean sinAltura;
        private int piso;
        private int cuerpo;
        private String departamento;
        private String ciudadNombre;
        private String provinciaNombre;
        private String paisNombre;

        public Direccion toDomain() {
            Direccion d = new Direccion();
            d.setCalle1(calle1);
            d.setCalle2(calle2);
            d.setAltura(altura);
            d.setSinAltura(sinAltura);
            d.setPiso(piso);
            d.setCuerpo(cuerpo);
            d.setDepartamento(departamento);

            if (paisNombre != null) {
                Pais pais = new Pais(paisNombre);
                Provincia provincia = new Provincia(provinciaNombre, pais);
                d.setCiudad(new Ciudad(ciudadNombre, provincia));
            }
            return d;
        }

        public String getCalle1() { return calle1; }
        public void setCalle1(String calle1) { this.calle1 = calle1; }
        public String getCalle2() { return calle2; }
        public void setCalle2(String calle2) { this.calle2 = calle2; }
        public int getAltura() { return altura; }
        public void setAltura(int altura) { this.altura = altura; }
        public boolean isSinAltura() { return sinAltura; }
        public void setSinAltura(boolean sinAltura) { this.sinAltura = sinAltura; }
        public int getPiso() { return piso; }
        public void setPiso(int piso) { this.piso = piso; }
        public int getCuerpo() { return cuerpo; }
        public void setCuerpo(int cuerpo) { this.cuerpo = cuerpo; }
        public String getDepartamento() { return departamento; }
        public void setDepartamento(String departamento) { this.departamento = departamento; }
        public String getCiudadNombre() { return ciudadNombre; }
        public void setCiudadNombre(String ciudadNombre) { this.ciudadNombre = ciudadNombre; }
        public String getProvinciaNombre() { return provinciaNombre; }
        public void setProvinciaNombre(String provinciaNombre) { this.provinciaNombre = provinciaNombre; }
        public String getPaisNombre() { return paisNombre; }
        public void setPaisNombre(String paisNombre) { this.paisNombre = paisNombre; }
    }

    public static class ContactoValorRequest {
        private TipoNotificador medio;
        private String valor;

        public TipoNotificador getMedio() { return medio; }
        public void setMedio(TipoNotificador medio) { this.medio = medio; }
        public String getValor() { return valor; }
        public void setValor(String valor) { this.valor = valor; }
    }

    public static class RepresentanteRequest {
        private String nombre;
        private String apellido;
        private int nroDocumento;

        public PersonaRepresentante toDomain() {
            return new PersonaRepresentante(nombre, apellido, nroDocumento);
        }

        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public int getNroDocumento() { return nroDocumento; }
        public void setNroDocumento(int nroDocumento) { this.nroDocumento = nroDocumento; }
    }
}
