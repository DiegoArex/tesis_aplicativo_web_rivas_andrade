package com.example.demo.service;

import com.example.demo.dto.AuditoriaDTO;
import com.example.demo.dto.ActividadUsuarioDTO;
import com.example.demo.dto.AuditoriaUsoLaboratorioDTO;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Servicio para gestionar auditoría y exportación de registros
 * Exporta a Excel con formato profesional para reportería
 */
@Service
@RequiredArgsConstructor
public class AuditoriaService {

    /**
     * Exporta una lista de registros de auditoría a Excel con formato mejorado
     * Incluye: Fecha, Usuario, IP, Entidad, Acción, Nombre, Detalles
     */
    public byte[] exportarAExcel(List<AuditoriaDTO> registros) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Auditoría");
            
            // ===== ESTILOS =====
            // Estilo para encabezado
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setWrapText(true);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // Estilo para datos generales
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.LEFT);
            dataStyle.setVerticalAlignment(VerticalAlignment.TOP);
            dataStyle.setWrapText(true);
            
            // Estilo para fechas
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setBorderBottom(BorderStyle.THIN);
            dateStyle.setBorderTop(BorderStyle.THIN);
            dateStyle.setBorderLeft(BorderStyle.THIN);
            dateStyle.setBorderRight(BorderStyle.THIN);
            dateStyle.setAlignment(HorizontalAlignment.CENTER);
            dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            // Estilo para IPs
            CellStyle ipStyle = workbook.createCellStyle();
            ipStyle.setBorderBottom(BorderStyle.THIN);
            ipStyle.setBorderTop(BorderStyle.THIN);
            ipStyle.setBorderLeft(BorderStyle.THIN);
            ipStyle.setBorderRight(BorderStyle.THIN);
            ipStyle.setAlignment(HorizontalAlignment.CENTER);
            Font ipFont = workbook.createFont();
            ipFont.setFontName("Courier New");
            ipFont.setFontHeightInPoints((short) 9);
            ipStyle.setFont(ipFont);
            
            // ===== ENCABEZADOS =====
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            
            String[] headers = {
                "Fecha/Hora",
                "Usuario",
                "IP Address",
                "Tipo Entidad",
                "Nombre Entidad",
                "Acción",
                "Detalles",
                "ID Registro",
                "Rev ID"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // ===== DATOS =====
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            int rowNum = 1;
            
            for (AuditoriaDTO registro : registros) {
                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(20);
                
                int colNum = 0;
                
                // Fecha y Hora
                Cell celdaFecha = row.createCell(colNum++);
                if (registro.getFechaCambio() != null) {
                    celdaFecha.setCellValue(registro.getFechaCambio().format(formatter));
                } else {
                    celdaFecha.setCellValue("-");
                }
                celdaFecha.setCellStyle(dateStyle);
                
                // Usuario
                Cell celdaUsuario = row.createCell(colNum++);
                celdaUsuario.setCellValue(registro.getUsuario() != null ? registro.getUsuario() : "-");
                celdaUsuario.setCellStyle(dataStyle);
                
                // IP Address
                Cell celdaIp = row.createCell(colNum++);
                celdaIp.setCellValue(registro.getIpAddress() != null ? registro.getIpAddress() : "-");
                celdaIp.setCellStyle(ipStyle);
                
                // Tipo de Entidad
                Cell celdaTipoEntidad = row.createCell(colNum++);
                celdaTipoEntidad.setCellValue(registro.getTipoEntidad() != null ? registro.getTipoEntidad() : "-");
                celdaTipoEntidad.setCellStyle(dataStyle);
                
                // Nombre/Descripción de la Entidad
                Cell celdaNombreEntidad = row.createCell(colNum++);
                celdaNombreEntidad.setCellValue(registro.getNombreEntidad() != null ? registro.getNombreEntidad() : "-");
                celdaNombreEntidad.setCellStyle(dataStyle);
                
                // Acción
                Cell celdaAccion = row.createCell(colNum++);
                String descripcionAccion = registro.getDescripcionAccion() != null 
                    ? registro.getDescripcionAccion() 
                    : (registro.getAccion() != null ? registro.getAccion() : "-");
                celdaAccion.setCellValue(descripcionAccion);
                celdaAccion.setCellStyle(dataStyle);
                
                // Detalles Completos
                Cell celdaDetalles = row.createCell(colNum++);
                celdaDetalles.setCellValue(registro.getDetalles() != null ? registro.getDetalles() : "-");
                celdaDetalles.setCellStyle(dataStyle);
                
                // ID del Registro
                Cell celdaIdRegistro = row.createCell(colNum++);
                if (registro.getIdEntidad() != null) {
                    celdaIdRegistro.setCellValue(registro.getIdEntidad());
                } else {
                    celdaIdRegistro.setCellValue("-");
                }
                celdaIdRegistro.setCellStyle(dataStyle);
                
                // ID de Revisión (Envers)
                Cell celdaRevId = row.createCell(colNum++);
                if (registro.getRevisionId() != null) {
                    celdaRevId.setCellValue(registro.getRevisionId());
                } else {
                    celdaRevId.setCellValue("-");
                }
                celdaRevId.setCellStyle(dataStyle);
            }
            
            // ===== AJUSTAR ANCHO DE COLUMNAS =====
            sheet.setColumnWidth(0, 22 * 256);  // Fecha/Hora
            sheet.setColumnWidth(1, 18 * 256);  // Usuario
            sheet.setColumnWidth(2, 18 * 256);  // IP
            sheet.setColumnWidth(3, 16 * 256);  // Tipo Entidad
            sheet.setColumnWidth(4, 25 * 256);  // Nombre Entidad
            sheet.setColumnWidth(5, 14 * 256);  // Acción
            sheet.setColumnWidth(6, 35 * 256);  // Detalles
            sheet.setColumnWidth(7, 12 * 256);  // ID Registro
            sheet.setColumnWidth(8, 10 * 256);  // Rev ID
            
            // ===== AGREGAR PIE DE PÁGINA (INFORMACIÓN) =====
            if (!registros.isEmpty()) {
                int lastRow = rowNum + 2;
                Row summaryRow = sheet.createRow(lastRow);
                Cell summaryCell = summaryRow.createCell(0);
                summaryCell.setCellValue("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                Font footerFont = workbook.createFont();
                footerFont.setItalic(true);
                footerFont.setFontHeightInPoints((short) 9);
                CellStyle summaryStyle = workbook.createCellStyle();
                summaryStyle.setFont(footerFont);
                summaryCell.setCellStyle(summaryStyle);
            }
            
            // ===== ESCRIBIR A BYTES =====
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            return outputStream.toByteArray();
        }
    }

    /**
     * Exporta actividades de usuarios a Excel con formato profesional
     * Incluye: Usuario, Actividad, Entidad, Detalles, Fecha, IP
     * Perfecto para auditoría de qué hizo cada usuario
     */
    public byte[] exportarActividadesUsuarioExcel(List<ActividadUsuarioDTO> actividades) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Actividades Usuarios");
            
            // ===== ESTILOS =====
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setWrapText(true);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            // Estilo para datos
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setAlignment(HorizontalAlignment.LEFT);
            dataStyle.setVerticalAlignment(VerticalAlignment.TOP);
            dataStyle.setWrapText(true);
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            Font dataFont = workbook.createFont();
            dataFont.setFontHeightInPoints((short) 10);
            dataStyle.setFont(dataFont);
            
            // Estilo para fecha
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setAlignment(HorizontalAlignment.CENTER);
            dateStyle.setWrapText(true);
            dateStyle.setBorderBottom(BorderStyle.THIN);
            dateStyle.setBorderTop(BorderStyle.THIN);
            dateStyle.setBorderLeft(BorderStyle.THIN);
            dateStyle.setBorderRight(BorderStyle.THIN);
            Font dateFont = workbook.createFont();
            dateFont.setFontHeightInPoints((short) 9);
            dateStyle.setFont(dateFont);
            
            // ===== CREAR ENCABEZADO =====
            Row headerRow = sheet.createRow(0);
            String[] headers = {
                "Fecha/Hora",
                "Usuario",
                "Descripción de Actividad",
                "Tipo de Entidad",
                "Nombre de Entidad",
                "Detalles",
                "Dirección IP",
                "ID Entidad",
                "Rev ID"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // ===== AGREGAR DATOS =====
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            
            for (ActividadUsuarioDTO actividad : actividades) {
                Row row = sheet.createRow(rowNum++);
                
                // Fecha/Hora
                Cell cellFecha = row.createCell(0);
                if (actividad.getFecha() != null) {
                    cellFecha.setCellValue(actividad.getFecha().format(formatter));
                }
                cellFecha.setCellStyle(dateStyle);
                
                // Usuario
                Cell cellUsuario = row.createCell(1);
                cellUsuario.setCellValue(actividad.getUsuario() != null ? actividad.getUsuario() : "");
                cellUsuario.setCellStyle(dataStyle);
                
                // Descripción de Actividad
                Cell cellDesc = row.createCell(2);
                cellDesc.setCellValue(actividad.getDescripcionActividad() != null ? actividad.getDescripcionActividad() : "");
                cellDesc.setCellStyle(dataStyle);
                
                // Tipo de Entidad
                Cell cellTipo = row.createCell(3);
                cellTipo.setCellValue(actividad.getTipoEntidad() != null ? actividad.getTipoEntidad() : "");
                cellTipo.setCellStyle(dataStyle);
                
                // Nombre de Entidad
                Cell cellNombre = row.createCell(4);
                cellNombre.setCellValue(actividad.getNombreEntidad() != null ? actividad.getNombreEntidad() : "");
                cellNombre.setCellStyle(dataStyle);
                
                // Detalles
                Cell cellDetalles = row.createCell(5);
                cellDetalles.setCellValue(actividad.getDetalles() != null ? actividad.getDetalles() : "");
                cellDetalles.setCellStyle(dataStyle);
                
                // IP Address
                Cell cellIP = row.createCell(6);
                cellIP.setCellValue(actividad.getIpAddress() != null ? actividad.getIpAddress() : "N/A");
                cellIP.setCellStyle(dataStyle);
                
                // ID Entidad
                Cell cellID = row.createCell(7);
                if (actividad.getIdEntidad() != null) {
                    cellID.setCellValue(actividad.getIdEntidad());
                }
                cellID.setCellStyle(dataStyle);
                
                // Rev ID
                Cell cellRevID = row.createCell(8);
                if (actividad.getRevisionId() != null) {
                    cellRevID.setCellValue(actividad.getRevisionId());
                }
                cellRevID.setCellStyle(dataStyle);
                
                // Establecer altura de fila
                row.setHeightInPoints(30);
            }
            
            // ===== AJUSTAR ANCHO DE COLUMNAS =====
            sheet.setColumnWidth(0, 22 * 256);  // Fecha/Hora
            sheet.setColumnWidth(1, 18 * 256);  // Usuario
            sheet.setColumnWidth(2, 28 * 256);  // Descripción de Actividad
            sheet.setColumnWidth(3, 16 * 256);  // Tipo de Entidad
            sheet.setColumnWidth(4, 25 * 256);  // Nombre de Entidad
            sheet.setColumnWidth(5, 35 * 256);  // Detalles
            sheet.setColumnWidth(6, 18 * 256);  // IP Address
            sheet.setColumnWidth(7, 12 * 256);  // ID Entidad
            sheet.setColumnWidth(8, 10 * 256);  // Rev ID
            
            // ===== AGREGAR PIE DE PÁGINA =====
            if (!actividades.isEmpty()) {
                int lastRow = rowNum + 2;
                Row summaryRow = sheet.createRow(lastRow);
                Cell summaryCell = summaryRow.createCell(0);
                summaryCell.setCellValue("Generado: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")));
                Font footerFont = workbook.createFont();
                footerFont.setItalic(true);
                footerFont.setFontHeightInPoints((short) 9);
                CellStyle summaryStyle = workbook.createCellStyle();
                summaryStyle.setFont(footerFont);
                summaryCell.setCellStyle(summaryStyle);
            }
            
            // ===== ESCRIBIR A BYTES =====
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            return outputStream.toByteArray();
        }
    }

    /**
     * Exporta auditoría de uso de laboratorios a Excel
     * Incluye: Usuario, Laboratorios (1 o 2), Entrada, Salida, Propósito, Duración, Tipo de Registro
     */
    public byte[] exportarUsoLaboratoriosExcel(List<AuditoriaUsoLaboratorioDTO> registros) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Uso Laboratorios");
            
            // ===== ESTILOS =====
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setWrapText(true);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.LEFT);
            dataStyle.setWrapText(true);
            
            CellStyle centerStyle = workbook.createCellStyle();
            centerStyle.setBorderBottom(BorderStyle.THIN);
            centerStyle.setBorderTop(BorderStyle.THIN);
            centerStyle.setBorderLeft(BorderStyle.THIN);
            centerStyle.setBorderRight(BorderStyle.THIN);
            centerStyle.setAlignment(HorizontalAlignment.CENTER);
            
            CellStyle examenStyle = workbook.createCellStyle();
            examenStyle.setBorderBottom(BorderStyle.THIN);
            examenStyle.setBorderTop(BorderStyle.THIN);
            examenStyle.setBorderLeft(BorderStyle.THIN);
            examenStyle.setBorderRight(BorderStyle.THIN);
            examenStyle.setAlignment(HorizontalAlignment.CENTER);
            examenStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            examenStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font examenFont = workbook.createFont();
            examenFont.setBold(true);
            examenStyle.setFont(examenFont);
            
            // ===== ENCABEZADOS =====
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(30);
            
            String[] headers = {
                "Fecha Entrada",
                "Fecha Salida",
                "Usuario",
                "Nombre Completo",
                "Laboratorio(s)",
                "Ubicación",
                "Propósito",
                "Observaciones",
                "Tipo Registro",
                "Duración",
                "Novedad",
                "Estado Novedad"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // ===== DATOS =====
            int rowNum = 1;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
            
            for (AuditoriaUsoLaboratorioDTO registro : registros) {
                Row row = sheet.createRow(rowNum++);
                
                // Fecha Entrada
                Cell cellEntrada = row.createCell(0);
                cellEntrada.setCellValue(registro.getHoraEntrada() != null ? 
                    registro.getHoraEntrada().format(formatter) : "");
                cellEntrada.setCellStyle(centerStyle);
                
                // Fecha Salida
                Cell cellSalida = row.createCell(1);
                cellSalida.setCellValue(registro.getHoraSalida() != null ? 
                    registro.getHoraSalida().format(formatter) : "");
                cellSalida.setCellStyle(centerStyle);
                
                // Usuario
                Cell cellUsuario = row.createCell(2);
                cellUsuario.setCellValue(registro.getUsuario() != null ? registro.getUsuario() : "");
                cellUsuario.setCellStyle(dataStyle);
                
                // Nombre Completo
                Cell cellNombre = row.createCell(3);
                cellNombre.setCellValue(registro.getNombreCompletoUsuario() != null ? 
                    registro.getNombreCompletoUsuario() : "");
                cellNombre.setCellStyle(dataStyle);
                
                // Laboratorios (uno o dos)
                Cell cellLabs = row.createCell(4);
                cellLabs.setCellValue(registro.getLaboratoriosCompleto() != null ? 
                    registro.getLaboratoriosCompleto() : "");
                
                // Aplicar estilo diferente si es examen
                if (registro.getEsExamen() != null && registro.getEsExamen()) {
                    cellLabs.setCellStyle(examenStyle);
                } else {
                    cellLabs.setCellStyle(dataStyle);
                }
                
                // Ubicación
                Cell cellUbicacion = row.createCell(5);
                String ubicacion = registro.getUbicacionLaboratorio() != null ? 
                    registro.getUbicacionLaboratorio() : "";
                if (registro.getUbicacionLaboratorioSecundario() != null) {
                    ubicacion += " / " + registro.getUbicacionLaboratorioSecundario();
                }
                cellUbicacion.setCellValue(ubicacion);
                cellUbicacion.setCellStyle(dataStyle);
                
                // Propósito
                Cell cellProposito = row.createCell(6);
                cellProposito.setCellValue(registro.getProposito() != null ? 
                    registro.getProposito() : "");
                cellProposito.setCellStyle(dataStyle);
                
                // Observaciones
                Cell cellObservaciones = row.createCell(7);
                cellObservaciones.setCellValue(registro.getObservaciones() != null ? 
                    registro.getObservaciones() : "");
                cellObservaciones.setCellStyle(dataStyle);
                
                // Tipo Registro
                Cell cellTipo = row.createCell(8);
                cellTipo.setCellValue(registro.getTipoRegistro() != null ? 
                    registro.getTipoRegistro() : "");
                
                // Aplicar estilo de examen si corresponde
                if ("EXAMEN".equals(registro.getTipoRegistro())) {
                    cellTipo.setCellStyle(examenStyle);
                } else {
                    cellTipo.setCellStyle(centerStyle);
                }
                
                // Duración
                Cell cellDuracion = row.createCell(9);
                cellDuracion.setCellValue(registro.getDuracionFormatada() != null ? 
                    registro.getDuracionFormatada() : "-");
                cellDuracion.setCellStyle(centerStyle);
                
                // Novedad
                Cell cellNovedad = row.createCell(10);
                cellNovedad.setCellValue(registro.getNombreNovedad() != null ? 
                    registro.getNombreNovedad() : "-");
                cellNovedad.setCellStyle(dataStyle);
                
                // Estado Novedad
                Cell cellEstadoNovedad = row.createCell(11);
                cellEstadoNovedad.setCellValue(registro.getEstadoNovedad() != null ? 
                    registro.getEstadoNovedad() : "-");
                cellEstadoNovedad.setCellStyle(centerStyle);
            }
            
            // ===== AJUSTAR ANCHO DE COLUMNAS =====
            sheet.setColumnWidth(0, 20 * 256);  // Fecha Entrada
            sheet.setColumnWidth(1, 20 * 256);  // Fecha Salida
            sheet.setColumnWidth(2, 15 * 256);  // Usuario
            sheet.setColumnWidth(3, 20 * 256);  // Nombre Completo
            sheet.setColumnWidth(4, 25 * 256);  // Laboratorios
            sheet.setColumnWidth(5, 20 * 256);  // Ubicación
            sheet.setColumnWidth(6, 20 * 256);  // Propósito
            sheet.setColumnWidth(7, 25 * 256);  // Observaciones
            sheet.setColumnWidth(8, 15 * 256);  // Tipo Registro
            sheet.setColumnWidth(9, 15 * 256);  // Duración
            sheet.setColumnWidth(10, 20 * 256); // Novedad
            sheet.setColumnWidth(11, 15 * 256); // Estado Novedad
            
            // ===== ESCRIBIR A BYTES =====
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            return outputStream.toByteArray();
        }
    }

    /**
     * Exporta una lista de novedades a Excel con formato mejorado
     */
    public byte[] exportarNovedadesAExcel(List<com.example.demo.entity.Novedad> novedades) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Novedades");
            
            // ===== ESTILOS =====
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 11);
            headerStyle.setFont(headerFont);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            headerStyle.setWrapText(true);
            headerStyle.setBorderBottom(BorderStyle.THIN);
            headerStyle.setBorderTop(BorderStyle.THIN);
            headerStyle.setBorderLeft(BorderStyle.THIN);
            headerStyle.setBorderRight(BorderStyle.THIN);
            
            CellStyle dataStyle = workbook.createCellStyle();
            dataStyle.setBorderBottom(BorderStyle.THIN);
            dataStyle.setBorderTop(BorderStyle.THIN);
            dataStyle.setBorderLeft(BorderStyle.THIN);
            dataStyle.setBorderRight(BorderStyle.THIN);
            dataStyle.setAlignment(HorizontalAlignment.LEFT);
            dataStyle.setVerticalAlignment(VerticalAlignment.TOP);
            dataStyle.setWrapText(true);
            
            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setBorderBottom(BorderStyle.THIN);
            dateStyle.setBorderTop(BorderStyle.THIN);
            dateStyle.setBorderLeft(BorderStyle.THIN);
            dateStyle.setBorderRight(BorderStyle.THIN);
            dateStyle.setAlignment(HorizontalAlignment.CENTER);
            dateStyle.setVerticalAlignment(VerticalAlignment.CENTER);
            
            // ===== ENCABEZADOS =====
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            
            String[] headers = {
                "Fecha Reporte",
                "Laboratorio",
                "Tipo de Novedad",
                "Título",
                "Descripción",
                "Reportado por",
                "Prioridad",
                "Estado",
                "Fecha Resolución",
                "Observaciones"
            };
            
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // ===== DATOS =====
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            int rowNum = 1;
            
            for (com.example.demo.entity.Novedad novedad : novedades) {
                Row row = sheet.createRow(rowNum++);
                row.setHeightInPoints(20);
                
                // Fecha Reporte
                Cell cellFecha = row.createCell(0);
                cellFecha.setCellValue(novedad.getFechaReporte() != null ? novedad.getFechaReporte().format(formatter) : "");
                cellFecha.setCellStyle(dateStyle);
                
                // Laboratorio
                Cell cellLab = row.createCell(1);
                cellLab.setCellValue(novedad.getLaboratorio() != null ? novedad.getLaboratorio().getNombre() : "N/A");
                cellLab.setCellStyle(dataStyle);
                
                // Tipo de Novedad
                Cell cellTipo = row.createCell(2);
                cellTipo.setCellValue(novedad.getTipo() != null ? novedad.getTipo().toString() : "");
                cellTipo.setCellStyle(dataStyle);
                
                // Título
                Cell cellTitulo = row.createCell(3);
                cellTitulo.setCellValue(novedad.getTitulo() != null ? novedad.getTitulo() : "");
                cellTitulo.setCellStyle(dataStyle);
                
                // Descripción
                Cell cellDesc = row.createCell(4);
                cellDesc.setCellValue(novedad.getDescripcion() != null ? novedad.getDescripcion() : "");
                cellDesc.setCellStyle(dataStyle);
                
                // Reportado por
                Cell cellReportadoPor = row.createCell(5);
                cellReportadoPor.setCellValue(novedad.getUsuarioReporta() != null ? novedad.getUsuarioReporta().getNombreCompleto() : "");
                cellReportadoPor.setCellStyle(dataStyle);
                
                // Prioridad
                Cell cellPrioridad = row.createCell(6);
                cellPrioridad.setCellValue(novedad.getPrioridad() != null ? novedad.getPrioridad() : "");
                cellPrioridad.setCellStyle(dataStyle);
                
                // Estado
                Cell cellEstado = row.createCell(7);
                cellEstado.setCellValue(novedad.getEstado() != null ? novedad.getEstado().toString() : "");
                cellEstado.setCellStyle(dataStyle);
                
                // Fecha Resolución
                Cell cellFechaRes = row.createCell(8);
                cellFechaRes.setCellValue(novedad.getFechaResolucion() != null ? novedad.getFechaResolucion().format(formatter) : "");
                cellFechaRes.setCellStyle(dateStyle);
                
                // Observaciones
                Cell cellObs = row.createCell(9);
                cellObs.setCellValue(novedad.getObservacionesResolucion() != null ? novedad.getObservacionesResolucion() : "");
                cellObs.setCellStyle(dataStyle);
            }
            
            // ===== ANCHOS DE COLUMNAS =====
            sheet.setColumnWidth(0, 20 * 256);  // Fecha Reporte
            sheet.setColumnWidth(1, 20 * 256);  // Laboratorio
            sheet.setColumnWidth(2, 18 * 256);  // Tipo Novedad
            sheet.setColumnWidth(3, 25 * 256);  // Título
            sheet.setColumnWidth(4, 35 * 256);  // Descripción
            sheet.setColumnWidth(5, 20 * 256);  // Reportado por
            sheet.setColumnWidth(6, 15 * 256);  // Prioridad
            sheet.setColumnWidth(7, 15 * 256);  // Estado
            sheet.setColumnWidth(8, 20 * 256);  // Fecha Resolución
            sheet.setColumnWidth(9, 35 * 256);  // Observaciones
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            
            return outputStream.toByteArray();
        }
    }
}
