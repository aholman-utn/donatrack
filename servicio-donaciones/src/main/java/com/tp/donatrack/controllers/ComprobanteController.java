package com.tp.donatrack.controllers;

import com.tp.donatrack.domain.donacion.ComprobanteRecepcionDonacion;
import com.tp.donatrack.repositories.ComprobanteRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/api/comprobantes")
public class ComprobanteController {

    private final ComprobanteRepository comprobanteRepository;

    public ComprobanteController(ComprobanteRepository comprobanteRepository) {
        this.comprobanteRepository = comprobanteRepository;
    }

    @GetMapping("/{id}")
    public String verComprobante(@PathVariable String id, Model model) {
        ComprobanteRecepcionDonacion comprobante = comprobanteRepository.findById(id);

        if (comprobante == null) {
            return "error/404";
        }

        model.addAttribute("comprobante", comprobante);

        return "comprobante";
    }

    @GetMapping
    @ResponseBody
    public List<ComprobanteRecepcionDonacion> listarComprobantes() {
        return comprobanteRepository.findAll();
    }
}