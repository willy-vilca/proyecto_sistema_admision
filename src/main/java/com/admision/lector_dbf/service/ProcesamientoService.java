package com.admision.lector_dbf.service;

import com.admision.lector_dbf.dto.IdentifiDTO;
import com.admision.lector_dbf.dto.RespuestDTO;
import com.admision.lector_dbf.entity.Examen;
import com.admision.lector_dbf.entity.Carrera;
import com.admision.lector_dbf.repository.*;
import com.admision.lector_dbf.entity.ProcesoAdmision;
import com.admision.lector_dbf.entity.Alumno;
import com.admision.lector_dbf.entity.Clave;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.linuxense.javadbf.DBFReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.*;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.Comparator;

@Service
public class ProcesamientoService {

    @Autowired
    private ExamenRepository examenRepository;
    @Autowired
    private AlumnoRepository alumnoRepository;
    @Autowired
    private ProcesoAdmisionRepository procesoAdmisionRepository;
    @Autowired
    private ClaveRepository claveRepository;
    @Autowired
    private CarreraRepository carreraRepository;

    private final ObjectMapper objectMapper =
            new ObjectMapper();

    public void procesarArchivos(
            String nombreProceso,
            MultipartFile identifiFile,
            MultipartFile respuestFile,
            MultipartFile clavesFile,
            Double puntajeCorrecta,
            Double puntajeIncorrecta,
            Double puntajeBlanca
    ) throws Exception {

        ProcesoAdmision proceso = new ProcesoAdmision();

        proceso.setNombre(nombreProceso);

        proceso = procesoAdmisionRepository.save(proceso);

        Map<String, Map<String, String>> claves =
                leerClaves(
                        clavesFile,
                        proceso
                );

        Map<String, IdentifiDTO> identifiMap =
                leerIdentifi(identifiFile);

        List<RespuestDTO> respuestas =
                leerRespuestas(respuestFile);

        for (RespuestDTO respuesta : respuestas) {

            IdentifiDTO identifi =
                    identifiMap.get(
                            respuesta.getLitho()
                    );

            if (identifi == null) {
                continue;
            }

            Examen examen = new Examen();

            //buscar estudiante en la BD mediante el código de alumno
            String codigoAlumno = identifi.getCodigo();

            Optional<Alumno> alumnoOptional = alumnoRepository.findByCodigo(codigoAlumno);

            Alumno alumno;

            if (alumnoOptional.isPresent()) {
                alumno = alumnoOptional.get();
            } else {
                alumno = new Alumno();
                alumno.setCodigo(codigoAlumno);
                alumno = alumnoRepository.save(alumno);
            }
            examen.setAlumno(alumno);

            examen.setProcesoAdmision(
                    proceso
            );

            examen.setLitho(
                    respuesta.getLitho()
            );

            examen.setTema(
                    identifi.getTema()
            );

            examen.setCorrectas(0);
            examen.setIncorrectas(0);
            examen.setBlancas(0);

            examen.setPuntaje(0.0);

            examen.setAnulado(false);

            String respuestasJson =
                    objectMapper.writeValueAsString(
                            respuesta.getRespuestas()
                    );

            examen.setRespuestasJson(
                    respuestasJson
            );

            Map<String, String> claveTema =
                    claves.get(
                            respuesta.getTema()
                    );

            if (claveTema == null) {

                claveTema =
                        claves.get(
                                identifi.getTema()
                        );
            }

            if (claveTema == null) {

                examen.setAnulado(true);

                examen.setMotivoAnulacion(
                        "El alumno no digitó el tema del examen"
                );

                examen.setCorrectas(0);

                examen.setIncorrectas(0);

                examen.setBlancas(0);

                examen.setPuntaje(0.0);

            } else {

                examen.setAnulado(false);

                calificarExamen(
                        examen,
                        respuesta.getRespuestas(),
                        claveTema,
                        puntajeCorrecta,
                        puntajeIncorrecta,
                        puntajeBlanca
                );
            }

            examenRepository.save(examen);
        }

        recalcularOrdenMerito(proceso);
        recalcularIngresantes(proceso);
    }

