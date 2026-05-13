package com.tp.donatrack.controllers;

import com.tp.donatrack.ImportacionResponseDTO;
import com.tp.donatrack.services.DonanteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/donantes")
public class DonanteController {

    private final DonanteService service;

    public DonanteController(DonanteService service){
        this.service = service;
    }

    @PostMapping("/importar")
    public ResponseEntity<ImportacionResponseDTO> importarCSV(@RequestParam("file") MultipartFile csv) {

        ImportacionResponseDTO response = service.importarCSV(csv);

        return ResponseEntity.ok(response);
    }
}