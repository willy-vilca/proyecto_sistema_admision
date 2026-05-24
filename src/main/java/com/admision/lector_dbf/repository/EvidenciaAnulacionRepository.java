package com.admision.lector_dbf.repository;

import com.admision.lector_dbf.entity.EvidenciaAnulacion;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenciaAnulacionRepository
        extends JpaRepository<EvidenciaAnulacion, Long> {
}