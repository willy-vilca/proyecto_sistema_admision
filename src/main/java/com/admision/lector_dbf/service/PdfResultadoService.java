package com.admision.lector_dbf.service;

import com.admision.lector_dbf.entity.Carrera;
import com.admision.lector_dbf.entity.Examen;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface PdfResultadoService {

    ByteArrayInputStream generarPdfResultados(Long procesoId);

}
