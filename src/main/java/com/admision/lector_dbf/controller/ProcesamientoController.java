package com.admision.lector_dbf.controller;

import com.admision.lector_dbf.entity.Examen;
import com.admision.lector_dbf.entity.ProcesoAdmision;
import com.admision.lector_dbf.repository.ExamenRepository;
import com.admision.lector_dbf.repository.ProcesoAdmisionRepository;
import com.admision.lector_dbf.service.ProcesamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
public class ProcesamientoController {

    @Autowired
    private ProcesamientoService procesamientoService;

    @Autowired
    private ProcesoAdmisionRepository procesoAdmisionRepository;

    @Autowired
    private ExamenRepository examenRepository;

    @GetMapping("/")
    public String inicio(Model model) {
        List<ProcesoAdmision> procesos =
                procesoAdmisionRepository
                        .findAllByOrderByFechaCreacionDesc();

        model.addAttribute(
                "procesos",
                procesos
        );
        return "index";
    }

    @PostMapping("/procesar")
    public String procesar(
            @RequestParam("nombreProceso")
            String nombreProceso,
            @RequestParam("identifi")
            MultipartFile identifi,
            @RequestParam("respuest")
            MultipartFile respuest,
            @RequestParam("claves")
            MultipartFile claves,
            @RequestParam("puntajeCorrecta")
            Double puntajeCorrecta,
            @RequestParam("puntajeIncorrecta")
            Double puntajeIncorrecta,
            @RequestParam("puntajeBlanca")
            Double puntajeBlanca,
            Model model
    ) {
        try {
            procesamientoService
                    .procesarArchivos(
                            nombreProceso,
                            identifi,
                            respuest,
                            claves,
                            puntajeCorrecta,
                            puntajeIncorrecta,
                            puntajeBlanca
                    );
            return "redirect:/";
        } catch (Exception e) {
            e.printStackTrace();

            model.addAttribute(
                    "error",
                    e.getMessage()
            );
        }
        return "index";
    }

    @GetMapping("/proceso/{id}")
    public String verProceso(
            @PathVariable Long id,
            Model model
    ) {
        ProcesoAdmision proceso =
                procesoAdmisionRepository
                        .findById(id)
                        .orElseThrow();

        List<Examen> examenes =
                examenRepository
                        .findExamenesConAlumnoYCarrera(
                                proceso
                        );

        model.addAttribute(
                "proceso",
                proceso
        );

        model.addAttribute(
                "examenes",
                examenes
        );
        return "proceso-detalle";
    }

    @PostMapping("/anular/{id}")
    public String anularExamen(
            @PathVariable Long id,
            @RequestParam String motivo
    ) {
        Examen examen =
                examenRepository
                        .findById(id)
                        .orElseThrow();

        examen.setAnulado(true);

        examen.setMotivoAnulacion(motivo);

        examenRepository.save(examen);

        return "redirect:/proceso/"
                + examen.getProcesoAdmision().getId();
    }

    @PostMapping("/desanular/{id}")
    public String desanularExamen(
            @PathVariable Long id
    ) {
        Examen examen =
                examenRepository
                        .findById(id)
                        .orElseThrow();

        examen.setAnulado(false);

        examen.setMotivoAnulacion(null);

        examenRepository.save(examen);

        return "redirect:/proceso/"
                + examen.getProcesoAdmision().getId();
    }

    @PostMapping("/actualizar-motivo/{id}")
    public String actualizarMotivo(
            @PathVariable Long id,
            @RequestParam String motivo
    ) {
        Examen examen =
                examenRepository
                        .findById(id)
                        .orElseThrow();

        examen.setMotivoAnulacion(
                motivo
        );

        examenRepository.save(examen);

        return "redirect:/proceso/"
                + examen.getProcesoAdmision().getId();
    }

}