package com.tutorapp.tutorapp.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.tutorapp.tutorapp.model.Pago;

/**
 * Genera reportes en formato Excel (.xlsx) con Apache POI.
 *
 * Por seguridad, el reporte NO incluye la referencia del pago, ya que esta
 * contiene el celular de Yape o los últimos dígitos de la tarjeta del alumno.
 */
@Service
public class ReporteExcelService {

    private static final Logger log = LoggerFactory.getLogger(ReporteExcelService.class);

    private static final String[] CABECERAS = {
        "Alumno", "Curso", "Fecha de la sesión", "Método", "Monto (S/.)", "Estado"
    };

    /**
     * Construye el archivo Excel con los pagos recibidos y lo devuelve como
     * arreglo de bytes, listo para ser descargado por el navegador.
     */
    public byte[] generarReportePagos(List<Pago> pagos) {
        try (Workbook libro = new XSSFWorkbook();
             ByteArrayOutputStream salida = new ByteArrayOutputStream()) {

            Sheet hoja = libro.createSheet("Pagos");

            // Cabecera en negrita
            CellStyle estiloCabecera = libro.createCellStyle();
            Font negrita = libro.createFont();
            negrita.setBold(true);
            estiloCabecera.setFont(negrita);

            Row filaCabecera = hoja.createRow(0);
            for (int i = 0; i < CABECERAS.length; i++) {
                Cell celda = filaCabecera.createCell(i);
                celda.setCellValue(CABECERAS[i]);
                celda.setCellStyle(estiloCabecera);
            }

            int numeroFila = 1;
            for (Pago pago : pagos) {
                Row fila = hoja.createRow(numeroFila++);
                fila.createCell(0).setCellValue(pago.getSesion().getAlumno().getNombre());
                fila.createCell(1).setCellValue(pago.getSesion().getServicioTutor().getCurso().getNombre());
                fila.createCell(2).setCellValue(pago.getSesion().getFecha() + " " + pago.getSesion().getHora());
                fila.createCell(3).setCellValue(pago.getMetodo().name());
                fila.createCell(4).setCellValue(pago.getMonto());
                fila.createCell(5).setCellValue(pago.getEstado().name());
            }

            for (int i = 0; i < CABECERAS.length; i++) {
                hoja.autoSizeColumn(i);
            }

            libro.write(salida);
            log.info("Reporte Excel generado con {} pago(s)", pagos.size());
            return salida.toByteArray();

        } catch (IOException e) {
            log.error("Error al generar el reporte Excel de pagos", e);
            throw new IllegalStateException("No se pudo generar el reporte de pagos.");
        }
    }
}
