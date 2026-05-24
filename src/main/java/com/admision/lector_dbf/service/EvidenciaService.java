package com.admision.lector_dbf.service;

import com.admision.lector_dbf.entity.EvidenciaAnulacion;
import com.admision.lector_dbf.entity.Examen;
import com.admision.lector_dbf.repository.EvidenciaAnulacionRepository;
import com.admision.lector_dbf.repository.ExamenRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class EvidenciaService {

    @Autowired
    private EvidenciaAnulacionRepository evidenciaRepository;

    @Value("${ruta.evidencias}")
    private String rutaEvidencias;

    public void guardarEvidencia(
            MultipartFile archivo,
            Examen examen
    ) throws Exception {

        if (archivo.isEmpty()) {
            return;
        }

        String nombreOriginal = archivo.getOriginalFilename();

        String extension =
                nombreOriginal.substring(
                        nombreOriginal.lastIndexOf(".")
                );

        String nombreArchivo = UUID.randomUUID() + extension;

        Path ruta = Paths.get(rutaEvidencias);
        Files.createDirectories(ruta);
        Path rutaCompleta = ruta.resolve(nombreArchivo);

        Files.copy(
                archivo.getInputStream(),
                rutaCompleta
        );

        EvidenciaAnulacion evidencia = new EvidenciaAnulacion();

        evidencia.setNombreOriginal(
                nombreOriginal
        );
        evidencia.setNombreArchivo(
                nombreArchivo
        );
        evidencia.setRutaArchivo(
                rutaCompleta.toString()
        );
        evidencia.setFechaSubida(
                LocalDateTime.now()
        );

        evidencia.setExamen(examen);
        evidenciaRepository.save(evidencia);
    }

    @Transactional
    public Long eliminarEvidencia(
            Long evidenciaId
    ) throws Exception {

        EvidenciaAnulacion evidencia =
                evidenciaRepository
                        .findById(evidenciaId)
                        .orElseThrow();

        Long procesoId =
                evidencia.getExamen()
                        .getProcesoAdmision()
                        .getId();

        Path path =
                Paths.get(
                        evidencia.getRutaArchivo()
                );

        Files.deleteIfExists(path);

        evidenciaRepository.delete(evidencia);

        return procesoId;
    }
}
