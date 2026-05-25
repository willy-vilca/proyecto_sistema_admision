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
        Document document = new Document(PageSize.A4, 20, 20, 170, 80);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {

            PdfWriter writer = PdfWriter.getInstance(document, out);
            HeaderPageEvent headerEvent =
                    new HeaderPageEvent();

            writer.setPageEvent(new PdfPageEventHelper() {
                FooterPageEvent footerEvent = new FooterPageEvent();

                @Override
                public void onEndPage(
                        PdfWriter writer,
                        Document document
                ) {
                    headerEvent.onEndPage(writer, document);
                    footerEvent.onEndPage(writer, document);
                }
            });
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

                headerEvent.setCarrera(carrera);
                agregarTabla(document, examenes);
                document.newPage();
            }

            document.close();

        } catch (Exception e) {
            throw new RuntimeException("Error generando PDF", e);
        }
        return new ByteArrayInputStream(out.toByteArray());
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

            if(examen.getAnulado()){
                agregarCelda(table,
                        "",
                        bg);
            }else{
                agregarCelda(table,
                        String.format("%03d", examen.getOrdenMerito()),
                        bg);
            }

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
        if(examen.getAnulado()){
            return "ANULADO";
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

    private static class HeaderPageEvent extends PdfPageEventHelper {

        private Carrera carrera;
        public void setCarrera(Carrera carrera) {
            this.carrera = carrera;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {

            if (carrera == null) {
                return;
            }

            try {
                PdfPTable header = new PdfPTable(1);
                header.setTotalWidth(555);
                Font normal =
                        new Font(Font.HELVETICA, 12, Font.BOLD);
                Font small =
                        new Font(Font.HELVETICA, 10, Font.NORMAL);
                Font tituloFont =
                        new Font(Font.HELVETICA, 18, Font.BOLD);

                // UNIVERSIDAD
                PdfPCell cell1 = new PdfPCell(
                        new Phrase(
                                "UNIVERSIDAD NACIONAL \"SAN LUIS GONZAGA\"",
                                normal
                        )
                );

                cell1.setBorder(Rectangle.NO_BORDER);
                cell1.setHorizontalAlignment(Element.ALIGN_LEFT);
                cell1.setPaddingBottom(2);

                // COMISION
                PdfPCell cell2 = new PdfPCell(
                        new Phrase(
                                "COMISION EJECUTIVA CENTRAL DE ADMISION CEPU 2023",
                                small
                        )
                );

                cell2.setBorder(Rectangle.NO_BORDER);
                cell2.setPaddingBottom(1);

                // EXAMEN
                PdfPCell cell3 = new PdfPCell(
                        new Phrase(
                                "EXAMEN DE ADMISION CEPU 2023 - I",
                                small
                        )
                );

                cell3.setBorder(Rectangle.NO_BORDER);
                cell3.setPaddingBottom(10);

                // TITULO
                PdfPCell cell4 = new PdfPCell(
                        new Phrase(
                                "RESULTADOS POR CARRERA PROFESIONAL",
                                tituloFont
                        )
                );

                cell4.setBorder(Rectangle.NO_BORDER);
                cell4.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell4.setPaddingBottom(10);

                // MODALIDAD
                PdfPCell cell5 = new PdfPCell(
                        new Phrase(
                                "Modalidad: CENTRO DE ESTUDIOS PREUNIVERSITARIOS",
                                normal
                        )
                );

                cell5.setBorder(Rectangle.NO_BORDER);

                // FACULTAD
                PdfPCell cell6 = new PdfPCell(
                        new Phrase(
                                "Facultad: " + carrera.getfacultad(),
                                normal
                        )
                );

                cell6.setBorder(Rectangle.NO_BORDER);

                // CARRERA
                PdfPCell cell7 = new PdfPCell(
                        new Phrase(
                                "Carrera Profesional: " + carrera.getNombre(),
                                normal
                        )
                );

                cell7.setBorder(Rectangle.NO_BORDER);
                cell7.setPaddingBottom(10);

                header.addCell(cell1);
                header.addCell(cell2);
                header.addCell(cell3);
                header.addCell(cell4);
                header.addCell(cell5);
                header.addCell(cell6);
                header.addCell(cell7);

                header.writeSelectedRows(
                        0,
                        -1,
                        20,
                        820,
                        writer.getDirectContent()
                );

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}