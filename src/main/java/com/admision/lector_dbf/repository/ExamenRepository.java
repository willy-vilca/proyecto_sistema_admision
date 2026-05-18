package com.admision.lector_dbf.repository;

import com.admision.lector_dbf.entity.Examen;
import com.admision.lector_dbf.entity.ProcesoAdmision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ExamenRepository
        extends JpaRepository<Examen, Long> {
    @Query("""
        SELECT e
        FROM Examen e
        JOIN FETCH e.alumno a
        LEFT JOIN FETCH a.carrera
        WHERE e.procesoAdmision = :proceso
        ORDER BY e.puntaje DESC
    """)
    List<Examen> findExamenesConAlumnoYCarrera(
            @Param("proceso")
            ProcesoAdmision proceso
    );
}
