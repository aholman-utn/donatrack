    package com.tp.donatrack.domain.donante;

    import com.tp.commons.domain.donantes.Nivel;
    import com.tp.donatrack.domain.bien.CategoriaBien;
    import com.tp.donatrack.domain.persona.PersonaHumana;
    import com.tp.donatrack.domain.persona.PersonaJuridica;
    import org.junit.jupiter.api.Assertions;
    import org.junit.jupiter.api.DisplayName;
    import org.junit.jupiter.api.Test;

    public class DonanteTest {

        @Test
        @DisplayName("Un donante debe poder vincularse con una persona humana correctamente")
        public void donanteDebeTenerUnaPersonaAsociada() {
            Donante donante = new Donante();
            PersonaHumana persona = new PersonaHumana();
            persona.setNombre("Leandro");
            persona.setApellido("Perez");
            persona.setId(1L);

            donante.setPersona(persona);

            Assertions.assertEquals(1L, donante.getPersona().getId());
            Assertions.assertNotNull(donante.getPersona(), "La persona no debería ser nula");
            Assertions.assertEquals("Leandro", ((PersonaHumana) donante.getPersona()).getNombre());
        }

        @Test
        @DisplayName("El ID del donante debe actualizarse mediante el setter de la persona")
        public void elIdDelDonanteDebeSerModificable() {
            Donante donante = new Donante();
            PersonaHumana persona = new PersonaHumana();
            donante.setPersona(persona);
            donante.getPersona().setId(500L);

            Assertions.assertEquals(500L, donante.getPersona().getId());
        }

        @Test
        @DisplayName("Debe retornar el nombre completo del donante según el tipo de persona")
        public void testGetNombreCompleto() {
            // Persona Humana
            PersonaHumana ph = new PersonaHumana();
            ph.setNombre("Juan");
            ph.setApellido("Pérez");
            Donante donanteHumano = new Donante(ph);
            Assertions.assertEquals("Juan Pérez", donanteHumano.getNombreCompleto());

            // Persona Jurídica
            PersonaJuridica pj = new PersonaJuridica();
            pj.setRazonSocial("Fundación Ayuda");
            Donante donanteJuridico = new Donante(pj);
            Assertions.assertEquals("Fundación Ayuda", donanteJuridico.getNombreCompleto());

            // Caso Anónimo (persona nula)
            Donante donanteAnonimo = new Donante();
            Assertions.assertEquals("Donante Anónimo", donanteAnonimo.getNombreCompleto());
        }

        @Test
        @DisplayName("El perfil del donante debe inicializarse de forma predeterminada en todas las vías de creación")
        public void testPerfilInicializacionPredeterminada() {
            // Constructor vacío
            Donante d1 = new Donante();
            Assertions.assertNotNull(d1.getPerfil());
            Assertions.assertEquals(Nivel.COLABORADOR, d1.getPerfil().getNivelDonante());
            Assertions.assertTrue(d1.getPerfil().isVisibilidadInsignia());

            // Constructor con persona
            PersonaHumana ph = new PersonaHumana();
            Donante d2 = new Donante(ph);
            Assertions.assertNotNull(d2.getPerfil());
            Assertions.assertEquals(Nivel.COLABORADOR, d2.getPerfil().getNivelDonante());
            Assertions.assertTrue(d2.getPerfil().isVisibilidadInsignia());

            // Patrón Builder
            Donante d3 = Donante.builder().build();
            Assertions.assertNotNull(d3.getPerfil());
            Assertions.assertEquals(Nivel.COLABORADOR, d3.getPerfil().getNivelDonante());
            Assertions.assertTrue(d3.getPerfil().isVisibilidadInsignia());
        }

        @Test
        @DisplayName("El nivel del donante debe poder ser actualizado externamente (ej. por respuesta de Incentivos)")
        public void testActualizarNivelDonante() {
            PerfilDonante perfil = new PerfilDonante();

            // Comprobamos el estado inicial
            Assertions.assertEquals(Nivel.COLABORADOR, perfil.getNivelDonante(), "Debe nacer siendo COLABORADOR");

            // Simulamos que el motor de incentivos determina que subió de nivel
            perfil.setNivelDonante(Nivel.SOSTENEDOR);
            Assertions.assertEquals(Nivel.SOSTENEDOR, perfil.getNivelDonante(), "El perfil debe guardar el nuevo nivel");

            // Simulamos un nuevo ascenso tras completar más misiones
            perfil.setNivelDonante(Nivel.TRANSFORMADOR);
            Assertions.assertEquals(Nivel.TRANSFORMADOR, perfil.getNivelDonante(), "El perfil debe guardar el nivel máximo");
        }
    }
