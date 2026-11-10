package com.example.service;

import com.example.model.Libro;

import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReporteService {

    public byte[] generarReporteLibros(List<Libro> libros)
            throws Exception {

        InputStream archivo = getClass()
            .getResourceAsStream("/reports/libros.jrxml");

        if (archivo == null) {
            throw new Exception("No se encontró el diseño del informe");
        }

        JasperReport reporte = JasperCompileManager.compileReport(archivo);

        JRBeanCollectionDataSource datos =
            new JRBeanCollectionDataSource(libros);

        Map<String, Object> parametros = new HashMap<>();

        parametros.put("titulo", "Listado de libros - Bookly");

        JasperPrint resultado = JasperFillManager.fillReport(
            reporte,
            parametros,
            datos
        );

        return JasperExportManager.exportReportToPdf(resultado);
    }
}