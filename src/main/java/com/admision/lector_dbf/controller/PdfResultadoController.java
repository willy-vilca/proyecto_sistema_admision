package com.admision.lector_dbf.controller;

import com.admision.lector_dbf.service.PdfResultadoService;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pdf")
public class PdfResultadoController {

    private final PdfResultadoService pdfResultadoService;

    public PdfResultadoController(PdfResultadoService pdfResultadoService) {
        this.pdfResultadoService = pdfResultadoService;
    }

    @GetMapping("/resultados/{procesoId}")
    public ResponseEntity<InputStreamResource>
    descargarPdf(@PathVariable Long procesoId) {

        InputStreamResource file = new InputStreamResource(
                pdfResultadoService.generarPdfResultados(procesoId)
        );

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=resultados.pdf"
                )
                .contentType(MediaType.APPLICATION_PDF)
                .body(file);
    }
}
