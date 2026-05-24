package com.admision.lector_dbf.controller;

import com.admision.lector_dbf.entity.Carrera;
import com.admision.lector_dbf.entity.EvidenciaAnulacion;
import com.admision.lector_dbf.entity.Examen;
import com.admision.lector_dbf.entity.ProcesoAdmision;
import com.admision.lector_dbf.repository.EvidenciaAnulacionRepository;
import com.admision.lector_dbf.repository.ExamenRepository;
import com.admision.lector_dbf.repository.CarreraRepository;
import com.admision.lector_dbf.repository.ProcesoAdmisionRepository;
import com.admision.lector_dbf.service.EvidenciaService;
import com.admision.lector_dbf.service.ProcesamientoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.List;
import java.util.Map;

@Controller
public class ProcesamientoController {

    @Autowired
    private ProcesamientoService procesamientoService;

    @Autowired
    private EvidenciaService evidenciaService;

    @Autowired
    private ProcesoAdmisionRepository procesoAdmisionRepository;

    @Autowired
    private ExamenRepository examenRepository;

    @Autowired
    private EvidenciaAnulacionRepository evidenciaRepository;

    @Autowired
    private CarreraRepository carreraRepository;

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

        List<Carrera> carreras =
                carreraRepository.findAll();

        model.addAttribute(
                "carreras",
                carreras
        );
        return "proceso-detalle";
    }

    @PostMapping("/anular/{id}")
    public String anularExamen(
            @PathVariable Long id,
            @RequestParam String motivo,
            @RequestParam(
                    value = "imagenes",
                    required = false
            )
            List<MultipartFile> imagenes
    ) throws Exception {
        Examen examen =
                examenRepository
                        .findById(id)
                        .orElseThrow();

        examen.setAnulado(true);

        examen.setMotivoAnulacion(motivo);

        examen.setOrdenMerito(null);

        examenRepository.save(examen);

        if (imagenes != null) {
            for (MultipartFile imagen : imagenes) {
                if (!imagen.isEmpty()) {
                    evidenciaService.guardarEvidencia(
                            imagen,
                            examen
                    );
                }
            }
        }

        procesamientoService.recalcularOrdenMerito(examen.getProcesoAdmision());
        procesamientoService.recalcularIngresantes(examen.getProcesoAdmision());

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

        procesamientoService.recalcularOrdenMerito(examen.getProcesoAdmision());
        procesamientoService.recalcularIngresantes(examen.getProcesoAdmision());

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

    @PostMapping("/actualizar-vacantes")
    public String actualizarVacantes(
            @RequestParam Long procesoId,
            @RequestParam Map<String, String> vacantesMap
    ) {

        procesamientoService.actualizarVacantes(
                procesoId,
                vacantesMap
        );

        return "redirect:/proceso/" + procesoId;
    }

    @GetMapping("/evidencia/{id}")
    @ResponseBody
    public ResponseEntity<Resource> verEvidencia(
            @PathVariable Long id
    ) throws Exception {

        EvidenciaAnulacion evidencia =
                evidenciaRepository
                        .findById(id)
                        .orElseThrow();

        Path path =
                Paths.get(
                        evidencia.getRutaArchivo()
                );

        Resource resource =
                new UrlResource(
                        path.toUri()
                );

        return ResponseEntity.ok()
                .contentType(
                        MediaType.IMAGE_JPEG
                )
                .body(resource);
    }

    @PostMapping("/evidencia/eliminar/{id}")
    public String eliminarEvidencia(
            @PathVariable Long id
    ) throws Exception {

        Long procesoId =
                evidenciaService
                        .eliminarEvidencia(id);

        return "redirect:/proceso/" + procesoId;
    }

    @PostMapping("/evidencia/agregar/{examenId}")
    public String agregarEvidencias(
            @PathVariable Long examenId,
            @RequestParam("imagenes")
            List<MultipartFile> imagenes
    ) throws Exception {

        Examen examen =
                examenRepository
                        .findById(examenId)
                        .orElseThrow();

        for (MultipartFile imagen : imagenes) {
            if (!imagen.isEmpty()) {
                evidenciaService.guardarEvidencia(
                        imagen,
                        examen
                );
            }
        }

        return "redirect:/proceso/" + examen.getProcesoAdmision().getId();
    }

}