    private Map<String, IdentifiDTO> leerIdentifi(
            MultipartFile file
    ) throws Exception {

        Map<String, IdentifiDTO> datos =
                new HashMap<>();

        InputStream is = file.getInputStream();

        DBFReader reader = new DBFReader(is);

        Object[] row;

        while ((row = reader.nextRecord())
                != null) {

            String litho =
                    limpiar(row[0]);

            String tema =
                    limpiar(row[1]);

            String codigo =
                    limpiar(row[2]);

            IdentifiDTO dto =
                    new IdentifiDTO(
                            litho,
                            tema,
                            codigo
                    );

            datos.put(litho, dto);
        }

        return datos;
    }

    private List<RespuestDTO> leerRespuestas(
            MultipartFile file
    ) throws Exception {

        List<RespuestDTO> lista =
                new ArrayList<>();

        InputStream is = file.getInputStream();

        DBFReader reader = new DBFReader(is);

        Object[] row;

        while ((row = reader.nextRecord())
                != null) {

            String litho =
                    limpiar(row[0]);

            String tema =
                    limpiar(row[1]);

            Map<String, String> respuestas =
                    new LinkedHashMap<>();

            for (int i = 3; i <= 102; i++) {

                String numeroPregunta =
                        String.valueOf(i - 2);

                String respuesta =
                        limpiar(row[i]);

                respuestas.put(
                        numeroPregunta,
                        respuesta
                );
            }

            RespuestDTO dto =
                    new RespuestDTO(
                            litho,
                            tema,
                            respuestas
                    );

            lista.add(dto);
        }

        return lista;
    }

    private String limpiar(Object valor) {

        if (valor == null) {
            return "";
        }

        return valor.toString().trim();
    }

    private Map<String, Map<String, String>>
    leerClaves(

            MultipartFile file,

            ProcesoAdmision proceso

    ) throws Exception {

        Map<String, Map<String, String>> claves =
                new HashMap<>();

        InputStream is = file.getInputStream();

        DBFReader reader = new DBFReader(is);

        Object[] row;

        while ((row = reader.nextRecord()) != null) {

            String tema =
                    limpiar(row[1]);

            Map<String, String> respuestas =
                    new LinkedHashMap<>();

            for (int i = 3; i <= 102; i++) {

                String numeroPregunta =
                        String.valueOf(i-2);

                String respuesta =
                        limpiar(row[i]);

                respuestas.put(
                        numeroPregunta,
                        respuesta
                );
            }

            String respuestasJson =
                    objectMapper.writeValueAsString(
                            respuestas
                    );

            Clave clave = new Clave();

            clave.setTema(tema);

            clave.setRespuestasJson(
                    respuestasJson
            );

            clave.setProcesoAdmision(
                    proceso
            );

            claveRepository.save(clave);

            claves.put(
                    tema,
                    respuestas
            );
        }

        return claves;
    }

    private void calificarExamen(
            Examen examen,
            Map<String, String> respuestasAlumno,
            Map<String, String> respuestasClave,
            Double puntajeCorrecta,
            Double puntajeIncorrecta,
            Double puntajeBlanca
    ) {

        int correctas = 0;
        int incorrectas = 0;
        int blancas = 0;

        double puntaje = 0;

        for (int i = 1; i <= 100; i++) {

            String numero =
                    String.valueOf(i);

            String respuestaAlumno =
                    respuestasAlumno.get(numero);

            String respuestaCorrecta =
                    respuestasClave.get(numero);

            if (respuestaCorrecta == null
                    || respuestaCorrecta.isBlank()) {

                correctas++;
                puntaje += puntajeCorrecta;

                continue;
            }

            if (respuestaAlumno == null
                    || respuestaAlumno.isBlank()) {

                blancas++;
                puntaje += puntajeBlanca;

                continue;
            }

            if (respuestaAlumno.equalsIgnoreCase(
                    respuestaCorrecta
            )) {

                correctas++;
                puntaje += puntajeCorrecta;

            } else {

                incorrectas++;
                puntaje += puntajeIncorrecta;
            }
        }

        examen.setCorrectas(correctas);

        examen.setIncorrectas(incorrectas);

        examen.setBlancas(blancas);

        examen.setPuntaje(puntaje);
    }

