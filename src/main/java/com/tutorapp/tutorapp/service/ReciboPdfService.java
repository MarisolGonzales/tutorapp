package com.tutorapp.tutorapp.service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;
import com.tutorapp.tutorapp.model.Pago;
import com.tutorapp.tutorapp.model.Sesion;


//Genera la boleta de venta de una sesión en formato PDF (OpenPDF).

@Service
public class ReciboPdfService {

    private static final Logger log = LoggerFactory.getLogger(ReciboPdfService.class);

    // Datos ficticios del emisor
    private static final String EMPRESA   = "TUTORAPP S.A.C.";
    private static final String RUC       = "20601234567";
    private static final String DIRECCION = "Av. Universitaria 1801, Lima - Perú";
    private static final String SERIE     = "B001";
    private static final double TASA_IGV  = 0.18;

    private static final Color AZUL        = new Color(37, 99, 235);
    private static final Color GRIS        = new Color(100, 116, 139);
    private static final Color GRIS_CLARO  = new Color(203, 213, 225);

    private static final Font F_EMPRESA  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, AZUL);
    private static final Font F_DATOS    = FontFactory.getFont(FontFactory.HELVETICA, 9, GRIS);
    private static final Font F_TITULO   = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.BLACK);
    private static final Font F_ETIQUETA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.DARK_GRAY);
    private static final Font F_VALOR    = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font F_TABLA    = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
    private static final Font F_CABECERA = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 9, Color.WHITE);
    private static final Font F_TOTAL    = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, AZUL);
    private static final Font F_PIE      = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 7, GRIS);

    private static final DateTimeFormatter FORMATO_EMISION = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final String[] UNIDADES = {
        "", "UNO", "DOS", "TRES", "CUATRO", "CINCO", "SEIS", "SIETE", "OCHO", "NUEVE", "DIEZ",
        "ONCE", "DOCE", "TRECE", "CATORCE", "QUINCE", "DIECISEIS", "DIECISIETE", "DIECIOCHO",
        "DIECINUEVE", "VEINTE"
    };
    private static final String[] DECENAS = {
        "", "", "", "TREINTA", "CUARENTA", "CINCUENTA", "SESENTA", "SETENTA", "OCHENTA", "NOVENTA"
    };
    private static final String[] CENTENAS = {
        "", "CIENTO", "DOSCIENTOS", "TRESCIENTOS", "CUATROCIENTOS", "QUINIENTOS",
        "SEISCIENTOS", "SETECIENTOS", "OCHOCIENTOS", "NOVECIENTOS"
    };

    ///Construye la boleta
    
    public byte[] generarRecibo(Pago pago) {
        Sesion sesion = pago.getSesion();

        double total      = pago.getMonto();
        double opGravada  = total / (1 + TASA_IGV);
        double igv        = total - opGravada;

        try (ByteArrayOutputStream salida = new ByteArrayOutputStream()) {
            Document documento = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(documento, salida);
            documento.open();

            documento.add(construirEncabezado(pago));
            documento.add(new Paragraph(" "));
            documento.add(new Chunk(separador()));
            documento.add(new Paragraph(" "));

            documento.add(construirDatosCliente(pago, sesion));
            documento.add(new Paragraph(" "));

            documento.add(construirDetalle(sesion, total));
            documento.add(new Paragraph(" "));

            documento.add(construirTotales(opGravada, igv, total));

            Paragraph enLetras = new Paragraph(
                    "SON: " + importeALetras(total), F_ETIQUETA);
            enLetras.setSpacingBefore(14);
            documento.add(enLetras);

            documento.add(new Paragraph(" "));
            documento.add(new Chunk(separador()));
            documento.add(construirFormaDePago(pago));

            documento.add(construirPie());

            documento.close();
            log.info("Boleta PDF generada para el pago {}", pago.getId());
            return salida.toByteArray();

        } catch (Exception e) {
            log.error("Error al generar la boleta PDF del pago {}", pago.getId(), e);
            throw new IllegalStateException("No se pudo generar el recibo de pago.");
        }
    }

    /** Cabecera: datos del emisor a la izquierda y el recuadro de la boleta a la derecha. */
    private PdfPTable construirEncabezado(Pago pago) throws Exception {
        PdfPTable encabezado = new PdfPTable(2);
        encabezado.setWidthPercentage(100);
        encabezado.setWidths(new float[] { 58, 42 });

        // Emisor
        PdfPCell emisor = new PdfPCell();
        emisor.setBorder(Rectangle.NO_BORDER);
        emisor.addElement(new Paragraph(EMPRESA, F_EMPRESA));
        emisor.addElement(new Paragraph("Plataforma de tutorías académicas", F_DATOS));
        emisor.addElement(new Paragraph(DIRECCION, F_DATOS));
        encabezado.addCell(emisor);

        // Recuadro con RUC y número de boleta
        PdfPTable recuadro = new PdfPTable(1);
        recuadro.setWidthPercentage(100);
        recuadro.addCell(celdaRecuadro("R.U.C. " + RUC, F_TITULO));
        recuadro.addCell(celdaRecuadro("BOLETA DE VENTA ELECTRÓNICA", F_TITULO));
        recuadro.addCell(celdaRecuadro(SERIE + " - " + String.format("%08d", pago.getId()), F_TITULO));

        PdfPCell contenedor = new PdfPCell(recuadro);
        contenedor.setBorder(Rectangle.BOX);
        contenedor.setBorderColor(AZUL);
        contenedor.setPadding(4);
        encabezado.addCell(contenedor);

        return encabezado;
    }

    /** Datos del cliente y fecha de emisión. */
    private PdfPTable construirDatosCliente(Pago pago, Sesion sesion) throws Exception {
        PdfPTable datos = new PdfPTable(2);
        datos.setWidthPercentage(100);
        datos.setWidths(new float[] { 22, 78 });

        agregarDato(datos, "Fecha de emisión:", pago.getFechaPago().format(FORMATO_EMISION));
        agregarDato(datos, "Señor(es):", sesion.getAlumno().getNombre());
        agregarDato(datos, "Tipo de moneda:", "SOLES (S/)");

        return datos;
    }

    /** Tabla con el detalle del servicio prestado. */
    private PdfPTable construirDetalle(Sesion sesion, double total) throws Exception {
        PdfPTable detalle = new PdfPTable(4);
        detalle.setWidthPercentage(100);
        detalle.setWidths(new float[] { 10, 60, 15, 15 });

        agregarCabecera(detalle, "CANT.");
        agregarCabecera(detalle, "DESCRIPCIÓN");
        agregarCabecera(detalle, "P. UNIT.");
        agregarCabecera(detalle, "IMPORTE");

        String descripcion = "Tutoría académica: " + sesion.getServicioTutor().getCurso().getNombre()
                + "\nTutor: " + sesion.getServicioTutor().getTutor().getNombre()
                + "\nSesión: " + sesion.getFecha() + " a las " + sesion.getHora();

        agregarCelda(detalle, "1", Element.ALIGN_CENTER);
        agregarCelda(detalle, descripcion, Element.ALIGN_LEFT);
        agregarCelda(detalle, String.format("%.2f", total), Element.ALIGN_RIGHT);
        agregarCelda(detalle, String.format("%.2f", total), Element.ALIGN_RIGHT);

        return detalle;
    }

    /** Bloque de totales: operación gravada, IGV e importe total. */
    private PdfPTable construirTotales(double opGravada, double igv, double total) throws Exception {
        PdfPTable totales = new PdfPTable(2);
        totales.setWidthPercentage(45);
        totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.setWidths(new float[] { 55, 45 });

        agregarTotal(totales, "Op. Gravada:", String.format("S/ %.2f", opGravada), F_VALOR);
        agregarTotal(totales, "I.G.V. (18%):", String.format("S/ %.2f", igv), F_VALOR);
        agregarTotal(totales, "IMPORTE TOTAL:", String.format("S/ %.2f", total), F_TOTAL);

        return totales;
    }

    /** Forma de pago y estado de la operación. */
    private Paragraph construirFormaDePago(Pago pago) {
        Paragraph forma = new Paragraph();
        forma.setSpacingBefore(10);
        forma.add(new Phrase("Forma de pago: ", F_ETIQUETA));
        forma.add(new Phrase(pago.getMetodo().name(), F_VALOR));
        forma.add(new Phrase("        Estado: ", F_ETIQUETA));
        forma.add(new Phrase(pago.getEstado().name(), F_VALOR));
        return forma;
    }

    /** Pie con la leyenda de representación impresa */
    private Paragraph construirPie() {
        Paragraph pie = new Paragraph();
        pie.setSpacingBefore(26);
        pie.add(new Phrase("TutorApp retiene el pago y lo transfiere al tutor cuando la sesión se completa. "
                + "Si la sesión se cancela, el monto se reembolsa íntegramente al alumno.\n", F_PIE));
        return pie;
    }


    private LineSeparator separador() {
        LineSeparator linea = new LineSeparator();
        linea.setLineColor(GRIS_CLARO);
        linea.setLineWidth(1f);
        return linea;
    }

    private PdfPCell celdaRecuadro(String texto, Font fuente) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, fuente));
        celda.setBorder(Rectangle.NO_BORDER);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(3);
        return celda;
    }

    private void agregarDato(PdfPTable tabla, String etiqueta, String valor) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, F_ETIQUETA));
        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, F_VALOR));
        celdaEtiqueta.setBorder(Rectangle.NO_BORDER);
        celdaValor.setBorder(Rectangle.NO_BORDER);
        celdaEtiqueta.setPadding(3);
        celdaValor.setPadding(3);
        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
    }

    private void agregarCabecera(PdfPTable tabla, String texto) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, F_CABECERA));
        celda.setBackgroundColor(AZUL);
        celda.setHorizontalAlignment(Element.ALIGN_CENTER);
        celda.setPadding(6);
        tabla.addCell(celda);
    }

    private void agregarCelda(PdfPTable tabla, String texto, int alineacion) {
        PdfPCell celda = new PdfPCell(new Phrase(texto, F_TABLA));
        celda.setHorizontalAlignment(alineacion);
        celda.setBorderColor(GRIS_CLARO);
        celda.setPadding(6);
        tabla.addCell(celda);
    }

    private void agregarTotal(PdfPTable tabla, String etiqueta, String valor, Font fuente) {
        PdfPCell celdaEtiqueta = new PdfPCell(new Phrase(etiqueta, F_ETIQUETA));
        PdfPCell celdaValor = new PdfPCell(new Phrase(valor, fuente));
        celdaEtiqueta.setBorder(Rectangle.NO_BORDER);
        celdaValor.setBorder(Rectangle.NO_BORDER);
        celdaEtiqueta.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaValor.setHorizontalAlignment(Element.ALIGN_RIGHT);
        celdaEtiqueta.setPadding(3);
        celdaValor.setPadding(3);
        tabla.addCell(celdaEtiqueta);
        tabla.addCell(celdaValor);
    }

    /** Convierte 30.00 en "TREINTA CON 00/100 SOLES". */
    private String importeALetras(double importe) {
        int entero = (int) importe;
        int centavos = (int) Math.round((importe - entero) * 100);
        return numeroALetras(entero) + " CON " + String.format("%02d", centavos) + "/100 SOLES";
    }

    private String numeroALetras(int n) {
        if (n == 0) return "CERO";
        if (n <= 20) return UNIDADES[n];
        if (n < 30) return "VEINTI" + UNIDADES[n - 20];
        if (n < 100) {
            int decena = n / 10;
            int unidad = n % 10;
            return DECENAS[decena] + (unidad > 0 ? " Y " + UNIDADES[unidad] : "");
        }
        if (n == 100) return "CIEN";
        if (n < 1000) {
            int centena = n / 100;
            int resto = n % 100;
            return CENTENAS[centena] + (resto > 0 ? " " + numeroALetras(resto) : "");
        }
        int miles = n / 1000;
        int resto = n % 1000;
        String prefijo = (miles == 1) ? "MIL" : numeroALetras(miles) + " MIL";
        return prefijo + (resto > 0 ? " " + numeroALetras(resto) : "");
    }
}
