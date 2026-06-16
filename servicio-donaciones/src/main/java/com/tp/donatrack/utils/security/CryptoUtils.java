package com.tp.donatrack.utils.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class CryptoUtils {

    // Creamos una instancia única y estática del encoder de Spring para reutilizarla
    private static final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    // Constructor privado para evitar que alguien intente hacer "new CryptoUtils()" por error
    private CryptoUtils() {
        throw new IllegalStateException("Clase utilitaria - No debe ser instanciada");
    }

    public static String hashear(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña a hashear no puede estar vacía");
        }
        return encoder.encode(password);
    }


    public static boolean verificarPassword(String passwordPlano, String passwordHasheado) {
        if (passwordPlano == null || passwordHasheado == null) {
            return false;
        }
        return encoder.matches(passwordPlano, passwordHasheado);
    }
}