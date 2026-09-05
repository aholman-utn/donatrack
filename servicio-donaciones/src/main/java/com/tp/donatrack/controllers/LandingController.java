package com.tp.donatrack.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Redirige la raíz del sitio a la landing page estática (bocetos).
 * La landing se sirve desde src/main/resources/static/landing/.
 */
@Controller
public class LandingController {

    @GetMapping("/")
    public String home() {
        return "redirect:/landing/index.html";
    }
}
