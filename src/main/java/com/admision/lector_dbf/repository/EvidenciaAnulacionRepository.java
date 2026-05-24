package com.admision.lector_dbf.repository;

import com.admision.lector_dbf.entity.EvidenciaAnulacion;
import com.admision.lector_dbf.entity.Examen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvidenciaAnulacionRepository
        extends JpaRepository<EvidenciaAnulacion, Long> {
    List<EvidenciaAnulacion> findByExamen(
            Examen examen
    );
}