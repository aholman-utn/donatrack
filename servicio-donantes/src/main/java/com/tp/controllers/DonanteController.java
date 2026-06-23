package com.tp.controllers;

import com.tp.domain.donante.Donante;
import com.tp.dtos.input.ActualizarDonanteInputDTO;
import com.tp.dtos.input.CrearDonanteInputDTO;
import com.tp.dtos.input.DonanteFiltroDTO;
import com.tp.dtos.output.DonanteOutputDTO;
import com.tp.dtos.output.ImportacionOutputDTO;
import com.tp.services.DonanteService;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/donantes")
public class DonanteController {

    private final DonanteService donanteService;

    public DonanteController(DonanteService donanteService){
        this.donanteService = donanteService;
    }

    @GetMapping
    public List<DonanteOutputDTO> findAll(DonanteFiltroDTO dto){
        return this.donanteService.findAll(dto);
    }

    @GetMapping("/{id}")
    public DonanteOutputDTO find(@PathVariable Long id){
        return this.donanteService.find(id);
    }

    @PostMapping("/")
    public DonanteOutputDTO create(@RequestBody CrearDonanteInputDTO dto){
        return this.donanteService.create(dto);
    }

    @PatchMapping("/{id}")
    public DonanteOutputDTO update(@RequestBody ActualizarDonanteInputDTO dto, @PathVariable Long id){
        return this.donanteService.update(dto);
    }

    @DeleteMapping("/{id}")
    public DonanteOutputDTO delete(@PathVariable Long id){
        return this.donanteService.delete(id);
    }

    @PostMapping("/importar")
    public ImportacionOutputDTO importar(@RequestBody MultipartFile archivo){
        return this.donanteService.importar(archivo);
    }
}
