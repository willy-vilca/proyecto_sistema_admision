package com.admision.lector_dbf.service;

import com.admision.lector_dbf.entity.Carrera;
import com.admision.lector_dbf.entity.Examen;
import com.admision.lector_dbf.repository.CarreraRepository;
import com.admision.lector_dbf.repository.ExamenRepository;
import com.admision.lector_dbf.service.PdfResultadoService;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import com.lowagie.text.Rectangle;

import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class PdfResultadoServiceImpl implements PdfResultadoService {

    private final CarreraRepository carreraRepository;
    private final ExamenRepository examenRepository;

    public PdfResultadoServiceImpl(CarreraRepository carreraRepository,
                                   ExamenRepository examenRepository) {
        this.carreraRepository = carreraRepository;
        this.examenRepository = examenRepository;
    }

    @Override
    public ByteArrayInputStream generarPdfResultados(Long procesoId) {
        Document document = new Document(PageSize.A4, 20, 20, 30, 30);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new FooterPageEvent());
            document.open();

            List<Carrera> carreras = carreraRepository.findAll();
            for (Carrera carrera : carreras) {
                List<Examen> examenes =
                        examenRepository
                                .findByProcesoAdmisionIdAndAlumnoCarreraIdOrderByOrdenMeritoAsc(
                                        procesoId,
                                        carrera.getId()
                                );
                if (examenes.isEmpty()) {
                    continue;
                }

                agregarEncabezado(document, carrera);
                agregarTabla(document, examenes);
                document.newPage();
            }

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
    }

    private void agregarEncabezado(Document document, Carrera carrera)
            throws Exception {

        Font tituloFont = new Font(Font.HELVETICA, 18, Font.BOLD);
        Font normal = new Font(Font.HELVETICA, 12, Font.BOLD);

        Paragraph universidad = new Paragraph(
                "UNIVERSIDAD NACIONAL \"SAN LUIS GONZAGA\"",
                normal
        );

        universidad.setAlignment(Element.ALIGN_LEFT);

        document.add(universidad);

        document.add(new Paragraph(
                "COMISION EJECUTIVA CENTRAL DE ADMISION CEPU 2023",
                new Font(Font.HELVETICA, 10)
        ));

        document.add(new Paragraph(
                "EXAMEN DE ADMISION CEPU 2023 - I",
                new Font(Font.HELVETICA, 10)
        ));

        document.add(Chunk.NEWLINE);

        Paragraph titulo = new Paragraph(
                "RESULTADOS POR CARRERA PROFESIONAL",
                tituloFont
        );

        titulo.setAlignment(Element.ALIGN_CENTER);

        document.add(titulo);

        document.add(Chunk.NEWLINE);

        document.add(new Paragraph(
                "Modalidad: CENTRO DE ESTUDIOS PREUNIVERSITARIOS",
                normal
        ));

        document.add(new Paragraph(
                "Facultad: " + carrera.getNombre(),
                normal
        ));

        document.add(new Paragraph(
                "Carrera Profesional: " + carrera.getNombre(),
                normal
        ));

        document.add(Chunk.NEWLINE);
    }

    private void agregarTabla(Document document, List<Examen> examenes)
            throws Exception {

        PdfPTable table = new PdfPTable(6);

        table.setWidthPercentage(100);

        table.setWidths(new float[]{0.9f, 1.6f, 7f, 2f, 1.6f, 2.6f});

        Font headFont = new Font(Font.HELVETICA, 10, Font.BOLD);

        agregarHeader(table, "SEC", headFont, 0, 6);
        agregarHeader(table, "CODIGO", headFont, 1, 6);
        agregarHeader(table, "NOMBRE", headFont, 2, 6);
        agregarHeader(table, "PUNTAJE", headFont, 3, 6);
        agregarHeader(table, "MERITO", headFont, 4, 6);
        agregarHeader(table, "CONDICION", headFont, 5, 6);

        DecimalFormat df = new DecimalFormat("0.000");

        int sec = 1;

        for (Examen examen : examenes) {

            boolean gris = sec % 2 == 0;

            Color bg = gris
                    ? new Color(195, 195, 195)
                    : Color.WHITE;

            agregarCelda(table,
                    String.format("%04d", sec),
                    bg);

            agregarCelda(table,
                    examen.getAlumno().getCodigo(),
                    bg);

            agregarCelda(table,
                    examen.getAlumno().getNombres(),
                    bg);

            agregarCelda(table,
                    df.format(examen.getPuntaje()),
                    bg);

            agregarCelda(table,
                    String.format("%03d", examen.getOrdenMerito()),
                    bg);

            agregarCelda(table,
                    obtenerCondicion(examen),
                    bg);

            sec++;
        }

        table.setSpacingBefore(5f);
        table.setSpacingAfter(5f);
        document.add(table);
    }

    private void agregarHeader(
            PdfPTable table,
            String texto,
            Font font,
            int posicion,
            int totalColumnas
    ) {

        PdfPCell cell = new PdfPCell();
        cell.setPhrase(new Phrase(texto, font));

        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPaddingTop(2);
        cell.setPaddingBottom(2);
        cell.setPaddingLeft(4);
        cell.setPaddingRight(4);
        cell.setBackgroundColor(Color.WHITE);

        int borders = Rectangle.TOP | Rectangle.BOTTOM;

        if (posicion == 0) {
            borders |= Rectangle.LEFT;
        }

        if (posicion == totalColumnas - 1) {
            borders |= Rectangle.RIGHT;
        }

        cell.setBorder(borders);
        cell.setBorderWidth(1f);
        table.addCell(cell);
    }

    private void agregarCelda(PdfPTable table, String texto, Color bgColor) {

        Font bodyFont =
                new Font(
                        Font.HELVETICA,
                        9,
                        Font.NORMAL
                );

        PdfPCell cell = new PdfPCell(new Phrase(texto, bodyFont));

        cell.setBackgroundColor(bgColor);

        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPaddingTop(4);
        cell.setPaddingBottom(4);
        cell.setPaddingLeft(7);
        cell.setPaddingRight(4);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);

        table.addCell(cell);
    }

    private String obtenerCondicion(Examen examen) {

        if (examen.getPuntaje() == 0) {
            return "AUSENTE";
        }
        return examen.getIngreso()
                ? "INGRESO"
                : "NO INGRESO";
    }

    private static class FooterPageEvent extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

            PdfPTable footer = new PdfPTable(4);

            try {
                footer.setWidths(
                        new int[]{2, 2, 2, 2}
                );

                footer.setTotalWidth(560);
                PdfPCell unicaCell =
                        crearFooterSimple(
                                "UNICA",
                                true
                        );

                unicaCell.setBorderWidthLeft(1f);
                unicaCell.setBorderWidthTop(1f);
                unicaCell.setBorderWidthBottom(1f);
                unicaCell.setBorderWidthRight(0f);

                footer.addCell(unicaCell);


                Phrase paginaPhrase = new Phrase();
                paginaPhrase.add(
                        new Chunk(
                                "PAGINA: ",
                                new Font(
                                        Font.HELVETICA,
                                        10,
                                        Font.NORMAL
                                )
                        )
                );

                paginaPhrase.add(
                        new Chunk(
                                String.valueOf(writer.getPageNumber()),
                                new Font(
                                        Font.HELVETICA,
                                        10,
                                        Font.BOLD
                                )
                        )
                );

                PdfPCell paginaCell =
                        crearFooterPhrase(
                                paginaPhrase
                        );

                paginaCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);

                footer.addCell(paginaCell);


                DateTimeFormatter fecha = DateTimeFormatter.ofPattern("dd/MM/yy");
                Phrase fechaPhrase = new Phrase();
                fechaPhrase.add(
                        new Chunk(
                                "FECHA: ",
                                new Font(
                                        Font.HELVETICA,
                                        10,
                                        Font.NORMAL
                                )
                        )
                );

                fechaPhrase.add(
                        new Chunk(
                                LocalDateTime.now().format(fecha),
                                new Font(
                                        Font.HELVETICA,
                                        10,
                                        Font.BOLD
                                )
                        )
                );

                PdfPCell fechaCell =
                        crearFooterPhrase(
                                fechaPhrase
                        );

                fechaCell.setBorder(Rectangle.TOP | Rectangle.BOTTOM);
                footer.addCell(fechaCell);


                DateTimeFormatter hora = DateTimeFormatter.ofPattern("HH:mm:ss");
                Phrase horaPhrase = new Phrase();
                horaPhrase.add(
                        new Chunk(
                                "HORA: ",
                                new Font(
                                        Font.HELVETICA,
                                        10,
                                        Font.NORMAL
                                )
                        )
                );

                horaPhrase.add(
                        new Chunk(
                                LocalDateTime.now().format(hora),
                                new Font(
                                        Font.HELVETICA,
                                        10,
                                        Font.BOLD
                                )
                        )
                );

                PdfPCell horaCell =
                        crearFooterPhrase(
                                horaPhrase
                        );

                horaCell.setBorderWidthTop(1f);
                horaCell.setBorderWidthBottom(1f);
                horaCell.setBorderWidthRight(1f);
                horaCell.setBorderWidthLeft(0f);

                footer.addCell(horaCell);

                // POSICIÓN DEL FOOTER
                footer.writeSelectedRows(
                        0,
                        -1,
                        18,
                        55,
                        writer.getDirectContent()
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private PdfPCell crearFooterSimple(
                String texto,
                boolean bold
        ) {

            PdfPCell cell =
                    new PdfPCell(
                            new Phrase(
                                    texto,
                                    new Font(
                                            Font.HELVETICA,
                                            10,
                                            bold
                                                    ? Font.BOLD
                                                    : Font.NORMAL
                                    )
                            )
                    );

            cell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            cell.setVerticalAlignment(
                    Element.ALIGN_MIDDLE
            );

            cell.setPaddingTop(8);
            cell.setPaddingBottom(8);
            return cell;
        }

        private PdfPCell crearFooterPhrase(
                Phrase phrase
        ) {

            PdfPCell cell =
                    new PdfPCell(phrase);

            cell.setHorizontalAlignment(
                    Element.ALIGN_CENTER
            );

            cell.setVerticalAlignment(
                    Element.ALIGN_MIDDLE
            );

            cell.setPaddingTop(8);
            cell.setPaddingBottom(8);
            return cell;
        }
    }
}