    public void recalcularOrdenMerito(
            ProcesoAdmision proceso
    ) {

        List<Examen> examenes = examenRepository.findByProcesoAdmisionAndAnuladoFalse(proceso);

        // LIMPIAR ORDENES ANTIGUAS
        for (Examen examen : examenes) {
            examen.setOrdenMerito(null);
        }

        // AGRUPAR POR CARRERA
        Map<Long, List<Examen>> examenesPorCarrera =
                examenes.stream()
                        .filter(examen ->
                                examen.getAlumno() != null
                                        &&
                                        examen.getAlumno().getCarrera() != null
                        )
                        .collect(
                                Collectors.groupingBy(
                                        examen ->
                                                examen.getAlumno()
                                                        .getCarrera()
                                                        .getId()
                                )
                        );

        // PROCESAR CADA CARRERA
        for (List<Examen> listaCarrera
                : examenesPorCarrera.values()) {
            // ORDENAR DESCENDENTE
            listaCarrera.sort(
                    Comparator.comparing(
                            Examen::getPuntaje
                    ).reversed()
            );

            int posicionReal = 1;
            int ordenActual = 1;
            Double puntajeAnterior = null;

            for (Examen examen : listaCarrera) {
                if (puntajeAnterior != null && examen.getPuntaje().equals(puntajeAnterior)) {
                    examen.setOrdenMerito(
                            ordenActual
                    );
                } else {
                    ordenActual = posicionReal;
                    examen.setOrdenMerito(
                            ordenActual
                    );
                }
                puntajeAnterior = examen.getPuntaje();
                posicionReal++;
            }
        }

        examenRepository.saveAll(examenes);
    }

    public void recalcularIngresantes(
            ProcesoAdmision proceso
    ) {

        List<Examen> examenes = examenRepository.findByProcesoAdmision(proceso);

        // Reiniciamos todos los ingresos
        for (Examen examen : examenes) {
            examen.setIngreso(false);
        }

        // FILTRAR SOLO EXÁMENES VÁLIDOS
        List<Examen> examenesValidos =
                examenes.stream()
                        .filter(examen ->
                                !Boolean.TRUE.equals(
                                        examen.getAnulado()
                                )
                                        &&
                                        examen.getAlumno() != null
                                        &&
                                        examen.getAlumno().getCarrera() != null
                                        &&
                                        examen.getOrdenMerito() != null
                        )
                        .toList();

        // AGRUPAR POR CARRERA
        Map<Long, List<Examen>> examenesPorCarrera =
                examenesValidos.stream()
                        .collect(
                                Collectors.groupingBy(
                                        examen -> examen.getAlumno()
                                                .getCarrera()
                                                .getId()
                                )
                        );

        // PROCESAR CADA CARRERA
        for (List<Examen> listaCarrera : examenesPorCarrera.values()) {

            // ORDENAR POR ORDEN MÉRITO
            listaCarrera.sort(
                    Comparator.comparing(
                            Examen::getOrdenMerito
                    )
            );

            Carrera carrera = listaCarrera.get(0).getAlumno().getCarrera();

            Integer vacantes = carrera.getVacantes();

            // SI NO HAY VACANTES
            if (vacantes == null || vacantes <= 0) {
                continue;
            }

            // SI HAY MENOS POSTULANTES QUE VACANTES
            if (listaCarrera.size() <= vacantes) {
                for (Examen examen : listaCarrera) {
                    examen.setIngreso(true);
                }
                continue;
            }

            // OBTENER ÚLTIMA VACANTE
            Examen examenUltimaVacante = listaCarrera.get(vacantes - 1);

            Integer ultimoOrdenMerito = examenUltimaVacante.getOrdenMerito();

            // INGRESAN TODOS LOS QUE TENGAN ESE ORDEN O MENOR
            for (Examen examen : listaCarrera) {
                if (examen.getOrdenMerito() <= ultimoOrdenMerito) {
                    examen.setIngreso(true);
                }
            }
        }

        examenRepository.saveAll(examenes);
    }

    @Transactional
    public void actualizarVacantes(
            Long procesoId,
            Map<String, String> vacantesMap
    ) {
        ProcesoAdmision proceso =
                procesoAdmisionRepository
                        .findById(procesoId)
                        .orElseThrow();

        List<Carrera> carreras = carreraRepository.findAll();

        for (Carrera carrera : carreras) {
            String key = "vacante_" + carrera.getId();

            if (vacantesMap.containsKey(key)) {
                Integer nuevasVacantes = Integer.parseInt(vacantesMap.get(key));

                carrera.setVacantes(nuevasVacantes);

                carreraRepository.save(carrera);
            }
        }
        recalcularIngresantes(proceso);
    }
}